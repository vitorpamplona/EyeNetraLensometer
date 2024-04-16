/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * This program is offered under a commercial and under the AGPL license.
 * For commercial licensing, contact me at vitor@vitorpamplona.com.
 * For AGPL licensing, see below.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * This application has not been clinically tested, approved by or registered in any health agency.
 * Even though this repository grants licenses to use to any person that follow it's license,
 * any clinical or commercial use must additionally follow the laws and regulations of the
 * pertinent jurisdictions. Having a license to use the source code does not imply on having
 * regulatory approvals to use or market any part of this code.
 */
package com.vitorpamplona.netrometer.imageprocessing.utils;

import com.vitorpamplona.netrometer.imageprocessing.hardware.AbstractDevice;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.model.Point2DGrid;
import com.vitorpamplona.netrometer.imageprocessing.model.PolarFromGrid;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.imageprocessing.model.RefractionStats;
import com.vitorpamplona.netrometer.imageprocessing.model.VectorNeighbor;
import com.vitorpamplona.netrometer.imageprocessing.utils.GaussNewtonSineFitting.SinusoidalModel;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.AngleDiff;
import com.vitorpamplona.netrometer.utils.Point2D;
import com.vitorpamplona.netrometer.utils.Rect;
import com.vitorpamplona.netrometer.utils.RefRounding;


import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessingUtil {

    public static SinusoidalModel resultsToSinusoidal(GridResult results, GridResult zero, List<VectorNeighbor> neighbors, AbstractDevice device) {
        //long start= Calendar.getInstance().getTimeInMillis();

        List<Polar> diopterCollection =  convertToDiopters(results, zero, neighbors, device);

        GaussNewtonSineFitting sinusoid = new GaussNewtonSineFitting(2, 200, 0.5);

        SinusoidalModel result = sinusoid.calculate(diopterCollection);
        //long finish= Calendar.getInstance().getTimeInMillis();
        //System.out.println("Old Algo " + (finish-start) + "msecs over " + diopterCollection.size() + " points");

        return result;
    }

    public static SinusoidalModel resultsToSinusoidalROI(GridResult results, GridResult zero, List<VectorNeighbor> neighbors, Polar[] bufferedDiopters, AbstractDevice device) {
        //long start= Calendar.getInstance().getTimeInMillis();

        convertToDioptersROI(results, zero, neighbors, bufferedDiopters, device);

        GaussNewtonSineFitting sinusoid = new GaussNewtonSineFitting(2, 20, 0.5);

        SinusoidalModel result = sinusoid.calculate(bufferedDiopters);
        //long finish= Calendar.getInstance().getTimeInMillis();
        //System.out.println("New Algo " + (finish-start) + "msecs over " + bufferedDiopters.length + " points");

        return result;
    }

    private static final double MAX_SPH_NOLENS = -.36;
    private static final double MIN_SPH_NOLENS = -.40;

    public static boolean isLensInserted(double sphere, double cylRound){

        if (cylRound==0d && sphere >= MIN_SPH_NOLENS && sphere <= MAX_SPH_NOLENS ){
                return false;
        }
        return true;


    }

    public static List<Polar> convertToDiopters(GridResult results, GridResult zero, List<VectorNeighbor> neighbors, AbstractDevice device) {
        List<PolarFromGrid> polarZero, polarResult;

        polarZero = convertGridToPolar(zero, neighbors, (int) zero.centerGridPosition.x, (int) zero.centerGridPosition.y);
        polarResult = convertGridToPolar(results, neighbors, (int) results.centerGridPosition.x, (int) results.centerGridPosition.y);

        List<Polar> diopterCollection = convertRadiiToDiopters(polarZero, polarResult, device);
        return diopterCollection;
    }

    public static Refraction convertToPrescription(SinusoidalModel sine) {
        if (sine == null){
            Refraction invalidPrescription = new Refraction(99,0,0);
            invalidPrescription.isValid = false;
            return invalidPrescription;

        }
        // convert from sine model to prescription
        int sign = (sine.offset < 0) ? -1 : 1;

        // sphere
        double SP = Math.abs(sine.amplitude) + sign * Math.abs(sine.offset);

        // cylinder
        double CY = -2 * Math.abs(sine.amplitude);

        // axis
        double solveForX = (Math.PI/2 - sine.phase) / 2;
        double offsetAngle = solveForX + Math.PI/2;
        double phaseInDegrees = Math.toDegrees(offsetAngle);
        double angleNormalized = AngleDiff.angle0toN(180, (float) phaseInDegrees);
        double AX = angleNormalized;// 180d - angleNormalized; // reverse
        // round to 0.25 diopter precision
        float SPr = RefRounding.roundTo((float)SP, Params.RESULT_STEP_SPHERE);
        float CYr = RefRounding.roundTo((float)CY, Params.RESULT_STEP_CYLINDER);
        float AXr = RefRounding.roundTo((float)AX, Params.RESULT_STEP_AXIS);

        if (CYr>-0.25){
            AXr =0;
        }

        if (!isLensInserted(SP, CYr)) {
            Refraction invalidPrescription = new Refraction(99,0,0);
            invalidPrescription.isValid = false;
            return invalidPrescription;
        } else {
            return new Refraction(SPr, CYr, AXr);
        }
    }

    public static List<Polar> convertRadiiToDiopters(List<PolarFromGrid> basePoints, List<PolarFromGrid> points, AbstractDevice device) {

        List<Polar> diopterCollection = new ArrayList<Polar>();

        double coeffC = device.getCalibrationCoefficientC();
        double coeffD = device.getCalibrationCoefficientD();

        for (int i=0; i<basePoints.size(); i++) {

            if (basePoints.get(i).isValid && points.get(i).isValid) {

                double baseRadius = basePoints.get(i).r;
                double usedRadius = points.get(i).r;

                if (!(Double.isNaN(baseRadius) || Double.isNaN(usedRadius))) {

                    // Set coefficients (arrangement: D C B A)
                    //double[] coefficients = {fit.D, fit.C, fit.B, fit.A};

                    // Get change in y:
                    double Ychange = (usedRadius ) / ( baseRadius);

                    // Rearrange to y(x) = 0
                    //coefficients[0] -= Ychange;

                    // Find diopter by root solving (x given y)
                    //double diopter = new PolynomialRootSolver().solve(coefficients).getFirstRealRoot();
                    double diopter = coeffC*(1/Ychange) +coeffD;
                    diopterCollection.add(new Polar(diopter, points.get(i).theta));
                }
            }
        }
        return diopterCollection;

    }

    public static List<Polar> convertRadiiToDiopters(List<PolarFromGrid> basePoints, List<PolarFromGrid> points, double coeffC, double coeffD) {

        List<Polar> diopterCollection = new ArrayList<Polar>();

        for (int i=0; i<basePoints.size(); i++) {

            if (basePoints.get(i).isValid && points.get(i).isValid) {

                double baseRadius = basePoints.get(i).r;
                double usedRadius = points.get(i).r;

                if (!(Double.isNaN(baseRadius) || Double.isNaN(usedRadius))) {

                    // Set coefficients (arrangement: D C B A)
                    //double[] coefficients = {fit.D, fit.C, fit.B, fit.A};

                    // Get change in y:
                    double Ychange = (usedRadius ) / ( baseRadius);

                    // Rearrange to y(x) = 0
                    //coefficients[0] -= Ychange;

                    // Find diopter by root solving (x given y)
                    //double diopter = new PolynomialRootSolver().solve(coefficients).getFirstRealRoot();
                    double diopter = coeffC*(1/Ychange) +coeffD;
                    diopterCollection.add(new Polar(diopter, points.get(i).theta));
                }
            }
        }
        return diopterCollection;

    }

    public static List<VectorNeighbor> defineNeighbors(float RadiusMin, float RadiusMax){

        List<VectorNeighbor> neighbors = new ArrayList<VectorNeighbor>();

        float minSq = RadiusMin*RadiusMin;
        float maxSq = RadiusMax*RadiusMax;
        float distSq ;

        int HalfRadiusMaxInt = (int)Math.ceil(RadiusMax / 2);

        for(int cx=-HalfRadiusMaxInt;cx<=HalfRadiusMaxInt;cx++){
            for(int cy=-HalfRadiusMaxInt; cy<= HalfRadiusMaxInt; cy++){

                for(int x=-HalfRadiusMaxInt;x<=HalfRadiusMaxInt;x++) {
                    for (int y = -HalfRadiusMaxInt; y <= HalfRadiusMaxInt; y++) {


                        distSq = (x-cx) * (x-cx) + (y-cy) * (y-cy);
                        if (distSq >= minSq && distSq <= maxSq) {
                            VectorNeighbor vector = new VectorNeighbor();
                            vector.setCenter(new Point2D(cx,cy));
                            vector.setNeighbor(new Point2D(x,y));
                            neighbors.add(vector);

                        }
                    }
                }
            }
        }
        return neighbors;

    }


    public static List<VectorNeighbor> defineNeighbors(){

        List<VectorNeighbor> neighbors = new ArrayList<VectorNeighbor>();

        // 204x4 matrix that represents vector ctr (x,y) and tip (x,y) for neighbors in the grid
        int[][] matrixVectorXYCtrPoint = new int[][]{
                {	3	,	-6	,	2	,	6	},
                {	3	,	-6	,	0	,	6	},
                {	3	,	-6	,	-2	,	6	},
                {	6	,	-5	,	0	,	6	},
                {	4	,	-5	,	-4	,	5	},
                {	4	,	-3	,	-5	,	6	},
                {	4	,	-2	,	-6	,	6	},
                {	6	,	0	,	-5	,	6	},
                {	6	,	-2	,	-6	,	3	},
                {	6	,	-4	,	-6	,	-1	},
                {	6	,	-3	,	-6	,	-2	},
                {	6	,	-1	,	-6	,	-2	},
                {	6	,	1	,	-6	,	-2	},
                {	6	,	0	,	-6	,	-5	},
                {	6	,	0	,	-5	,	-6	},
                {	5	,	6	,	-5	,	-2	},
                {	6	,	3	,	-3	,	-6	},
                {	2	,	4	,	-6	,	-6	},
                {	0	,	6	,	-6	,	-5	},
                {	2	,	6	,	-3	,	-6	},
                {	1	,	6	,	-2	,	-6	},
                {	1	,	6	,	0	,	-6	},
                {	0	,	6	,	1	,	-6	},
                {	-3	,	6	,	0	,	-6	},
                {	-2	,	6	,	3	,	-6	},
                {	-4	,	6	,	2	,	-5	},
                {	-4	,	6	,	4	,	-4	},
                {	-3	,	4	,	6	,	-5	},
                {	-4	,	2	,	6	,	-6	},
                {	-6	,	4	,	5	,	-2	},
                {	-6	,	4	,	6	,	-1	},
                {	-6	,	-1	,	6	,	-4	},
                {	-6	,	-2	,	6	,	-3	},
                {	-6	,	-1	,	6	,	0	},
                {	-6	,	-3	,	6	,	0	},
                {	-6	,	-5	,	6	,	0	},
                {	-5	,	-1	,	6	,	5	},
                {	-4	,	-3	,	6	,	5	},
                {	-3	,	-6	,	6	,	3	},
                {	-6	,	-4	,	2	,	6	},
                {	-6	,	-6	,	0	,	5	},
                {	-5	,	-6	,	0	,	6	},
                {	-4	,	-6	,	-1	,	6	},
                {	2	,	-6	,	3	,	6	},
        };

        for(int i=0;i<matrixVectorXYCtrPoint.length;i++) {
            VectorNeighbor vector = new VectorNeighbor();
            vector.setCenter(new Point2D(matrixVectorXYCtrPoint[i][0], matrixVectorXYCtrPoint[i][1]));
            vector.setNeighbor(new Point2D(matrixVectorXYCtrPoint[i][2], matrixVectorXYCtrPoint[i][3]));
            neighbors.add(vector);
        };

        return neighbors;

    }

    public static Point2D calculateInitialStep(List<Point2D> anchors) {
        Point2D steps = new Point2D();
        steps.x = steps.y = Params.DEFAULT_GRID_STEP;

        if (anchors!=null) {
            if(anchors.get(2)!= null & anchors.get(1)!= null){
                steps.y = (float)( Math.sqrt((anchors.get(1).x - anchors.get(2).x) * (anchors.get(1).x - anchors.get(2).x) + (anchors.get(1).y - anchors.get(2).y) * (anchors.get(1).y - anchors.get(2).y)) / Params.DEFAULT_ANCHORS_DOTS_DISTANCE );
            }
            if(anchors.get(0)!= null & anchors.get(1)!= null) {
                steps.x = (float) (Math.sqrt((anchors.get(1).x - anchors.get(0).x) * (anchors.get(1).x - anchors.get(0).x) + (anchors.get(1).y - anchors.get(0).y) * (anchors.get(1).y - anchors.get(0).y)) / Params.DEFAULT_ANCHORS_DOTS_DISTANCE);
            }
        }

        return steps;
    }

    public static void convertToDioptersROI(GridResult results, GridResult zero, List<VectorNeighbor> neighbors, Polar[] bufferedDiopters, AbstractDevice device) {
        double distance;
        double theta;

        Point2DGrid position;
        Point2DGrid positionRef;

        int gridDotx;
        int gridDoty;
        int gridDotRefx;
        int gridDotRefy;

        int bufferIndex = 0;

        double coeffAngular= device.getCalibrationCoefficientC();
        double coeffLinear= device.getCalibrationCoefficientD();

        for (VectorNeighbor vector : neighbors){
            gridDotx = (int) zero.centerGridPosition.x+(int)(vector.getNeighbor().x);
            gridDoty = (int) zero.centerGridPosition.y+(int)(vector.getNeighbor().y);
            gridDotRefx = (int) zero.centerGridPosition.x+(int)(vector.getCenter().x);
            gridDotRefy = (int) zero.centerGridPosition.y+(int)(vector.getCenter().y);

            PolarFromGrid polarZero = getPolar(zero, gridDotx, gridDoty, gridDotRefx, gridDotRefy);

            gridDotx = (int) results.centerGridPosition.x+(int)(vector.getNeighbor().x);
            gridDoty = (int) results.centerGridPosition.y+(int)(vector.getNeighbor().y);
            gridDotRefx = (int) results.centerGridPosition.x+(int)(vector.getCenter().x);
            gridDotRefy = (int) results.centerGridPosition.y+(int)(vector.getCenter().y);

            PolarFromGrid polarResults = getPolar(results, gridDotx, gridDoty, gridDotRefx, gridDotRefy);

            if (polarZero != null && polarResults != null && polarZero.isValid && polarResults.isValid) {
                double ratio = polarResults.r / polarZero.r;

                double diopter = coeffAngular*(1/ratio) +coeffLinear;
                bufferedDiopters[bufferIndex].r = diopter;
                bufferedDiopters[bufferIndex].theta = polarResults.theta;
            } else {
                bufferedDiopters[bufferIndex].r = Double.NaN;
                bufferedDiopters[bufferIndex].theta = Double.NaN;
            }

            bufferIndex++;
        }
    }

    private static PolarFromGrid getPolar(GridResult zero, int gridDotx, int gridDoty, int gridDotRefx, int gridDotRefy) {
        if (isInsideGrid(zero, gridDotx, gridDoty) && isInsideGrid(zero, gridDotRefx, gridDotRefy)) {
            Point2DGrid position = zero.pointsOnGrid[gridDotx][gridDoty];
            Point2DGrid positionRef = zero.pointsOnGrid[gridDotRefx][gridDotRefy];
            double distance = Math.sqrt(
                    (position.x - positionRef.x) * (position.x - positionRef.x) + (position.y - positionRef.y) * (position.y - positionRef.y)
            );
            double theta = Math.atan2(position.y - positionRef.y, position.x - positionRef.x);

            if (theta < 0) {
                theta = theta + 2 * Math.PI;
            }

            return new PolarFromGrid(distance, theta, position.isValid && positionRef.isValid);
        }
        return null;
    }

    public static boolean isInsideGrid(GridResult grid, int x, int y) {
        return x >= 0 && x< grid.pointsOnGrid.length && y>=0 && y< grid.pointsOnGrid[0].length;
    }

    public static List<PolarFromGrid> convertGridToPolar(GridResult grid, List<VectorNeighbor> neighbors, int centerX, int centerY) {
        List<PolarFromGrid> pointsPolar = new ArrayList<PolarFromGrid>();

        for (VectorNeighbor vector:neighbors){
            int gridDotx = centerX+(int)(vector.getNeighbor().x);
            int gridDoty = centerY+(int)(vector.getNeighbor().y);
            int gridDotRefx = centerX+(int)(vector.getCenter().x);
            int gridDotRefy = centerY+(int)(vector.getCenter().y);
            PolarFromGrid polarNeigh = getPolar(grid, gridDotx, gridDoty, gridDotRefx, gridDotRefy);
            if (polarNeigh != null){
                pointsPolar.add(polarNeigh);
            }
        }
        return pointsPolar;
    }

    //Progressives
    public static RefractionStats histogramROI( Rect roiCoordinates, GridResult result, GridResult zero, AbstractDevice device){
        
        if (result==null || zero== null){
            return null;
        }

        int startingBorder = (int) (Params.MAX_NEIGHBORS_RADIUS / 2 - 1);
        int endingBorder = (int) (Params.MAX_NEIGHBORS_RADIUS / 2);

        // Remove kernel "borders"
        int expectedSize = ((zero.pointsOnGrid.length-endingBorder) - startingBorder) * ((zero.pointsOnGrid[0].length-endingBorder) - startingBorder);

//        RefractionStats refractionStats = new RefractionStats(expectedSize);
        RefractionStats refractionStats = new RefractionStats();
        Polar[] diopterBuffer = new Polar[Params.NEIGHBORS.size()];
        for (int i=0; i<diopterBuffer.length; i++) {
            diopterBuffer[i] = new Polar(Double.NaN, Double.NaN);
        }

        //Clock c = new Clock("Histogram ROI");

        long good= 0;
        long bad = 0;
        for (int x = startingBorder; x < zero.pointsOnGrid.length-endingBorder ; x +=Params.PROGRESSIVE_ROI_PROBE_STEP) {
            for (int y = startingBorder; y < zero.pointsOnGrid[0].length-endingBorder ; y+=Params.PROGRESSIVE_ROI_PROBE_STEP) {
                if (isInsideROI(result, x, y, roiCoordinates)){
                    zero.centerGridPosition = new Point2D(x, y);
                    result.centerGridPosition = new Point2D(x, y);

                    //long lapsed = System.currentTimeMillis();
                    Refraction localPrescription = ImageProcessingUtil.convertToPrescription(
                            ImageProcessingUtil.resultsToSinusoidalROI(
                                    result,
                                    zero,
                                    Params.NEIGHBORS, diopterBuffer, device));
//                    Log.e("GaussImgProcUtil", "SPH, CYL, AXIS = (" + localPrescription.dSphere + ", " + localPrescription.dCylinder + ", " + localPrescription.dAxis + ")");
                    //lapsed = System.currentTimeMillis() - lapsed;
                    if(localPrescription.dSphere < 98  ) {
                        refractionStats.getSphereStats().addValue(localPrescription.dSphere);
                        refractionStats.getCylinderStats().addValue(localPrescription.dCylinder);
                        if(localPrescription.dCylinder!=0) refractionStats.getAxisStats().addValue(localPrescription.dAxis);
//                        Log.e("GaussImgProcUtil", "SPH, CYL, AXIS = (" + localPrescription.dSphere + ", " + localPrescription.dCylinder + ", " + localPrescription.dAxis + ")");
                        //c.capture("New Point (" + x + "," + y + "): " + localPrescription.getFormattedPrescription());
                        good ++;
                    } else {
                        bad ++;
                    }

                }
            }
        }

        //Log.i("Histogram ROI Valids" , " " + good);
        //Log.i("Histogram ROI Invalids" , " " + bad);

        //c.capture("Full Histogram");
        //c.log();
        if((good+bad)==0 || (100*good/(good+bad)) < 30) {
            RefractionStats refractionStatsInvalid = new RefractionStats();
            refractionStatsInvalid.getSphereStats().addValue(99);
            refractionStatsInvalid.getCylinderStats().addValue(0);
            refractionStatsInvalid.getAxisStats().addValue(0);
            return refractionStatsInvalid;
        }
        else return refractionStats;
//        return refractionStats;

    }

    //Progressives
    public static RefractionStats histogramROIsmallCtr( Rect roiCoordinates, GridResult result, GridResult zero, AbstractDevice device){

        if (result==null || zero== null){
            return null;
        }

        int startingBorder = (int) (Params.MAX_NEIGHBORS_RADIUS / 2 - 1);
        int endingBorder = (int) (Params.MAX_NEIGHBORS_RADIUS / 2);

        int startingBorderX = (int)( (result.pointsOnGrid.length/ 2 )-(Params.MAX_NEIGHBORS_RADIUS / 2 )+1);
        int endingBorderX = (int)( (result.pointsOnGrid.length/ 2 )+(Params.MAX_NEIGHBORS_RADIUS / 2 ) -1);

        int startingBorderY = (int)( (result.pointsOnGrid[0].length/ 2 )-(Params.MAX_NEIGHBORS_RADIUS / 2 )+1);
        int endingBorderY = (int)( (result.pointsOnGrid[0].length/ 2 )+(Params.MAX_NEIGHBORS_RADIUS / 2 ) -1);

        // Remove kernel "borders"
        int expectedSize = ((zero.pointsOnGrid.length-endingBorder) - startingBorder) * ((zero.pointsOnGrid[0].length-endingBorder) - startingBorder);

//        RefractionStats refractionStats = new RefractionStats(expectedSize);
        RefractionStats refractionStats = new RefractionStats();
        Polar[] diopterBuffer = new Polar[Params.NEIGHBORS.size()];
        for (int i=0; i<diopterBuffer.length; i++) {
            diopterBuffer[i] = new Polar(Double.NaN, Double.NaN);
        }

        //Clock c = new Clock("Histogram ROI");

        long good= 0;
        long bad = 0;
//        Log.e("Gauss", "(result.pointsOnGrid[0].length/ 2)"+(result.pointsOnGrid[0].length/ 2)+"result.pointsOnGrid.length " +result.pointsOnGrid.length + ", result.pointsOnGrid[0].length " + result.pointsOnGrid[0].length + ", startingBorderX " + startingBorderX + ", endingBorderX "+endingBorderX+ ", startingBorderY " + startingBorderY + ", endingBorderY "+endingBorderY);
        for (int x = startingBorderX; x < endingBorderX ; x +=Params.PROGRESSIVE_ROI_PROBE_STEP) {
            for (int y = startingBorderY; y < endingBorderY ; y+=Params.PROGRESSIVE_ROI_PROBE_STEP) {
                if (isInsideROI(result, x, y, roiCoordinates)){
                    zero.centerGridPosition = new Point2D(x, y);
                    result.centerGridPosition = new Point2D(x, y);

                    //long lapsed = System.currentTimeMillis();
                    Refraction localPrescription = ImageProcessingUtil.convertToPrescription(
                            ImageProcessingUtil.resultsToSinusoidalROI(
                                    result,
                                    zero,
                                    Params.NEIGHBORS, diopterBuffer, device));
//                    Log.e("GaussImgProcUtil", "SPH, CYL, AXIS = (" + localPrescription.dSphere + ", " + localPrescription.dCylinder + ", " + localPrescription.dAxis + ")");
                    //lapsed = System.currentTimeMillis() - lapsed;
                    if(localPrescription.dSphere < 98  ) {
                        refractionStats.getSphereStats().addValue(localPrescription.dSphere);
                        refractionStats.getCylinderStats().addValue(localPrescription.dCylinder);
                        if(localPrescription.dCylinder!=0) refractionStats.getAxisStats().addValue(localPrescription.dAxis);
//                        Log.e("GaussImgProcUtil", "SPH, CYL, AXIS = (" + localPrescription.dSphere + ", " + localPrescription.dCylinder + ", " + localPrescription.dAxis + ")");
                        //c.capture("New Point (" + x + "," + y + "): " + localPrescription.getFormattedPrescription());
                        good ++;
                    } else {
                        bad ++;
                    }

                }
            }
        }

        //Log.i("Histogram ROI Valids" , " " + good);
        //Log.i("Histogram ROI Invalids" , " " + bad);

        //c.capture("Full Histogram");
        //c.log();

        if((good+bad)==0 || (100*good/(good+bad)) < 30) {
            RefractionStats refractionStatsInvalid = new RefractionStats();
            refractionStatsInvalid.getSphereStats().addValue(99);
            refractionStatsInvalid.getCylinderStats().addValue(0);
            refractionStatsInvalid.getAxisStats().addValue(0);
            return refractionStatsInvalid;
        }
        else return refractionStats;
//        return refractionStats;

    }

    public static boolean isInsideROI(GridResult data, int indX, int indY, Rect roiCoordinates) {
        if (data.pointsOnGrid[indX][indY] != null && data.pointsOnGrid[indX][indY].isValid) {
            return     data.pointsOnGrid[indX][indY].x >= roiCoordinates.left
                    && data.pointsOnGrid[indX][indY].x <= roiCoordinates.right
                    && data.pointsOnGrid[indX][indY].y >= roiCoordinates.top
                    && data.pointsOnGrid[indX][indY].y <= roiCoordinates.bottom;
        } else {
            return false;
        }
    }

    public static Rect findFrameBoundaries(GridResult grid){
        int rowTotal = grid.pointsOnGrid.length;
        int columnTotal = grid.pointsOnGrid[0].length;

        int centerX = rowTotal/2;
        int centerY = columnTotal/2;

        float bottom_0 = grid.pointsOnGrid[rowTotal-1][centerY].x;
        float bottom_1 = grid.pointsOnGrid[rowTotal-1][centerY-5].x;
        float bottom_2 = grid.pointsOnGrid[rowTotal-1][centerY+5].x;
        float bottom = bottom_0;

        float top_0 = grid.pointsOnGrid[0][centerY].x;
        float top_1 = grid.pointsOnGrid[0][centerY-5].x;
        float top_2 = grid.pointsOnGrid[0][centerY+5].x;
        float top = grid.pointsOnGrid[0][centerY].x;

        boolean flag_first_invalid_0=false;
        boolean flag_first_invalid_1=false;
        boolean flag_first_invalid_2=false;

        for (int x= centerX; x< rowTotal; x++){
            if(!grid.pointsOnGrid[x-1][centerY].isValid&&!grid.pointsOnGrid[x][centerY].isValid && !flag_first_invalid_0){
                bottom_0 = grid.pointsOnGrid[x-1][centerY].x;
                flag_first_invalid_0 = true;
            }

            if(!grid.pointsOnGrid[x-1][centerY-5].isValid&&!grid.pointsOnGrid[x][centerY-5].isValid && !flag_first_invalid_1){
                bottom_1 = grid.pointsOnGrid[x-1][centerY-5].x;
                flag_first_invalid_1 = true;
            }

            if(!grid.pointsOnGrid[x-1][centerY+5].isValid&&!grid.pointsOnGrid[x][centerY+5].isValid && !flag_first_invalid_2){
                bottom_2 = grid.pointsOnGrid[x-1][centerY+5].x;
                flag_first_invalid_2 = true;
            }

        }
        bottom = (bottom_0+bottom_1+bottom_2)/3;
        flag_first_invalid_0 = false;
        flag_first_invalid_1 = false;
        flag_first_invalid_2 = false;

        for (int x= centerX; x>= 0; x--){
            if(!grid.pointsOnGrid[x][centerY].isValid&& !grid.pointsOnGrid[x+1][centerY].isValid && !flag_first_invalid_0){
                top_0 = grid.pointsOnGrid[x+1][centerY].x;
                flag_first_invalid_0 = true;
            }

            if(!grid.pointsOnGrid[x][centerY-5].isValid&& !grid.pointsOnGrid[x+1][centerY-5].isValid && !flag_first_invalid_1){
                top_1 = grid.pointsOnGrid[x+1][centerY-5].x;
                flag_first_invalid_1 = true;
            }
            if(!grid.pointsOnGrid[x][centerY+5].isValid&& !grid.pointsOnGrid[x+1][centerY+5].isValid && !flag_first_invalid_2){
                top_2 = grid.pointsOnGrid[x+1][centerY+5].x;
                flag_first_invalid_2 = true;
            }
        }
        top=(top_0+top_1+top_2)/3;

        return new Rect(0,(int)top,0,(int)bottom);
    }

    public static Point2D findOpticalCtr(GridResult result, GridResult zero, AbstractDevice device){
        if (result == null || zero == null) return null;

        double distanceAux;

        double sumWeights=0.00000001;
        double sumPosX=0;
        double sumPosY=0;

        int auxNoLens=0;
        int auxSum=0;
        int auxNoLensCtr=0;
        int auxSumCtr=0;


        int rowTotal = result.pointsOnGrid.length;
        int columnTotal = result.pointsOnGrid[0].length;

        int centerX = rowTotal/2;
        int centerY = columnTotal/2;

        int rowTotalZero = zero.pointsOnGrid.length;
        int columnTotalZero = zero.pointsOnGrid[0].length;

        int centerXZero = rowTotalZero/2;
        int centerYZero = columnTotalZero/2;

        int minRow = Math.min(rowTotal,rowTotalZero);
        int minColumn = Math.min(columnTotal,columnTotalZero);
        float deltaX;
        float deltaY;

        float thresholdNoLensLargeROI = device.getThresholdNoLensLargeROI();
        float thresholdNoLensCenterROI = device.getThresholdNoLensCenterROI();
        double distanceNoLensCenter = device.getDistanceNoLensCtr();
        double distanceNoLensLarge = device.getDistanceNoLensLarge();


        for (int x = -minRow/2; x < minRow/2 ; x ++) {
            for (int y = -minColumn/2; y < minColumn/2 ; y++) {

                if(result.pointsOnGrid[x + centerX][y + centerY].isValid && zero.pointsOnGrid[x + centerXZero][y + centerYZero].isValid) {
//                    auxSum++;
                    deltaX = (result.pointsOnGrid[x + centerX][y + centerY].x - zero.pointsOnGrid[x + centerXZero][y + centerYZero].x);
                    deltaY = (result.pointsOnGrid[x + centerX][y + centerY].y - zero.pointsOnGrid[x + centerXZero][y + centerYZero].y);
                    distanceAux = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    if (distanceAux < 10) {
                        sumWeights = sumWeights + 1 / (distanceAux + 0.00000001);
                        sumPosX = (result.pointsOnGrid[x + centerX][y + centerY].x) / (distanceAux + 0.00000001) + sumPosX;
                        sumPosY = result.pointsOnGrid[x + centerX][y + centerY].y / (distanceAux + 0.00000001) + sumPosY;
//                        if ((y >= -11) && (y <= 11)){
//                            if(x>=-11 && x<=+11){
                                auxSum++;
//                        Log.e("PixelDebug","OptCtr Passa por aqui dentro ImageProcessingUtil distanceAux "+distanceAux+ ", distanceNoLensLarge "+ distanceNoLensLarge); // TODO:MOTOZ Observe distanceAux for no lens detection
                                if (distanceAux<=distanceNoLensLarge){
                                    auxNoLens++;
                                }
//                            }
//                        }

                    }
//                    Log.e("PixelDebug","OptCtr Passa por aqui dentro ImageProcessingUtil distanceAux "+distanceAux+ ", distanceNoLensCenter "+ distanceNoLensCenter); // TODO:MOTOZ Observe distanceAux for no lens detection

                    if ((x == -2) || (x == 2)){
                        if(y>=-11 && y<=+11){
                            auxSumCtr++;
                            if (distanceAux<=distanceNoLensCenter){
//                                Log.e("Gauss", "                                        distanceAux "+distanceAux);
                                auxNoLensCtr++;
                            }
                        }

                    }
                }



            }
        }

        Point2D optCtr = new Point2D(sumPosX/sumWeights,sumPosY/sumWeights);

        sumWeights=0.00000001;
        sumPosX=0;
        sumPosY=0;
        // Local refinement to optical center
        for (int x = -minRow/2; x < minRow/2 ; x ++) {
            for (int y = -minColumn/2; y < minColumn/2 ; y++) {


                // Check if point on grid is local to estimated optical Center
                deltaX = (result.pointsOnGrid[x+centerX][y+centerY].x - optCtr.x);
                deltaY = (result.pointsOnGrid[x+centerX][y+centerY].y - optCtr.y);
                distanceAux = Math.sqrt(   deltaX*deltaX + deltaY*deltaY);

                if (distanceAux<Params.DEFAULT_GRID_STEP*10){

                    deltaX = (result.pointsOnGrid[x+centerX][y+centerY].x - zero.pointsOnGrid[x+centerXZero][y+centerYZero].x);
                    deltaY = (result.pointsOnGrid[x+centerX][y+centerY].y - zero.pointsOnGrid[x+centerXZero][y+centerYZero].y);
                    distanceAux = Math.sqrt(   deltaX*deltaX + deltaY*deltaY);

                    if (distanceAux<8){
                        sumWeights = sumWeights + 1/(distanceAux+0.00000001);
                        sumPosX=(result.pointsOnGrid[x+centerX][y+centerY].x) / (distanceAux+0.00000001) + sumPosX;
                        sumPosY=result.pointsOnGrid[x+centerX][y+centerY].y / (distanceAux+0.00000001) + sumPosY;
                    }
                }
            }
        }
        optCtr.x = (float)(sumPosX/sumWeights);
        optCtr.y = (float)(sumPosY/sumWeights);
//        if(auxSum!=0 && auxSumCtr!=0) Log.e("Gauss", "auxNoLens/auxSum = " + (100 * auxNoLens / auxSum) + " ( " + auxSum + " )" + ", auxNoLensCtr/auxSumCtr = " + (100 * auxNoLensCtr / auxSumCtr) + " ( " + auxSumCtr + " )");

//        if((float)auxNoLens/auxSum > .99){
//            optCtr = null;
//        }

        if((auxSum!=0)&&(float)auxNoLens/auxSum > thresholdNoLensLargeROI){ // to differentiate none from -.25 lens
            return null;
        }

        if((auxSumCtr!=0)&& auxSumCtr>Math.min(22,minColumn) && (float)auxNoLensCtr/auxSumCtr > thresholdNoLensCenterROI){

            if((auxSum!=0)&&(float)auxNoLens/auxSum < thresholdNoLensLargeROI){ // to differentiate none from -.25 lens

            } else optCtr = null;

        } else if((auxSumCtr!=0)&&auxSumCtr<=Math.min(22,minColumn)) {
            optCtr = null;
        } else if(auxSumCtr!=0 && auxSumCtr>Math.min(22,minColumn) && (float)auxNoLensCtr/auxSumCtr <= thresholdNoLensCenterROI && (auxSum!=0)&&(float)auxNoLens/auxSum > thresholdNoLensLargeROI){
            optCtr=null;
        }
        return optCtr;

    }



    public static boolean isLensOnCenter(GridResult result, GridResult zero) {

        double distanceAux;

        int auxNoLensCtr = 0;
        int auxSumCtr = 0;


        int rowTotal = result.pointsOnGrid.length;
        int columnTotal = result.pointsOnGrid[0].length;

        int centerX = rowTotal / 2;
        int centerY = columnTotal / 2;

        int rowTotalZero = zero.pointsOnGrid.length;
        int columnTotalZero = zero.pointsOnGrid[0].length;

        int centerXZero = rowTotalZero / 2;
        int centerYZero = columnTotalZero / 2;

        int minRow = Math.min(rowTotal, rowTotalZero);
        int minColumn = Math.min(columnTotal, columnTotalZero);
        float deltaX;
        float deltaY;
        for (int x = -minRow / 2; x < minRow / 2; x++) {
            for (int y = -minColumn / 2; y < minColumn / 2; y++) {

                if (result.pointsOnGrid[x + centerX][y + centerY].isValid && zero.pointsOnGrid[x + centerXZero][y + centerYZero].isValid) {

                    if (x >= -9 && x <= +9) {
                        if (y >= -9 && y <= +9) {
                            auxSumCtr++;
                            deltaX = (result.pointsOnGrid[x + centerX][y + centerY].x - zero.pointsOnGrid[x + centerXZero][y + centerYZero].x);
                            deltaY = (result.pointsOnGrid[x + centerX][y + centerY].y - zero.pointsOnGrid[x + centerXZero][y + centerYZero].y);
                            distanceAux = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                            if (distanceAux <= 1) {
                                auxNoLensCtr++;
                            }
                        }

                    }
                }


            }
        }

        if ((float) auxNoLensCtr / (float)auxSumCtr > .94) {
            return false;
        } else return true;

    }

    public static double calculateCircularMean(DescriptiveStatistics mAxisStats){

        int windowSize = 21; //Circular window size
        int sumAxis ;
        int sumDist;
        int histArray[] = new int[180+windowSize];
        int maxDist =0;
        int sumCore;
        int windowCore = 3;
        double circularMean=0;


        for (int i=0;i<(int)mAxisStats.getN();i++){
            histArray[(int)mAxisStats.getElement(i)]= histArray[(int)mAxisStats.getElement(i)]+1;
        }

        for(int i= 180;i< histArray.length;i++){
            histArray[i]=histArray[i-180];
        }

        for(int i=0; i< histArray.length-windowSize;i++){
            sumAxis = 0;
            sumDist=0;
            sumCore=0;
            for(int j=0;j<windowSize;j++){
                sumAxis = sumAxis + histArray[i+j]*(i+j);
                sumDist = sumDist+histArray[i+j];

            }

//            for(int c=windowSize/2-windowCore/2;c<=windowSize/2+windowCore/2;c++){
//                sumCore = sumCore+histArray[c+i];
//            }
//            if(sumCore>maxDist){
            if(sumDist>maxDist){
                maxDist = sumDist;
                circularMean = (double)sumAxis/sumDist;
            }
        }

        if (circularMean>180){
            circularMean =circularMean-180;
        }

        if(circularMean==0){
            circularMean=180;
        }

        return circularMean;
    }

    public static double calculateMeanDistance(GridResult result){
        double distance=0, deltaX, deltaY, distanceX=0, distanceY=0;
        int auxX=0, auxY=0;
        int rowTotal = result.pointsOnGrid.length;
        int columnTotal = result.pointsOnGrid[0].length;

        int centerX = rowTotal/2;
        int centerY = columnTotal/2;


        int minRow = Math.min(rowTotal,Params.ROW_DOT_COUNT_SINGLEVISION_UI);
        int minColumn = Math.min(columnTotal,Params.COLUMN_CENTER_POINT_SINGLEVISION_UI);

        for(int x = -minRow/2 ; x < (minRow/2 - 1) ; x++){
            for(int y = -minColumn/2; y < (minColumn/2 - 1); y++){
                deltaX = result.pointsOnGrid[centerX+x+1][centerY+y].x- result.pointsOnGrid[centerX+x][centerY+y].x;
                deltaY = result.pointsOnGrid[centerX+x+1][centerY+y].y- result.pointsOnGrid[centerX+x][centerY+y].y;
                if(!Double.isNaN(deltaX) && !Double.isNaN(deltaY)) {
                    distanceX += Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    auxX++;
                }
                deltaX = result.pointsOnGrid[centerX+x][centerY+y+1].x- result.pointsOnGrid[centerX+x][centerY+y].x;
                deltaY = result.pointsOnGrid[centerX+x][centerY+y+1].y- result.pointsOnGrid[centerX+x][centerY+y].y;
                if(!Double.isNaN(deltaX) && !Double.isNaN(deltaY)) {
                    distanceY += Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    auxY++;
                }
            }
        }

        if(auxX!=0 && auxY != 0)
            distance = ((distanceX/auxX)+(distanceY/auxY))*.5;

//        Log.e("Zero","distanceX "+distanceX+", (distanceX/aux) "+ (distanceX/auxX)+ " , distanceY " + (distanceY/auxY) + " , distance "+distance+" , auxX "+ auxX);

        return distance;
    }

   
}

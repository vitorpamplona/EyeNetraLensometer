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
package com.vitorpamplona.netrometer.imageprocessing;

import android.support.annotation.NonNull;
import android.util.Log;

import com.vitorpamplona.netrometer.utils.RefRounding;
import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.imageprocessing.hardware.AbstractDevice;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.imageprocessing.model.RefractionStats;
import com.vitorpamplona.netrometer.imageprocessing.processors.ProcessorsManager;
import com.vitorpamplona.netrometer.imageprocessing.utils.ImageProcessingUtil;
import com.vitorpamplona.netrometer.imageprocessing.utils.OtsuThresholder;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.ByteArrayReadWrite;
import com.vitorpamplona.netrometer.utils.Point2D;
import com.vitorpamplona.netrometer.utils.Rect;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

public class ProgressiveImageProcessing extends NetrometerImageProcessing {

    protected GridResult mLeftNearGridResult;
    protected GridResult mRightNearGridResult;

    protected boolean isDistance = true;
    protected boolean isSkipNear = true;
    protected boolean isSkipRightNear = false;
    protected boolean isSkipLeftNear = false;
    protected boolean isProgressive = true;

    private CenterBuffer mCenterBuffer = new CenterBuffer();

    double distance;
    double distanceThreshold = 80;

    public ProgressiveImageProcessing(AbstractDevice device) {
        super(device);
        mProcessorsManager = new ProcessorsManager(true);
    }

    private Rect mROIRegion = Params.CENTER_SEARCH_BOX_PROGRESSIVE;

    public void feedFrame (byte[] frame) {
        Log.i("FeedFrame","Start");
        Point2D vector = new Point2D();
        if (!mEnabled) return;

        mLastFrame = frame;
        if (frame == null || frame.length != mFrameLength) return;

        if (mCenterFinderListener == null) return;
        GridResult grid = mProcessorsManager.runGridFinderUI(frame, isProgressive);

        mCenterFinderListener.drawDebug(grid, getZero());

        if(grid!=null) {
            if (grid.centerPosition != null) {
                vector = computeAlignmentVector(grid, isDistance);
            }
//            if (vector != null) {
//                mCenterBuffer.addToBuffer(vector);
//            }
            if (vector != null) { //Limiting reach of center dot to a radius
                distance = Math.sqrt(vector.x * vector.x + vector.y * vector.y);
                if (distance > distanceThreshold) { //related to setValidZoneRadius, right now set for 54
                    vector.x = (float) ((distanceThreshold * vector.x / distance) + (vector.x * 0.1 * (distance - distanceThreshold) / distance));
                    vector.y = (float) ((distanceThreshold * vector.y / distance) + (vector.y * 0.1 * (distance - distanceThreshold) / distance));
                }
            }
            if (ImageProcessingUtil.findOpticalCtr(grid, getZero(), device)!= null ) {
//                Log.e("PixelDebug","7 Passa por aqui dentro feedFrame Prg (isCameraPreviewActive() && isLensOnCenter(grid,getZero())) "+(NetrometerApplication.get().getSettings().isCameraPreviewActive() && ImageProcessingUtil.isLensOnCenter(grid,getZero()))); //TODO: MOTOZ test if gets here

                if (NetrometerApplication.get().getSettings().isCameraPreviewActive() && ImageProcessingUtil.isLensOnCenter(grid,getZero())) {
                    Refraction refraction = ImageProcessingUtil.histogramROIsmallCtr(mROIRegion, grid, getZero(), device).generateMeanPrescription();
//                    Log.e("PixelDebug","8 Passa por aqui dentro feedFrame Prg (refraction != null) "+(refraction != null)); //TODO: MOTOZ test if gets here

                    if (refraction != null ) {
                        if (Math.abs(refraction.dCylinder) < 0.08) {
                            refraction.dCylinder = 0;
                            refraction.dAxis = 0;
                        }
//                        Log.e("PrgImProc","refraction "+ refraction.getFormattedPrescription());
                        if(refraction.dSphere<98) {
                            mCenterFinderListener.onCenterFound(vector, (grid == null) ? false : grid.isLightPipeInView, refraction);
                        } else {
                            mCenterFinderListener.onCenterFound(vector,(grid == null) ? false : grid.isLightPipeInView, null);
                        }

                    } else {
                        mCenterFinderListener.onCenterFound(vector, (grid == null) ? false : grid.isLightPipeInView,null);
                    }
                } else {
                    mCenterFinderListener.onCenterFound(vector,(grid == null) ? false : grid.isLightPipeInView, null);
                }
            } else {
                mCenterFinderListener.onCenterFound(null,false, null);
            }
        } else{
            mCenterFinderListener.onCenterFound(null,false, null);
        }

        Log.i("FeedFrame","Final");
    }

    public Point2D computeCenter(GridResult grid, boolean isDistance) {
        Point2D optCtr = ImageProcessingUtil.findOpticalCtr(grid, getZero(), device);

        Rect glassesBoundaries = ImageProcessingUtil.findFrameBoundaries(grid);
        if (glassesBoundaries==null){
            glassesBoundaries = new Rect(0,0,0,Params.PREVIEW_FRAME_WIDTH);
        }
        if (optCtr==null){
            return null;
        }

        Point2D center;
        if(isDistance) center = new Point2D((float) glassesBoundaries.top, optCtr.y);
        else center = new Point2D((float) glassesBoundaries.bottom, optCtr.y);

        return center;
    }

    @Override
    public boolean isProcessingComplete() {
        if (mZeroResult !=null && mRightGridResult !=null &&  mLeftGridResult !=null
                && mRightNearGridResult !=null &&  mLeftNearGridResult !=null ) {
            return true;
        }
        return false;
    }

    public Point2D computeAlignmentVector(GridResult grid, boolean isDistance) {
        Point2D opticalCenter = null;

        // If no zero, plot on the center.
        if (getZero() == null || getZero().centerGridPosition == null) {
            return null;
        }

        if (grid != null) {
            opticalCenter = computeCenter(grid, isDistance);
        }
        if (opticalCenter==null) return null;

        float lengthScale = 0.8f;

        Point2D refPoint;

        if(isDistance){
            refPoint = new Point2D(0.5 * Params.PREVIEW_FRAME_WIDTH - 1.35 * Params.CENTER_SEARCH_WIDTH, 0.5 * Params.PREVIEW_FRAME_HEIGHT); // 472,360
        } else {
            refPoint = new Point2D(0.7*Params.PREVIEW_FRAME_WIDTH,0.5*Params.PREVIEW_FRAME_HEIGHT);

        }

        return opticalCenter
                .minus(refPoint)
                .multiply(lengthScale);
    }

    public boolean runGridFinder(boolean isRightLenses) {

        if(isRightLenses && isSkipNear){
            isSkipRightNear = true;
            mRightNearGridResult = null;
            return true;
        } else if(!isRightLenses && isSkipNear){
            isSkipLeftNear=true;
            mLeftNearGridResult = null;
            return true;
        }

        GridResult tmp = mProcessorsManager.runGridFinder(mLastFrame);
        if (tmp == null) return false;

        if (isRightLenses && isDistance) {
//            ByteArrayReadWrite.writeAndroid(mLastFrame, "rightDistance", 1);

            mRightGridResult = tmp.clone();
            setRightFrame(mLastFrame);
            mRightFrameHolderResults =  mProcessorsManager.runFrameHolderFinder(mLastFrame,mRightGridResult, mZeroResult, isRightLenses, device);

            if (mRightGridResult.centerIsValid) return true;

        } else if (isRightLenses && !isDistance) {
//            ByteArrayReadWrite.writeAndroid(mLastFrame,"rightNear",1);

            mRightNearGridResult = tmp.clone();

            if (mRightNearGridResult.centerIsValid) return true;

        } else if (!isRightLenses && isDistance) {
//            ByteArrayReadWrite.writeAndroid(mLastFrame, "leftDistance", 1);

            mLeftGridResult = tmp.clone();
            setLeftFrame(mLastFrame);
            mLeftFrameHolderResults =  mProcessorsManager.runFrameHolderFinder(mLastFrame,mLeftGridResult, mZeroResult, isRightLenses, device);

            if (mLeftGridResult.centerIsValid) return true;

        } else {

//            ByteArrayReadWrite.writeAndroid(mLastFrame, "leftNear", 1);
            mLeftNearGridResult = tmp.clone();

            if (mLeftNearGridResult.centerIsValid) return true;
        }

        return false;
    }

    public int generateZeroValues() {
        GridResult result;
        double gridDistance;
        mZeroResult = null;
        mRightGridResult = null;
        mLeftGridResult = null;
        float averageDotDistance = device.getAverageDotDistance();

        if (mLastFrame == null) {
            return NetrometerImageProcessing.LAST_FRAME_IS_ZERO;
        }
        
        clearDebugData();
        setZeroFrame(mLastFrame);
        //ByteArrayReadWrite.writeAndroid(mLastFrame, "zero", 1);

        result = mProcessorsManager.runGridFinder(mLastFrame);
        if (result==null) {
            return CENTER_IS_NOT_VALID;
        }

        if (!result.centerIsValid) {
            return CENTER_IS_NOT_VALID;
        }

        gridDistance = ImageProcessingUtil.calculateMeanDistance(result);

        if (Double.isNaN(gridDistance)) {
 //           return CENTER_IS_NOT_VALID;
        }

        if (gridDistance< averageDotDistance*(1-0.02) || gridDistance > averageDotDistance*(1+0.02)) {
//            return HAS_LENS;
        }

        mZeroResult = result.clone();

        return ALL_SET;
    }

    public Refraction calculatePrescription(boolean isRightLenses){
        if (isRightLenses) {
            Refraction rightPrescription = getRefraction( mZeroResult, mRightGridResult, mRightNearGridResult, isSkipRightNear);
            if (rightPrescription == null) {
                return null;
            }

            if (mRightFrameHolderResults!=null){
                mRightFrameHolderResults.calculatePD(rightPrescription.calculateDiopterProjection(0),isRightLenses);
                if(mRightFrameHolderResults.PD!=null)
                    rightPrescription.setPD(mRightFrameHolderResults.PD);

                if(rightPrescription.dCylinder !=0 && mRightFrameHolderResults!=null && mRightFrameHolderResults.frameAngle != null)
                    rightPrescription.setAxis(rightPrescription.dAxis + mRightFrameHolderResults.frameAngle);
            }

            rightPrescription.checkAxisOutOfBounds();
            Log.e("PrgImProc", "right PD: "+rightPrescription.dPartialPD+", Sph projection: "+rightPrescription.calculateDiopterProjection(0));
            Log.e("PrgImProc", "right Prescrip 2: "+rightPrescription.getFormattedPrescription());
            return rightPrescription;
        } else {
            Refraction leftPrescription = getRefraction(mZeroResult, mLeftGridResult, mLeftNearGridResult, isSkipLeftNear);

            if (leftPrescription == null) {
                return null;
            }

            if(mLeftFrameHolderResults!=null) {
                mLeftFrameHolderResults.calculatePD(leftPrescription.calculateDiopterProjection(0),isRightLenses);
                if(mLeftFrameHolderResults.PD!=null)
                    leftPrescription.setPD(mLeftFrameHolderResults.PD);

                if (leftPrescription.dCylinder != 0 && mLeftFrameHolderResults!=null && mLeftFrameHolderResults.frameAngle != null )
                    leftPrescription.setAxis(leftPrescription.dAxis + mLeftFrameHolderResults.frameAngle);
            }

            leftPrescription.checkAxisOutOfBounds();
            Log.e("PrgImProc", "left PD: "+leftPrescription.dPartialPD+", Sph projection: "+leftPrescription.calculateDiopterProjection(0));
            Log.e("PrgImProc", "left Prescrip: "+leftPrescription.getFormattedPrescription());
            return leftPrescription;
        }
    }

    @NonNull
    private Refraction getRefraction(GridResult mZeroResult, GridResult mDistGridResult, GridResult mLeftNearGridResult, boolean isSkipNear) {
        if (mZeroResult==null || mDistGridResult ==null || (mLeftNearGridResult==null&& !isSkipNear)) return null;
        float ADDr;

        Point2D optCtr = ImageProcessingUtil.findOpticalCtr(mDistGridResult, mZeroResult, device);
        if (optCtr==null){
            optCtr = new Point2D(0.5* Params.PREVIEW_FRAME_WIDTH, 0.5*Params.PREVIEW_FRAME_HEIGHT);
        }

        Rect glassesFrame = ImageProcessingUtil.findFrameBoundaries(mDistGridResult);

        Point2D centerDistanceROI= new Point2D(glassesFrame.top + 1.5 * Params.CENTER_SEARCH_WIDTH, optCtr.y);

        Rect center_PROGRESSIVE_DISTANCE_BOX = new Rect(
                (int) (centerDistanceROI.x - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
                (int) (centerDistanceROI.y - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
                (int) (centerDistanceROI.x + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
                (int) (centerDistanceROI.y + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2));

        Rect center_PROGRESSIVE_READING_BOX = new Rect(
                glassesFrame.top,
                (int) (0),
                glassesFrame.bottom,
                Params.PREVIEW_FRAME_HEIGHT);

        Refraction distancePrescription = ImageProcessingUtil.histogramROI(center_PROGRESSIVE_DISTANCE_BOX, mDistGridResult, mZeroResult, device).generateDistancePrescription();
        Log.e("PrgImProc", "distance Prescrip: "+distancePrescription.getFormattedPrescription());
        if (isSkipNear){
            // round to 0.25 diopter precision
            float SPr = RefRounding.roundTo((float)distancePrescription.dSphere, Params.RESULT_STEP_SPHERE);
            float CYr = RefRounding.roundTo((float)distancePrescription.dCylinder, Params.RESULT_STEP_CYLINDER);
            float AXr= (float) distancePrescription.dAxis;// = RefRounding.roundTo((float)distancePrescription.dAxis, Params.RESULT_STEP_AXIS);

            if (CYr>-0.25){
                AXr =0;
            } else{ if(AXr==0){
                AXr = 180;
            }
            }

            return new Refraction(SPr, CYr, AXr, 0f);
        }
        //Calculate sphere value of Near in the corridor of the lens, after thresholding the histogram between two Gaussian distributions, using Otsu method
        RefractionStats readingStats = ImageProcessingUtil.histogramROI(center_PROGRESSIVE_READING_BOX, mLeftNearGridResult, mZeroResult, device);
        Log.e("PrgImProc", "reading Prescrip: "+readingStats.generateReadingPrescription().getFormattedPrescription());
        if(readingStats.getSphereStats().getN()<=1 && readingStats.getSphereStats().getMean()==99){
            ADDr = 99f;
        } else {
            RefractionStats corridorStats = new RefractionStats();
            DescriptiveStatistics corridorSphStats = new DescriptiveStatistics();
            DescriptiveStatistics corridorSphStats2 = new DescriptiveStatistics();

            for (int jj = 0; jj < readingStats.getSphereStats().getValues().length; jj++) {
                if (readingStats.getCylinderStats().getElement(jj) >= distancePrescription.dCylinder - .25 && readingStats.getCylinderStats().getElement(jj) <= distancePrescription.dCylinder + .25) {
                    corridorSphStats.addValue(readingStats.getSphereStats().getElement(jj));
                }
            }
            corridorStats.setSphereStats(corridorSphStats);

            double threshold = OtsuThresholder.doThreshold(corridorStats.getSphereStats().getValues());
            for (int jj = 0; jj < corridorSphStats.getValues().length; jj++) {
                if (corridorSphStats.getElement(jj) >= threshold) {
                    corridorSphStats2.addValue(corridorSphStats.getElement(jj));
                }
            }
            ADDr = RefRounding.roundTo((float)calculateAdd(corridorSphStats2.getMean(), distancePrescription.dSphere), Params.RESULT_STEP_SPHERE);

        }
        // round to 0.25 diopter precision
        float SPr = RefRounding.roundTo((float)distancePrescription.dSphere, Params.RESULT_STEP_SPHERE);
        float CYr = RefRounding.roundTo((float)distancePrescription.dCylinder, Params.RESULT_STEP_CYLINDER);
        float AXr = RefRounding.roundTo((float)distancePrescription.dAxis, Params.RESULT_STEP_AXIS);

        if( ADDr<0){
            ADDr = 0;
        }

        if (CYr>-0.25){
            AXr =0;
        } else { if(AXr==0){
            AXr = 180;
            }
        }
        Log.e("PrgImProc", "distance Prescrip: "+SPr+ ", "+CYr+ ", "+AXr+ ", ADD "+ ADDr);
        return new Refraction(SPr, CYr, AXr, ADDr);
    }

    public static double calculateAdd(double sphNear, double sphDistance){
        return (sphNear-sphDistance);
    }

    @Override
    public Point2D getCrosshairReferPoint() {
        //return new Point2D(0.4*Params.PREVIEW_FRAME_WIDTH,0.5*Params.PREVIEW_FRAME_HEIGHT);
        return new Point2D(0.5*Params.PREVIEW_FRAME_WIDTH-1.5*Params.CENTER_SEARCH_WIDTH,0.5*Params.PREVIEW_FRAME_HEIGHT);
    }

    public void setIsDistance(boolean isDistance) {
        this.isDistance = isDistance;
    }

    public void setIsSkipNear(boolean isSkipNear) {
        this.isSkipNear = isSkipNear;
    }

    public class CenterBuffer extends ArrayList<Point2D> {
        private static final int BUFFER_SIZE = 3;

        protected synchronized void addToBuffer(Point2D point){
            add(0, point);

            if (size() > BUFFER_SIZE) {
                remove(BUFFER_SIZE);
            }
        }

        protected Point2D getAverage() {
            float sumX = 0;
            float sumY = 0;
            float count = 0;

            for (Point2D point:mCenterBuffer) {
                count++;
                sumX += point.x;
                sumY += point.y;

            }

            return new Point2D(sumX/count, sumY/count);
        }
    }
}

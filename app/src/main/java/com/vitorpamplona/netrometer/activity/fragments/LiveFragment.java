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
package com.vitorpamplona.netrometer.activity.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.activity.views.EasyPlotter;
import com.vitorpamplona.netrometer.imageprocessing.calibration.CalibrationHelper;
import com.vitorpamplona.netrometer.imageprocessing.listeners.CenterFinderListener;
import com.vitorpamplona.netrometer.imageprocessing.model.FrameHolderResults;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.imageprocessing.model.RefractionStats;
import com.vitorpamplona.netrometer.imageprocessing.processors.FrameHolderProcessor;
import com.vitorpamplona.netrometer.imageprocessing.utils.CoordinateTransform2D;
import com.vitorpamplona.netrometer.imageprocessing.utils.FittingCoefficients;
import com.vitorpamplona.netrometer.imageprocessing.utils.ImageProcessingUtil;
import com.vitorpamplona.netrometer.model.CenterOfMass;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Point2D;
import com.vitorpamplona.netrometer.utils.Rect;
import com.vitorpamplona.netrometer.utils.TimingTools;

import java.util.ArrayList;



public class LiveFragment extends AbstractNetrometerFragment implements CenterFinderListener {

    private EasyPlotter mLivePlottingView;
    private Button mLiveCalibrateButton;
    private Button mLiveLoadCoeffButton;
    private Button mLiveSaveCoeffButton;
    private Button mLiveResultsButton;
    private Button mLivePDButton;
    private TextView mLiveCoefficientsText;
    private TextView mLiveCurrentResultsText;
    private TextView mLiveSampleResultsText;
    private TextView mLiveSphereCyl;


    private boolean mProcessing;
    private boolean mIsSphere = true;
    private boolean mIsRightPD = false;

    private CalibrationHelper mCalibrationHelper;

    private FittingCoefficients mFit;
    private boolean mToggleCalibrateButtons;

    public LiveFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live, container, false);

        // Set up views
        mLivePlottingView = (EasyPlotter) view.findViewById(R.id.live_plotting_view);


        // Hide buttons
        mToggleCalibrateButtons = false;
//        toggleCalibrateButtons();
        getNetActivity().disableToolbarView();

        // Calibrator
        mCalibrationHelper = new CalibrationHelper();
        mFit = getSettings().getFittingCoefficients();


        // Listeners
        buttonListeners(view);
        liveSwitchListener(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getNetActivity().animateToShowCamera();
        ((NetrometerActivity) getActivity()).initializeImageProcessing(AbstractCalibratingFragment.LENS_TYPE.SINGLE_VISION, false);
        getImageProcessor().registerCenterFinderListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getImageProcessor().registerCenterFinderListener(null);
        ((NetrometerActivity) getActivity()).finishImageProcessing();
    }

    private void buttonListeners(View view) {

        // Tare the system
        Button zeroButton = (Button) view.findViewById(R.id.live_zero_button);
        zeroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageProcessor().generateZeroValues();
                if (getImageProcessor().getZero() != null) {
                    mCalibrationHelper.saveResultToCsv("zero", getImageProcessor().getZero());
                }
            }
        });

        // Show / Hide calibrate button views
//        Button calibrateOpenCloseButton = (Button) view.findViewById(R.id.live_calibrate_open_close_button);
//        calibrateOpenCloseButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mCalibrationHelper.reset();
////                mLiveCalibrateButton.setText(mCalibrationHelper.getInstructionsText());
//                toggleCalibrateButtons();
//            }
//        });


//        mLiveSphereCyl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mIsSphere) {
//                    mIsSphere = false;
//                    mLiveSphereCyl.setText("Cyl");
//                } else {
//                    mIsSphere = true;
//                    mLiveSphereCyl.setText("Sph");
//                }
//            }
//        });

    }

    private void toggleCalibrateButtons() {

        if (mToggleCalibrateButtons) {

            mLiveCalibrateButton.setVisibility(View.VISIBLE);
            mLiveLoadCoeffButton.setVisibility(View.VISIBLE);
            mLiveSaveCoeffButton.setVisibility(View.VISIBLE);
            mLiveCoefficientsText.setVisibility(View.VISIBLE);
            mLiveResultsButton.setVisibility(View.VISIBLE);
            mToggleCalibrateButtons = false;

        } else {

            mLiveCalibrateButton.setVisibility(View.GONE);
            mLiveLoadCoeffButton.setVisibility(View.GONE);
            mLiveSaveCoeffButton.setVisibility(View.GONE);
            mLiveCoefficientsText.setVisibility(View.GONE);
            mLiveResultsButton.setVisibility(View.GONE);
            mToggleCalibrateButtons = true;

        }

    }


    TimingTools time = new TimingTools();

    public void drawDebug(GridResult result, GridResult zero) {}

    private void drawROIsDotsProgressives(GridResult result, GridResult zero) {

        Point2D baseCenter = zero.centerPosition;
        Point2D center = result.centerPosition;
        CoordinateTransform2D co = Params.TF;

        byte[] frame = getImageProcessor().getLastFrame();


        Point2D optCtr = ImageProcessingUtil.findOpticalCtr(result, zero, getNetActivity().getApp().getDevice());
        Point2D picCtr = new Point2D(Params.PREVIEW_FRAME_WIDTH/2,Params.PREVIEW_FRAME_HEIGHT/2 );

        Rect frameBoundary = ImageProcessingUtil.findFrameBoundaries(result);

        if(optCtr == null){
            optCtr = new Point2D(Params.PREVIEW_FRAME_WIDTH/2, Params.PREVIEW_FRAME_HEIGHT/2);
        }

        Rect glassesFrame = ImageProcessingUtil.findFrameBoundaries(result);
        Point2D centerDistanceROI;
        Point2D centerReadingROI;

        mLivePlottingView.clearData();
        FrameHolderProcessor frameHolderProcessor = new FrameHolderProcessor(Params.PREVIEW_FRAME_WIDTH, Params.PREVIEW_FRAME_HEIGHT);
        frameHolderProcessor.setFilter(Params.BLUE_STANDARD_FILTER);
        Rect roiCenter = new Rect();

        if(glassesFrame.top>0){
            Log.e("gauss", 0 + ", " + 0 + "," + Params.PREVIEW_FRAME_HEIGHT + ", " + glassesFrame.top);
            roiCenter.set(0, 0, glassesFrame.top-80,Params.PREVIEW_FRAME_HEIGHT);
            if(glassesFrame.top-80<0) roiCenter.set(0, 0, 80,Params.PREVIEW_FRAME_HEIGHT);
        } else roiCenter=glassesFrame;

        frameHolderProcessor.setSearchBox(roiCenter);
        ArrayList<CenterOfMass> centersOfMass = new ArrayList<CenterOfMass>() ;
        centersOfMass= frameHolderProcessor.run(frame);

//        mLivePlottingView.rectangle(co.rect(roiCenter.convertToAndroid()), "b", 12);

        centerDistanceROI = new Point2D(Params.PREVIEW_FRAME_WIDTH/2, Params.PREVIEW_FRAME_HEIGHT/2);
        Point2D landmarkTop = new Point2D(-1.7*Params.CENTER_SEARCH_WIDTH+0.5*Params.PREVIEW_FRAME_WIDTH, 0.5*Params.PREVIEW_FRAME_HEIGHT);
        Point2D landmarkBottom = new Point2D(0.7*Params.PREVIEW_FRAME_WIDTH, 0.5*Params.PREVIEW_FRAME_HEIGHT);

        Rect center_BOX = new Rect(
                (int) (centerDistanceROI.x - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH /2),
                (int) (centerDistanceROI.y - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2 ),
                (int) (centerDistanceROI.x + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH /2),
                (int) (centerDistanceROI.y + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2));


        //centerDistanceROI = new Point2D(glassesFrame.top + 1.5 * Params.CENTER_SEARCH_WIDTH, optCtr.y);
        centerDistanceROI = new Point2D(glassesFrame.top + 1.5 * Params.CENTER_SEARCH_WIDTH, Params.PREVIEW_FRAME_HEIGHT/2);


        RefractionStats statsCenter = ImageProcessingUtil.histogramROI(center_BOX, result, zero, getNetActivity().getApp().getDevice());

        // Frame
        Point2D vectorA, vectorB;
        double projectionA=0, lenghtB=0, angleB=0, angleFinal, vectorBsize;
        double distOptCtr=0, refCenterMM=0, refOptCtrPx=0;
//        boolean isRightLenses = false;//

        if(centersOfMass!=null && centersOfMass.size()>0 ) {
            if (centersOfMass.size() > 1) {
                vectorB = new Point2D(centersOfMass.get(0).getCenterOfMass().x - centersOfMass.get(1).getCenterOfMass().x, centersOfMass.get(0).getCenterOfMass().y - centersOfMass.get(1).getCenterOfMass().y);
                vectorBsize = Math.sqrt(vectorB.x*vectorB.x+vectorB.y*vectorB.y);
                if(mIsRightPD) {
                    if(centersOfMass.get(1).getSizeOfMass()>=1.3*centersOfMass.get(0).getSizeOfMass()) {//Equal size
                        refCenterMM = ( (Params.PREVIEW_FRAME_HEIGHT/2) - centersOfMass.get(1).getCenterOfMass().y)* Params.FH_RIGHT_MAJOR_DISTANCE/vectorB.y +Params.FH_RIGHT_MAIN_REF_DISTANCE ;
//                            Log.e("gauss","RIGHT Lens and major axis: both equal");
                    } else {//Left larger
                        refCenterMM = (Params.FH_RIGHT_MAIN_REF_DISTANCE-Params.FH_RIGHT_MINOR_DISTANCE) + ( (Params.PREVIEW_FRAME_HEIGHT/2) - centersOfMass.get(1).getCenterOfMass().y)* Params.FH_RIGHT_MINOR_DISTANCE/vectorB.y;
//                            Log.e("gauss","RIGHT Lens and minor: different");
                    }
                } else{

                    if(centersOfMass.get(1).getSizeOfMass()>=1.3*centersOfMass.get(0).getSizeOfMass()) { //Equal size
                        refCenterMM = -( (Params.PREVIEW_FRAME_HEIGHT/2) - centersOfMass.get(1).getCenterOfMass().y)* Params.FH_LEFT_MAJOR_DISTANCE/vectorB.y +Params.FH_LEFT_MAIN_REF_DISTANCE ;
//                            Log.e("gauss","LEFT Lens and major: both equal, (0): "+ centersOfMass.get(0).getSizeOfMass()+" , (1): "+centersOfMass.get(1).getSizeOfMass());
                    } else{ //left bigger
//                            Log.e("gauss","LEFT Lens and minor: different");
                        refCenterMM = (Params.FH_LEFT_MAIN_REF_DISTANCE+Params.FH_LEFT_MINOR_DISTANCE) - ( (Params.PREVIEW_FRAME_HEIGHT/2) - centersOfMass.get(1).getCenterOfMass().y)* Params.FH_LEFT_MINOR_DISTANCE/vectorB.y;
                    }
                }
                angleB = Math.atan(vectorB.x/vectorB.y)*(180/3.14159);
//                mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 1020, 150), "CM(1) (" + String.format("%.1f", centersOfMass.get(1).getCenterOfMass().x) + ", "+ String.format("%.1f", centersOfMass.get(1).getCenterOfMass().y) + "), CM(0) ("+ String.format("%.1f", centersOfMass.get(0).getCenterOfMass().x)+", " + String.format("%.1f", centersOfMass.get(0).getCenterOfMass().y)+"), vBsize: "+ String.format("%.1f",vectorBsize), "c", 40);

            }
            refOptCtrPx = optCtr.y-(Params.PREVIEW_FRAME_HEIGHT/2);

        }
        FrameHolderResults frameHolderResults = new FrameHolderResults(angleB, (double)0, refCenterMM, refOptCtrPx);
//        frameHolderResults.calculatePD(statsCenter.getSphereStats().getPercentile(50), mIsRightPD);//+.5*statsCenter.getCylinderStats().getPercentile(50)

        if(centersOfMass!=null) {
            Log.e("gauss", "centersOfMass.size(): "+centersOfMass.size());
            if(  centersOfMass.size()>0 && centersOfMass.get(0).getCenterOfMass()!=null) {
                mLivePlottingView.circle(co.point(centersOfMass.get(0).getCenterOfMass()), 10, "bw");

                if (centersOfMass.size() > 1) {
                    mLivePlottingView.circle(co.point(centersOfMass.get(1).getCenterOfMass()), 10, "bw");
                }
            }
        }
//        if(mIsRightPD) {
//            mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 900, 50), "refCenterMM "+String.format("%.2f", frameHolderResults.refCenterMm)+", refOptCtrPx: " + String.format("%.2f", frameHolderResults.refOptCtrPx) , "c", 40);
//            mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 900, 100), "Right PD " + String.format("%.2f", frameHolderResults.PD) + ", Angle: " + String.format("%.1f", frameHolderResults.frameAngle), "c", 40);
//
//        }else {
//            mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 900, 50), "refCenterMM "+String.format("%.2f", frameHolderResults.refCenterMm)+", refOptCtrPx: " + String.format("%.2f", frameHolderResults.refOptCtrPx) , "c", 40);
//            mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 900, 100), "Left PD " + String.format("%.2f", frameHolderResults.PD) + ", Angle: " + String.format("%.1f", frameHolderResults.frameAngle), "c", 40);
//        }
//        mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 900, 200), "OptCtr: " + String.format("%.2f", optCtr.x) + ", "+String.format("%.2f", optCtr.y),"c", 40);
//        Log.e("gauss","OptCtr: " + String.format("%.2f", optCtr.x) + ", "+String.format("%.2f", optCtr.y));

        // Draw ROIs rectangles

//        mLivePlottingView.rectangle(co.rect(center_BOX.convertToAndroid()), "b",15);
//        mLivePlottingView.rectangle(co.rect(Params.CENTER_SEARCH_BOX_PROGRESSIVE.convertToAndroid()),"m",12);

//        mLivePlottingView.circle(co.point(Params.CENTER_SEARCH_LOCATION), 100, "r", 5);
        mLivePlottingView.circle(co.point(zero.centerPosition), 100, "r", 5);

        mLivePlottingView.circle(co.point(result.centerPosition), 10, "ww");


        // Draw Optical Center
//        mLivePlottingView.circle(co.point(optCtr), 10, "br");
//        mLivePlottingView.circle(co.point(new Point2D(640f,360f)), 10, "bc");
//        mLivePlottingView.circle(co.point(landmarkTop),15,"mm");
//        mLivePlottingView.circle(co.point(landmarkBottom),15,"mm");


        // Draw newly found center and circle points
//        if (result.pointsOnGrid != null && center != null) {
//
//            Log.e("gauss", "result.pointsOnGrid.length: "+result.pointsOnGrid.length+" , result.pointsOnGrid[0].length"+result.pointsOnGrid[0].length);
//            // Center
////            mLivePlottingView.circle(co.point(center), 5, "pw");
//
//            for (int x = 0; x < Params.ROW_DOT_COUNT_SINGLEVISION_UI; x++) {
//                for (int y = 0; y < Params.COLUMN_DOT_COUNT_SINGLEVISION_UI; y++) {
//
//                    String color = "it";
////
//
//                    if (result.pointsOnGrid[x][y] != null && result.pointsOnGrid[x][y].isValid)
//                        mLivePlottingView.circle(co.point(new Point2D(result.pointsOnGrid[x][y].x, result.pointsOnGrid[x][y].y)), 5, color);
//                }
//            }
//        }

//        // Draw line between corresponding dots center and circle points
//        if (result.pointsOnGrid != null && center != null) {
//
//            // Center
//            //mLivePlottingView.circle(co.point(center), 5, "pr");
//
//            for (int y = 0; y < Params.COLUMN_DOT_COUNT_SINGLEVISION_UI; y++) {
//                for (int x = 0; x < Params.ROW_DOT_COUNT_SINGLEVISION_UI; x++) {
//
//                    if (result.pointsOnGrid[x][y] != null && result.pointsOnGrid[x][y].isValid)
//                        mLivePlottingView.line(co.point(zero.pointsOnGrid[x][y]), co.point(result.pointsOnGrid[x][y]), "w", 2f);
//                }
//            }
//        }

//        mLivePlottingView.circle(co.point(new Point2D(frameBoundary.top, result.pointsOnGrid[0][result.pointsOnGrid[0].length/2].y)), 20, "bc");
//        mLivePlottingView.circle(co.point(new Point2D(frameBoundary.bottom,result.pointsOnGrid[result.pointsOnGrid.length-1][result.pointsOnGrid[0].length/2].y)), 20, "br");

        if(statsCenter.getCylinderStats().getPercentile(50)!=0) {
            angleFinal = angleB + statsCenter.getAxisStats().getPercentile(50);
            mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 1000, 100), "SPH " + String.format("%.2f", statsCenter.getSphereStats().getPercentile(50)) + ", CYL " + String.format("%.2f", statsCenter.getCylinderStats().getPercentile(50)) + ", AXIS " + String.format("%.0f", statsCenter.getAxisStats().getPercentile(50)) + ", AXIS F " + String.format("%.0f", angleFinal), "c", 50);
        } else{
            mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 1000, 100), "SPH " + String.format("%.2f", statsCenter.getSphereStats().getPercentile(50)) + ", CYL " + 0 + ", AXIS " + 0 + ", AXIS Fin " + 0, "c", 50);
        }
        mLivePlottingView.plotNow();

    }




    private double interpolate( double val, double y0, double x0, double y1, double x1 ) {
        return (val-x0)*(y1-y0)/(x1-x0) + y0;
    }

    private double base( double val ) {
        if ( val <= -0.75 ) return 0;
        else if ( val <= -0.25 ) return interpolate( val, 0.0, -0.75, 1.0, -0.25 );
        else if ( val <= 0.25 ) return 1.0;
        else if ( val <= 0.75 ) return interpolate( val, 1.0, 0.25, 0.0, 0.75 );
        else return 0.0;
    }

    private double red( double gray ) {
        return base( gray - 0.5 );
    }
    private double green( double gray ) {
        return base( gray );
    }
    private double blue( double gray ) {
        return base( gray + 0.3 );
    }

    private void liveSwitchListener(View view) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ((NetrometerActivity) getActivity()).loadResultNoSmartStageFragment(false, null);
                return false;
            }
        });

    }


    private void drawLiveProgressives(GridResult result, GridResult zero) {

        Point2D baseCenter = zero.centerPosition;
        Point2D center = result.centerPosition;
        CoordinateTransform2D co = Params.TF;
        Point2D optCtr = ImageProcessingUtil.findOpticalCtr(result,zero, getNetActivity().getApp().getDevice());
        if(optCtr == null){
            optCtr = new Point2D(Params.PREVIEW_FRAME_WIDTH/2, Params.PREVIEW_FRAME_HEIGHT/2);
        }


//        float distCenter2OptCtr = (float) Math.sqrt((optCtr.x-baseCenter.x)*(optCtr.x-baseCenter.x)+(optCtr.y-baseCenter.y)*(optCtr.y-baseCenter.y));
        Point2D centerDistanceROI;
        Point2D centerReadingROI;
        Rect glassesFrame = ImageProcessingUtil.findFrameBoundaries(result);
        mLivePlottingView.clearData();

        if (optCtr.x - glassesFrame.top < 5* Params.UNIT_CONVERSION_PIXEL2MM) {
            centerDistanceROI = new Point2D(glassesFrame.top + 1.5 * Params.CENTER_SEARCH_WIDTH, optCtr.y);
        } else {
            //centerDistanceROI = new Point2D(optCtr.x - 5 * Params.UNIT_CONVERSION_PIXEL2MM, optCtr.y);
            // centerDistanceROI = new Point2D((optCtr.x+ glassesFrame.top)/2, optCtr.y);
            centerDistanceROI = new Point2D(glassesFrame.top + 1.5 * Params.CENTER_SEARCH_WIDTH, optCtr.y);
        }
        Rect center_PROGRESSIVE_DISTANCE_BOX = new Rect(
                (int) (centerDistanceROI.x - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH /2),
                (int) (centerDistanceROI.y - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2 ),
                (int) (centerDistanceROI.x + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH /2),
                (int) (centerDistanceROI.y + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2));


        if(glassesFrame.bottom-optCtr.x < 12*Params.UNIT_CONVERSION_PIXEL2MM){
            centerReadingROI = new Point2D(glassesFrame.bottom - 2*Params.CENTER_SEARCH_WIDTH, optCtr.y+ 1* Params.CENTER_PROGRESSIVE_SEARCH_WIDTH);

        } else {
            centerReadingROI = new Point2D(optCtr.x + 12 * Params.UNIT_CONVERSION_PIXEL2MM, optCtr.y + 1* Params.CENTER_PROGRESSIVE_SEARCH_WIDTH);

        }
        Rect center_PROGRESSIVE_READING_BOX = new Rect(
                (int)optCtr.x,
                (int)optCtr.y,
                glassesFrame.bottom,
                Params.PREVIEW_FRAME_HEIGHT);

        if(glassesFrame.bottom-optCtr.x < 12*Params.UNIT_CONVERSION_PIXEL2MM){
            centerReadingROI = new Point2D(glassesFrame.bottom - 2*Params.CENTER_SEARCH_WIDTH, optCtr.y- 1* Params.CENTER_PROGRESSIVE_SEARCH_WIDTH);

        } else {
            centerReadingROI = new Point2D(optCtr.x + 12 * Params.UNIT_CONVERSION_PIXEL2MM, optCtr.y - 1* Params.CENTER_PROGRESSIVE_SEARCH_WIDTH);

        }
        Rect center_PROGRESSIVE_READING_BOX2 = new Rect(
                (int)optCtr.x,
                0,
                glassesFrame.bottom,
                (int)optCtr.y);


        //Draw newly found center and circle points
        if (result.pointsOnGrid != null && center != null) {


            for (int x = (int) (Params.MAX_NEIGHBORS_RADIUS/2 - 1); x < Params.ROW_DOT_COUNT_PROGRESSIVE - (int) (Params.MAX_NEIGHBORS_RADIUS/2); x += Params.PROGRESSIVE_PROBE_STEP) {
                for (int y = (int) (Params.MAX_NEIGHBORS_RADIUS/2 - 1); y < Params.COLUMN_DOT_COUNT_PROGRESSIVE - (int) (Params.MAX_NEIGHBORS_RADIUS/2); y += Params.PROGRESSIVE_PROBE_STEP) {

                    if (result.pointsOnGrid[x][y] != null && result.pointsOnGrid[x][y].isValid) {

                        zero.centerGridPosition = new Point2D(x, y);
                        result.centerGridPosition = new Point2D(x, y);
                        // Sphere value of specific dot
                        double dotSphere;
                        if(mIsSphere) {
                            dotSphere = ImageProcessingUtil.convertToPrescription(
                                    ImageProcessingUtil.resultsToSinusoidal(
                                            result,
                                            zero, Params.NEIGHBORS, getNetActivity().getApp().getDevice())).dSphere;
                        } else{
                            dotSphere = ImageProcessingUtil.convertToPrescription(
                                    ImageProcessingUtil.resultsToSinusoidal(
                                            result,
                                            zero, Params.NEIGHBORS, getNetActivity().getApp().getDevice())).dCylinder;
                        }
                        //Normalization of Diopter Value
                        double range_diopter_min = -6;
                        double range_diopter_max =1;

                        double range_display_min = 0;
                        double range_display_max = 1;

                        double normGray = interpolate(dotSphere, range_display_min, range_diopter_min, range_display_max, range_diopter_max);
                        // Define red, green,blue
                        double normRed = red(normGray);
                        double normGreen = green(normGray);
                        double normBlue = blue(normGray);

                        //Transform to hex values
                        String hexRed = Integer.toHexString((int) (normRed * 255));
                        if (hexRed.length() < 2) {
                            hexRed = "0" + hexRed; // pad with leading zero if needed
                        }
                        String hexGreen = Integer.toHexString((int) (normGreen * 255));
                        if (hexGreen.length() < 2) {
                            hexGreen = "0" + hexGreen; // pad with leading zero if needed
                        }
                        String hexBlue = Integer.toHexString((int) (normBlue * 255));
                        if (hexBlue.length() < 2) {
                            hexBlue = "0" + hexBlue; // pad with leading zero if needed
                        }
                        String color = hexRed + hexGreen + hexBlue;
//                        if(y== Params.COLUMN_CENTER_POINT & x==Params.ROW_CENTER_POINT)
//                            Log.e("LiveFrag", "isSphere? "+mIsSphere+ " dotSphere= "+dotSphere+" gray: "+ normGray + "(RGB): ("+ normRed+", "+normGreen+", "+ normBlue+")");
                        // Plot field with jet color
                        mLivePlottingView.rectangle(co.rect(new Rect((int) (result.pointsOnGrid[x][y].x - 7 * Params.PROGRESSIVE_PROBE_STEP), (int) (result.pointsOnGrid[x][y].y - 7 * Params.PROGRESSIVE_PROBE_STEP), (int) (result.pointsOnGrid[x][y].x + 7 * Params.PROGRESSIVE_PROBE_STEP), (int) (result.pointsOnGrid[x][y].y + 7 * Params.PROGRESSIVE_PROBE_STEP)).convertToAndroid()), "#ff" + color + "#ff" + color);
                        Point2D pointR = new Point2D(result.pointsOnGrid[x][y].x, result.pointsOnGrid[x][y].y);
                        mLivePlottingView.text(co.point(pointR), String.format("%.2f", dotSphere) , "w", 30);

                    }

                }
            }
        }
        // Draw base center
        if (zero.pointsOnGrid != null && baseCenter != null) {

            // center
            mLivePlottingView.circle(co.point(baseCenter), 5, "bc");

        }

        // Current Center
        //mLivePlottingView.circle(co.point(center), 5, "bb");

//        mLivePlottingView.rectangle(co.rect(center_PROGRESSIVE_DISTANCE_BOX.convertToAndroid()), "b",15);
//        mLivePlottingView.rectangle(co.rect(center_PROGRESSIVE_READING_BOX.convertToAndroid()), "b",15);
//        mLivePlottingView.rectangle(co.rect(center_PROGRESSIVE_READING_BOX2.convertToAndroid()), "b",15);

        // Draw Optical Center
        mLivePlottingView.circle(co.point(optCtr), 10, "br");
        mLivePlottingView.circle(co.point(new Point2D(0.4 * Params.PREVIEW_FRAME_WIDTH, 0.5 * Params.PREVIEW_FRAME_HEIGHT)), 15, "bb");

//        mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 900, 150), "rR " + String.format("%.2f", statsROI.getSphereStats().getMean()) + " +-" + String.format("%.2f", statsROI.getSphereStats().getStandardDeviation()) + ", Mdian " + String.format("%.2f", statsROI.getSphereStats().getPercentile(50)) + ", 95P " + String.format("%.2f", statsROI.getSphereStats().getPercentile(95)) + ", MAX " + String.format("%.2f", statsROI.getSphereStats().getMax()), "r", 40);
//        mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 900, 200), "rL " + String.format("%.2f", statsROI2.getSphereStats().getMean()) + " +-" + String.format("%.2f", statsROI2.getSphereStats().getStandardDeviation()) + ", Mdian " + String.format("%.2f", statsROI2.getSphereStats().getPercentile(50)) + ", 95P " + String.format("%.2f", statsROI2.getSphereStats().getPercentile(95)) + ", MAX " + String.format("%.2f", statsROI2.getSphereStats().getMax()), "r", 40);
//        mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 900, 50), "d SPH " + String.format("%.2f", statsRoiDistance.getSphereStats().getMean()) + " +-" + String.format("%.2f", statsRoiDistance.getSphereStats().getStandardDeviation()) + ", Md " + String.format("%.2f", statsRoiDistance.getSphereStats().getPercentile(50)) + ", 95P " + String.format("%.2f", statsRoiDistance.getSphereStats().getPercentile(95)) + ", MAX " + String.format("%.2f", statsRoiDistance.getSphereStats().getMax()), "r", 40);
//        mLivePlottingView.text(new Point2D(Params.PREVIEW_DISPLAY_HEIGHT - 900, 100), "d CYL " + String.format("%.2f", statsRoiDistance.getCylinderStats().getMean()) + " +-" + String.format("%.2f", statsRoiDistance.getCylinderStats().getStandardDeviation()) + ", Md " + String.format("%.2f", statsRoiDistance.getCylinderStats().getPercentile(50)) + ", 95P " + String.format("%.2f", statsRoiDistance.getCylinderStats().getPercentile(95)) + ", MAX " + String.format("%.2f", statsRoiDistance.getCylinderStats().getMax()), "r", 40);


        mLivePlottingView.plotNow();


    }



    @Override
    public void onCenterFound(Point2D center, boolean isLightPipeInView, Refraction realtime) {
        if (center!=null && !mProcessing) {
            mProcessing = true;
            new FindGridTask().execute();
        }
    }

    private class FindGridTask extends AsyncTask<Void, Void, GridResult> {

        @Override
        protected GridResult doInBackground(Void... params) {
            return getImageProcessor().runGridDebug();
        }


        @Override
        protected void onPostExecute(GridResult result) {
            if (result!=null && getImageProcessor().getZero()!=null) {

                drawROIsDotsProgressives(result, getImageProcessor().getZero());

            }
            mProcessing = false;
        }
    }
}

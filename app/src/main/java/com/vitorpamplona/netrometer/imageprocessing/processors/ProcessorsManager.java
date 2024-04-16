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
package com.vitorpamplona.netrometer.imageprocessing.processors;

import com.vitorpamplona.netrometer.imageprocessing.hardware.AbstractDevice;
import com.vitorpamplona.netrometer.imageprocessing.model.FrameHolderResults;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.utils.ImageProcessingUtil;
import com.vitorpamplona.netrometer.model.CenterOfMass;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Clock;
import com.vitorpamplona.netrometer.utils.Point2D;
import com.vitorpamplona.netrometer.utils.Rect;

import java.util.ArrayList;

public class ProcessorsManager {

    private CenterProcessor mCenterProcessor;
    private CenterProcessor mAnchorProcessor;
    private GridProcessor mGridProcessor;
    private GridProcessor mGridProcessorUI;
    private FrameHolderProcessor mFrameHolderProcessor;

    public ProcessorsManager(boolean isProgressives) {

        // set up image processors
        int w = Params.PREVIEW_FRAME_WIDTH;
        int h = Params.PREVIEW_FRAME_HEIGHT;
        mCenterProcessor = new CenterProcessor(w, h);
        mCenterProcessor.setFilter(Params.BLUE_STANDARD_FILTER);
        mCenterProcessor.setRefineDistance(10);
        mCenterProcessor.setSearchBox(Params.CENTER_SEARCH_BOX);

        mAnchorProcessor = new CenterProcessor(w, h);
        mAnchorProcessor.setFilter(Params.BLUE_STANDARD_FILTER);
        mAnchorProcessor.setRefineDistance(10);

        mFrameHolderProcessor = new FrameHolderProcessor(w,h);
        mFrameHolderProcessor.setFilter(Params.BLUE_AUX_FILTER);
        mFrameHolderProcessor.setRefineDistance(20);


        if(isProgressives) {
            mCenterProcessor.setSearchBox(Params.CENTER_SEARCH_BOX_PROGRESSIVE);
            mGridProcessor = new GridProcessor(Params.ROW_DOT_COUNT_PROGRESSIVE, Params.COLUMN_DOT_COUNT_PROGRESSIVE, Params.ROW_CENTER_POINT_PROGRESSIVE, Params.COLUMN_CENTER_POINT_PROGRESSIVE);
            mGridProcessorUI = new GridProcessor(Params.ROW_DOT_COUNT_PROGRESSIVE_UI, Params.COLUMN_DOT_COUNT_PROGRESSIVE_UI, Params.ROW_CENTER_POINT_PROGRESSIVE_UI, Params.COLUMN_CENTER_POINT_PROGRESSIVE_UI);
        } else {
            mCenterProcessor.setSearchBox(Params.CENTER_SEARCH_BOX);
            mGridProcessor = new GridProcessor(Params.ROW_DOT_COUNT, Params.COLUMN_DOT_COUNT, Params.ROW_CENTER_POINT, Params.COLUMN_CENTER_POINT);
            mGridProcessorUI = new GridProcessor(Params.ROW_DOT_COUNT_SINGLEVISION_UI, Params.COLUMN_DOT_COUNT_SINGLEVISION_UI, Params.ROW_CENTER_POINT_SINGLEVISION_UI, Params.COLUMN_CENTER_POINT_SINGLEVISION_UI);
        }
    }

    public Point2D runCenterProcessor(byte[] frame) {
       return mCenterProcessor.run(frame);
    }

    public Point2D runAnchorProcessor(byte[] frame, Rect rect) {
       mAnchorProcessor.setSearchBox(rect);
       return mAnchorProcessor.run(frame);
    }


    public GridResult runGridFinder(byte[] frame) {
        Clock c = new Clock("RunGridFinder");

        Point2D center = mCenterProcessor.run(frame);
        c.capture("Run Center Processor");
        if (center == null) {
            c.log();
            return null;
        }

        ArrayList<Point2D> anchors = new ArrayList<Point2D>();
        anchors.add(runAnchorProcessor(frame, Params.CENTER_ANCHOR_1));
        anchors.add(runAnchorProcessor(frame, Params.CENTER_ANCHOR_2));
        anchors.add(runAnchorProcessor(frame, Params.CENTER_ANCHOR_3));
        anchors.add(runAnchorProcessor(frame, Params.CENTER_ANCHOR_4));

        Point2D step = ImageProcessingUtil.calculateInitialStep(anchors);

        c.capture("Run Initial ");

        GridResult gridResult = mGridProcessor.runFilteredImage(frame, center, step, anchors);

        c.capture("Grid Processor");

        c.log();

        return gridResult;
    }

    public FrameHolderResults runFrameHolderFinder(byte[] frame, GridResult gridResult, GridResult gridZero, boolean isRightLenses, AbstractDevice device){

        Point2D optCtr = ImageProcessingUtil.findOpticalCtr(gridResult, gridZero, device);
        Rect glassesFrame = ImageProcessingUtil.findFrameBoundaries(gridResult);
        Rect roiFrameHolder = new Rect();
        Point2D vectorB;
        Double angleB=null, distOptCtr=null, refCenterMM=null, refOptCtrPx ;
        ArrayList<CenterOfMass> centersOfMass;
        if(optCtr==null) return null;
        
        if(glassesFrame.top>0){
            roiFrameHolder.set(0, 0, glassesFrame.top - 80, Params.PREVIEW_FRAME_HEIGHT);
            if(glassesFrame.top-80<0) roiFrameHolder.set(0, 0, 80,Params.PREVIEW_FRAME_HEIGHT);
        } else roiFrameHolder.set(0, 0, 80,Params.PREVIEW_FRAME_HEIGHT);
        mFrameHolderProcessor.setSearchBox(roiFrameHolder);

        centersOfMass= mFrameHolderProcessor.run(frame);
        refOptCtrPx = (double)optCtr.y - (Params.PREVIEW_FRAME_HEIGHT/2);
//        Log.e("gauss", "isRightLenses "+ isRightLenses+" , centerOfMass.size "+centersOfMass.size());
        if(centersOfMass!=null && centersOfMass.size()>0 ) {
                if (centersOfMass.size() > 1) {
                    vectorB = new Point2D(centersOfMass.get(0).getCenterOfMass().x - centersOfMass.get(1).getCenterOfMass().x, centersOfMass.get(0).getCenterOfMass().y - centersOfMass.get(1).getCenterOfMass().y);

                    if(isRightLenses) {
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
                }

        } else return null;

//        Log.e("gauss","angleB " +angleB +" , distOptCtr"+distOptCtr+ " , refCenterMM " + refCenterMM +", refOptCtrPx " +refOptCtrPx+", optCtr.y "+optCtr.y);
        return new FrameHolderResults(angleB,distOptCtr, refCenterMM, refOptCtrPx);
    }

    public GridResult runGridFinderUI(byte[] frame, boolean isProgressive) {
        Clock c = new Clock("RunGridFinder UI");
        Point2D center = mCenterProcessor.run(frame);
        c.capture("Run Center Processor");
        ArrayList<CenterOfMass> centerLightPipe = new ArrayList<>(3);

        if (center == null) return null;

        ArrayList<Point2D> anchors = new ArrayList<Point2D>();
        anchors.add(runAnchorProcessor(frame, Params.CENTER_ANCHOR_1));
        anchors.add(runAnchorProcessor(frame, Params.CENTER_ANCHOR_2));
        anchors.add(runAnchorProcessor(frame, Params.CENTER_ANCHOR_3));

        Point2D step = ImageProcessingUtil.calculateInitialStep(anchors);

        c.capture("Run Initial ");

        GridResult gridResult = mGridProcessorUI.run(frame, center, step, anchors);
        if( mFrameHolderProcessor.isUILightPipeOn(frame)) gridResult.isLightPipeInView =true;

        c.capture("Grid Processor");

        c.log();

        return gridResult;
    }

}


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

import com.vitorpamplona.netrometer.utils.RefRounding;
import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.imageprocessing.hardware.AbstractDevice;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.imageprocessing.model.VectorNeighbor;
import com.vitorpamplona.netrometer.imageprocessing.processors.ProcessorsManager;
import com.vitorpamplona.netrometer.imageprocessing.utils.ImageProcessingUtil;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.ByteArrayReadWrite;
import com.vitorpamplona.netrometer.utils.Point2D;
import com.vitorpamplona.netrometer.utils.Rect;


import java.util.ArrayList;
import java.util.List;

public class SingleVisionImageProcessing extends NetrometerImageProcessing{

    private List<VectorNeighbor> mNeighborsMask;
    private CenterBuffer mCenterBuffer = new CenterBuffer();

    double distance;
    double distanceThreshold = 80;
    protected boolean isProgressive = false;

    public SingleVisionImageProcessing(AbstractDevice device) {
        super(device);
        mNeighborsMask = Params.NEIGHBORS;
        mProcessorsManager = new ProcessorsManager(false);
    }

    private Rect mROIRegion = new Rect(
                (int)(Params.CENTER_SEARCH_LOCATION.x - Params.CENTER_SEARCH_WIDTH/4),
            (int)(Params.CENTER_SEARCH_LOCATION.y - Params.CENTER_SEARCH_WIDTH/4),
            (int)(Params.CENTER_SEARCH_LOCATION.x + Params.CENTER_SEARCH_WIDTH/4),
            (int)(Params.CENTER_SEARCH_LOCATION.y + Params.CENTER_SEARCH_WIDTH/4));

    int frameCount = 0;

    public void feedFrame (byte[] frame) {
        //ByteArrayReadWrite.writeAndroid(frame, "frame" + frameCount++, 0);

        Point2D vector = new Point2D();
        if (!mEnabled) return;

        mLastFrame = frame;
        if (frame == null || frame.length != mFrameLength) return;

        if (mCenterFinderListener == null) return;

        GridResult grid = mProcessorsManager.runGridFinderUI(frame, isProgressive);

        mCenterFinderListener.drawDebug(grid, getZero());

        if(grid != null) {
            if (grid.centerPosition != null) {
                vector = computeAlignmentVector(grid.centerPosition);
            }
            if (vector != null) {
                mCenterBuffer.addToBuffer(vector);
            }

            if (ImageProcessingUtil.findOpticalCtr(grid, getZero(), device)!= null ) {
                if (NetrometerApplication.get().getSettings().isCameraPreviewActive() && ImageProcessingUtil.isLensOnCenter(grid,getZero())) {
                    Refraction refraction = ImageProcessingUtil.histogramROI(mROIRegion, grid, getZero(), device).generateMeanPrescription();
                    if (refraction != null ) {
                        if (Math.abs(refraction.dCylinder) < 0.08) {
                            refraction.dCylinder = 0;
                            refraction.dAxis = 0;
                        }
                        if(refraction.dSphere<98) {
                            mCenterFinderListener.onCenterFound(mCenterBuffer.getAverage(), false, refraction);
                        } else {
                            mCenterFinderListener.onCenterFound(mCenterBuffer.getAverage(),false, null);
                        }

                    } else {
                        mCenterFinderListener.onCenterFound(mCenterBuffer.getAverage(), false,null);
                    }
                } else {
                    mCenterFinderListener.onCenterFound(mCenterBuffer.getAverage(),false, null);
                }
            } else {
                mCenterFinderListener.onCenterFound(null,false, null);
            }
        } else{
            mCenterFinderListener.onCenterFound(null,false, null);
        }
    }

    public Point2D computeAlignmentVector(Point2D center) {
        if (center == null) {
            return null;
//            center = getCrosshairReferPoint();
        }

        float lengthScale = 8.0f;
        GridResult zero = getZero();
        // If no zero, plot on the center.
        if (zero == null || zero.centerPosition == null) {
            return null;
        }
        return center.minus(zero.centerPosition).multiply(lengthScale);
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

    public boolean runGridFinder(boolean isRightLenses) {

        //TODO move the multiple calls to GridProcessor
        GridResult tmp = mProcessorsManager.runGridFinder(mLastFrame);

        if (tmp == null) return false;

        if (isRightLenses) {
            //ByteArrayReadWrite.writeAndroid(mLastFrame, "rightFinal", 1);
            mRightGridResult = tmp.clone();
//          setRightFrame(mLastFrame);
            if (mRightGridResult.centerIsValid) return true;
        } else {
            //ByteArrayReadWrite.writeAndroid(mLastFrame, "leftFinal", 1);
           mLeftGridResult =  tmp.clone();
//            setLeftFrame(mLastFrame);
            if (mLeftGridResult.centerIsValid) return true;
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
//        setZeroFrame(mLastFrame);

        //ByteArrayReadWrite.writeAndroid(mLastFrame, "zero", 1);

        //TODO move the multiple calls to GridProcessor
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
            return getRefraction( mZeroResult, mRightGridResult);
        } else {
            return getRefraction(mZeroResult, mLeftGridResult);
        }

    }
    @NonNull
    private Refraction getRefraction(GridResult mZeroResult, GridResult mGridResult) {
        if (mZeroResult==null || mGridResult == null)return null;

        Point2D centerROI= new Point2D(mGridResult.pointsOnGrid[Params.ROW_CENTER_POINT][Params.COLUMN_CENTER_POINT].x,mGridResult.pointsOnGrid[Params.ROW_CENTER_POINT][Params.COLUMN_CENTER_POINT].y);

        Rect center_SINGLEVISION_BOX = new Rect(
                (int) (centerROI.x - Params.CENTER_SEARCH_WIDTH/4),
                (int) (centerROI.y - Params.CENTER_SEARCH_WIDTH/4),
                (int) (centerROI.x + Params.CENTER_SEARCH_WIDTH/4),
                (int) (centerROI.y + Params.CENTER_SEARCH_WIDTH/4));


        Refraction gridPrescription = ImageProcessingUtil.histogramROI(center_SINGLEVISION_BOX, mGridResult, mZeroResult, device).generateDistancePrescription();

        // round to 0.25 diopter precision
        float SPr = RefRounding.roundTo((float) gridPrescription.dSphere, Params.RESULT_STEP_SPHERE);
        float CYr = RefRounding.roundTo((float) gridPrescription.dCylinder, Params.RESULT_STEP_CYLINDER);
        float AXr = RefRounding.roundTo((float) gridPrescription.dAxis, Params.RESULT_STEP_AXIS);

        if (CYr>-0.25){
            AXr =0;
        } else{ if(AXr==0) {
            AXr = 180;
        }
        }
        if(!ImageProcessingUtil.isLensOnCenter(mGridResult,mZeroResult)){
            Refraction invalidPrescription = new Refraction(99,0,0);
            invalidPrescription.isValid = false;
            return invalidPrescription;
        }

        return new Refraction(SPr, CYr, AXr);
    }

    @Override
    public Point2D getCrosshairReferPoint() {
        return new Point2D(0.5*Params.PREVIEW_FRAME_WIDTH,0.5*Params.PREVIEW_FRAME_HEIGHT);
    }

}

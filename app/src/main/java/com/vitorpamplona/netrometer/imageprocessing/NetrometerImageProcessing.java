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

import android.graphics.ImageFormat;
import android.graphics.YuvImage;

import com.vitorpamplona.netrometer.imageprocessing.hardware.AbstractDevice;
import com.vitorpamplona.netrometer.imageprocessing.listeners.CenterFinderListener;
import com.vitorpamplona.netrometer.imageprocessing.model.FrameHolderResults;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.imageprocessing.processors.ProcessorsManager;
import com.vitorpamplona.netrometer.utils.Rect;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Point2D;

import java.io.ByteArrayOutputStream;

public abstract class NetrometerImageProcessing {

    public static int ALL_SET = 0;
    public static int LAST_FRAME_IS_ZERO = 1;
    public static int HAS_LENS = 2;
    public static int CENTER_IS_NOT_VALID = 3;

    protected CenterFinderListener mCenterFinderListener;

    private byte[] mZeroFrame, mLeftFrame, mRightFrame;

    protected byte[] mLastFrame;
    protected GridResult mLeftGridResult;
    protected GridResult mRightGridResult;
    protected GridResult mZeroResult;
    protected FrameHolderResults mRightFrameHolderResults;
    protected FrameHolderResults mLeftFrameHolderResults;

    protected ProcessorsManager mProcessorsManager;

    protected int mWidth, mHeight, mFrameLength;

    protected boolean mEnabled = true;

    protected AbstractDevice device;

    public NetrometerImageProcessing(AbstractDevice device){
        this.device = device;
        this.mWidth = Params.PREVIEW_FRAME_WIDTH;
        this.mHeight = Params.PREVIEW_FRAME_HEIGHT;
        this.mFrameLength = (int) (mWidth * mHeight * 1.5);
    }

    public GridResult runGridDebug() {
        if (mLastFrame == null) return null;

        return mProcessorsManager.runGridFinder(mLastFrame);
    }

    public GridResult getZero() {
        return mZeroResult;
    }

    public byte[] getLastFrame(){return mLastFrame;}

    public GridResult getRightGridResult() {
        return mRightGridResult;
    }
    public GridResult getLeftGridResult() {
        return mLeftGridResult;
    }

    public void registerCenterFinderListener (CenterFinderListener listener) {
        mCenterFinderListener = listener;
    }

    public boolean isProcessingComplete() {
        if (mZeroResult !=null && mRightGridResult !=null &&  mLeftGridResult !=null) {
            return true;
        }
        return false;
    }

    public void setEnabled(boolean value) {
        mEnabled = value;
    }

    public abstract void feedFrame (byte[] frame);

    public abstract boolean runGridFinder(boolean isRightLenses);

    public abstract int generateZeroValues();

    public abstract Refraction calculatePrescription(boolean isRightLenses);

    public abstract Point2D getCrosshairReferPoint();

    public void clearDebugData() {
        mZeroFrame = null;
        mLeftFrame = null;
        mRightFrame = null;
    }

    public void setZeroFrame(byte[] b) { mZeroFrame = jpeg(b); }
    public void setLeftFrame(byte[] b) {
        mLeftFrame  = jpeg(b);
    }
    public void setRightFrame(byte[] b) {
        mRightFrame  = jpeg(b);
    }

    public byte[] getZeroFrame() { return mZeroFrame; }
    public byte[] getLeftFrame() {
        return mLeftFrame;
    }
    public byte[] getRightFrame() {
        return mRightFrame;
    }

    // **********
    // JPEG UTILS
    // **********
    public static final int DEGUG_IMAGE_QUALITY = 100;
    public static final Rect DEBUG_IMAGE_RECT = new Rect(
            (int)(Params.CENTER_SEARCH_LOCATION.x - 200),
            (int)(Params.CENTER_SEARCH_LOCATION.y - 300),
            (int)(Params.CENTER_SEARCH_LOCATION.x + 200),
            (int)(Params.CENTER_SEARCH_LOCATION.y + 300));

    private byte[] jpeg(byte[] b) {
        YuvImage yuv = new YuvImage(b, ImageFormat.NV21, Params.PREVIEW_FRAME_WIDTH, Params.PREVIEW_FRAME_HEIGHT, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(DEBUG_IMAGE_RECT.convertToAndroid(), DEGUG_IMAGE_QUALITY, out);
        return out.toByteArray();
    }
}

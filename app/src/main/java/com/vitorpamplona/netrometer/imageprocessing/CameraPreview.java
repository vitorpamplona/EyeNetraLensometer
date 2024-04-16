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

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.vitorpamplona.netrometer.NetrometerApplication;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.mode;
import static android.hardware.Camera.Parameters.SCENE_MODE_AUTO;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback,
        AutoFocusMoveCallback {
    private static final String TAG = "CameraPreview";

    private Camera mCamera;
    private boolean mPreviewRunning;
    private SurfaceView mSurfaceView;
    private final Context mContext;
    private final int mPreviewWidth;
    private final int mPreviewHeight;
    private CameraPreviewListener mCameraPreviewListener;

    public CameraPreview(Context context, int width, int height) {

        mContext = context;
        mPreviewWidth = width;
        mPreviewHeight = height;

        mPreviewRunning = false;
        mCamera = null;
    }

    public boolean safeStartCamera() {
        // abort trying after some attempts
        mPreviewRunning = false;
        Log.d(TAG, "Camera open attempt: Timeout!");

        // check if camera is still in use
        if(isCameraUsed()) {
            return false;
        }

        // safely open camera
        try {
            if (mCamera != null) {
                stopCamera();
            }
            mCamera = Camera.open();
            Log.d(TAG, "Camera opened..");

            // attach surface view to enable preview frame
            attachSurface(mSurfaceView);

            startCamera();
            Log.d(TAG, "Camera started..");

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to open Camera!");
        }
        return false;
    }

    private boolean isCameraUsed() {
        Camera dummy = null;
        try {
            dummy = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (dummy != null) dummy.release();
        }
        return false;
    }

    private void attachSurface(SurfaceView surface) {

        if (mPreviewRunning) {
            pauseCamera();
        }
           
        try {
            SurfaceHolder surfaceHolder = surface.getHolder();
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    byte mBuffer[];

    public void startCamera() {
        
        if (mCamera != null) {
        	// set the callback buffer for efficient memory reuse
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.setAutoFocusMoveCallback(this);
            mCamera.setDisplayOrientation(90);
            setParameters();
            setBufferSize();
            mCamera.addCallbackBuffer(mBuffer);
            mCamera.startPreview();
            if (mCameraPreviewListener != null) {
                mCameraPreviewListener.onCameraLoadFinished();
            }
            mPreviewRunning = true;
            focus();
        }
    }

    private void setBufferSize() {
        Size frameSize = mCamera.getParameters().getPreviewSize();
        int bufferSize;
        bufferSize = frameSize.width * frameSize.height
                * ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8;

        mBuffer = new byte[bufferSize];
    }

    public void pauseCamera() {
        if (mPreviewRunning) {
            mCamera.stopPreview();
            mPreviewRunning = false;
        }
    }

    public void stopCamera() {
        if (mCamera != null ) {
            if(mSurfaceView!=null && mSurfaceView.getHolder()!=null) {
                mSurfaceView.getHolder().removeCallback(this);
                mSurfaceView = null;
            }
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setAutoFocusMoveCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mBuffer = null;
            mPreviewRunning = false;
        }
    }

    public void setParameters() {
        Parameters parameters = mCamera.getParameters();
        Size size = mCamera.new Size(mPreviewWidth, mPreviewHeight);

        adjustCameraParameters(parameters, size);

        mCamera.setParameters(parameters);
        if (mCamera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_OFF)) {
            // Scene mode is sometimes incompatible with flash modes. 
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            parameters.setPreviewFpsRange(30000, 30000);
            parameters.setPreviewFrameRate(30);
            parameters.setFlashMode(getFlashMode(parameters));
            mCamera.setParameters(parameters);
        }
    }

    public void adjustCameraParameters(Camera.Parameters parameters, Size size) {
        if (parameters == null) return;

        // set preview frame size
        List<Size> sizes = parameters.getSupportedPreviewSizes();

        if (sizes.contains(size)) {
            Log.i(TAG, "Camera preview resolution: (" + size.width + ","
                    + size.height + ") is supported");
            parameters.setPreviewSize(size.width, size.height);
        } else {
            Log.e(TAG, "Camera preview resolution: (" + size.width + ","
                    + size.height + ") is NOT supported!");
        }

        // set focus mode MACRO for close objects, set to AUTO if not available
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
            Log.i(TAG, "Focus mode set to: "
                    + Camera.Parameters.FOCUS_MODE_MACRO);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        } else {
            Log.i(TAG, "Focus mode set to: "
                    + Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        // set to max frames-per-second
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
//        Log.e(TAG, "Camera SCENE modes: (" +parameters.getSupportedSceneModes()+")");
        parameters.setPreviewFpsRange(30000, 30000);
        parameters.setPreviewFrameRate(30);
        parameters.setFlashMode(getFlashMode(parameters));
////        Log.e("Camera Preview"," chroma-flash-values "+parameters.get("chroma-flash-values")); // TODO:MOTOZ Camera Setup Zoom important
////        parameters.set("luma-adaptation", 3) ;
//       parameters.set("zoom",7);
//        parameters.set("whitebalance","auto");
//        parameters.set("flash-mode", "torch");
//        Log.e("CameraPreview"," chroma-flash "+parameters.get("chroma-flash")+ " whitebalance "+parameters.get("whitebalance") +"  flash-mode "+parameters.get("flash-mode"));

    }

    public String getFlashMode(Camera.Parameters parameters) {
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        Log.e(TAG, "Camera flash modes: (" + Arrays.toString(supportedFlashModes.toArray()) + ")");
        if (supportedFlashModes != null) {
            if (supportedFlashModes.contains(Parameters.FLASH_MODE_TORCH)) {
                return Parameters.FLASH_MODE_TORCH;
            } else if (supportedFlashModes.contains(Parameters.FLASH_MODE_ON)) {
                return Parameters.FLASH_MODE_ON;
            } else {
                Log.e(TAG, "Camera flash not supported: (" + Arrays.toString(supportedFlashModes.toArray()) + ")");
            }
        } else {
            Log.e(TAG, "Camera flash not supported: (No Flash Modes Available)");
        }
        return Parameters.FLASH_MODE_OFF;
    }

    public boolean isFlashActive() {
        Log.e(TAG, "Flash Mode: " + mCamera.getParameters().getFlashMode());
        switch (mCamera.getParameters().getFlashMode()) {
            case Camera.Parameters.FLASH_MODE_OFF : return false;
            case Camera.Parameters.FLASH_MODE_ON : return true;
            case Camera.Parameters.FLASH_MODE_AUTO : return true;
            case Parameters.FLASH_MODE_TORCH : return true;
        }
        return false;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    	
        if (mCameraPreviewListener != null) {
            mCameraPreviewListener.onFrameReceived(data);
        }
        camera.addCallbackBuffer(mBuffer);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        Log.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.d(TAG, "surfaceDestroyed");
    }

    public void focus() {

        if (mPreviewRunning == false)
            return;

        Log.i(TAG, "Focusing ");
        mCamera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                mCameraPreviewListener.onAutoFocusFinished(success);
                Log.i(TAG, "AutoFocus" + success);
            }
        });
    }

    @Override
    public void onAutoFocusMoving(boolean start, Camera camera) {
        Log.i(TAG, "Focusing " + (start ? " Starting " : " Finishing"));
    }
    
    public interface CameraPreviewListener { 
        public void onFrameReceived(byte[] data);
        public void onCameraLoadFinished();
        public void onAutoFocusFinished(boolean success);
    }
    
    public void setCameraPreviewListener(CameraPreviewListener listener) {
        mCameraPreviewListener = listener;
    }

	public void setSurface(SurfaceView s) {
        if (mSurfaceView != null && mSurfaceView.getHolder() != null) {
            mSurfaceView.getHolder().removeCallback(this);
            mSurfaceView = null;
        }

		mSurfaceView = s;		
	}
}

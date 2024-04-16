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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.TypeFaceProvider;
import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.imageprocessing.NetrometerImageProcessing;
import com.vitorpamplona.netrometer.imageprocessing.listeners.CenterFinderListener;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.utils.Point2D;

import static com.vitorpamplona.netrometer.activity.fragments.AbstractCalibratingFragment.LENS_TYPE.SINGLE_VISION;

public abstract class AbstractCalibratingFragment extends AbstractNetrometerFragment implements CenterFinderListener{

    private TextView mInstructions1, mInstructions2;
    private ImageView mSuccess, mError, mNetrometerImage;
    private Button mStopCalibration;
    public static enum LENS_TYPE {SINGLE_VISION, BIFOCALS, PROGRESSIVES};
    private LENS_TYPE lensType;

    public AbstractCalibratingFragment() {
        this.lensType = SINGLE_VISION;
    }

    public AbstractCalibratingFragment setLensType(LENS_TYPE lensType1) {
        this.lensType =  lensType1;
        return this;
    }

    public abstract int getLayout();
    public abstract boolean isSmartStage();
    public abstract void decideNextFragment();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);

        mInstructions1 = (TextView) view.findViewById(R.id.instructions_1);
        mInstructions2 = (TextView) view.findViewById(R.id.instructions_2);

        mSuccess = (ImageView) view.findViewById(R.id.sucess_image);
        mError = (ImageView) view.findViewById(R.id.error_image);
        mNetrometerImage = (ImageView) view.findViewById(R.id.netrometer_image);

        mStopCalibration = (Button) view.findViewById(R.id.stop_calibrating);
        mStopCalibration.setEnabled(false);

        mInstructions2.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));

        mStopCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStopCalibrating();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getNetActivity().hidePrinterButton();
        getNetActivity().hideNewCustomReadingButton();
        getNetActivity().hideCameraPreviewButton();
        getNetActivity().animateToHideCamera();
        getNetActivity().enableToolbarView();

        new InitializeImgProc(getNetActivity()).execute();
        getNetActivity().enableProgress();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getImageProcessor()!=null) {
            getImageProcessor().registerCenterFinderListener(null);
        }
    }

    public LENS_TYPE getLensType() {
        return lensType;
    }

    private void onStopCalibrating() {
        if (getImageProcessor() != null)
            getImageProcessor().registerCenterFinderListener(null);

        if (getNetActivity() != null) {
            getNetActivity().disableProgress();
            getNetActivity().finishImageProcessing();
            getNetActivity().loadStartFragment();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mStopCalibration.isEnabled())
            onStopCalibrating();
        return true;
    }

    private boolean mExecuting = false;

    @Override
    public void drawDebug(GridResult result, GridResult zero) {

    }

    @Override
    public void onCenterFound(Point2D center, boolean isLightPipeInView, Refraction realtime) {
        if (!mExecuting) {
            mExecuting = true;
            new GenerateZeroTask().execute();
        }
    }

    private class InitializeImgProc extends AsyncTask<Void, Void, Boolean> {
        NetrometerActivity activity;

        public InitializeImgProc(NetrometerActivity act) {
            this.activity = act;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (getNetActivity() != null)
                return (getNetActivity().initializeImageProcessing(lensType, isSmartStage()));
            return false;
        }

        @Override
        protected void onPostExecute(Boolean sucessful) {
            if (mStopCalibration != null)
                mStopCalibration.setEnabled(true);

            if (sucessful != null && sucessful) {
                if (getImageProcessor() != null)
                    getImageProcessor().registerCenterFinderListener(AbstractCalibratingFragment.this);
            } else {
                onStopCalibrating();
                if (getNetActivity() != null) getNetActivity().cameraBusyAlert();
            }
        }
    }

    private class GenerateZeroTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            if (getImageProcessor() == null) return null;

            return getImageProcessor().generateZeroValues();
        }

        @Override
        protected void onPostExecute(Integer errorCode) {
            if (getNetActivity() != null) getNetActivity().disableProgress();

            if (errorCode!= null && errorCode.intValue() == NetrometerImageProcessing.ALL_SET) {
                //mNetrometerImage.setAlpha(0.3f);
                //mSuccess.setVisibility(View.VISIBLE);
                //mInstructions1.setText(R.string.we_are_all_set_title);
                //mInstructions1.setTextColor(getResources().getColor(R.color.success));
                //mInstructions2.setText(R.string.we_are_all_set_desc);
                //try {
                //    Thread.sleep(1000, 0);
                //} catch (InterruptedException e) {
                //    e.printStackTrace();
                //}
                decideNextFragment();
            } else {

                if (getNetActivity() != null && !getNetActivity().isFlashActive()) {
                    mNetrometerImage.setAlpha(0.3f);
                    mError.setVisibility(View.VISIBLE);
                    mInstructions1.setTextColor(getResources().getColor(R.color.error));
                    mInstructions1.setText(R.string.flash_does_not_start_title);
                    mInstructions2.setText(R.string.flash_does_not_start_desc);
                    mStopCalibration.setText(R.string.restart_uppercase);

                    if (getImageProcessor() != null)
                        getImageProcessor().registerCenterFinderListener(null);

                    if (getNetActivity() != null)
                        getNetActivity().finishImageProcessing();
                } else if (errorCode!= null && errorCode.intValue() == NetrometerImageProcessing.LAST_FRAME_IS_ZERO) {
                    mNetrometerImage.setAlpha(0.3f);
                    mError.setVisibility(View.VISIBLE);
                    mInstructions1.setTextColor(getResources().getColor(R.color.error));
                    mInstructions1.setText(R.string.unable_to_find_pattern_title);
                    mInstructions2.setText(R.string.unable_to_find_pattern_desc);
                    mStopCalibration.setText(R.string.restart_uppercase);

                    if (getImageProcessor() != null)
                        getImageProcessor().registerCenterFinderListener(null);

                    if (getNetActivity() != null)
                        getNetActivity().finishImageProcessing();
                } else if (errorCode!= null && errorCode.intValue() == NetrometerImageProcessing.CENTER_IS_NOT_VALID) {
                    mNetrometerImage.setAlpha(0.3f);
                    mError.setVisibility(View.VISIBLE);
                    mInstructions1.setTextColor(getResources().getColor(R.color.error));
                    mInstructions1.setText(R.string.unable_to_find_pattern_title);
                    mInstructions2.setText(R.string.unable_to_find_pattern_desc);
                    mStopCalibration.setText(R.string.restart_uppercase);

                    if (getImageProcessor() != null)
                        getImageProcessor().registerCenterFinderListener(null);

                    if (getNetActivity() != null)
                        getNetActivity().finishImageProcessing();
                } else if (errorCode!= null && errorCode.intValue() == NetrometerImageProcessing.HAS_LENS) {
                    mNetrometerImage.setAlpha(0.3f);
                    mError.setVisibility(View.VISIBLE);
                    mInstructions1.setTextColor(getResources().getColor(R.color.error));
                    mInstructions1.setText(R.string.remove_lens_title);
                    mInstructions2.setText(R.string.remove_lens_desc);
                    mStopCalibration.setText(R.string.restart_uppercase);
                    if (getImageProcessor() != null)
                        getImageProcessor().registerCenterFinderListener(null);

                    if (getNetActivity() != null)
                        getNetActivity().finishImageProcessing();
                }

            }




        }
    }
}

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
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.TypeFaceProvider;
import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.activity.views.CrosshairView;
import com.vitorpamplona.netrometer.activity.views.SingleClickListener;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.imageprocessing.model.RefractionStats;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Point2D;

public class MeasureSingleVisionFragment extends AbstractMeasuringFragment implements Animation.AnimationListener{

    private static final int GLASSES_INITIAL_X_POSITION = 0;
    private static final int GLASSES_FINAL_X_POSITION = -820;
    private static final int GLASSES_INITIAL_Y_POSITION = 180;
    private static final int GLASSES_FINAL_Y_POSITION = -180;

    private static final int GLASSES_ANIM_DURATION = 2700;

    private CrosshairView mCrosshairView;
    private ImageView mGlasses;
    private TextView mInstructions1, mInstructions2, mWaitingGlasses;

    protected Button mNextButton;

    private boolean mIsRightLenses = false;

    private TextView mRealTimeSph, mRealTimeCyl, mRealTimeAxis;
    private RefractionStats formatter = new RefractionStats(15);

    public MeasureSingleVisionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singlevision_measure, container, false);

        mGlasses = (ImageView) view.findViewById(R.id.glasses);
        mCrosshairView = (CrosshairView) view.findViewById(R.id.crosshair_view);
        mInstructions1 = (TextView) view.findViewById(R.id.instructions_1);

        mInstructions2 = (TextView) view.findViewById(R.id.instructions_2);
        mNextButton = (Button) view.findViewById(R.id.next_button);
        mWaitingGlasses = (TextView) view.findViewById(R.id.warning_no_glasses);

        mRealTimeSph = (TextView) view.findViewById(R.id.realtime_results_sph);
        mRealTimeCyl =(TextView) view.findViewById(R.id.realtime_results_cyl);
        mRealTimeAxis = (TextView) view.findViewById(R.id.realtime_results_axis);

        //mInstructions1.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        mInstructions2.setTypeface(TypeFaceProvider.getTypeFace(getActivity(),TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        mWaitingGlasses.setTypeface(TypeFaceProvider.getTypeFace(getActivity(),TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));

        //mNextButton.setTypeface(TypeFaceProvider.getTypeFace(getActivity(),TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT), Typeface.BOLD);

        mCrosshairView.setGuideCenterPosition(Params.PREVIEW_DISPLAY_HEIGHT/2, getResources().getInteger(R.integer.crosshair_view_center));
        mCrosshairView.setValidZoneRadius(54);
        mCrosshairView.setMaxRadius(300);

        mNextButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSafeClick(View v) {
                onNextPressed();
            }
        });

        return view;
    }

    public void onResume() {
        super.onResume();
        getNetActivity().showCameraPreviewButton();
        if (getSettings().isCameraPreviewActive()) {
            getNetActivity().animateToShowCamera();
        } else {
            getNetActivity().animateToHideCamera();
        }
    }

    @Override
    public void onCenterFound(Point2D center, boolean isLightPipeInView, Refraction realtime) {
        if(realtime == null) {
            mInstructions2.setVisibility(View.GONE);
            mRealTimeSph.setVisibility(View.VISIBLE);
            mRealTimeCyl.setVisibility(View.VISIBLE);
            mRealTimeAxis.setVisibility(View.VISIBLE);
            mRealTimeSph.setText("--.--");
            mRealTimeCyl.setText("--.--");
            mRealTimeAxis.setText("---");
        }

        if (center != null) {
            mCrosshairView.setVisibility(View.VISIBLE);
            mCrosshairView.setCenterGuide(center, true);
            mWaitingGlasses.setVisibility(View.GONE);

            if (realtime != null) {
                mInstructions2.setVisibility(View.GONE);
                mRealTimeSph.setVisibility(View.VISIBLE);
                mRealTimeCyl.setVisibility(View.VISIBLE);
                mRealTimeAxis.setVisibility(View.VISIBLE);

                formatter.add(realtime);

                mRealTimeSph.setText(String.format("%2.2f", formatter.getSph()));
                mRealTimeCyl.setText(String.format("%2.2f", formatter.getCyl()));
                mRealTimeAxis.setText(String.format("%3.0f", formatter.getAxis()));
            }
        } else {
            mCrosshairView.setGuideVectorNull();
            mWaitingGlasses.setVisibility(View.VISIBLE);
            mCrosshairView.setVisibility(View.GONE);
        }

        if (!getSettings().isCameraPreviewActive()) {
            mInstructions2.setText(R.string.hold_trigger_tap_next);
            mInstructions2.setVisibility(View.VISIBLE);
            mRealTimeSph.setVisibility(View.GONE);
            mRealTimeCyl.setVisibility(View.GONE);
            mRealTimeAxis.setVisibility(View.GONE);
        }
    }

    public void resetTexts() {
        mInstructions2.setText(R.string.hold_trigger_tap_next);
    }

    public void onNextPressed() {
        if (mCrosshairView.isInsideValidZone()) {
            new FindGridTask().execute(mIsRightLenses);
        } else {
            Toast.makeText(getActivity(), R.string.message_adjust_glass_position, Toast.LENGTH_SHORT).show();
        }
    }

    private class FindGridTask extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            enableDisplayingProgress();
            mNextButton.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            boolean b = (getImageProcessor() != null) ? getImageProcessor().runGridFinder(params[0]) : false;
            return b;
        }

        @Override
        protected void onPostExecute(Boolean found) {
            disableDisplayingProgress();

            if (found) {
                if (!mIsRightLenses) {
                    mIsRightLenses = true;
                    mInstructions1.setText(R.string.load_right);

                    switchToRightLenses();
                } else {
                    done();
                    if (getActivity() != null)
                        ((NetrometerActivity) getActivity()).loadResultNoSmartStageFragment(true, null);
                }
            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.message_cant_find_pattern, Toast.LENGTH_SHORT).show();
                mNextButton.setEnabled(true);
            }
        }
    }

    public void setShowInstructions(boolean visible) {
        mCrosshairView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mInstructions1.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mInstructions2.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mRealTimeSph.setVisibility(View.GONE);
        mRealTimeCyl.setVisibility(View.GONE);
        mRealTimeAxis.setVisibility(View.GONE);
        formatter.clear();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        setShowInstructions(false);
        if (getImageProcessor() != null) getImageProcessor().setEnabled(false);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        setShowInstructions(true);
        if (getImageProcessor() != null) getImageProcessor().setEnabled(true);
        mNextButton.setEnabled(true);
    }

    @Override
    public void onAnimationRepeat(Animation animation) { }

    private void switchToRightLenses() {
        AnimationSet as = new AnimationSet(true);
        as.setInterpolator(new AnticipateOvershootInterpolator(1));
        as.setFillAfter(true);

        Animation a1 = new TranslateAnimation(0, 0, GLASSES_INITIAL_Y_POSITION, GLASSES_FINAL_Y_POSITION);
        a1.setDuration(GLASSES_ANIM_DURATION/3);
        as.addAnimation(a1);

        Animation a2 = new TranslateAnimation(GLASSES_INITIAL_X_POSITION, GLASSES_FINAL_X_POSITION,0, 0);
        a2.setDuration(GLASSES_ANIM_DURATION/3);
        a2.setStartOffset(GLASSES_ANIM_DURATION/3);
        as.addAnimation(a2);

        Animation a3 = new TranslateAnimation(0, 0, GLASSES_FINAL_Y_POSITION, GLASSES_INITIAL_Y_POSITION);
        a3.setDuration(GLASSES_ANIM_DURATION/3);
        a3.setStartOffset(2 * GLASSES_ANIM_DURATION/3);
        as.addAnimation(a3);

        as.setAnimationListener(this);

        mGlasses.startAnimation(as);
    }


}

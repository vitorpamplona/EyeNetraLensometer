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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.TypeFaceProvider;
import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.activity.views.CrosshairView;
import com.vitorpamplona.netrometer.activity.views.SingleClickListener;
import com.vitorpamplona.netrometer.imageprocessing.ProgressiveImageProcessing;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.imageprocessing.model.RefractionStats;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Point2D;

public class MeasureProgressiveFragment extends AbstractMeasuringFragment implements Animation.AnimationListener{

    private static final int GLASSES_Y_DISTANCE_POSITION = 0;
    private static final int GLASSES_Y_READING_POSITION = -180;
    private static final int GLASSES_Y_OUTSIDE_POSITION = -230;

    private static final int GLASSES_X_LEFT_LENS_POSITION = -180;
    private static final int GLASSES_X_RIGHT_LENS_POSITION = -980;

    private static final int GLASSES_ANIM_DURATION = 2700;

    private CrosshairView mCrosshairView;
    private ImageView mGlasses;
    private TextView mInstructions1, mInstructions2, mWaitingGlasses,mRemoveGlasses;
    private Button mNextButton;
    private ImageButton mSkipNearButton;

    private TextView mRealTimeSph, mRealTimeCyl, mRealTimeAxis;
    private RefractionStats formatter = new RefractionStats(15);


    private State mState = State.LEFT_DISTANCE;

    private enum State {
        LEFT_DISTANCE(R.string.load_left_distance),
        LEFT_NEAR(R.string.load_left_near),
        RIGHT_DISTANCE(R.string.load_right_distance),
        RIGHT_NEAR(R.string.load_right_near),
        SKIP_NEAR_RIGHT(R.string.load_skip_near),
        SKIP_NEAR_LEFT(R.string.load_skip_near);

        int stringId;

        State(int stringId) {
            this.stringId = stringId;
        }

        private int getStringId() { return this.stringId; }

        private boolean isRightLens() {
            return this.equals(RIGHT_DISTANCE) || this.equals(RIGHT_NEAR) ||  this.equals(SKIP_NEAR_RIGHT);
        }

        private boolean isDistance() {
            return this.equals(LEFT_DISTANCE) || this.equals(RIGHT_DISTANCE);
        }

        private boolean isSkipNear() {
            return this.equals(SKIP_NEAR_LEFT) || this.equals(SKIP_NEAR_RIGHT);
        }
    }

    public MeasureProgressiveFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progressive_measure, container, false);

        mGlasses = (ImageView) view.findViewById(R.id.glasses);
        mCrosshairView = (CrosshairView) view.findViewById(R.id.crosshair_view);
        mInstructions1 = (TextView) view.findViewById(R.id.instructions_1);

        mInstructions2 = (TextView) view.findViewById(R.id.instructions_2);
        mNextButton = (Button) view.findViewById(R.id.next_button);
        mSkipNearButton = (ImageButton) view.findViewById(R.id.skip_near_button);

        mWaitingGlasses = (TextView) view.findViewById(R.id.warning_no_glasses);
        mRemoveGlasses = (TextView) view.findViewById(R.id.remove_glasses);
        mRemoveGlasses.setTypeface(TypeFaceProvider.getTypeFace(getActivity(),TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        //mInstructions1.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        mInstructions2.setTypeface(TypeFaceProvider.getTypeFace(getActivity(),TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        mWaitingGlasses.setTypeface(TypeFaceProvider.getTypeFace(getActivity(),TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        //mSkipNearButton.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));

        //mNextButton.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT), Typeface.BOLD);

        mRealTimeSph = (TextView) view.findViewById(R.id.realtime_results_sph);
        mRealTimeCyl =(TextView) view.findViewById(R.id.realtime_results_cyl);
        mRealTimeAxis = (TextView) view.findViewById(R.id.realtime_results_axis);


        mCrosshairView.setGuideCenterPosition(Params.PREVIEW_DISPLAY_HEIGHT / 2, getResources().getInteger(R.integer.crosshair_view_center));
        mCrosshairView.setValidZoneRadius(54);
        mCrosshairView.setMaxRadius(300);


        mNextButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSafeClick(View v) {
                onNextPressed();
            }
        });
        mSkipNearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSkipNearPressed();
            }
        });
        mSkipNearButton.setVisibility(View.INVISIBLE);
        setState(State.LEFT_DISTANCE);
        mRemoveGlasses.setVisibility(View.GONE);
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

    public void onNextPressed() {
        if (mCrosshairView.isInsideValidZone()) {
            new FindGridTask().execute(mState);
        } else {
            if (getActivity() != null) Toast.makeText(getActivity(), R.string.message_adjust_glass_position, Toast.LENGTH_SHORT).show();
        }
    }

    public void onSkipNearPressed() {

        if (!mState.isRightLens()){
            mState = State.SKIP_NEAR_LEFT;
            setState(State.SKIP_NEAR_LEFT);
        } else {
            mState = State.SKIP_NEAR_RIGHT;
            setState(State.SKIP_NEAR_RIGHT);
        }

        new FindGridTask().execute(mState);

    }

    private class FindGridTask extends AsyncTask<State, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            enableDisplayingProgress();
            mNextButton.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(State... params) {
            if( params.length > 0 && getImageProcessor()!=null)
                return getImageProcessor().runGridFinder(params[0].isRightLens());
            else return null;
        }

        @Override
        protected void onPostExecute(Boolean found) {
            disableDisplayingProgress();

            if (found!=null & found) {
                if (mState == State.LEFT_DISTANCE) {
                    setState(State.LEFT_NEAR);
                    switchReadingLeft();
                    mSkipNearButton.setVisibility(View.VISIBLE);
                } else if (mState == State.LEFT_NEAR || mState == State.SKIP_NEAR_LEFT) {
                    setState(State.RIGHT_DISTANCE);
                    switchToRightLenses();
                    mSkipNearButton.setVisibility(View.GONE);
                } else if (mState == State.RIGHT_DISTANCE) {
                    setState(State.RIGHT_NEAR);
                    switchReadingRight();
                    mSkipNearButton.setVisibility(View.VISIBLE);
                } else if (mState ==State.RIGHT_NEAR || mState == State.SKIP_NEAR_RIGHT){
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

    private void setState(State state) {
        mState = state;
        mInstructions1.setText(mState.getStringId());
        if (getImageProcessor() != null) {
            ((ProgressiveImageProcessing) getImageProcessor()).setIsDistance(mState.isDistance());
            ((ProgressiveImageProcessing) getImageProcessor()).setIsSkipNear(mState.isSkipNear());
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

    private void switchReadingRight() {
        // WHY DO I NEED TO PUT GLASSES_X_RIGHT_LENS_POSITION+180 HERE??? Vitor is confused.
        Animation a1 = new TranslateAnimation(GLASSES_X_RIGHT_LENS_POSITION+180, GLASSES_X_RIGHT_LENS_POSITION+180, GLASSES_Y_DISTANCE_POSITION, GLASSES_Y_READING_POSITION);
        a1.setDuration(GLASSES_ANIM_DURATION / 5);
        a1.setFillAfter(true);
        a1.setAnimationListener(this);

        mGlasses.startAnimation(a1);
    }

    private void switchReadingLeft() {
        Animation a1 = new TranslateAnimation(0, 0, GLASSES_Y_DISTANCE_POSITION, GLASSES_Y_READING_POSITION);
        a1.setDuration(GLASSES_ANIM_DURATION / 5);
        a1.setFillAfter(true);
        a1.setAnimationListener(this);

        mGlasses.startAnimation(a1);

    }

    private void switchToRightLenses() {
        AnimationSet as = new AnimationSet(true);
        as.setInterpolator(new AnticipateOvershootInterpolator(1));
        as.setFillAfter(true);

        // WHY DO I NEED TO PUT 180 HERE??? Vitor is confused.
        Animation a1 = new TranslateAnimation(180, 180, 50, GLASSES_Y_OUTSIDE_POSITION);
        a1.setDuration(GLASSES_ANIM_DURATION/3);
        as.addAnimation(a1);

        Animation a2 = new TranslateAnimation(GLASSES_X_LEFT_LENS_POSITION, GLASSES_X_RIGHT_LENS_POSITION, 0, 0);
        a2.setDuration(GLASSES_ANIM_DURATION/3);
        a2.setStartOffset(GLASSES_ANIM_DURATION / 3);
        as.addAnimation(a2);

        Animation a3 = new TranslateAnimation(0, 0, GLASSES_Y_OUTSIDE_POSITION, 200);
        a3.setDuration(GLASSES_ANIM_DURATION/3);
        a3.setStartOffset(2 * GLASSES_ANIM_DURATION/3);
        as.addAnimation(a3);

        as.setAnimationListener(this);

        mGlasses.startAnimation(as);
    }

}

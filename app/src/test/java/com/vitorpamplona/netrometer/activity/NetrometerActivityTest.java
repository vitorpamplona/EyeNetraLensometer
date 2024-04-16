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
package com.vitorpamplona.netrometer.activity;

import android.app.Fragment;
import android.view.View;

import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.activity.fragments.AbstractCalibratingFragment;
import com.vitorpamplona.netrometer.activity.fragments.MeasureSingleVisionFragment;
import com.vitorpamplona.netrometer.activity.fragments.ResultsSmartStageFragment;
import com.vitorpamplona.netrometer.activity.fragments.StartNoStageFragment;
import com.vitorpamplona.netrometer.model.db.objects.DebugExam;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=22)
public class NetrometerActivityTest {

    private NetrometerActivity activityAfterOnCreate;

    @Before
    public void setUp() {
        ActivityController<NetrometerActivity> controller = buildActivity(NetrometerActivity.class);
        activityAfterOnCreate = controller.get();
        controller.create().resume();
        AbstractCalibratingFragment.LENS_TYPE lensType = AbstractCalibratingFragment.LENS_TYPE.SINGLE_VISION;
        activityAfterOnCreate.initializeImageProcessing(lensType, false);
    }

    @Test
    public void hasFragmentView() {
        assertNotNull(activityAfterOnCreate.findViewById(R.id.fragment_view));
    }

    @Test
    public void fragmentIsVisible() {
        int visibility = activityAfterOnCreate.findViewById(R.id.fragment_view).getVisibility();
        assertEquals(View.VISIBLE, visibility);
    }

    @Test
    public void firstFragmentIsStartFragment() {
        Fragment fragment = activityAfterOnCreate.getFragmentManager().findFragmentById(R.id.fragment_view);

        assertEquals(StartNoStageFragment.class, fragment.getClass());
    }

    @Test
    @Ignore("Aparently the the fragment transaction doesn't work on testing anymore")
    public void loadResultFragment() {
        activityAfterOnCreate.loadResultSmartStageFragment(false,new DebugExam());
        Fragment fragment = activityAfterOnCreate.getFragmentManager().findFragmentById(R.id.fragment_view);
        assertEquals(ResultsSmartStageFragment.class, fragment.getClass());
    }

    @Test
    @Ignore("Aparently the the fragment transaction doesn't work on testing anymore")
    public void loadMeasureFragment() {
        activityAfterOnCreate.loadMeasureSingleVisionFragment();
        Fragment fragment = activityAfterOnCreate.getFragmentManager().findFragmentById(R.id.fragment_view);
        assertEquals(MeasureSingleVisionFragment.class, fragment.getClass());
    }

    @Test
    public void imageProcessorIsNotNull() {
        assertNotNull(activityAfterOnCreate.getImageProcessor());
    }

    @Test
    public void cameraPreviewIsNotNull() {
        assertNotNull(activityAfterOnCreate.mCameraPreview);
    }


    @Test
    @Ignore("Aparently the the fragment transaction doesn't work on testing anymore")
    public void stateIsRunningAfterStartProcessing() {
        assertEquals(NetrometerActivity.ProcessingState.STARTED, activityAfterOnCreate.mState);
        if (activityAfterOnCreate.startProcessing())
            assertEquals(NetrometerActivity.ProcessingState.RUNNING, activityAfterOnCreate.mState);
        else
            assertEquals(NetrometerActivity.ProcessingState.STARTED, activityAfterOnCreate.mState);
    }

    @Test
    public void stateIsPausedAfterStopProcessing() {
        activityAfterOnCreate.stopProcessing();
        assertEquals(NetrometerActivity.ProcessingState.PAUSED, activityAfterOnCreate.mState);
    }
}

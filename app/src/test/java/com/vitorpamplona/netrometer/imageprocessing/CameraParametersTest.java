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


import android.hardware.Camera;

import com.vitorpamplona.netrometer.BuildConfig;

import com.vitorpamplona.netrometer.activity.CustomRobolectricGradleTestRunner;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test the Calibration of the System
 * NGVG020
 */

@RunWith(CustomRobolectricGradleTestRunner.class)
@Config(manifest=Config.NONE, sdk=21)
public class CameraParametersTest {

    Camera.Parameters parameters;
    Camera.Size previewSize;

    public CameraParametersTest() {

    }

    @Before
    public void setUp() throws Exception {
        // Breaks security to instanciate a new hardware-defined Camera Params.
        Constructor<Camera.Parameters> constructor = Camera.Parameters.class.getDeclaredConstructor(new Class[0]);
        constructor.setAccessible(true);
        parameters = constructor.newInstance(new Object[0]);

        Class<?> enclosingClass = Class.forName("android.hardware.Camera");
        Object enclosingInstance = enclosingClass.newInstance();

        Constructor<Camera.Size> constructor2 = Camera.Size.class.getDeclaredConstructor(enclosingClass, Integer.TYPE, Integer.TYPE);
        constructor2.setAccessible(true);
        previewSize = constructor2.newInstance(enclosingInstance, 640, 480);

        // fixing null pointer exceptions by creating the internal nMap.
        Map<String, String> mMap = new LinkedHashMap<String, String>(/*initialCapacity*/64);
        Field field = Camera.Parameters.class.getDeclaredField("mMap");
        field.setAccessible(true);
        field.set(parameters, mMap);

        // loading default params.
        parameters.unflatten("preview-format-values=yuv420sp,yuv420p,yuv422i-yuyv,yuv420p;" +
                "preview-format=yuv420sp;" +
                "preview-size-values=640x480;preview-size=640x480;" +
                "picture-size-values=320x240;picture-size=320x240;" +
                "jpeg-thumbnail-size-values=320x240,0x0;jpeg-thumbnail-width=320;jpeg-thumbnail-height=240;" +
                "jpeg-thumbnail-quality=60;jpeg-quality=95;" +
                "preview-frame-rate-values=30,15;preview-frame-rate=30;" +
                "focus-mode-values=continuous-video,auto,macro,infinity,continuous-picture;focus-mode=auto;" +
                "preview-fps-range-values=(15000,30000);preview-fps-range=15000,30000;" +
                "scene-mode-values=auto,action,night;scene-mode=auto;" +
                "flash-mode-values=off,on,auto,torch;flash-mode=off;" +
                "whitebalance-values=auto,daylight,fluorescent,incandescent;whitebalance=auto;" +
                "effect-values=none,mono,sepia;effect=none;" +
                "zoom-supported=true;zoom-ratios=100,200,400;max-zoom=2;" +
                "picture-format-values=jpeg;picture-format=jpeg;" +
                "min-exposure-compensation=-30;max-exposure-compensation=30;" +
                "exposure-compensation=0;exposure-compensation-step=0.1;" +
                "horizontal-view-angle=40;vertical-view-angle=40;"); // camera supports continuous mode
    }

    @Test
    @Ignore("Test not working anymore")
    public void testHasContinuousModeVideo() {
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        assertEquals(Camera.Parameters.FOCUS_MODE_MACRO, parameters.getFocusMode());
    }



    @Test
    @Ignore("Test not working anymore")
    public void testSetParameters()  {
        CameraPreview stack = new CameraPreview(RuntimeEnvironment.application, 640, 480);
        stack.adjustCameraParameters(parameters, previewSize);

        assertEquals(30, parameters.getPreviewFrameRate());
        assertEquals(Camera.Parameters.SCENE_MODE_ACTION, parameters.getSceneMode());
        assertEquals(Camera.Parameters.FLASH_MODE_OFF, parameters.getFlashMode());
        int[] range = new int[2];
        parameters.getPreviewFpsRange(range);
        assertEquals(30000, range[0]);
        assertEquals(30000, range[1]);
        assertEquals(480,  parameters.getPreviewSize().height);
        assertEquals(640,  parameters.getPreviewSize().width);
    }




}

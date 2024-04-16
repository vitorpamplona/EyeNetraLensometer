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
package com.vitorpamplona.netrometer.imageprocessing.utils;

import com.vitorpamplona.netrometer.BuildConfig;

import com.vitorpamplona.netrometer.activity.CustomRobolectricGradleTestRunner;
import com.vitorpamplona.netrometer.imageprocessing.model.Point2DGrid;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.ByteArrayReadWrite;
import com.vitorpamplona.netrometer.utils.Point2D;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(CustomRobolectricGradleTestRunner.class)
@Config(sdk=21)
public class FrameToolsTest {

    FrameTools tools = new FrameTools(Params.PREVIEW_FRAME_WIDTH,Params.PREVIEW_FRAME_HEIGHT);

        @Test
        public void gridDataFromByteFileIsNotNull() throws IOException {

            Point2D center = new Point2D(668.7224, 452.72736);
            byte[] frame = ByteArrayReadWrite.readFromTestAssets("frame"+center.x+"_"+center.y+".txt");

            Point2DGrid[][] points = tools.gridDataOld(frame, center, Params.ROW_DOT_COUNT, Params.COLUMN_DOT_COUNT, Params.ROW_CENTER_POINT, Params.COLUMN_CENTER_POINT, ImageProcessingUtil.calculateInitialStep(null));
            assertNotNull(points);
        }

        @Test
        public void gridDataResultHasTheRightSize() throws IOException {

            Point2D center = new Point2D(668.7224, 452.72736);
            byte[] frame = ByteArrayReadWrite.readFromTestAssets("frame"+center.x+"_"+center.y+".txt");

            Point2DGrid[][] points = tools.gridDataOld(frame, center, Params.ROW_DOT_COUNT, Params.COLUMN_DOT_COUNT, Params.ROW_CENTER_POINT, Params.COLUMN_CENTER_POINT, ImageProcessingUtil.calculateInitialStep(null));
            assertEquals(Params.COLUMN_DOT_COUNT, points.length);
            assertEquals(Params.ROW_DOT_COUNT, points[0].length);
        }

        @Test
        public void gridDataResultHasTheRightSiz() throws IOException {

            Point2D center = new Point2D(668.7224, 452.72736);
            byte[] frame = ByteArrayReadWrite.readFromTestAssets("frame"+center.x+"_"+center.y+".txt");

            Point2DGrid[][] points = tools.gridDataOld(frame, center, Params.ROW_DOT_COUNT, Params.COLUMN_DOT_COUNT, Params.ROW_CENTER_POINT, Params.COLUMN_CENTER_POINT,ImageProcessingUtil.calculateInitialStep(null));

            int validPointsCount = 0;
            for (int column =0; column< Params.COLUMN_DOT_COUNT; column++) {
                for (int row = 0; row< Params.ROW_DOT_COUNT; row++ ) {

                    if (points[column][row].isValid) {
                        validPointsCount++;
                    }
                }
            }
            assertEquals(Params.ROW_DOT_COUNT * Params.COLUMN_DOT_COUNT, validPointsCount);
        }

}

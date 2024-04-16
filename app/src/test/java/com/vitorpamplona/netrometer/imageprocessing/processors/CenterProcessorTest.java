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

import com.vitorpamplona.netrometer.BuildConfig;

import com.vitorpamplona.netrometer.activity.CustomRobolectricGradleTestRunner;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.ByteArrayReadWrite;
import com.vitorpamplona.netrometer.utils.Point2D;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(CustomRobolectricGradleTestRunner.class)
@Config(sdk=21)
public class CenterProcessorTest {

    CenterProcessor centerProcessor;

    @Before
    public void setUp() {
        centerProcessor = new CenterProcessor(Params.PREVIEW_FRAME_WIDTH, Params.PREVIEW_FRAME_HEIGHT);
        centerProcessor.setFilter(Params.BLUE_STANDARD_FILTER);
        centerProcessor.setRefineDistance(10);
        centerProcessor.setSearchBox(Params.CENTER_SEARCH_BOX);
    }

    @Test
    public void centerPositionTest() throws IOException {

        Point2D center = new Point2D(655.67, 357.51);
        byte[] frame = ByteArrayReadWrite.readFromTestAssets("NMIM001.txt");
        Point2D centerFound = centerProcessor.run(frame);

        assertEquals(center.x, centerFound.x, 20f);
        assertEquals(center.y, centerFound.y, 20f);

    }


    @Test
    public void centerFinderTestNull() throws IOException {


        byte[] frame = ByteArrayReadWrite.readFromTestAssets("NMIM002.txt");
        Point2D centerFound = centerProcessor.run(frame);

        assertNull(centerFound);

    }

}

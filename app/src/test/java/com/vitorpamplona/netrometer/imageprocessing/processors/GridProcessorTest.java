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
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.ByteArrayReadWrite;
import com.vitorpamplona.netrometer.utils.Clock;
import com.vitorpamplona.netrometer.utils.Point2D;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(CustomRobolectricGradleTestRunner.class)
@Config(sdk=21)
public class GridProcessorTest {
//GridResult run(byte[] data, Point2D centerPosition, Point2D steps)
    GridProcessor gridProcessorOld;
    GridProcessor gridProcessorUnfiltered;
    GridProcessor gridProcessorFiltered;
    @Before
    public void setUp() {
        gridProcessorOld = new GridProcessor(Params.ROW_DOT_COUNT, Params.COLUMN_DOT_COUNT, Params.ROW_CENTER_POINT,Params.COLUMN_CENTER_POINT);
        gridProcessorUnfiltered = new GridProcessor(Params.ROW_DOT_COUNT, Params.COLUMN_DOT_COUNT, Params.ROW_CENTER_POINT,Params.COLUMN_CENTER_POINT);
        gridProcessorFiltered = new GridProcessor(Params.ROW_DOT_COUNT, Params.COLUMN_DOT_COUNT, Params.ROW_CENTER_POINT,Params.COLUMN_CENTER_POINT);

    }

    public ArrayList<Point2D> getAnchors(byte[] frame) {
        CenterProcessor mAnchorProcessor = new CenterProcessor(Params.PREVIEW_FRAME_WIDTH,Params.PREVIEW_FRAME_HEIGHT);
        mAnchorProcessor.setFilter(Params.BLUE_STANDARD_FILTER);
        mAnchorProcessor.setRefineDistance(10);

        ArrayList<Point2D> anchors = new ArrayList<Point2D>();

        mAnchorProcessor.setSearchBox(Params.CENTER_ANCHOR_1);
        anchors.add(mAnchorProcessor.run(frame));

        mAnchorProcessor.setSearchBox(Params.CENTER_ANCHOR_2);
        anchors.add(mAnchorProcessor.run(frame));

        mAnchorProcessor.setSearchBox(Params.CENTER_ANCHOR_3);
        anchors.add(mAnchorProcessor.run(frame));

        mAnchorProcessor.setSearchBox(Params.CENTER_ANCHOR_4);
        anchors.add(mAnchorProcessor.run(frame));

        return anchors;
    }

    @Test
    public void gridFinderTest() throws IOException {
        Clock clock = new Clock("Start");

        Point2D center = new Point2D(655.67, 357.51);
        Point2D stepFound = new Point2D(14.578, 14.609);
        byte[] frame = ByteArrayReadWrite.readFromTestAssets("NMIM001.txt");

        clock.capture("Reading Frame");

        GridResult gridResultOld = gridProcessorOld.runOld(frame, center, new Point2D(Params.DEFAULT_GRID_STEP, Params.DEFAULT_GRID_STEP));
        clock.capture("Algo Grid Result Old");

        GridResult gridResultUnfiltered = gridProcessorUnfiltered.run(frame, center, new Point2D(Params.DEFAULT_GRID_STEP, Params.DEFAULT_GRID_STEP), getAnchors(frame));
        clock.capture("Algo Grid Result Unfiltered");

        GridResult gridResultFiltered = gridProcessorFiltered.runFilteredImage(frame, center, new Point2D(Params.DEFAULT_GRID_STEP, Params.DEFAULT_GRID_STEP), getAnchors(frame));
        clock.capture("Algo Grid Result Filtered");

        System.out.println(clock.toString());
        System.out.println("Grid Old\n" + gridResultOld.toString());
        System.out.println("Grid Unfiltered\n" + gridResultUnfiltered.toString());
        System.out.println("Grid Filtered\n" + gridResultFiltered.toString());

//        assertEquals("Algos are the same", gridResult.toString(), gridResult2.toString());

        int rowTotal = gridResultUnfiltered.pointsOnGrid.length;
        int columnTotal = gridResultUnfiltered.pointsOnGrid[0].length;
        int rowCenter = rowTotal/2;
        int columnCenter = columnTotal/2;

        // Center Point
        assertEquals(center.x, gridResultUnfiltered.pointsOnGrid[rowCenter][columnCenter].x, 2d);
        assertEquals(center.y, gridResultUnfiltered.pointsOnGrid[rowCenter][columnCenter].y, 2d);

        // Samples of Red Dots on the Grid

        int kx = 7;
        int ky = 0;
        if (  (kx+rowCenter) < rowTotal && (kx+rowCenter)>=0  && (ky+columnCenter)< columnTotal && (ky+columnCenter)>=0) {
            assertEquals(center.x + (kx) * stepFound.x, gridResultUnfiltered.pointsOnGrid[rowCenter + kx][columnCenter + ky].x, 2d);
            assertEquals(center.y + (ky) * stepFound.y, gridResultUnfiltered.pointsOnGrid[rowCenter+ kx][columnCenter+ky].y, 2d);
        }

        kx = -7;
        ky = 0;
        if (  (kx+rowCenter) < rowTotal && (kx+rowCenter)>=0  && (ky+columnCenter)< columnTotal && (ky+columnCenter)>=0) {
            assertEquals(center.x + (kx) * stepFound.x, gridResultUnfiltered.pointsOnGrid[rowCenter + kx][columnCenter + ky].x, .5*stepFound.x);
            assertEquals(center.y + (ky) * stepFound.y, gridResultUnfiltered.pointsOnGrid[rowCenter+ kx][columnCenter+ky].y, .5*stepFound.y);
        }

        kx = 0;
        ky = 7;
        if (  (kx+rowCenter) < rowTotal && (kx+rowCenter)>=0  && (ky+columnCenter)< columnTotal && (ky+columnCenter)>=0) {
            assertEquals(center.x + (kx) * stepFound.x, gridResultUnfiltered.pointsOnGrid[rowCenter + kx][columnCenter + ky].x, .5*stepFound.x);
            assertEquals(center.y + (ky) * stepFound.y, gridResultUnfiltered.pointsOnGrid[rowCenter+ kx][columnCenter + ky].y, .5*stepFound.y);
        }

        kx = 0;
        ky = -7;
       if (  (kx+rowCenter) < rowTotal && (kx+rowCenter)>=0  && (ky+columnCenter)< columnTotal && (ky+columnCenter)>=0) {
            assertEquals(center.x + (kx) * stepFound.x, gridResultUnfiltered.pointsOnGrid[rowCenter + kx][columnCenter + ky].x, .5*stepFound.x);
            assertEquals(center.y + (ky) * stepFound.y, gridResultUnfiltered.pointsOnGrid[rowCenter+ kx][columnCenter+ky].y, .5*stepFound.y);
       }


    }

    @Test
    public void gridFinderTestNull() throws IOException {
        Point2D center = null;

        byte[] frame = ByteArrayReadWrite.readFromTestAssets("NMIM001.txt");
        GridResult gridResult = gridProcessorUnfiltered.run(frame, center, new Point2D(Params.DEFAULT_GRID_STEP,Params.DEFAULT_GRID_STEP), getAnchors(frame));

        assertNull(gridResult);
    }

}

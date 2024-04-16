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

import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.model.Point2DGrid;
import com.vitorpamplona.netrometer.imageprocessing.utils.FrameTools;
import com.vitorpamplona.netrometer.imageprocessing.utils.ImageFilter;
import com.vitorpamplona.netrometer.imageprocessing.utils.PreFilteredFrameTools;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Clock;
import com.vitorpamplona.netrometer.utils.Point2D;

import java.util.ArrayList;

public class GridProcessor {

    private int mRowDotCount, mColumnDotCount, mRowCenterPoint, mColumnCenterPoint;

	private FrameTools mTools;
    private PreFilteredFrameTools mFilteredTools;
    int[][] filteredImageBuffer;
    Point2DGrid[][] grid;

	public GridProcessor(int rowDotCount, int columnDotCount, int rowCenterPoint, int columnCenterPoint) {

		mTools          = new FrameTools(Params.PREVIEW_FRAME_WIDTH,Params.PREVIEW_FRAME_HEIGHT);
        mFilteredTools  = new PreFilteredFrameTools();

        mRowDotCount = rowDotCount;
        mColumnDotCount = columnDotCount;
        mRowCenterPoint = rowCenterPoint;
        mColumnCenterPoint = columnCenterPoint;

        grid = new Point2DGrid[rowDotCount][columnDotCount];

        for (int row=0;row<rowDotCount; row++) {
            for (int col=0;col<columnDotCount; col++) {
                grid[row][col] = new Point2DGrid(Double.NaN, Double.NaN);
            }
        }
	}

	public GridResult runOld(byte[] data, Point2D centerPosition, Point2D steps) {

		if (centerPosition == null)
			return null;

        Point2DGrid[][] pointsOnGrid = mTools.gridDataOld(data, centerPosition, mRowDotCount, mColumnDotCount,
                mRowCenterPoint, mColumnCenterPoint, steps);

        //mBuffer.addToBuffer(pointsOnGrid);


		return new GridResult(centerPosition, pointsOnGrid);
	}

    public GridResult run(byte[] data, Point2D centerPosition, Point2D steps, ArrayList<Point2D> anchors) {

        if (centerPosition == null)
            return null;

        mTools.gridData2(data, grid, centerPosition, mRowDotCount, mColumnDotCount,
                mRowCenterPoint, mColumnCenterPoint, steps, anchors);

        //mBuffer.addToBuffer(pointsOnGrid);

        return new GridResult(centerPosition, grid);
    }

    public GridResult runFilteredImage(byte[] data, Point2D centerPosition, Point2D steps, ArrayList<Point2D> anchors) {

        Clock c = new Clock("GridProcessor");

        if (centerPosition == null)
            return null;

        if (filteredImageBuffer == null) {
            filteredImageBuffer = new int[Params.PREVIEW_FRAME_WIDTH][Params.PREVIEW_FRAME_HEIGHT];
        }

        c.capture("Start");

        ImageFilter.YUVtoIntensityWithRedFilter(data, filteredImageBuffer);

        c.capture("Filtered");

        mFilteredTools.gridData2(filteredImageBuffer, grid, centerPosition, mRowDotCount, mColumnDotCount,
                mRowCenterPoint, mColumnCenterPoint, steps, anchors);

        c.capture("Processed");

        //mBuffer.addToBuffer(pointsOnGrid);

        GridResult r = new GridResult(centerPosition, grid);

        c.capture("GridResult");

//        c.write();

        return r;
    }


}

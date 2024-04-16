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

import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.model.Point2DGrid;
import com.vitorpamplona.netrometer.settings.Params;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class ImageQuality {
    

    public static double calculateSNR(byte[] data, GridResult grid){
        //List<Point2D> neighDot = defineNeighbors(0.5f,1f);

        int rowTotal = grid.pointsOnGrid.length;
        int columnTotal = grid.pointsOnGrid[0].length;

        int rowCenter = rowTotal/2;
        int columnCenter = columnTotal/2;

        YuvPixel frame = new YuvPixel(
                Params.PREVIEW_FRAME_WIDTH,
                Params.PREVIEW_FRAME_HEIGHT
        );

        DescriptiveStatistics foregroundStats = new DescriptiveStatistics();
        DescriptiveStatistics backgroundStats = new DescriptiveStatistics();

        float distBetweenDots = Math.abs(
                  grid.pointsOnGrid[rowCenter][columnCenter].x
                - grid.pointsOnGrid[rowCenter-1][columnCenter].x);

        for(int row = 0; row< rowTotal; row++){
            for(int col=0;  col< columnTotal; col++){
                Point2DGrid currentNeighbor = grid.pointsOnGrid[row][col];

                if(!Double.isNaN(currentNeighbor.x)) {
                    foregroundStats.addValue(frame.getY(data, (int) currentNeighbor.x, (int) currentNeighbor.y));
                    backgroundStats.addValue(frame.getY(data, (int) (.5 * distBetweenDots + currentNeighbor.x), (int) currentNeighbor.y));
                }
            }
        }

        double snrValue = Math.abs(foregroundStats.getMean()-backgroundStats.getMean())/backgroundStats.getStandardDeviation();
        return snrValue;
    }
}

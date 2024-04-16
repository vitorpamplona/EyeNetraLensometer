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
package com.vitorpamplona.netrometer.imageprocessing.model;

import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Point2D;

import java.text.DecimalFormat;

public class GridResult extends AbstractResults {

    public Point2DGrid[][] pointsOnGrid;
    public Point2D centerGridPosition;
    public boolean centerIsValid = true;
    public boolean isLightPipeInView = false;

    public GridResult(){}

    public GridResult (	Point2D centerPosition,
                           Point2DGrid[][] pointsOnGrid) {

        this.centerPosition = centerPosition;
        this.pointsOnGrid = pointsOnGrid;
        this.centerGridPosition = new Point2D(Params.ROW_CENTER_POINT, Params.COLUMN_CENTER_POINT);
        this.isLightPipeInView = false;
    }

    public GridResult (	Point2D centerPosition,
                           Point2DGrid[][] pointsOnGrid, Point2D centerGridPosition) {

        this.centerPosition = centerPosition;
        this.pointsOnGrid = pointsOnGrid;
        this.centerGridPosition = centerGridPosition;
        this.isLightPipeInView = false;

    }

    public GridResult (	Point2D centerPosition,
                           Point2DGrid[][] pointsOnGrid, Point2D centerGridPosition, boolean isLightPipeInView) {

        this.centerPosition = centerPosition;
        this.pointsOnGrid = pointsOnGrid;
        this.centerGridPosition = centerGridPosition;
        this.isLightPipeInView = isLightPipeInView;

    }


    public String toString() {
        DecimalFormat formatter = new DecimalFormat("0");
        StringBuilder builder = new StringBuilder();
        for (int x=0; x<pointsOnGrid.length; x++) {
            for (int y=0; y<pointsOnGrid[x].length; y++) {
                if (pointsOnGrid[x][y].isValid)
                    builder.append(formatter.format(pointsOnGrid[x][y].x) +","+ formatter.format(pointsOnGrid[x][y].y) + "\t");
                else
                    builder.append("NotFound" + "\t");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public GridResult clone() {
        GridResult gr = new GridResult();
        gr.centerGridPosition = centerGridPosition.clone();
        gr.centerPosition = centerPosition.clone();
        gr.centerIsValid = centerIsValid;
        gr.pointsOnGrid = new Point2DGrid[pointsOnGrid.length][pointsOnGrid[0].length];
        gr.isLightPipeInView = isLightPipeInView;
        for (int i=0; i<pointsOnGrid.length; i++) {
            for (int j=0; j<pointsOnGrid[0].length; j++) {
                gr.pointsOnGrid[i][j] = pointsOnGrid[i][j].clone();
            }
        }

        return gr;
    }
}

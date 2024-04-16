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


import com.vitorpamplona.netrometer.utils.Point2D;

public class Point2DGrid extends Point2D {

    public boolean isValid;

    public Point2DGrid(float _x, float _y, boolean _isValid) {
        super(_x, _y);
        isValid = _isValid;
    }

    public Point2DGrid(double _x, double _y, boolean _isValid) {
        super(_x, _y);
        isValid = _isValid;
    }

    public Point2DGrid(double _x, double _y) {
        super(_x, _y);
        isValid = (!Double.isNaN(_x) && !Double.isNaN(_y));
    }

    public Point2DGrid(Point2D point, boolean _isValid) {
        super(point.x, point.y);
        isValid = _isValid;
    }

    public Point2DGrid(Point2D point) {
        if (point != null) {
            x = point.x;
            y = point.y;
            isValid = (!Double.isNaN(x) && !Double.isNaN(y));
        } else {
            x = Float.NaN;
            y = Float.NaN;
            isValid = false;
        }
    }

    public Point2DGrid() {
        super();
        isValid = false;
    }

    public Point2DGrid clone() {
        Point2DGrid p = new Point2DGrid();
        p.x = x;
        p.y = y;
        p.isValid = isValid;
        return p;
    }
}

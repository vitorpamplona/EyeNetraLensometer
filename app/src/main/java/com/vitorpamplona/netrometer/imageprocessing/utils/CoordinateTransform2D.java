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

import android.graphics.Rect;

import com.vitorpamplona.netrometer.utils.Point2D;

import org.ejml.simple.SimpleMatrix;

import java.util.Arrays;

public class CoordinateTransform2D {
	
	public double s, a, x0, y0;

	public CoordinateTransform2D(Point2D pi1, Point2D pi2, Point2D pw1, Point2D pw2) {
		
		// get angle a
		double ang_i = Math.atan((pi2.y-pi1.y)/(pi2.x-pi1.x));
		double ang_w = Math.atan((pw2.y-pw1.y)/(pw2.x-pw1.x));
		a = ang_w - ang_i;
		
		double Qx1 = pi1.x * Math.cos(a) - pi1.y * Math.sin(a);
		double Qy1 = pi1.x * Math.sin(a) + pi1.y * Math.cos(a);
		double Qx2 = pi2.x * Math.cos(a) - pi2.y * Math.sin(a);

		SimpleMatrix M = new SimpleMatrix(3,3);
		SimpleMatrix u = new SimpleMatrix(3,1);
		
		M.setColumn(0, 0, Qx1, Qy1, Qx2);
		M.setColumn(1, 0, 1, 0, 1);
		M.setColumn(2, 0, 0, 1, 0);
		u.setColumn(0, 0, pw1.x, pw1.y, pw2.x);
		
		// solve linear equation to get s, xo, yo
		SimpleMatrix v = M.solve(u);
		s = v.get(0,0);
		x0 = v.get(1,0);
		y0 = v.get(2,0);
	}

	// input a point from old coordinate system and get coordinate in new system
	public Point2D point(float xi, float yi) {
		float xw = (float) (xi * s * Math.cos(a) - yi * s * Math.sin(a) + x0);
		float yw = (float) (xi * s * Math.sin(a) + yi * s * Math.cos(a) + y0);
		return new Point2D(xw, yw);
	}

    // input a point from old coordinate system and get coordinate in new system
    public Point2D point(Point2D p) {
        return point(p.x, p.y);
    }

	// input a rectangle from old coordinate system and get rectangle in new system
	public Rect rect(Rect r) {

        Point2D p1_in = new Point2D(r.left,r.top);
        Point2D p2_in = new Point2D(r.right,r.bottom);

		Point2D p1 = point(p1_in);
		Point2D p2 = point(p2_in);

		int[] Xa = new int[]{(int) p1.x, (int) p2.x};
		int[] Ya = new int[]{(int) p1.y, (int) p2.y};
		Arrays.sort(Xa);
		Arrays.sort(Ya);		
		return new Rect(Xa[0], Ya[0], Xa[1], Ya[1]);
	}

}
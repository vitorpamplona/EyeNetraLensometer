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
package com.vitorpamplona.netrometer.utils;

public class Point2D {
	public float x;
	public float y;

	public Point2D() {
		x = 0;
		y = 0;
	}

	public Point2D(float _x, float _y) {
		x = _x;
		y = _y;
	}

	public Point2D(double _x, double _y) {
		x = (float) _x;
		y = (float) _y;
	}

	public Point2D(Point2D point) {
		x = point.x;
		y = point.y;
	}

	public Point2D multiply(float factor) {
		x = x * factor;
        y = y * factor;
        return this;
	}

	public Point2D divide(float factor) {
		x = x / factor;
        y = y / factor;
        return this;
	}

	public Point2D add(Point2D o) {
		x += o.x;
        y += o.y;
        return this;
	}

	public Point2D minus(Point2D o) {
		x -= o.x;
        y -= o.y;
        return this;
	}

	public float magnitude() {
		return (float) Math.sqrt((x * x) + (y * y));
	}

	public Point2D normalize() {
		return divide(magnitude());
	}

	public Point2D setLength(float len) {
		double percent = len / magnitude();
		return growInPercent(percent);
	}

	public float dot(Point2D o) {
		return x * o.x + y * o.y;
	}

	public float angle() {
		return angle(new Point2D(1, 0));
	}

	public float angle(Point2D o) {
		Float ret = (float) Math.acos(dot(o) / (o.magnitude() * magnitude()));

		if (y < 0) {
			ret = -ret;
		}

		return ret;
	}

	public Point2D growInPercent(double percent) {
		return new Point2D(x * percent, y * percent);
	}

	public Point2D rotate(double angle) {
		double cs = Math.cos(Math.toRadians(angle));
		double sn = Math.sin(Math.toRadians(angle));

		Point2D ret = new Point2D(0, 0);
		ret.x = (float) (x * cs - y * sn);
		ret.y = (float) (x * sn + y * cs);
		return ret;
	}
	
	public String toString() {
		return "[" + x + "," + y + "]";
	}

	public Point2D clone() {
		Point2D p = new Point2D();
		p.x = x;
		p.y = y;

		return p;
	}

}
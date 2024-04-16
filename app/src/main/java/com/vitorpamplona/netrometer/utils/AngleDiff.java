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


public class AngleDiff {
	
	public static float angle0toN(float N, float angle)  // This deals with any angle, even with multiple factors away from 0
	{
	    float newAngle = angle;
	    while (newAngle < 0) newAngle += N;
	    while (newAngle >= N) newAngle -= N;
	    return newAngle;
	}
	
	public static float angle0to180(float angle) {
		if (angle >= 180) {
			angle = angle % 180;
		}
		if (angle < 0) {
			angle = (angle + 360) % 180;
		}
		return angle;
	}
	
	public static float angle0to360(float angle) {
		if (angle >= 360) {
			angle = angle % 360;
		}
		if (angle < 0) {
			angle = (angle + 360) % 360;
		}
		return angle;
	}
	
	public static float angle0to720(float angle) {
		if (angle >= 720) {
			angle = angle % 720;
		}
		if (angle < 0) {
			angle = (angle + 720) % 720;
		}
		return angle;
	}
	
	public static boolean isSmaller180(float angle1, float angle2) {
		boolean isSmaller = angle0to360(angle1) < angle0to360(angle2);
		
		float diff = Math.abs(angle0to360(angle1) - angle0to360(angle2));
		// if circularly smaller, angle1 is 179 and angle2 was 20. 
		if (diff > 180) {
			if (angle1 > 180 && angle2 < 180) {
				isSmaller = true;
			} else {
				isSmaller = false;
			}
		}
		
		return isSmaller;
	}
	
	public static float signedDiff180(float angle1, float angle2) {
		float diff = diff180(angle1, angle2);
		if (isSmaller180(angle1, angle2)) {
			return -diff;
		} else {
			return diff;
		}
	}
	
	public static float diff180(float angle1, float angle2) {
		angle1 = angle0to180(angle1);
		angle2 = angle0to180(angle2);
		
		float diff = angle0to180(angle1 - angle2);
		
		if (diff > 90) {
			diff = 180-diff;
		}
		
		return diff; 
	}
	
	public static float diff360(float angle1, float angle2) {
		angle1 = angle0to360(angle1);
		angle2 = angle0to360(angle2);
		
		float diff = angle0to360(angle1 - angle2);
		
		if (diff > 180) {
			diff = 360-diff;
		}
		
		return diff; 
	}
	
	
	/**
	 * Vectorizes to capture the mean. 
	 * 
	 * @param angles
	 * @return
	 */
	public static double mean360(double... angles) {
		double x_component = 0.0;
		double y_component = 0.0;
		double avg_d, avg_r;

		for (double angle_d : angles) {
			double angle_r;
			angle_r = Math.toRadians(angle0to360((float) angle_d));
			x_component += Math.cos(angle_r);
			y_component += Math.sin(angle_r);
		}
		x_component /= angles.length;
		y_component /= angles.length;
		avg_r = Math.atan2(y_component, x_component);
		avg_d = Math.toDegrees(avg_r);

		return angle0to360((float) avg_d);
	}

	public static double mean360(float... angles) {
		double x_component = 0.0;
		double y_component = 0.0;
		double avg_d, avg_r;

		for (double angle_d : angles) {
			double angle_r;
			angle_r = Math.toRadians(angle0to360((float)angle_d));
			x_component += Math.cos(angle_r);
			y_component += Math.sin(angle_r);
		}
		x_component /= angles.length;
		y_component /= angles.length;
		avg_r = Math.atan2(y_component, x_component);
		avg_d = Math.toDegrees(avg_r);

		return angle0to360((float) avg_d);
	}
	
	/**
	 * Vectorizes to capture the mean. 
	 * 
	 * @param angles
	 * @return
	 */
	public static double mean180(float... angles) {
		double x_component = 0.0;
		double y_component = 0.0;
		double avg_d, avg_r;

		for (double angle_d : angles) {
			double angle_r;
			angle_r = Math.toRadians(angle0to180((float) angle_d)*2);
			x_component += Math.cos(angle_r);
			y_component += Math.sin(angle_r);
		}
		x_component /= angles.length;
		y_component /= angles.length;
		avg_r = Math.atan2(y_component, x_component);
		avg_d = Math.toDegrees(avg_r);

		return angle0to180((float) avg_d/2);
	}
}

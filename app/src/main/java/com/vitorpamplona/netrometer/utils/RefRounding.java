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

public class RefRounding {
	
	public static float roundTo(float value, float stepSize) {
		stepSize = (int)(stepSize * 10000);
		int trunc = (int)(value * 10000);
		int div = (int) Math.round(trunc / stepSize) * (int)(stepSize);
		
		return div/10000.0f;
	}
	
	public static float ceilTo(float value, float stepSize) {
		stepSize = (int)(stepSize * 10000);
		int trunc = (int)(value * 10000);
		int div = (int) Math.ceil(trunc / stepSize) * (int)(stepSize);
		
		return div/10000.0f;
	}
		
	public static float ceilTo25(float value) {
		int trunc = (int)(value * 100);
		int div = (int) Math.ceil(trunc / 25.0f) * 25;
		
		return div/100.0f;
	}
	
	public static int ceilTo25Times100(float value) {
		int trunc = (int)(value * 100);
		int div = (int) Math.ceil(trunc / 25.0f) * 25;
		
		return div;
	}
}

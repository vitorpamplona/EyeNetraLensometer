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

public class YuvFilter {

	int Umin, Umax;
	int Vmin, Vmax;
	
	// constructor for rectangular YUV-space mask
	public YuvFilter(int Umin, int Umax, int Vmin, int Vmax) {
		this.Umin = Umin;
		this.Umax = Umax;
		this.Vmin = Vmin;
		this.Vmax = Vmax;
	}

    // constructor for rectangular YUV-space mask (input range 0-1 for both channels)
    public YuvFilter(float Umin, float Umax, float Vmin, float Vmax) {
        this.Umin = rescale0To255(Umin);
        this.Umax = rescale0To255(Umax);
        this.Vmin = rescale0To255(Vmin);
        this.Vmax = rescale0To255(Vmax);
    }

    private int rescale0To255(float val) {
        return (val < 0) ? 0 : (val > 1f) ? 255 : (int) (val * 255);
    }
	
	public boolean isValid(int Uvalue, int Vvalue) {		
		return (Uvalue >= Umin && Uvalue <= Umax && Vvalue >= Vmin && Vvalue <= Vmax);
	}

    public int getUmin() {
        return (Umin );
    }

    public int getUmax() {
        return (Umax );
    }
    public int getVmin() {
        return (Vmin );
    }
    public int getVmax() {
        return (Vmax );
    }


}

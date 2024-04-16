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
package com.vitorpamplona.netrometer.imageprocessing.hardware;

public abstract class AbstractDevice<T extends AbstractDevice> {
	public long id;
	public String what;
	public String model;
	public String where;

	public AbstractDevice(long id, String what, String model, String where) {
		super();
		this.id = id;
		this.what = what;
		this.model = model;
		this.where = where;
	}

	public void copyInto(T newDevice) {
		newDevice.id = id;
		newDevice.what = what;
		newDevice.model = model;
		newDevice.where = where;
	}

	AbstractDevice() {

	}

	public abstract T clone();

	public String toString() {
		return  id + "\t" +
				what + "\t" +
				model + "\t" +
				where + "\t";
	}

	public abstract float getThresholdNoLensLargeROI();
	public abstract float getThresholdNoLensCenterROI();
	public abstract double getDistanceNoLensCtr();
	public abstract double getDistanceNoLensLarge();
	public abstract float getAverageDotDistance();
	public abstract double getCalibrationCoefficientC();
	public abstract double getCalibrationCoefficientD();
	public abstract double getFineTunedDiopterCoeffA(double A, double B, double C, double D, double std);
	public abstract double getFineTunedDiopterCoeffB(double A, double B, double C, double D, double std);
	public abstract double getFineTunedDiopterCoeffC(double A, double B, double C, double D, double std);
	public abstract double getFineTunedDiopterCoeffD(double A, double B, double C, double D, double std);
}
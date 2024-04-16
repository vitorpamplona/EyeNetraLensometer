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
package com.vitorpamplona.netrometer.model;

import com.vitorpamplona.netrometer.utils.AngleDiff;
import com.vitorpamplona.netrometer.utils.SinusoidalFunction;
import com.vitorpamplona.netrometer.utils.SphericalEquivalent;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.UUID;


public class Prescription implements Serializable {
	private static final long serialVersionUID = 1L;

	RefractionType procedure;

	private UUID    id;
	private Float   sphere;
	private Float   cylinder;
	private Float   axis;
	private Float   addLens;
	private Float   vaCorrected;
	private Float   nosePupilDistance;
	private Boolean cycloplegia = false;

	public Prescription(Float spherical, Float cylindrical, Float axis, RefractionType proc) {
		sphere = spherical;
		cylinder = cylindrical;
		this.axis = axis;
		procedure = proc;

		putInNegativeCilinder();
	}

	protected Prescription(Prescription p) {
		this.cycloplegia = p.cycloplegia;
		this.procedure = p.procedure;
		this.id = p.id;
		this.sphere = p.sphere;
		this.cylinder = p.cylinder;
		this.axis = p.axis;
		this.addLens = p.addLens;
		this.vaCorrected = p.vaCorrected;
		this.nosePupilDistance = p.nosePupilDistance;
	}

	public Prescription(RefractionType proc) {
		procedure = proc;
	}

	/**
	 * Positive Notation to Negative Notation.
	 */
	public void putInNegativeCilinder() {
		if (cylinder != null && cylinder > 0.001) {
			sphere = sphere + cylinder;
			cylinder = -cylinder;
			axis = axis+90;
		}

		checkAxisOutOfBounds();
	}

	/**
	 * Positive Notation to Negative Notation.
	 */
	public void putInPositiveCilinder() {
		if (cylinder != null && cylinder < -0.001) {
			sphere = sphere + cylinder;
			cylinder = -cylinder;
			axis = axis+90;
		}

		checkAxisOutOfBounds();
	}

	/**
	 * Positive Notation to Negative Notation.
	 */
	public Prescription copyInNegativeCilinder() {
		Prescription p = new Prescription(this);
		p.putInNegativeCilinder();
		p.checkAxisOutOfBounds();

		return p;
	}

	/**
	 * Positive Notation to Negative Notation.
	 */
	public Prescription copyInPositiveCilinder() {
		Prescription p = new Prescription(this);
		p.putInPositiveCilinder();
		p.checkAxisOutOfBounds();

		return p;
	}

	public boolean isValid() {
		return sphere != null && sphere < 98;
	}

	public boolean isNearValid() {
		return addLens != null && addLens < 98;
	}

	public Float getNosePupilDistance() {
		return nosePupilDistance;
	}

	public void setNosePupilDistance(Float nosePupilDistance) {
		this.nosePupilDistance = nosePupilDistance;
	}

	/**
	 * Keep the axis between 0 and 180.
	 */
	public void checkAxisOutOfBounds() {
		if (axis != null && (axis>180 ||axis<0))
			axis = AngleDiff.angle0to180(axis);
	}

	/**
	 * Returns the spherical equivalent (Sphere + Cylinder / 2)
	 */
	public Float sphEquivalent() {
		return SphericalEquivalent.compute(sphere, cylinder);
	}

	/**
	 * Returns the interpolated power of a given angle.
	 */
	public float interpolate(Float angleDegrees) {
		return SinusoidalFunction.interpolate(sphere, cylinder, axis, angleDegrees);
	}

	@Override
	public String toString() {
		if (sphere == null) {
			return "Null Object";
		}

		DecimalFormat formatter = new DecimalFormat("0.00");
		return formatter.format(sphere) +  " " + formatter.format(cylinder) + " @ " + axis.intValue();
	}

	public Float getSphere() {
		return sphere;
	}

	public void setSphere(Float spherical) {
		sphere = spherical;
	}

	public Float getCylinder() {
		return cylinder;
	}

	public void setCylinder(Float cylindrical) {
		cylinder = cylindrical;
	}

	public Float getAxis() {
		return axis;
	}

	public void setAxis(Float axis) {
		this.axis = axis;
	}

	public Float getAddLens() {
		return addLens;
	}

	public void setAddLens(Float plusLens) {
		addLens = plusLens;
	}

	public Float getVaCorrected() {
		return vaCorrected;
	}

	public void setVaCorrected(Float va) {
		vaCorrected = va;
	}

	public boolean getCyclo() {
		return cycloplegia;
	}

	public void setCyclo(boolean c ) {
		cycloplegia = c;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID i ) {
		id = i;
	}

	public RefractionType getProcedure() {
		return procedure;
	}

}

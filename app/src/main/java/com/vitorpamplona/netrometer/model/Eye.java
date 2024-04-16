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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Eye implements Serializable{
	public static final long serialVersionUID = 1L;
	
	private Float visualAcuityNoCorrection;
	private Float visualAcuityPinhole;
	private Float pupilDiameter;
	List<Prescription> assessments;
	
	public Float getVisualAcuityNoCorrection() {
		return visualAcuityNoCorrection;
	}

	public void setVisualAcuityNoCorrection(float visualAcuityNoCorrection) {
		this.visualAcuityNoCorrection = visualAcuityNoCorrection;
	}

	public Float getVisualAcuityPinhole() {
		return visualAcuityPinhole;
	}

	public void setVisualAcuityPinhole(float visualAcuityPinhole) {
		this.visualAcuityPinhole = visualAcuityPinhole;
	}

	public List<Prescription> getAssessments() {
		if (assessments == null) {
			assessments = new ArrayList<Prescription>();
		}
		return assessments;
	}

	public void setAssessments(List<Prescription> assessments) {
		this.assessments = assessments;
	}

	public Prescription get(RefractionType type) {
		for (Prescription p : getAssessments()) {
			if (p.getProcedure() == type) {
				return p;
			}
		}
		return null;
	}

	public Prescription getOrCreate(RefractionType type) {
		for (Prescription p : getAssessments()) {
			if (p.getProcedure() == type) {
				return p;
			}
		}
		Prescription newNetra = new Prescription(type);
		getAssessments().add(newNetra);
		return newNetra;
	}

	public Prescription getOrCreateFrom(RefractionType type, RefractionType basedOn) {
		for (Prescription p : getAssessments()) {
			if (p.getProcedure() == type) {
				return p;
			}
		}
		Prescription copyFrom = null;
		for (Prescription p : getAssessments()) {
			if (p.getProcedure() == basedOn) {
				copyFrom = p;
			}
		}
		Prescription newNetra = new Prescription(type);
		getAssessments().add(newNetra);
		newNetra.setSphere(copyFrom.getSphere());
		newNetra.setCylinder(copyFrom.getCylinder());
		newNetra.setAxis(copyFrom.getAxis());
		newNetra.setAddLens(copyFrom.getAddLens());
		return newNetra;
	}
	
	public Prescription getNetra() {
		return getOrCreate(RefractionType.NETRA);
	}
	
	public Prescription getEnteringRX() {
		return getOrCreate(RefractionType.ENTERING_RX);
	}
	
	public Prescription getNetraReaders() {
		return getOrCreate(RefractionType.NETRA_READING);
	}
	
	public Prescription getEnteringRXReaders() {
		return getOrCreate(RefractionType.ENTERING_RX_READING);
	}

	public Float getPupilDiameter() {
		return pupilDiameter;
	}

	public void setPupilDiameter(float pupilDiameter) {
		this.pupilDiameter = pupilDiameter;
	}
}

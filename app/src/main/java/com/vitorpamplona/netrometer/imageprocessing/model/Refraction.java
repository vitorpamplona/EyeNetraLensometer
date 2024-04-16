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

import com.vitorpamplona.netrometer.utils.AngleDiff;

import java.text.DecimalFormat;

public class Refraction {

    public double dSphere;
    public double dCylinder;
    public double dAxis;
    public double dAdd;
    public String sSphere;
    public String sCylinder;
    public String sAxis;
    public String sAdd;
    public String parsed;
    public boolean isValid = true;
    public Double dPartialPD;

    private DecimalFormat dF = new DecimalFormat("0.00");
    private DecimalFormat dA = new DecimalFormat("0");

    public Refraction(double dSphere,
                      double dCylinder,
                      double dAxis) {

        this.dSphere = dSphere;
        this.dCylinder = dCylinder;
        this.dAxis = dAxis;
        this.dAdd = 0;
        this.sSphere = dF.format(dSphere);
        this.sCylinder = dF.format(dCylinder);
        this.sAxis = dA.format(dAxis);
        this.sAdd = dF.format(dAdd);
        this.parsed = sSphere + " " + sCylinder + " @ " + sAxis;

    }
    public Refraction(double dSphere,
                      double dCylinder,
                      double dAxis,
                      double dAdd) {

        this.dSphere = dSphere;
        this.dCylinder = dCylinder;
        this.dAxis = dAxis;
        this.dAdd = dAdd;
        this.sSphere = dF.format(dSphere);
        this.sCylinder = dF.format(dCylinder);
        this.sAxis = dA.format(dAxis);
        this.sAdd = dF.format(dAdd);
        this.parsed = sSphere + " " + sCylinder + " @ " + sAxis;

    }

    public Refraction(double dSphere,
                      double dCylinder,
                      double dAxis,
                      double dAdd,
                      Double dPartialPD) {

        this.dSphere = dSphere;
        this.dCylinder = dCylinder;
        this.dAxis = dAxis;
        this.dAdd = dAdd;
        this.sSphere = dF.format(dSphere);
        this.sCylinder = dF.format(dCylinder);
        this.sAxis = dA.format(dAxis);
        this.sAdd = dF.format(dAdd);
        this.parsed = sSphere + " " + sCylinder + " @ " + sAxis;
        this.dPartialPD = dPartialPD;

    }

    public void setPD(double pd){ this.dPartialPD=pd;}

    public void setAxis(double axis){ this.dAxis=axis;}

    public void checkAxisOutOfBounds() {
        if (dAxis<0 || dAxis>180){
            dAxis = AngleDiff.angle0to180((float)dAxis);
            sAxis = dF.format(dAxis);
        }

    }

    public String getFormattedPrescription(){
        return "SP: " + sSphere + "\tCY: " + sCylinder + "\tAX: " + sAxis;
    }

    public double calculateDiopterProjection( double angle){
        return (dSphere+dCylinder/2)-(dCylinder/2)*Math.cos(2*angle-2*dAxis*Math.PI/180);
    }

}
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

import com.vitorpamplona.netrometer.utils.Point2D;


public class CenterOfMass {


    private Point2D center;
    private Integer   sizeOfMass;
    private Integer   sugNumberOfMasses, yFirstUpPeak;

    public CenterOfMass(Point2D centerOfMass, Integer sizeOfCenter, Integer sugNumberOfMasses, Integer yFirstUpPeak) {
        this.center = centerOfMass;
        this.sizeOfMass = sizeOfCenter;
        this.sugNumberOfMasses = sugNumberOfMasses;
        this.yFirstUpPeak = yFirstUpPeak;
    }

    public CenterOfMass(CenterOfMass p) {
        this.center = p.center;
        this.sizeOfMass = p.sizeOfMass;
        this.sugNumberOfMasses=p.sugNumberOfMasses;
        this.yFirstUpPeak=p.yFirstUpPeak;

    }

    public boolean isValid() {
        return center != null && sizeOfMass!=null;
    }

    public Point2D getCenterOfMass() { return center; }
    public int getSizeOfMass() {return sizeOfMass;}
    public int getSugNumberOfMasses() {return sugNumberOfMasses;}
    public int getFirstUpPeak(){return yFirstUpPeak;}
}

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

public enum RefractionType {
    SUBJECTIVE(0),
    AUTOREFRACTOR(1),
    RETINOSCOPY(2),
    NETRA(3),
    ENTERING_RX(4),
    PHOTOREFRACTION(5),
    NETRA_READING(6),
    ENTERING_RX_READING(7);

    private int type;

    RefractionType(int type) {this.type = type;}
    
    @Override
    public String toString() {
        if (type == 0) { return "Subjective"; }
        if (type == 1) { return "Auto-Refractor"; }
        if (type == 2) { return "Retinoscopy"; }
        if (type == 3) { return "NETRA"; }
        if (type == 4) { return "Entering RX"; }
        if (type == 5) { return "Photo-Refraction"; }
        if (type == 6) { return "NETRA Reading"; }
        if (type == 7) { return "Entering RX Reading"; }
        return "";
    }
    
    public String toShortString() {
        if (type == 0) { return "SR"; }
        if (type == 1) { return "AR"; }
        if (type == 2) { return "RT"; }
        if (type == 3) { return "NE"; }
        if (type == 4) { return "EN"; }
        if (type == 5) { return "PR"; }
        if (type == 6) { return "NR"; }
        if (type == 7) { return "ER"; }
        return "";
    }
    
    public int toInt() {
        return this.type;
    }

    public static RefractionType fromInt( int x ) {
        switch ( x ) {
            case 0: return SUBJECTIVE;
            case 1: return AUTOREFRACTOR;
            case 2: return RETINOSCOPY; 
            case 3: return NETRA; 
            case 4: return ENTERING_RX; 
            case 5: return PHOTOREFRACTION;
            case 6: return NETRA_READING; 
            case 7: return ENTERING_RX_READING;
        }
        return null;
    }

}
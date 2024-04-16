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

public class FrameHolderResults {

        public Double frameAngle;
        public Double PD;
        public Double refCenterMm;
        public Double refOptCtrPx;

        public FrameHolderResults(){}

        public FrameHolderResults (Double frameAngle, Double pd, Double refCenterMm, Double refOptCtrPx) {

            this.frameAngle = frameAngle;
            this.PD = pd;
            this.refCenterMm = refCenterMm;
            this.refOptCtrPx = refOptCtrPx;

        }
    public void setPD(double pd){
        this.PD = pd;
    }

    public void setFrameAngle(double frameAngle){
        this.frameAngle = frameAngle;
    }

    public void calculatePD(double diopters, boolean isRightLens){

        double signal = (isRightLens)?1:-1;

        if(diopters>0 && refCenterMm!=null && refOptCtrPx!=null){
            if(diopters<4){
                this.PD= refCenterMm + signal*refOptCtrPx/(1.0298*diopters*diopters-7.2543*diopters-6.2335);
            } else if(diopters>=4 && diopters!=+99) {
                this.PD= refCenterMm + signal*refOptCtrPx/(-19.602);
            } else this.PD=null;

//            Log.e("gauss","isRightLens "+isRightLens+", PD "+this.PD+" = "+refCenterMm +" + "+(signal*refOptCtrPx/(1.0298*diopters*diopters-7.2543*diopters-6.2335))+ ", (1.0298*diopters*diopters-7.2543*diopters-6.2335) "+ (1.0298*diopters*diopters-7.2543*diopters-6.2335));
        } else if(diopters<= -1&& diopters>-10 && refCenterMm!=null && refOptCtrPx!=null){
            this.PD=refCenterMm+ signal*refOptCtrPx/(-.07*diopters*diopters-1.3454*diopters-35.923);
//            Log.e("gauss","isRightLens "+isRightLens+", PD "+this.PD+" = "+refCenterMm +" + "+(signal*refOptCtrPx/(-.07*diopters*diopters-1.3454*diopters-35.923))+ ", (-.07*diopters*diopters-1.3454*diopters-35.923) "+(-.07*diopters*diopters-1.3454*diopters-35.923));
        } else if(diopters<=-10 &&  refCenterMm!=null && refOptCtrPx!=null) {
            this.PD=refCenterMm+ signal*refOptCtrPx/(-29.779);
        } else{
            this.PD = null;
        }
//        DecimalFormat formNum = new DecimalFormat("+#0.0;-#0.0");
//        if(refCenterMm!=null & refOptCtrPx!=null & frameAngle!=null)
//            System.out.println( "refCenterMm " + formNum.format(refCenterMm) + ", refOptCtrPx " + formNum.format(refOptCtrPx) + ", frameAngle " + formNum.format(frameAngle));

    }

}

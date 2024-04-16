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
package com.vitorpamplona.netrometer.imageprocessing.processors;

import com.vitorpamplona.netrometer.imageprocessing.utils.FrameTools;
import com.vitorpamplona.netrometer.imageprocessing.utils.YuvFilter;
import com.vitorpamplona.netrometer.model.CenterOfMass;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Point2D;
import com.vitorpamplona.netrometer.utils.Rect;

import java.util.ArrayList;

public class FrameHolderProcessor {

        private final int previewWidth;
        private final int previewHeight;

        private Rect searchBox;
        private int refineDistance = 10;
        private YuvFilter filter = Params.NO_FILTER;
        private FrameTools tools;

        public FrameHolderProcessor(int imWidth, int imHeight) {

            previewWidth = imWidth;
            previewHeight = imHeight;

            searchBox = new Rect(0,0, previewWidth,previewHeight);

//                    (int)(previewWidth/2 - previewWidth*0.05f),
//                    (int)(previewHeight/2 - previewHeight*0.05f),
//                    (int)(previewWidth/2 + previewWidth*0.05f),
//                    (int)(previewHeight/2 + previewHeight*0.05f));

            tools = new FrameTools(Params.PREVIEW_FRAME_WIDTH,Params.PREVIEW_FRAME_HEIGHT);

        }

        public ArrayList<CenterOfMass> run(byte[] data) {

            Point2D refCenterPoint = new Point2D();
            CenterOfMass centerOfMass;
            Rect searchBoxAux = new Rect(searchBox.left, searchBox.top, searchBox.right, searchBox.bottom);
            ArrayList<CenterOfMass> centerRefined= new ArrayList<>();
            centerOfMass = tools.findGeneralCenterOfMassAndSize(data, searchBox, 1, filter);
            if (centerOfMass == null) return null;
//            Log.e("gauss", "1st Size 0 (one Center): "+centerOfMass.getSizeOfMass()+ ", #masses: "+centerOfMass.getSugNumberOfMasses());

           switch (centerOfMass.getSugNumberOfMasses()){
               case 1:
//                   Log.e("gauss", "CASE 1 ");
                   centerRefined = tools.refinePositionVector(data, centerOfMass.getCenterOfMass(), refineDistance, 1, filter, searchBox);
                   break;

               case 2:
//                   Log.e("gauss", "CASE 2 ");
                   centerRefined = tools.refinePositionVector(data, centerOfMass.getCenterOfMass(), refineDistance, 1, filter, searchBox);
                   break;

               case 3:
//                   Log.e("gauss", "CASE 3 ");
                        if (centerOfMass.getFirstUpPeak()<10){
                            searchBoxAux.set(searchBox.left,150,searchBox.right,previewHeight);
                            refCenterPoint = new Point2D(centerOfMass.getCenterOfMass().x,centerOfMass.getFirstUpPeak()+450);
//                            Log.e("gauss", "CASE 3 , refY "+(centerOfMass.getFirstUpPeak()+450)+", searchBoxAux ("+searchBoxAux.left+", "+searchBoxAux.top+", "+searchBoxAux.right+", "+searchBoxAux.bottom+")");
                        } else {
                            searchBoxAux.set(searchBox.left,0,searchBox.right,previewHeight-150);
                            refCenterPoint = new Point2D(centerOfMass.getCenterOfMass().x,centerOfMass.getFirstUpPeak()+180);
//                            Log.e("gauss", "CASE 3 , refY "+(centerOfMass.getFirstUpPeak()+180)+", searchBoxAux ("+searchBoxAux.left+", "+searchBoxAux.top+", "+searchBoxAux.right+", "+searchBoxAux.bottom+")");
                        }

                         centerRefined = tools.refinePositionVector(data,refCenterPoint , refineDistance, 1, filter, searchBoxAux);
                   break;

               default:
//                   Log.e("gauss", "CASE DEFAULT , number of sug masses:"+centerOfMass.getSugNumberOfMasses()+", searchBoxAux ("+searchBoxAux.left+", "+searchBoxAux.top+", "+searchBoxAux.right+", "+searchBoxAux.bottom+")");
                   centerRefined = tools.refinePositionVector(data, centerOfMass.getCenterOfMass(), refineDistance, 1, filter, searchBox);
                   break;


           }

            if(centerRefined!=null) {
                if (centerRefined.size() > 1) {
//                    Log.e("gauss", "Resized Size 0 (right): " + centerRefined.get(0).getSizeOfMass() + ", size 1 (left): " + centerRefined.get(1).getSizeOfMass());
                } else if(centerRefined.size()==1){
//                    Log.e("gauss", "Resized Size 0 (right): " + centerRefined.get(0).getSizeOfMass());
                }
            } else return null;
            return centerRefined;

        }

    public boolean isUILightPipeOn(byte[] data) {

        boolean isLightPipeOn=false;
        int numPixels;
        numPixels = tools.findPixelsFilteredCount(data, searchBox, 2, filter);
        if(numPixels>Params.ACTIVE_PIXELS_LIGHT_PIPE/4) isLightPipeOn = true;
        return isLightPipeOn;

    }

        public FrameHolderProcessor setFilter(YuvFilter f) {
            filter = f;
            return this;
        }

        public FrameHolderProcessor setRefineDistance(int d) {
            refineDistance = d;
            return this;
        }

        public FrameHolderProcessor setSearchBox(Rect b) {
            searchBox = b;
            return this;
        }



}

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

import com.vitorpamplona.netrometer.settings.Params;

public class ImageFilter {

    public static void YUVtoIntensityWithRedFilter(byte[] toFilter, int[][] filtered) {
        Thread myTopLeft = new Thread(new Segment(toFilter, filtered,       0,                              Params.PREVIEW_FRAME_WIDTH/2,       0,                              Params.PREVIEW_FRAME_HEIGHT/2), "ImageFilter Top Left");
        Thread myTopRight = new Thread(new Segment(toFilter, filtered,      Params.PREVIEW_FRAME_WIDTH/2,   Params.PREVIEW_FRAME_WIDTH  ,       0,                              Params.PREVIEW_FRAME_HEIGHT/2), "ImageFilter Top Right");
        Thread myBottomLeft = new Thread(new Segment(toFilter, filtered,    0,                              Params.PREVIEW_FRAME_WIDTH/2,       Params.PREVIEW_FRAME_HEIGHT/2,  Params.PREVIEW_FRAME_HEIGHT), "ImageFilter Bottom Left");
        Thread myBottomRight = new Thread(new Segment(toFilter, filtered,   Params.PREVIEW_FRAME_WIDTH/2,   Params.PREVIEW_FRAME_WIDTH  ,       Params.PREVIEW_FRAME_HEIGHT/2,  Params.PREVIEW_FRAME_HEIGHT), "ImageFilter Bottom Right");

        myTopLeft.start();
        myTopRight.start();
        myBottomLeft.start();
        myBottomRight.start();

        try {
            myTopLeft.join();
            myTopRight.join();
            myBottomLeft.join();
            myBottomRight.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class Segment implements Runnable {

        byte[] toFilter;
        int[][] filtered;
        int minX;
        int maxX;
        int minY;
        int maxY;

        public Segment(byte[] toFilter, int[][] filtered, int minX, int maxX, int minY, int maxY) {
            this.toFilter = toFilter;
            this.filtered = filtered;
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        public void run() {
            int offset = Params.PREVIEW_FRAME_WIDTH * Params.PREVIEW_FRAME_HEIGHT;
            int xOffset;
            int yOffset;
            int Uval;
            int Vval;

            int sumX;

            for (int x = minX; x < maxX; x++) {
                xOffset = (x & ~1);
                sumX = offset + xOffset;
                for (int y = minY; y < maxY; y++) {
                    filtered[x][y] = 0x00;

                    yOffset = (y >> 1) * Params.PREVIEW_FRAME_WIDTH;
                    Uval = 0xff & (int) toFilter[sumX + yOffset + 1];
                    Vval = 0xff & (int) toFilter[sumX + yOffset];

                    if (Uval >= 0 && Uval <= 153 && Vval >= 135 && Vval <= 255)
                        filtered[x][y]= ((Uval+Vval) >> 1);
                }
            }
        }
    }
}

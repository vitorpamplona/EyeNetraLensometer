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

public class OtsuThresholder {


    public static double doThreshold(double[] srcData)
    {
        int numOfBins = 32;
        int[] histData= new int[numOfBins];
        double maxLevelValue;
        double minLevelValue;
        int threshold;

        double binSize;
        int ptr;


        // Clear histogram data
        // Set all values to zero
        ptr = 0;
        while (ptr < histData.length) histData[ptr++] = 0;

        // Calculate histogram and find the level with the max value
        // Note: the max level value isn't required by the Otsu method
        ptr = 0;
        maxLevelValue = -999;
        minLevelValue = 999;

        while (ptr < srcData.length)
        {
            if (srcData[ptr] > maxLevelValue) maxLevelValue = srcData[ptr];
            if (srcData[ptr] < minLevelValue) minLevelValue = srcData[ptr];
            ptr ++;
        }

        binSize = (maxLevelValue-minLevelValue)/(float)(numOfBins-1);
        ptr=0;
        while (ptr < srcData.length)
        {
            int h = (int)Math.round((srcData[ptr]-minLevelValue)/binSize);
            histData[h] ++;
            if (histData[h] > maxLevelValue) maxLevelValue = histData[h];
            ptr ++;
        }

        // Total number of pixels
        int total = srcData.length;

        float sum = 0;
        for (int t=0 ; t<numOfBins ; t++) sum += t * histData[t];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        threshold = 0;

        for (int t=0 ; t<numOfBins ; t++)
        {
            wB += histData[t];					// Weight Background
            if (wB == 0) continue;

            wF = total - wB;						// Weight Foreground
            if (wF == 0) break;

            sumB += (float) (t * histData[t]);

            float mB = sumB / wB;				// Mean Background
            float mF = (sum - sumB) / wF;		// Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }


        return ((float)(threshold+1)*binSize +minLevelValue);
    }

}

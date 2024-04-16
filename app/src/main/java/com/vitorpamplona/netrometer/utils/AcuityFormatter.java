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
package com.vitorpamplona.netrometer.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AcuityFormatter {

    public enum ACUITY_TYPE {METRIC, IMPERIAL};
    private static final String NULL_VALUE = "--/--";

    public String format(Float va, ACUITY_TYPE type) {
        return getSnellenVA(va, type);
    }

    public float parse(String text) {
        return visualAcuityToDec(text);
    }

    public int getNominator(ACUITY_TYPE type) {
        if (type == ACUITY_TYPE.IMPERIAL) {
            return 20;
        } else {
            return 6;
        }
    }

    public String getSnellenVA(Float va, ACUITY_TYPE type) {
        if (va == null) {
            return NULL_VALUE;
        }

        if (Math.abs(va) < 0.00001) {
            return NULL_VALUE;
        }
        return getNominator(type) + "/" + (int)getSnellenDemonimator(va, type);
    }

    public float getDecimalVALogMar(float logMar) {
        return (float) Math.pow(10.0f, -logMar);
    }

    public float getSnellenDemonimatorLogMar(float logMar, ACUITY_TYPE type) {
        return getNominator(type)/getDecimalVALogMar(logMar);
    }

    public String getSnellenVALogMar(float logMar, ACUITY_TYPE type) {
        if (Float.isNaN(logMar) || Float.isInfinite(logMar))
            return "";
        return getNominator(type) + "/" + (int)getSnellenDemonimatorLogMar(logMar, type);
    }

    public float getSnellenDemonimator(float va, ACUITY_TYPE type) {
        return getNominator(type)/va;
    }

    // PARSING.

    public static final String VAPATTERN = "(\\d+)\\/(\\d+)([+-](\\d+))?";

    public static Map<Integer, Integer> snellenChart = new HashMap<Integer, Integer>() {{
        put(200,0); // 200
        put(100,1); // 100
        put(70,2); // 70
        put(50,3); // 50
        put(40,4); // 40
        put(30,5); // 30
        put(25,6); // 25
        put(20,7); // 20
        put(15,8); // 15
        put(13,9); // 13
        put(10,10); // 10

        put(60,11); // 10
        put(36,12); // 10
        put(24,13); // 10
        put(18,14); // 10
        put(12,15); // 10
        put(9,16); // 10
        put(6,17); // 10
        put(5,18); // 10
        put(4,19); // 10
        put(3,20); // 10
    }};
    public static int[][] snellenChartNumberOfLetters = {
            {200, 1}, // 200
            {100, 2}, // 100
            {70, 3}, // 70
            {50, 4}, // 50
            {40, 5}, // 40
            {30, 6}, // 30
            {25, 7}, // 25
            {20, 8}, // 20
            {15, 8}, // 15
            {13, 9}, // 13
            {10, 9}, // 10


            {60, 1},
            {36, 2},
            {24, 3},
            {18, 4},
            {12, 5},
            {9, 6},
            {6, 7},
            {5, 8},
            {4, 8},
            {3, 9},
    };

    public static int[] snellenChartImperial = {
            200, // 200
            100, // 100
            70, // 70
            50, // 50
            40, // 40
            30, // 30
            25, // 25
            20, // 20
            15, // 15
            13, // 13
            10, // 10
    };

    public static int[] snellenChartMetric = {
            60,
            36,
            24,
            18,
            12,
            9,
            6,
            5,
            4,
            3,
    };


    public float visualAcuityToDec(String formattedValue) {
        if (formattedValue == null) return 0;
        if (NULL_VALUE.equals(formattedValue)) return 0;

        // Create a Pattern object
        Pattern r = Pattern.compile(VAPATTERN);

        // Now create matcher object.
        Matcher m = r.matcher(formattedValue);

        if (!m.find()) {
            return Float.parseFloat(formattedValue);
        }

        Integer eyeChartType = Integer.parseInt(m.group(1));
        Integer linePatientCanRead = Integer.parseInt(m.group(2));
        float decimalVA =  eyeChartType/(float)linePatientCanRead;

        // extra +/- letters.
        String fractionPart = m.group(3);
        if (fractionPart != null) {
            int fraction = Integer.parseInt(m.group(4));

            if (fractionPart.contains("-")) {
                fraction = -fraction;
            }

            int snellenIndex = snellenChart.get(linePatientCanRead);
            if (fractionPart.contains("+")) {
                snellenIndex++;
            }

            int[] lettersOnTheFractionLine = snellenChartNumberOfLetters[snellenIndex];
            float decimalDistance = Math.abs(
                    eyeChartType/(float)snellenChartNumberOfLetters[snellenIndex][0]
                            - eyeChartType/(float)snellenChartNumberOfLetters[snellenIndex-1][0]);

            float percent = fraction/(float)lettersOnTheFractionLine[1];

            decimalVA += decimalDistance*percent;
        }

        return decimalVA;
    }
}

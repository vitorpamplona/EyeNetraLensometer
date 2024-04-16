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
package com.vitorpamplona.netrometer.imageprocessing.calibration;

import android.os.AsyncTask;

import com.vitorpamplona.netrometer.imageprocessing.utils.FittingCoefficients;
import com.vitorpamplona.netrometer.imageprocessing.utils.ImageProcessingUtil;
import com.vitorpamplona.netrometer.imageprocessing.utils.PolynomialRegression;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.model.PolarFromGrid;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.WriteToCsv;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.text.DecimalFormat;
import java.util.List;

public class CalibrationHelper {

    private DecimalFormat dP = new DecimalFormat("+#0.00;-#0.00");
    private FittingCoefficients fittingCoefficients;
    private int mStep;
    private boolean mFinished, mStarted;

    private double[] mSamplePoints = {
//            0.00    ,
//            0.25    ,
//            0.50    ,
//            0.75    ,
//            1.00	,
//            1.25	,
//            1.50	,
//            1.75	,
//            2.00	,
//            2.25	,
//            2.50	,
//            2.75	,
//            3.00	,
//            3.25	,
//            3.50	,
//            3.75	,
//            4.00	,
//            4.25	,
//            4.50	,
//            4.75	,
//            5.00	,
//            5.25	,
//            5.50	,
//            5.75	,
//            6.00	,
//
//            7.00	,
//            8.00	,
//            9.00	,
//            10.00	,
//            11.00	,
//            12.00	,
//            -0.25   ,
//            -0.50   ,
//            -0.75   ,
//            -1.00	,
//            -1.25	,
//            -1.50	,
            -1.75	,
            -2.00	,
            -2.25	,
            -2.50	,
            -2.75	,
            -3.00	,
            -3.25	,
            -3.50	,
            -3.75	,
            -4.00	,
            -4.25	,
            -4.50	,
            -4.75	,
            -5.00	,
            -5.25	,
//            -5.50	,
//            -5.75	,
//            -6.00	,
//            -6.50   ,
//            -7.00	,
//            -7.50   ,
//            -8.00	,
//            -8.50   ,
//            -9.00	,
//            -10.00	,
//            -11.00	,
//            -12.00	,
//            -13.00  ,
//            -14.00  ,
//            -15.00  ,




    };

    private double[] mSampleObservations = new double[mSamplePoints.length];

    private String mInstructionsText = "";

    public CalibrationHelper() {
        reset();
    }

    public void reset() {
        mStarted = false;
        mFinished = false;
        mStep = 0;
        mInstructionsText = "Start Calibration";
    }

    public boolean addLensesResult(final GridResult result,final GridResult zero){

        if (result==null || zero==null) return false;

        if (!mStarted) {
            mStarted = true;
            mInstructionsText = "Load lens: " + dP.format(mSamplePoints[mStep]);
            return false;
        }

        if (!hasFinished()) {

            mSampleObservations[mStep] = extractResultRatios(result, zero).getMean();
            saveResultToCsv(String.valueOf(dP.format(mSamplePoints[mStep])), result);

            mStep++;

            if (mStep>=mSamplePoints.length) {
                mFinished = true;
                mInstructionsText = "Completed!";
            } else {
                mInstructionsText = "Load lens: " + dP.format(mSamplePoints[mStep]);
            }

            return true;
        }

    return false;
    }

    private DescriptiveStatistics extractResultRatios(GridResult result, GridResult zero) {

        DescriptiveStatistics stats = new DescriptiveStatistics();
        int rowTotal = result.pointsOnGrid.length;
        int columnTotal = result.pointsOnGrid[0].length;
        int rowCenter = rowTotal/2;
        int columnCenter = columnTotal/2;

        List<PolarFromGrid> polarPoints =  ImageProcessingUtil.convertGridToPolar(result, Params.NEIGHBORS, rowCenter, columnCenter);
        List<PolarFromGrid> polarZeroPoints =  ImageProcessingUtil.convertGridToPolar(zero, Params.NEIGHBORS, rowCenter, columnCenter);

        for (int i=0; i<polarZeroPoints.size(); i++) {

            if (polarZeroPoints.get(i).isValid && polarPoints.get(i).isValid) {

                double zeroRadius = polarZeroPoints.get(i).r;
                double resultRadius = polarPoints.get(i).r;

                if (!(Double.isNaN(zeroRadius) || Double.isNaN(resultRadius))) {

                    // Get change ratio (1 = 100%, unchanged):
                    stats.addValue(resultRadius / zeroRadius);
                }
        }
        }

        return stats;
    }

    public FittingCoefficients calculateCoefficients() {

        // fit data to 3rd-degree polynomial
        PolynomialRegression polyFit = new PolynomialRegression(3);
        polyFit.fit(mSamplePoints, mSampleObservations);
        double[] coeffs = polyFit.getCoef();

        if (coeffs.length == 4) {
            fittingCoefficients = new FittingCoefficients(coeffs[3], coeffs[2], coeffs[1], coeffs[0]);
        }
        return fittingCoefficients;
    }

    public void saveResultToCsv(String name, GridResult result) {
        new CsvTask().execute(new Object[]{name, result});
    }

    private class CsvTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {

            WriteToCsv csv = new WriteToCsv((String) params[0]);
            csv.saveGridToFile((GridResult) params[1]);

            return null;
        }
    }

    public boolean hasStarted(){
        return mStarted;
    }

    public boolean hasFinished(){
        return mFinished;
    }

    public String getInstructionsText() {
        return mInstructionsText;
    }

}
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
package com.vitorpamplona.netrometer.imageprocessing;

import com.vitorpamplona.netrometer.BuildConfig;
import com.vitorpamplona.netrometer.NetrometerApplication;

import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.imageprocessing.utils.FittingCoefficients;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.ByteArrayReadWrite;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=21)
public class ProgressiveImageProcessingTest {
    ProgressiveImageProcessing processor;
    private NetrometerActivity nmActivity;

    @Before
    public void setUp() {
        ActivityController<NetrometerActivity> controller = Robolectric.buildActivity(NetrometerActivity.class);
        nmActivity = controller.get();
        controller.create().resume();

        processor = new ProgressiveImageProcessing(nmActivity.getApp().getDevice());
        processor.isSkipRightNear= false;
        processor.isSkipLeftNear = false;
        processor.isSkipNear = false;
    }
    public FittingCoefficients getFittingCoefficients() {
        return new FittingCoefficients(
                Params.FITTING_COEFFICIENTS_A,
                Params.FITTING_COEFFICIENTS_B,
                Params.FITTING_COEFFICIENTS_C,
                Params.FITTING_COEFFICIENTS_D
        );

    }
    public void testOnCreate(){
        NetrometerApplication nmApplication = new NetrometerApplication();
        nmApplication.onCreate();
    }
    public double calculateDiopterValueAt180(Refraction prescription){
        double offset, angleRad, amplitude, diopter;
        offset = prescription.dSphere + prescription.dCylinder/2;
        angleRad = Math.PI*(prescription.dAxis + 45)/180;
        amplitude = -prescription.dCylinder/2;
        diopter = amplitude*Math.sin(2*Math.PI +2*angleRad ) + offset;
        return diopter;
    }
//    public void calculatePDforTests(int lensID, double rightActualPD, double leftActualPD, int stepMillimeter, int firstMillimeter) throws IOException {
//        DecimalFormat StdDev = new DecimalFormat("#0.00;#0.00");
//        DecimalFormat Mean = new DecimalFormat("+#0.00;-#0.00");
//        DecimalFormat error = new DecimalFormat("+#0.00;-#0.00");
//
//        ArrayList<Refraction> testGlassesForDiopterLeft = new ArrayList<>();
//        ArrayList<Refraction> testGlassesForDiopterRight = new ArrayList<>();
//
//        int framesID, lastFrameID = 2, actualMillimeter, relativeMillimeter, lastMillimeter = 16;
//        int failedLeftPD=0, failedRightPD=0, index =0;
//        int totalFramesNumber = ((lastMillimeter - firstMillimeter)/stepMillimeter+1)*2;//+1 for '0', *2 because of double pictures
//        String dataFilePath, leftFilePath, rightFilePath;
//        byte[] frameZero, frameLeft, frameRight;
//        Refraction leftPrescription, rightPrescription;
//        DescriptiveStatistics leftPDData = new DescriptiveStatistics(), rightPDData = new DescriptiveStatistics();
//
//        processor.isSkipLeftNear = false;
//        processor.isSkipRightNear = false;
//        processor.isSkipNear = false;
//
//        boolean isLeftLens = false, isRightLens = true;
//        leftPDData.setWindowSize(totalFramesNumber);
//        rightPDData.setWindowSize(totalFramesNumber);
//        System.out.println("Left lens millimeters, NE"+lensID);
//
//        for (actualMillimeter = firstMillimeter; actualMillimeter <= lastMillimeter; actualMillimeter = actualMillimeter + stepMillimeter) {
//            dataFilePath = String.format(Locale.US, "ProgressiveSmartStageData/NE %d/LeftLens/%dmm/",lensID, actualMillimeter);
//            relativeMillimeter = 12 - actualMillimeter;
//            frameZero = ByteArrayReadWrite.readFromTestAssets(dataFilePath + "zero.txt");
//            processor.feedFrame(frameZero);
//            processor.generateZeroValues();
//
//            for (framesID = 1; framesID <= lastFrameID; framesID++) {
//                leftFilePath = String.format(Locale.US, "left%d.txt", framesID);
//                frameLeft = ByteArrayReadWrite.readFromTestAssets(dataFilePath + leftFilePath);
//                processor.isDistance = true;
//                processor.feedFrame(frameLeft);
//                processor.runGridFinder(isLeftLens);
//
//                processor.isDistance = false;
//                processor.feedFrame(frameLeft);
//                processor.runGridFinder(isLeftLens);
//                if (processor.calculatePrescription(isLeftLens) != null){
//                    leftPrescription = processor.calculatePrescription(isLeftLens);
//                    if (leftPrescription.dPartialPD!=null){
//                        System.out.println(relativeMillimeter);
//                        leftPDData.addValue(leftPrescription.dPartialPD);
//                        testGlassesForDiopterLeft.add(leftPrescription);
//                    }
//                    else{
//                        failedLeftPD++;
//                        System.out.println("error, " + relativeMillimeter + "mm,");
//                    }
//                }
//                else{
//                    failedLeftPD++;
//                    System.out.println("error, " + relativeMillimeter + "mm,");
//                }
//            }
//        }
//        System.out.println("Right lens millimeters");
//        for (actualMillimeter = firstMillimeter; actualMillimeter <= lastMillimeter; actualMillimeter = actualMillimeter + stepMillimeter) {
//            dataFilePath = String.format(Locale.US, "ProgressiveSmartStageData/NE %d/RightLens/%dmm/",lensID, actualMillimeter);
//            relativeMillimeter = 12 - actualMillimeter;
//            frameZero = ByteArrayReadWrite.readFromTestAssets(dataFilePath + "zero.txt");
//            processor.feedFrame(frameZero);
//            processor.generateZeroValues();
//
//            for (framesID = 1; framesID <= lastFrameID; framesID++) {
//                rightFilePath = String.format(Locale.US, "right%d.txt", framesID);
//                frameRight = ByteArrayReadWrite.readFromTestAssets(dataFilePath + rightFilePath);
//                processor.isDistance = true;
//                processor.feedFrame(frameRight);
//                processor.runGridFinder(isRightLens);
//
//                processor.isDistance = false;
//                processor.feedFrame(frameRight);
//                processor.runGridFinder(isRightLens);
//
//                if (processor.calculatePrescription(isRightLens) != null){
//                    rightPrescription = processor.calculatePrescription(isRightLens);
//                    if (rightPrescription.dPartialPD!=null){
//                        System.out.println(relativeMillimeter);
//                        rightPDData.addValue(rightPrescription.dPartialPD);
//                        testGlassesForDiopterRight.add(rightPrescription);
//                    }
//                    else{
//                        failedRightPD++;
//                        System.out.println("error, " + relativeMillimeter + "mm,");
//                    }
//                }
//                else{
//                    failedRightPD++;
//                    System.out.println("error, " + relativeMillimeter + "mm,");
//                }
//            }
//        }
//        System.out.println("Left Lenses, Partial PD");
//        for (index = 0; index < (totalFramesNumber - failedLeftPD); index ++){
//            System.out.println(error.format(leftPDData.getElement(index)));
//        }
//        System.out.println("Right Lenses, Partial PD");
//        for (index = 0; index < (totalFramesNumber - failedRightPD); index ++){
//            System.out.println(error.format(rightPDData.getElement(index)));
//        }
//        int validLenses;
//        validLenses = failedLeftPD>failedRightPD ? (totalFramesNumber-failedLeftPD) : (totalFramesNumber-failedRightPD);
//        System.out.println("Complete PD Lens number NE" + lensID);
//        for (index = 0; index < (validLenses); index ++){
//            System.out.println(error.format(rightPDData.getElement(index) + leftPDData.getElement(index)));
//        }
//        System.out.println("Left Lenses, Diopter value");
//        for (index = 0; index < (validLenses); index ++){
//                System.out.println(error.format(calculateDiopterValueAt180(testGlassesForDiopterLeft.get(index))));
//        }
//        System.out.println("Right Lenses, Diopter value");
//        for (index = 0; index < (validLenses); index ++){
//            System.out.println(error.format(calculateDiopterValueAt180(testGlassesForDiopterRight.get(index))));
//        }
//
//        System.out.println(failedLeftPD + "# Errors (NullPD) || " + "Left NE_" + lensID + ", " + Mean.format(leftPDData.getMean()) + "\u00B1" + StdDev.format(leftPDData.getStandardDeviation()));
//        System.out.println(failedRightPD + "# Errors (NullPD) || " + "Right NE_" + lensID + ", " + Mean.format(rightPDData.getMean()) + "\u00B1" + StdDev.format(rightPDData.getStandardDeviation()));
//    }
    @Test
    public void testDiopterValueAt180() throws  IOException {
        Refraction test = new Refraction(-5,-1,0);
        System.out.println("Diopter found in 180, axis 0 " + calculateDiopterValueAt180(test));
        for (int i = 0; i <= 180; i = i +10){
            test.dSphere = -3;
            test.dCylinder = -1;
            test.dAxis = i;
            System.out.println("Diopter found in 180, axis " + i + ", " + calculateDiopterValueAt180(test));
        }
    }
    @Test
    public void runFullNetrometerProgressiveTest() throws IOException {
        byte[] frameZero = ByteArrayReadWrite.readFromTestAssets("PGIMzero.txt");

        byte[] frameRightDistanceLens = ByteArrayReadWrite.readFromTestAssets("PGIMrightDistance.txt");
        byte[] frameRightNearLens = ByteArrayReadWrite.readFromTestAssets("PGIMrightNear.txt");

        byte[] frameLeftDistanceLens = ByteArrayReadWrite.readFromTestAssets("PGIMleftDistance.txt");
        byte[] frameLeftNearLens = ByteArrayReadWrite.readFromTestAssets("PGIMleftNear.txt");

        boolean isRightLens = true;

        processor.isSkipLeftNear = false;
        processor.isSkipRightNear = false;
        processor.isSkipNear = false;

        processor.feedFrame(frameZero);
        processor.generateZeroValues();

        processor.isDistance = true;

        processor.feedFrame(frameRightDistanceLens);
        processor.runGridFinder(isRightLens);

        processor.isDistance = false;

        processor.feedFrame(frameRightNearLens);
        processor.runGridFinder(isRightLens);

        processor.isDistance = true;

        processor.feedFrame(frameLeftDistanceLens);
        processor.runGridFinder(!isRightLens);

        processor.isDistance = false;

        processor.feedFrame(frameLeftNearLens);
        processor.runGridFinder(!isRightLens);

        Refraction rightPrescription = processor.calculatePrescription(isRightLens);
        Refraction leftPrescription = processor.calculatePrescription(!isRightLens);


        double deltaAxis = Math.min(Math.abs(rightPrescription.dAxis-0), Math.abs(180-rightPrescription.dAxis+0)); // Zero is the desired angle
        assertEquals(-0.25, rightPrescription.dSphere,   0.25);
        assertEquals(0.0, rightPrescription.dCylinder, 0.25);
        assertEquals(   0, deltaAxis,     10);
        assertEquals(   1, rightPrescription.dAdd,     0.5);

        assertEquals(-1.25, leftPrescription.dSphere,   0.25);
        assertEquals(-0.50, leftPrescription.dCylinder, 0.25);
        assertEquals(    29, leftPrescription.dAxis,     10);
        assertEquals(   1, leftPrescription.dAdd,     0.5);

    }

    @Test
    public void calculateADDTest() throws IOException{

        double SPHdistance = -1;
        double SPHnear = 2;

        double ADDvalue = processor.calculateAdd(SPHnear,SPHdistance);

        assertEquals(3.00, ADDvalue, .25);
    }


    @Test
    public void imageProcessingProgressiveTimeTest() throws IOException {

        byte[] frameZero = ByteArrayReadWrite.readFromTestAssets("PGIMzero.txt");
        byte[] frameDistanceLens = ByteArrayReadWrite.readFromTestAssets("PGIMrightDistance.txt");
        byte[] frameNearLens = ByteArrayReadWrite.readFromTestAssets("PGIMrightDistance.txt");
        long timestamp = System.currentTimeMillis();

        processor.isSkipLeftNear = false;
        processor.isSkipRightNear = false;
        processor.isSkipNear = false;

        processor.feedFrame(frameZero);
        processor.generateZeroValues();

        processor.isDistance = true;

        processor.feedFrame(frameDistanceLens);
        processor.runGridFinder(true);

        processor.isDistance = false;

        processor.feedFrame(frameNearLens);
        processor.runGridFinder(true);

        boolean isRightLenses = true;

        Refraction prescription = processor.calculatePrescription(isRightLenses);


        assertTrue(System.currentTimeMillis() - timestamp < 45000);
    }



//    @Test
//    public void calculatePD_NE_06() throws IOException {
//        int lensID = 06, stepMillimeter = 1, firstMillimeter = 9;
//        double leftActualPD = 32.5, rightActualPD = 32;
//        calculatePDforTests(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter);
//    }
//    @Test
//    public void calculatePD_NE_22() throws IOException {
//        int lensID = 22, stepMillimeter = 1, firstMillimeter = 8;
//        double leftActualPD = 31.0, rightActualPD = 30.5;
//        calculatePDforTests(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter);
//    }
//    @Test
//    public void calculatePD_NE_12() throws IOException {
//        int lensID = 12, stepMillimeter = 2, firstMillimeter = 8;
//        double leftActualPD = 30.5, rightActualPD = 33.0;
//        calculatePDforTests(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter);
//    }
//    @Test
//    public void calculatePD_NE_17() throws IOException {
//        int lensID = 17, stepMillimeter = 2, firstMillimeter = 8;
//        double leftActualPD = 35.0, rightActualPD = 32.0;
//        calculatePDforTests(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter);
//    }
//    @Test
//    public void calculatePD_NE_23() throws IOException {
//        int lensID = 23, stepMillimeter = 2, firstMillimeter = 8;
//        double leftActualPD = 34.0, rightActualPD = 32.0;
//        calculatePDforTests(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter);
//    }
//    @Test
//    public void calculatePD_NE_25() throws IOException {
//        int lensID = 25, stepMillimeter = 2, firstMillimeter = 8;
//        double leftActualPD = 33.0, rightActualPD = 33.0;
//        calculatePDforTests(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter);
//    }


    @Test
    public void averageMeasurementsProgressive126() {
        int absentLenses;
        ArrayList<Refraction> testGlassesRefraction = new ArrayList<>();

        for (absentLenses = 0; absentLenses <17; absentLenses++){
            testGlassesRefraction.add(absentLenses, new Refraction(0.0, 0.0, 0.0, 0.0, 0.0));      // absent Lenses
        }

        // Right Refractions
        testGlassesRefraction.add(17, new Refraction(6, -2.5, 63, 1.75, 66.0));
        testGlassesRefraction.add(18, new Refraction(-0.25, -0.25, 3, 1, 62.0));
        testGlassesRefraction.add(19, new Refraction(-1, -0.25, 175, 1, 61.0));
        testGlassesRefraction.add(20, new Refraction(-1.5, -0.5, 166, 1.25, 62.0));
        testGlassesRefraction.add(21, new Refraction(0.0, 0.0, 0.0, 0.0, 0.0));      // absent
        testGlassesRefraction.add(22, new Refraction(-0.25, 0, 0, 1, 63.0));
        testGlassesRefraction.add(23, new Refraction(0, 0, 0, 2, 63.0));
        testGlassesRefraction.add(24, new Refraction(0.75, -0.5, 17, 1.25, 58.0));
        testGlassesRefraction.add(25, new Refraction(1.25, -0.5, 11, 1, 60.0));
        testGlassesRefraction.add(26, new Refraction(2.25, -0.5, 19, 1, 61.0));
        testGlassesRefraction.add(27, new Refraction(3.25, 0, 0, 1.5, 63.0));
        // Left Refractions
        testGlassesRefraction.add(28, new Refraction(5, -1.25, 165, 1.75, 66.0));
        testGlassesRefraction.add(29, new Refraction(-0.5, -0.25, 6, 1, 62.0));
        testGlassesRefraction.add(30, new Refraction(0.75, -1, 15, 1, 61.0));
        testGlassesRefraction.add(31, new Refraction(-2, -0.5, 25, 1.25, 62.0));
        testGlassesRefraction.add(32, new Refraction(0.0, 0.0, 0.0, 0.0, 0.0));      // absent
        testGlassesRefraction.add(33, new Refraction(-1.25, -0.5, 29, 1, 63.0));
        testGlassesRefraction.add(34, new Refraction(0.25, 0, 0, 2, 63.0));
        testGlassesRefraction.add(35, new Refraction(1, -0.75, 150, 1.25, 58.0));
        testGlassesRefraction.add(36, new Refraction(1.5, -1, 157, 1, 60.0));
        testGlassesRefraction.add(37, new Refraction(2.75, -0.25, 178, 1, 61.0));
        testGlassesRefraction.add(38, new Refraction(0.5, -0.25, 6, 1.5, 63.0));

        byte[] frameZero = new byte[0], frameRightNear = new byte[0], frameRightDistance = new byte[0], frameLeftNear = new byte[0], frameLeftDistance = new byte[0];
        boolean isRightLens = true;
        int numberOfEyeglassesTotal = 28;
        int numberOfEyeglassesUsed  = 11;
        Refraction rightRefraction, leftRefraction;

        DescriptiveStatistics dataSphere = new DescriptiveStatistics();
        DescriptiveStatistics dataCylinder = new DescriptiveStatistics();
        DescriptiveStatistics dataAxis = new DescriptiveStatistics();
        DescriptiveStatistics dataAdd = new DescriptiveStatistics();

        dataAxis.setWindowSize(numberOfEyeglassesUsed * 2);
        dataCylinder.setWindowSize(numberOfEyeglassesUsed * 2);
        dataSphere.setWindowSize(numberOfEyeglassesUsed * 2);
        dataAdd.setWindowSize(numberOfEyeglassesUsed * 2);
        DecimalFormat result = new DecimalFormat("+#0.00;-#0.00");

        int version = 126;  // choose Hardware Version!

        /* Testing lenses from 18 - 28 */
        double addFinalExpected, addFinalFound;

        int lensNumberL;         // R for Right Lens, L for Left Lens.
        nmActivity.setVersionFromNdefMessageForUnitTest(version);
        for (int lensNumberR = 0; lensNumberR < numberOfEyeglassesTotal; lensNumberR++) {
            if ((lensNumberR != 21) && (lensNumberR > 16) && (lensNumberR != 19)) {           // not using the absent lenses
                for (int frameNumber = 0; frameNumber < 3; frameNumber++) {      // different frames from same lenses
                    lensNumberL = lensNumberR + numberOfEyeglassesUsed;
                    try {
                        frameZero = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataPR/v%d/Frames%d/zero%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameRightNear = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataPR/v%d/Frames%d/rightNear%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameRightDistance = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataPR/v%d/Frames%d/rightDistance%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameLeftNear = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataPR/v%d/Frames%d/leftNear%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameLeftDistance = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataPR/v%d/Frames%d/leftDistance%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    processor.feedFrame(frameZero);
                    processor.generateZeroValues();

                    processor.isDistance = true;

                    processor.feedFrame(frameRightDistance);
                    processor.runGridFinder(isRightLens);

                    processor.isDistance = false;

                    processor.feedFrame(frameRightNear);
                    processor.runGridFinder(isRightLens);

                    rightRefraction = processor.calculatePrescription(isRightLens);

                    processor.isDistance = true;

                    processor.feedFrame(frameLeftDistance);
                    processor.runGridFinder(!isRightLens);

                    processor.isDistance = false;

                    processor.feedFrame(frameLeftNear);
                    processor.runGridFinder(!isRightLens);

                    leftRefraction = processor.calculatePrescription(!isRightLens);

                //    System.out.println("Right refraction number, " + lensNumberR + ", Value, " + rightRefraction.dCylinder + ", Result, " + (testGlassesRefraction.get(lensNumberR).dCylinder - rightRefraction.dCylinder));
                //    System.out.println("Left refraction number , " + lensNumberR + ", Value, " + leftRefraction.dCylinder + ", Result, " + (testGlassesRefraction.get(lensNumberL).dCylinder - leftRefraction.dCylinder));

                    dataSphere.addValue((rightRefraction.dSphere - testGlassesRefraction.get(lensNumberR).dSphere));
                    dataCylinder.addValue((rightRefraction.dCylinder - testGlassesRefraction.get(lensNumberR).dCylinder));
                    if ((testGlassesRefraction.get(lensNumberR).dAxis - rightRefraction.dAxis) > 90)
                        rightRefraction.dAxis = rightRefraction.dAxis + 180;
                    else if ((testGlassesRefraction.get(lensNumberR).dAxis - rightRefraction.dAxis) < -90)
                        rightRefraction.dAxis = rightRefraction.dAxis - 180;
                    if ((rightRefraction.dCylinder != 0) && (testGlassesRefraction.get(lensNumberR).dCylinder != 0)) {
                        dataAxis.addValue((rightRefraction.dAxis - testGlassesRefraction.get(lensNumberR).dAxis));
                    }

                    addFinalExpected = (testGlassesRefraction.get(lensNumberR).dAdd + testGlassesRefraction.get(lensNumberR).dSphere);
                    addFinalFound = (rightRefraction.dAdd + rightRefraction.dSphere);
                    dataAdd.addValue((addFinalFound - addFinalExpected));

                    dataSphere.addValue((leftRefraction.dSphere - testGlassesRefraction.get(lensNumberL).dSphere));
                    dataCylinder.addValue((leftRefraction.dCylinder - testGlassesRefraction.get(lensNumberL).dCylinder));
                    if ((testGlassesRefraction.get(lensNumberL).dAxis - leftRefraction.dAxis) > 90)
                        leftRefraction.dAxis = leftRefraction.dAxis + 180;
                    else if ((testGlassesRefraction.get(lensNumberL).dAxis - leftRefraction.dAxis) < -90)
                        leftRefraction.dAxis = leftRefraction.dAxis - 180;
                    if ((leftRefraction.dCylinder != 0) && (testGlassesRefraction.get(lensNumberL).dCylinder != 0)) {
                        dataAxis.addValue((leftRefraction.dAxis - testGlassesRefraction.get(lensNumberL).dAxis));
                    }
                    addFinalExpected = (testGlassesRefraction.get(lensNumberL).dAdd + testGlassesRefraction.get(lensNumberL).dSphere);
                    addFinalFound = (leftRefraction.dAdd + leftRefraction.dSphere);
                    dataAdd.addValue((addFinalFound - addFinalExpected));
                }
            }
        }
        System.out.println("Progressive, Version " + version + " Sphere, " + result.format(dataSphere.getMean()) + " \u00B1 " + result.format(dataSphere.getStandardDeviation()));
        System.out.println("Progressive, Version " + version + " Cylinder, " + result.format(dataCylinder.getMean()) + " \u00B1 " + result.format(dataCylinder.getStandardDeviation()));
        System.out.println("Progressive, Version " + version + " Axis, " + result.format(dataAxis.getMean()) + " \u00B1 " + result.format(dataAxis.getStandardDeviation()));
        System.out.println("Progressive, Version " + version + " Add, " + result.format(dataAdd.getMean()) + " \u00B1 " + result.format(dataAdd.getStandardDeviation()));

    }

    @Test
    public void averageMeasurementsProgressive37() {
        int absentLens;
        ArrayList<Refraction> testGlassesRefraction = new ArrayList<>();

        for (absentLens = 0; absentLens <17; absentLens++){
            testGlassesRefraction.add(absentLens, new Refraction(0.0, 0.0, 0.0, 0.0, 0.0));      // absent Lenses
        }

        // Right Refractions
        testGlassesRefraction.add(17, new Refraction(6, -2.5, 63, 1.75, 66.0));
        testGlassesRefraction.add(18, new Refraction(-0.25, -0.25, 3, 1, 62.0));
        testGlassesRefraction.add(19, new Refraction(-1, -0.25, 175, 1, 61.0));
        testGlassesRefraction.add(20, new Refraction(-1.5, -0.5, 166, 1.25, 62.0));
        testGlassesRefraction.add(21, new Refraction(0.0, 0.0, 0.0, 0.0, 0.0));      // absent
        testGlassesRefraction.add(22, new Refraction(-0.25, 0, 0, 1, 63.0));
        testGlassesRefraction.add(23, new Refraction(0, 0, 0, 2, 63.0));
        testGlassesRefraction.add(24, new Refraction(0.75, -0.5, 17, 1.25, 58.0));
        testGlassesRefraction.add(25, new Refraction(1.25, -0.5, 11, 1, 60.0));
        testGlassesRefraction.add(26, new Refraction(2.25, -0.5, 19, 1, 61.0));
        testGlassesRefraction.add(27, new Refraction(3.25, 0, 0, 1.5, 63.0));
        // Left Refractions
        testGlassesRefraction.add(28, new Refraction(5, -1.25, 165, 1.75, 66.0));
        testGlassesRefraction.add(29, new Refraction(-0.5, -0.25, 6, 1, 62.0));
        testGlassesRefraction.add(30, new Refraction(0.75, -1, 15, 1, 61.0));
        testGlassesRefraction.add(31, new Refraction(-2, -0.5, 25, 1.25, 62.0));
        testGlassesRefraction.add(32, new Refraction(0.0, 0.0, 0.0, 0.0, 0.0));      // absent
        testGlassesRefraction.add(33, new Refraction(-1.25, -0.5, 29, 1, 63.0));
        testGlassesRefraction.add(34, new Refraction(0.25, 0, 0, 2, 63.0));
        testGlassesRefraction.add(35, new Refraction(1, -0.75, 150, 1.25, 58.0));
        testGlassesRefraction.add(36, new Refraction(1.5, -1, 157, 1, 60.0));
        testGlassesRefraction.add(37, new Refraction(2.75, -0.25, 178, 1, 61.0));
        testGlassesRefraction.add(38, new Refraction(0.5, -0.25, 6, 1.5, 63.0));

        byte[] frameZero = new byte[0], frameRightNear = new byte[0], frameRightDistance = new byte[0], frameLeftNear = new byte[0], frameLeftDistance = new byte[0];
        boolean isRightLens = true;
        int numberOfEyeglassesTotal = 28;
        int numberOfEyeglassesUsed = 11;
        Refraction rightRefraction, leftRefraction;

        DescriptiveStatistics dataSphere = new DescriptiveStatistics();
        DescriptiveStatistics dataCylinder = new DescriptiveStatistics();
        DescriptiveStatistics dataAxis = new DescriptiveStatistics();
        DescriptiveStatistics dataAdd = new DescriptiveStatistics();

        dataAxis.setWindowSize(numberOfEyeglassesUsed * 2);
        dataCylinder.setWindowSize(numberOfEyeglassesUsed * 2);
        dataSphere.setWindowSize(numberOfEyeglassesUsed * 2);
        dataAdd.setWindowSize(numberOfEyeglassesUsed * 2);
        DecimalFormat result = new DecimalFormat("+#0.00;-#0.00");

        int version = 37;  // choose Hardware Version!

        /* Testing lenses from 18 - 28 */
        double valueFinalExpected, valueFinalFound;

        int lensNumberL;         // R for Right Lens, L for Left Lens.
        nmActivity.setVersionFromNdefMessageForUnitTest(version);
        for (int lensNumberR = 0; lensNumberR < numberOfEyeglassesTotal; lensNumberR++) {
            if ((lensNumberR != 21) && (lensNumberR > 16) && (lensNumberR != 19)) {           // not using the absent lenses
                for (int frameNumber = 0; frameNumber < 3; frameNumber++) {      // different frames from same lenses
                    lensNumberL = lensNumberR + numberOfEyeglassesUsed;
                    try {
                        frameZero = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataPR/v%d/Frames%d/zero%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameRightNear = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataPR/v%d/Frames%d/rightNear%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameRightDistance = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataPR/v%d/Frames%d/rightDistance%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameLeftNear = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataPR/v%d/Frames%d/leftNear%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameLeftDistance = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataPR/v%d/Frames%d/leftDistance%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    processor.feedFrame(frameZero);
                    processor.generateZeroValues();

                    processor.isDistance = true;

                    processor.feedFrame(frameRightDistance);
                    processor.runGridFinder(isRightLens);

                    processor.isDistance = false;

                    processor.feedFrame(frameRightNear);
                    processor.runGridFinder(isRightLens);

                    rightRefraction = processor.calculatePrescription(isRightLens);

                    processor.isDistance = true;

                    processor.feedFrame(frameLeftDistance);
                    processor.runGridFinder(!isRightLens);

                    processor.isDistance = false;

                    processor.feedFrame(frameLeftNear);
                    processor.runGridFinder(!isRightLens);

                    leftRefraction = processor.calculatePrescription(!isRightLens);

                    dataSphere.addValue((rightRefraction.dSphere - testGlassesRefraction.get(lensNumberR).dSphere));
                    dataCylinder.addValue((rightRefraction.dCylinder - testGlassesRefraction.get(lensNumberR).dCylinder));
                    if ((testGlassesRefraction.get(lensNumberR).dAxis - rightRefraction.dAxis) > 90)
                        rightRefraction.dAxis = rightRefraction.dAxis + 180;
                    else if ((testGlassesRefraction.get(lensNumberR).dAxis - rightRefraction.dAxis) < -90)
                        rightRefraction.dAxis = rightRefraction.dAxis - 180;
                    if ((rightRefraction.dCylinder != 0) && (testGlassesRefraction.get(lensNumberR).dCylinder != 0)) {
                        dataAxis.addValue ((rightRefraction.dAxis - testGlassesRefraction.get(lensNumberR).dAxis));
                    }
                    valueFinalExpected = (testGlassesRefraction.get(lensNumberR).dAdd + testGlassesRefraction.get(lensNumberR).dSphere);
                    valueFinalFound = (rightRefraction.dAdd + rightRefraction.dSphere);
                    dataAdd.addValue(valueFinalFound - valueFinalExpected);

                    dataSphere.addValue((leftRefraction.dSphere - testGlassesRefraction.get(lensNumberL).dSphere));
                    dataCylinder.addValue((leftRefraction.dCylinder - testGlassesRefraction.get(lensNumberL).dCylinder));
                    if ((testGlassesRefraction.get(lensNumberL).dAxis - leftRefraction.dAxis) > 90)
                        leftRefraction.dAxis = leftRefraction.dAxis + 180;
                    else if ((testGlassesRefraction.get(lensNumberL).dAxis - leftRefraction.dAxis) < -90)
                        leftRefraction.dAxis = leftRefraction.dAxis - 180;
                    if ((leftRefraction.dCylinder != 0) && (testGlassesRefraction.get(lensNumberL).dCylinder != 0)) {
                        dataAxis.addValue((leftRefraction.dAxis - testGlassesRefraction.get(lensNumberL).dAxis));
                    }
                    valueFinalExpected = (testGlassesRefraction.get(lensNumberL).dAdd + testGlassesRefraction.get(lensNumberL).dSphere);
                    valueFinalFound = (leftRefraction.dAdd + leftRefraction.dSphere);
                    dataAdd.addValue((valueFinalFound - valueFinalExpected));
                }
            }
        }
        System.out.println("Progressive, Version " + version + " Sphere, " + result.format(dataSphere.getMean()) + " \u00B1 " + result.format(dataSphere.getStandardDeviation()));
        System.out.println("Progressive, Version " + version + " Cylinder, " + result.format(dataCylinder.getMean()) + " \u00B1 " + result.format(dataCylinder.getStandardDeviation()));
        System.out.println("Progressive, Version " + version + " Axis, " + result.format(dataAxis.getMean()) + " \u00B1 " + result.format(dataAxis.getStandardDeviation()));
        System.out.println("Progressive, Version " + version + " Add, " + result.format(dataAdd.getMean()) + " \u00B1 " + result.format(dataAdd.getStandardDeviation()));
    }

    public void calculatePDforTestsSingleVision(int lensID, double rightActualPD, double leftActualPD, int stepMillimeter, int firstMillimeter, int lastMillimeter) throws IOException {
        DecimalFormat StdDev = new DecimalFormat("#0.00;#0.00");
        DecimalFormat Mean = new DecimalFormat("+#0.00;-#0.00");
        DecimalFormat error = new DecimalFormat("+#0.00;-#0.00");

        ArrayList<Refraction> testGlassesForDiopterLeft = new ArrayList<>();
        ArrayList<Refraction> testGlassesForDiopterRight = new ArrayList<>();

        int framesID, lastFrameID = 2, actualMillimeter, relativeMillimeter;
        int failedLeftPD=0, failedRightPD=0, index =0;
        int totalFramesNumber = ((lastMillimeter - firstMillimeter)/stepMillimeter)+1;//+1 for '0', *2 because of double pictures
        String dataFilePath, leftFilePath, rightFilePath;
        byte[] frameZero, frameLeft, frameRight;
        Refraction leftPrescription, rightPrescription;
        DescriptiveStatistics leftPDData = new DescriptiveStatistics(), rightPDData = new DescriptiveStatistics();

        processor.isSkipLeftNear = false;
        processor.isSkipRightNear = false;
        processor.isSkipNear = false;

        boolean isLeftLens = false, isRightLens = true;
        leftPDData.setWindowSize(totalFramesNumber);
        rightPDData.setWindowSize(totalFramesNumber);
        System.out.println("Left lens millimeters, NE"+lensID);

        for (actualMillimeter = firstMillimeter; actualMillimeter <= lastMillimeter; actualMillimeter = actualMillimeter + stepMillimeter) {
            if(lensID>9){
                dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"NE%dleftzero.txt",lensID);
            }else dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"NE0%dleftzero.txt",lensID);

            relativeMillimeter = 12 - actualMillimeter;
            frameZero = ByteArrayReadWrite.readFromTestAssets(dataFilePath);
            processor.feedFrame(frameZero);
            processor.generateZeroValues();

            if(lensID>9) {
                leftFilePath = String.format(Locale.US, "PDSingleVision/v126/" + "NE%dleftframe%d.txt", lensID, actualMillimeter);
            }else leftFilePath = String.format(Locale.US, "PDSingleVision/v126/" + "NE0%dleftframe%d.txt", lensID, actualMillimeter);

            frameLeft = ByteArrayReadWrite.readFromTestAssets( leftFilePath);
            processor.isDistance = true;
            processor.feedFrame(frameLeft);
            processor.runGridFinder(isLeftLens);

            processor.isDistance = false;
            processor.feedFrame(frameLeft);
            processor.runGridFinder(isLeftLens);
            leftPrescription = processor.calculatePrescription(isLeftLens);
            if (leftPrescription!= null){
                if (leftPrescription.dPartialPD!=null){
//                    System.out.println(relativeMillimeter);
                    leftPDData.addValue(leftPrescription.dPartialPD);
                    testGlassesForDiopterLeft.add(leftPrescription);
                }
                else{
                    failedLeftPD++;
//                    System.out.println("error, " + relativeMillimeter + "mm,");
                }
            }
            else{
                failedLeftPD++;
//                System.out.println("error, " + relativeMillimeter + "mm,");
            }

        }
        System.out.println("Right lens millimeters");
        for (actualMillimeter = firstMillimeter; actualMillimeter <= lastMillimeter; actualMillimeter = actualMillimeter + stepMillimeter) {
            if(lensID>9){
                dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"NE%drightzero.txt",lensID);
            } else  dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"NE0%drightzero.txt",lensID);
            relativeMillimeter = 12 - actualMillimeter;
            frameZero = ByteArrayReadWrite.readFromTestAssets(dataFilePath);
            processor.feedFrame(frameZero);
            processor.generateZeroValues();


            if(lensID>9) {
                rightFilePath = String.format(Locale.US, "PDSingleVision/v126/" + "NE%drightframe%d.txt", lensID, actualMillimeter);
            }else rightFilePath = String.format(Locale.US, "PDSingleVision/v126/" + "NE0%drightframe%d.txt", lensID, actualMillimeter);

            frameRight = ByteArrayReadWrite.readFromTestAssets(rightFilePath);
            processor.isDistance = true;
            processor.feedFrame(frameRight);
            processor.runGridFinder(isRightLens);

            processor.isDistance = false;
            processor.feedFrame(frameRight);
            processor.runGridFinder(isRightLens);
            rightPrescription = processor.calculatePrescription(isRightLens);
            if (rightPrescription != null){
                if (rightPrescription.dPartialPD!=null){
//                    System.out.println(relativeMillimeter);
                    rightPDData.addValue(rightPrescription.dPartialPD);
                    testGlassesForDiopterRight.add(rightPrescription);
                }
                else{
                    failedRightPD++;
//                    System.out.println("error, " + relativeMillimeter + "mm,");
                }
            }
            else{
                failedRightPD++;
//                System.out.println("error, " + relativeMillimeter + "mm,");
            }
        }
        System.out.println("Left Lenses, Partial PD");
        for (index = 0; index < (totalFramesNumber - failedLeftPD); index ++){
            System.out.println(error.format(leftPDData.getElement(index)));
        }
        System.out.println("Right Lenses, Partial PD");
        for (index = 0; index < (totalFramesNumber - failedRightPD); index ++){
            System.out.println(error.format(rightPDData.getElement(index)));
        }
        int validLenses;
        validLenses = failedLeftPD>failedRightPD ? (totalFramesNumber-failedLeftPD) : (totalFramesNumber-failedRightPD);
        System.out.println("Complete PD Lens number NE" + lensID);
        for (index = 0; index < (validLenses); index ++){
            System.out.println(error.format(rightPDData.getElement(index) + leftPDData.getElement(index)));
        }
        System.out.println("Left Lenses, Diopter value");
        for (index = 0; index < (validLenses); index ++){
            System.out.println(error.format(testGlassesForDiopterLeft.get(index).calculateDiopterProjection(0)));
        }
        System.out.println("Right Lenses, Diopter value");
        for (index = 0; index < (validLenses); index ++){
            System.out.println(error.format(testGlassesForDiopterRight.get(index).calculateDiopterProjection(0)));
        }

        System.out.println(failedLeftPD + "# Errors (NullPD) || " + "Left NE_" + lensID + ", " + Mean.format(leftPDData.getMean()) + "\u00B1" + StdDev.format(leftPDData.getStandardDeviation()));
        System.out.println(failedRightPD + "# Errors (NullPD) || " + "Right NE_" + lensID + ", " + Mean.format(rightPDData.getMean()) + "\u00B1" + StdDev.format(rightPDData.getStandardDeviation()));
    }


    @Test
    public void calculatePD_NE_01_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 1, stepMillimeter = 1, firstMillimeter = 8, lastMillimiter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimiter);
    }

    @Test
    public void calculatePD_NE_02_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 2, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_04_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 4, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_05_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 5, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }
    @Test
    public void calculatePD_NE_06_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 06, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 32.5, rightActualPD = 32;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_07_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 7, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_08_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 8, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_09_Alyssa() throws IOException {
        int lensID = 9, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 32.5, rightActualPD = 32;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_10_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 10, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_11_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 11, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_12_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 12, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 30.5, rightActualPD = 33.0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_13_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 13, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_14_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 14, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_15_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 15, stepMillimeter = 1, firstMillimeter = 8, lastMillimiter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimiter);
    }

    @Test
    public void calculatePD_NE_16_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 16, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_17_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 17, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 35.0, rightActualPD = 32.0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }
    @Test
    public void calculatePD_NE_19_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 19, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    } @Test
    public void calculatePD_NE_20_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 20, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    } @Test
    public void calculatePD_NE_21_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 21, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0, rightActualPD =0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }
    @Test
    public void calculatePD_NE_22_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 22, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 31.0, rightActualPD = 30.5;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

    @Test
    public void calculatePD_NE_24_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 24, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 0.0, rightActualPD = 0.0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }
    @Test
    public void calculatePD_NE_25_Alyssa() throws IOException {
        nmActivity.setVersionFromNdefMessageForUnitTest(126);
        int lensID = 25, stepMillimeter = 1, firstMillimeter = 8, lastMillimeter=16;
        double leftActualPD = 33.0, rightActualPD = 33.0;
        calculatePDforTestsSingleVision(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimeter);
    }

}

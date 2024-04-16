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

import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.imageprocessing.utils.FittingCoefficients;
import com.vitorpamplona.netrometer.imageprocessing.utils.ImageProcessingUtil;
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
public class SingleVisionImageProcessingTest {

    SingleVisionImageProcessing processor;
    private NetrometerActivity nmActivity;

    @Before
    public void setUp() {
        ActivityController<NetrometerActivity> controller = Robolectric.buildActivity(NetrometerActivity.class);
        nmActivity = controller.get();
        controller.create().resume();

        processor = new SingleVisionImageProcessing(nmActivity.getApp().getDevice());
    }

    public FittingCoefficients getFittingCoefficients() {
        return new FittingCoefficients(
                Params.FITTING_COEFFICIENTS_A,
                Params.FITTING_COEFFICIENTS_B,
                Params.FITTING_COEFFICIENTS_C,
                Params.FITTING_COEFFICIENTS_D
        );
    }

    @Test
    public void runSingleVisionTest() throws IOException {
        byte[] frameZero = ByteArrayReadWrite.readFromTestAssets("NMIM001.txt");
        byte[] frameLens = ByteArrayReadWrite.readFromTestAssets("NMIM003.txt");

        processor.feedFrame(frameZero);
        processor.generateZeroValues();

        processor.feedFrame(frameLens);
        processor.runGridFinder(true);

        Refraction prescription = ImageProcessingUtil.convertToPrescription(
                ImageProcessingUtil.resultsToSinusoidal(
                        processor.runGridDebug(),
                        processor.getZero(),
                        Params.NEIGHBORS, nmActivity.getApp().getDevice())
        );

        assertEquals(-3.25, prescription.dSphere, 0.25);
        assertEquals(-0.50, prescription.dCylinder, 0.25);
        assertEquals(84, prescription.dAxis, 10);

    }

    @Test
    public void runFullNetrometerTest() throws IOException {
        byte[] frameZero = ByteArrayReadWrite.readFromTestAssets("NMIM001.txt");
        byte[] frameRight = ByteArrayReadWrite.readFromTestAssets("NMIM003.txt");
        byte[] frameLeft = ByteArrayReadWrite.readFromTestAssets("NMIM004.txt");

        processor.feedFrame(frameZero);
        processor.generateZeroValues();

        processor.feedFrame(frameLeft);
        processor.runGridFinder(false);

        processor.feedFrame(frameRight);
        processor.runGridFinder(true);

        Refraction rightPrescription = processor.calculatePrescription(true);
        Refraction leftPrescription = processor.calculatePrescription(false);

        assertEquals(-3.25, rightPrescription.dSphere, 0.25);
        assertEquals(-0.50, rightPrescription.dCylinder, 0.25);
        assertEquals(84, rightPrescription.dAxis, 10);


        assertEquals(-0.00, leftPrescription.dSphere, 0.25);
        assertEquals(-0.00, leftPrescription.dCylinder, 0.25);
        if (leftPrescription.dCylinder == 0) {
            assertEquals(0, leftPrescription.dAxis, 10);
        }
    }

    @Test
    public void imageProcessingTimeTest() throws IOException {

        byte[] frameZero = ByteArrayReadWrite.readFromTestAssets("NMIM001.txt");
        byte[] frameLens = ByteArrayReadWrite.readFromTestAssets("NMIM003.txt");
        long timestamp = System.currentTimeMillis();


        processor.feedFrame(frameZero);
        processor.generateZeroValues();

        processor.feedFrame(frameLens);
        processor.runGridFinder(true);

        Refraction prescription = ImageProcessingUtil.convertToPrescription(
                ImageProcessingUtil.resultsToSinusoidal(
                        processor.runGridDebug(),
                        processor.getZero(),
                        Params.NEIGHBORS, nmActivity.getApp().getDevice())
        );

        assertTrue(System.currentTimeMillis() - timestamp < 90000);
    }

    @Test
    public void averageMeasurements37() {

        // Right Refractions
        ArrayList<Refraction> testGlassesRefraction = new ArrayList<>();
        testGlassesRefraction.add(0, new Refraction(-4.25, -0.25, 5));
        testGlassesRefraction.add(1, new Refraction(-1, -0.5, 50));
        testGlassesRefraction.add(2, new Refraction(-0.75, -0.75, 159));
        testGlassesRefraction.add(3, new Refraction(2, -1, 105));
        testGlassesRefraction.add(4, new Refraction(-1.5, -0.5, 160));
        testGlassesRefraction.add(5, new Refraction(-3.75, 0, 0));
        testGlassesRefraction.add(6, new Refraction(-7.25, -0.5, 12));
        testGlassesRefraction.add(7, new Refraction(-4, -1.5, 98));
        testGlassesRefraction.add(8, new Refraction(-3.75, 0, 0));
        testGlassesRefraction.add(9, new Refraction(-0.5, -1, 97));
        testGlassesRefraction.add(10, new Refraction(7.5, -1.25, 94));
        testGlassesRefraction.add(11, new Refraction(1.75, -4, 35));
        testGlassesRefraction.add(12, new Refraction(2.5, -0.5, 74));
        testGlassesRefraction.add(13, new Refraction(0.5, 0, 0));
        testGlassesRefraction.add(14, new Refraction(-2.25, 0, 0));
        testGlassesRefraction.add(15, new Refraction(-3.25, -0.5, 84));
        testGlassesRefraction.add(16, new Refraction(1.5, -1, 174));
        testGlassesRefraction.add(17, new Refraction(-6, -4, 89));
        testGlassesRefraction.add(18, new Refraction(-0.25, -0.75, 3));
        testGlassesRefraction.add(19, new Refraction(-5.75, -1.75, 168));
        testGlassesRefraction.add(20, new Refraction(0.5, -0.5, 148));
        testGlassesRefraction.add(21, new Refraction(2.25, 0, 0));
        testGlassesRefraction.add(22, new Refraction(4.75, -1, 94));
        testGlassesRefraction.add(23, new Refraction(1.75, -1, 10));
        testGlassesRefraction.add(24, new Refraction(-3, -2, 149));

        // Left Refractions
        testGlassesRefraction.add(25, new Refraction(-4.5, 0, 0));
        testGlassesRefraction.add(26, new Refraction(-1, -1, 116));
        testGlassesRefraction.add(27, new Refraction(0, -1.25, 22));
        testGlassesRefraction.add(28, new Refraction(1.25, -0.5, 68));
        testGlassesRefraction.add(29, new Refraction(-1.5, -0.5, 41));
        testGlassesRefraction.add(30, new Refraction(-3.5, 0, 0));
        testGlassesRefraction.add(31, new Refraction(-8, -0.75, 155));
        testGlassesRefraction.add(32, new Refraction(-4.5, -0.5, 91));
        testGlassesRefraction.add(33, new Refraction(-3.5, -0.5, 170));
        testGlassesRefraction.add(34, new Refraction(-0.5, -0.75, 79));
        testGlassesRefraction.add(35, new Refraction(6.5, -0.25, 114));
        testGlassesRefraction.add(36, new Refraction(-5.25, -0.25, 101));
        testGlassesRefraction.add(37, new Refraction(3.25, -0.25, 87));
        testGlassesRefraction.add(38, new Refraction(0.5, 0, 0));
        testGlassesRefraction.add(39, new Refraction(-2.75, -2.25, 97));
        testGlassesRefraction.add(40, new Refraction(0, 0, 0));
        testGlassesRefraction.add(41, new Refraction(-4.5, -1.5, 2));
        testGlassesRefraction.add(42, new Refraction(4, 0, 0));
        testGlassesRefraction.add(43, new Refraction(-0.25, -0.75, 160));
        testGlassesRefraction.add(44, new Refraction(-5.75, -1, 2));
        testGlassesRefraction.add(45, new Refraction(1.25, -0.75, 41));
        testGlassesRefraction.add(46, new Refraction(1.25, 0, 0));
        testGlassesRefraction.add(47, new Refraction(3.75, -0.25, 109));
        testGlassesRefraction.add(48, new Refraction(3.75, -2, 164));
        testGlassesRefraction.add(49, new Refraction(-2, -3, 12));


        byte[] frameZero = new byte[0], frameRight = new byte[0], frameLeft = new byte[0];
        boolean isRightLens = true;
        int numberOfEyeglasses =25;
        Refraction rightRefraction, leftRefraction;

        DescriptiveStatistics dataSphere = new DescriptiveStatistics();
        DescriptiveStatistics dataCylinder = new DescriptiveStatistics();
        DescriptiveStatistics dataAxis = new DescriptiveStatistics();

        dataAxis.setWindowSize(numberOfEyeglasses * 2);
        dataCylinder.setWindowSize(numberOfEyeglasses * 2);
        dataSphere.setWindowSize(numberOfEyeglasses * 2);
        DecimalFormat result = new DecimalFormat("#0.00;-#0.00");

        int version = 37;  // choose Hardware Version!

        int lensNumberL;         // R for Right Lens, L for Left Lens.
        nmActivity.setVersionFromNdefMessageForUnitTest(version);
        for (int lensNumberR = 0; lensNumberR < numberOfEyeglasses; lensNumberR++) {
            if ((lensNumberR != 2) && (lensNumberR != 17)) {           // not using the void files

                System.out.println("Testing lens " + lensNumberR);

                for (int frameNumber = 0; frameNumber < 3; frameNumber++) {      // different frames from same lenses
                    lensNumberL = lensNumberR + numberOfEyeglasses;
                    try {
                        frameZero = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataSV/v%d/Frames%d/ZeroFrame%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameRight = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataSV/v%d/Frames%d/SingleVisionRight%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameLeft = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataSV/v%d/Frames%d/SingleVisionLeft%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    processor.feedFrame(frameZero);
                    processor.generateZeroValues();

                    processor.feedFrame(frameLeft);
                    processor.runGridFinder(!isRightLens);

                    processor.feedFrame(frameRight);
                    processor.runGridFinder(isRightLens);

                    rightRefraction = processor.calculatePrescription(isRightLens);
                    leftRefraction = processor.calculatePrescription(!isRightLens);

                    dataSphere.addValue((testGlassesRefraction.get(lensNumberR).dSphere - rightRefraction.dSphere));
                    dataCylinder.addValue((testGlassesRefraction.get(lensNumberR).dCylinder - rightRefraction.dCylinder));
                    if ((testGlassesRefraction.get(lensNumberR).dAxis - rightRefraction.dAxis) > 90)
                        rightRefraction.dAxis = rightRefraction.dAxis + 180;
                    else if ((testGlassesRefraction.get(lensNumberR).dAxis - rightRefraction.dAxis) < -90)
                        rightRefraction.dAxis = rightRefraction.dAxis - 180;
                    if ((rightRefraction.dCylinder != 0) && (testGlassesRefraction.get(lensNumberR).dCylinder != 0)) {
                        dataAxis.addValue((testGlassesRefraction.get(lensNumberR).dAxis - rightRefraction.dAxis));
                    }

                    dataSphere.addValue((testGlassesRefraction.get(lensNumberL).dSphere - leftRefraction.dSphere));
                    dataCylinder.addValue((testGlassesRefraction.get(lensNumberL).dCylinder - leftRefraction.dCylinder));
                    if ((testGlassesRefraction.get(lensNumberL).dAxis - leftRefraction.dAxis) > 90)
                        leftRefraction.dAxis = leftRefraction.dAxis + 180;
                    else if ((testGlassesRefraction.get(lensNumberL).dAxis - leftRefraction.dAxis) < -90)
                        leftRefraction.dAxis = leftRefraction.dAxis - 180;
                    if ((leftRefraction.dCylinder != 0) && (testGlassesRefraction.get(lensNumberL).dCylinder != 0)) {
                        dataAxis.addValue((testGlassesRefraction.get(lensNumberL).dAxis - leftRefraction.dAxis));
                    }
                }
            }
        }
        System.out.println("Single Vision, Version " + version + " Mean - Sphere, " + result.format(dataSphere.getMean()) + " \u00B1 " + result.format(dataSphere.getStandardDeviation()));
        System.out.println("Single Vision, Version " + version + " Mean - Cylinder, " + result.format(dataCylinder.getMean()) + " \u00B1 " + result.format(dataCylinder.getStandardDeviation()));
        System.out.println("Single Vision, Version " + version + " Mean - Axis, " + result.format(dataAxis.getMean()) + " \u00B1 " + result.format(dataAxis.getStandardDeviation()));
    }
    @Test
    public void averageMeasurements126() {

        // Right Refractions
        ArrayList<Refraction> testGlassesRefraction = new ArrayList<>();
        testGlassesRefraction.add(0, new Refraction(-4.25, -0.25, 5));
        testGlassesRefraction.add(1, new Refraction(-1, -0.5, 50));
        testGlassesRefraction.add(2, new Refraction(-0.75, -0.75, 159));
        testGlassesRefraction.add(3, new Refraction(2, -1, 105));
        testGlassesRefraction.add(4, new Refraction(-1.5, -0.5, 160));
        testGlassesRefraction.add(5, new Refraction(-3.75, 0, 0));
        testGlassesRefraction.add(6, new Refraction(-7.25, -0.5, 12));
        testGlassesRefraction.add(7, new Refraction(-4, -1.5, 98));
        testGlassesRefraction.add(8, new Refraction(-3.75, 0, 0));
        testGlassesRefraction.add(9, new Refraction(-0.5, -1, 97));
        testGlassesRefraction.add(10, new Refraction(7.5, -1.25, 94));
        testGlassesRefraction.add(11, new Refraction(1.75, -4, 35));
        testGlassesRefraction.add(12, new Refraction(2.5, -0.5, 74));
        testGlassesRefraction.add(13, new Refraction(0.5, 0, 0));
        testGlassesRefraction.add(14, new Refraction(-2.25, 0, 0));
        testGlassesRefraction.add(15, new Refraction(-3.25, -0.5, 84));
        testGlassesRefraction.add(16, new Refraction(1.5, -1, 174));
        testGlassesRefraction.add(17, new Refraction(-6, -4, 89));
        testGlassesRefraction.add(18, new Refraction(-0.25, -0.75, 3));
        testGlassesRefraction.add(19, new Refraction(-5.75, -1.75, 168));
        testGlassesRefraction.add(20, new Refraction(0.5, -0.5, 148));
        testGlassesRefraction.add(21, new Refraction(2.25, 0, 0));
        testGlassesRefraction.add(22, new Refraction(4.75, -1, 94));
        testGlassesRefraction.add(23, new Refraction(1.75, -1, 10));
        testGlassesRefraction.add(24, new Refraction(-3, -2, 149));

        // Left Refractions
        testGlassesRefraction.add(25, new Refraction(-4.5, 0, 0));
        testGlassesRefraction.add(26, new Refraction(-1, -1, 116));
        testGlassesRefraction.add(27, new Refraction(0, -1.25, 22));
        testGlassesRefraction.add(28, new Refraction(1.25, -0.5, 68));
        testGlassesRefraction.add(29, new Refraction(-1.5, -0.5, 41));
        testGlassesRefraction.add(30, new Refraction(-3.5, 0, 0));
        testGlassesRefraction.add(31, new Refraction(-8, -0.75, 155));
        testGlassesRefraction.add(32, new Refraction(-4.5, -0.5, 91));
        testGlassesRefraction.add(33, new Refraction(-3.5, -0.5, 170));
        testGlassesRefraction.add(34, new Refraction(-0.5, -0.75, 79));
        testGlassesRefraction.add(35, new Refraction(6.5, -0.25, 114));
        testGlassesRefraction.add(36, new Refraction(-5.25, -0.25, 101));
        testGlassesRefraction.add(37, new Refraction(3.25, -0.25, 87));
        testGlassesRefraction.add(38, new Refraction(0.5, 0, 0));
        testGlassesRefraction.add(39, new Refraction(-2.75, -2.25, 97));
        testGlassesRefraction.add(40, new Refraction(0, 0, 0));
        testGlassesRefraction.add(41, new Refraction(-4.5, -1.5, 2));
        testGlassesRefraction.add(42, new Refraction(4, 0, 0));
        testGlassesRefraction.add(43, new Refraction(-0.25, -0.75, 160));
        testGlassesRefraction.add(44, new Refraction(-5.75, -1, 2));
        testGlassesRefraction.add(45, new Refraction(1.25, -0.75, 41));
        testGlassesRefraction.add(46, new Refraction(1.25, 0, 0));
        testGlassesRefraction.add(47, new Refraction(3.75, -0.25, 109));
        testGlassesRefraction.add(48, new Refraction(3.75, -2, 164));
        testGlassesRefraction.add(49, new Refraction(-2, -3, 12));


        byte[] frameZero = new byte[0], frameRight = new byte[0], frameLeft = new byte[0];
        boolean isRightLens = true;
        int numberOfEyeglasses = 25;
        Refraction rightRefraction, leftRefraction;

        DescriptiveStatistics dataSphere = new DescriptiveStatistics();
        DescriptiveStatistics dataCylinder = new DescriptiveStatistics();
        DescriptiveStatistics dataAxis = new DescriptiveStatistics();

        dataAxis.setWindowSize(numberOfEyeglasses * 2);
        dataCylinder.setWindowSize(numberOfEyeglasses * 2);
        dataSphere.setWindowSize(numberOfEyeglasses * 2);

        DecimalFormat result = new DecimalFormat("#0.00;-#0.00");

        int version = 126;  // choose Hardware Version!

        /* Testing lenses from 1 - 25 */

        int lensNumberL;         // R for Right Lens, L for Left Lens.
        nmActivity.setVersionFromNdefMessageForUnitTest(version);
        for (int lensNumberR = 0; lensNumberR < numberOfEyeglasses; lensNumberR++) {

            System.out.println("Testing lens " + lensNumberR);

            if ((lensNumberR != 2) && (lensNumberR != 17) && (lensNumberR != 9) && (lensNumberR != 24)) {           // not using the void files
                for (int frameNumber = 0; frameNumber < 3; frameNumber++) {      // different frames from same lenses
                    lensNumberL = lensNumberR + numberOfEyeglasses;
                    try {
                        frameZero = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataSV/v%d/Frames%d/ZeroFrame%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameRight = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataSV/v%d/Frames%d/SingleVisionRight%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        frameLeft = ByteArrayReadWrite.readFromTestAssets(String.format("LensDataSV/v%d/Frames%d/SingleVisionLeft%d.txt", version, lensNumberR, frameNumber));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    processor.feedFrame(frameZero);
                    processor.generateZeroValues();

                    processor.feedFrame(frameLeft);
                    processor.runGridFinder(!isRightLens);

                    processor.feedFrame(frameRight);
                    processor.runGridFinder(isRightLens);

                    rightRefraction = processor.calculatePrescription(isRightLens);
                    leftRefraction = processor.calculatePrescription(!isRightLens);

                    dataSphere.addValue((rightRefraction.dSphere - testGlassesRefraction.get(lensNumberR).dSphere));
                    dataCylinder.addValue((rightRefraction.dCylinder - testGlassesRefraction.get(lensNumberR).dCylinder));
                    if ((testGlassesRefraction.get(lensNumberR).dAxis - rightRefraction.dAxis) > 90)
                        rightRefraction.dAxis = rightRefraction.dAxis + 180;
                    else if ((testGlassesRefraction.get(lensNumberR).dAxis - rightRefraction.dAxis) < -90)
                        rightRefraction.dAxis = rightRefraction.dAxis - 180;
                    if ((rightRefraction.dCylinder != 0) && (testGlassesRefraction.get(lensNumberR).dCylinder != 0)) {
                        dataAxis.addValue((rightRefraction.dAxis - testGlassesRefraction.get(lensNumberR).dAxis));
                    }

                    dataSphere.addValue((leftRefraction.dSphere - testGlassesRefraction.get(lensNumberL).dSphere));
                    dataCylinder.addValue((leftRefraction.dCylinder - testGlassesRefraction.get(lensNumberL).dCylinder));
                    if ((testGlassesRefraction.get(lensNumberL).dAxis - leftRefraction.dAxis) > 90)
                        leftRefraction.dAxis = leftRefraction.dAxis + 180;
                    else if ((testGlassesRefraction.get(lensNumberL).dAxis - leftRefraction.dAxis) < -90)
                        leftRefraction.dAxis = leftRefraction.dAxis - 180;
                    if ((leftRefraction.dCylinder != 0) && (testGlassesRefraction.get(lensNumberL).dCylinder != 0)) {
                        dataAxis.addValue((leftRefraction.dAxis - testGlassesRefraction.get(lensNumberL).dAxis));
                    }
                }
            }
        }

        System.out.println("Single Vision, Version " + version + " Mean - Sphere, " + result.format(dataSphere.getMean()) + " \u00B1 " + result.format(dataSphere.getStandardDeviation()));
        System.out.println("Single Vision, Version " + version + " Mean - Cylinder, " + result.format(dataCylinder.getMean()) + " \u00B1 " + result.format(dataCylinder.getStandardDeviation()));
        System.out.println("Single Vision, Version " + version + " Mean - Axis, " + result.format(dataAxis.getMean()) + " \u00B1 " + result.format(dataAxis.getStandardDeviation()));
    }

    public void calculatePDforTests(int lensID, double rightActualPD, double leftActualPD, int stepMillimeter, int firstMillimeter, int lastMilimiter) throws IOException {
        DecimalFormat StdDev = new DecimalFormat("#0.00;#0.00");
        DecimalFormat Mean = new DecimalFormat("+#0.00;-#0.00");
        DecimalFormat error = new DecimalFormat("+#0.00;-#0.00");

        ArrayList<Refraction> testGlassesForDiopterLeft = new ArrayList<>();
        ArrayList<Refraction> testGlassesForDiopterRight = new ArrayList<>();

        int framesID, lastFrameID = 2, actualMillimeter, relativeMillimeter, lastMillimeter = 16;
        int failedLeftPD=0, failedRightPD=0, index =0;
        int totalFramesNumber = ((lastMillimeter - firstMillimeter)/stepMillimeter+1)*2;//+1 for '0', *2 because of double pictures
        String dataFilePath, leftFilePath, rightFilePath;
        byte[] frameZero, frameLeft, frameRight;
        Refraction leftPrescription, rightPrescription;
        DescriptiveStatistics leftPDData = new DescriptiveStatistics(), rightPDData = new DescriptiveStatistics();

        boolean isLeftLens = false, isRightLens = true;
        leftPDData.setWindowSize(totalFramesNumber);
        rightPDData.setWindowSize(totalFramesNumber);
        System.out.println("Left lens ID, NE"+lensID);

        for (actualMillimeter = firstMillimeter; actualMillimeter <= lastMillimeter; actualMillimeter = actualMillimeter + stepMillimeter) {
//            dataFilePath = String.format(Locale.US, "ProgressiveSmartStageData/NE %d/LeftLens/%dmm/",lensID, actualMillimeter);
//            if(lensID>9){
//                dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"NE%dleftzero.txt",lensID);
//            }else dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"NE0%dleftzero.txt",lensID);
            if(lensID>9){
                dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"NE%dleftzero.txt",lensID);
            } else dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"NE0%dleftzero.txt",lensID);
            System.out.println("left "+dataFilePath);
            relativeMillimeter = actualMillimeter- 12 ;
            frameZero = ByteArrayReadWrite.readFromTestAssets(dataFilePath);
            processor.feedFrame(frameZero);
//            processor.generateZeroValues();


            if(lensID>9) {
                leftFilePath = String.format(Locale.US, "PDSingleVision/v126/" + "NE%dleftframe%d.txt", lensID, actualMillimeter);
            }else leftFilePath = String.format(Locale.US, "PDSingleVision/v126/" + "NE0%dleftframe%d.txt", lensID, actualMillimeter);
            System.out.println("left "+ leftFilePath);
            frameLeft = ByteArrayReadWrite.readFromTestAssets(leftFilePath);
            processor.feedFrame(frameLeft);
            processor.runGridFinder(isRightLens);

;
            if (processor.calculatePrescription(isLeftLens) != null){
                leftPrescription = processor.calculatePrescription(isLeftLens);
                if (leftPrescription.dPartialPD!=null){
                    System.out.println(relativeMillimeter);
                    leftPDData.addValue(leftPrescription.dPartialPD);
                    testGlassesForDiopterLeft.add(leftPrescription);
                }
                else{
                    failedLeftPD++;
                    System.out.println("error, " + relativeMillimeter + "mm,");
                }
            }
            else{
                failedLeftPD++;
                System.out.println("error, " + relativeMillimeter + "mm,");
            }

        }
        System.out.println("Right lens millimeters");
        for (actualMillimeter = firstMillimeter; actualMillimeter <= lastMillimeter; actualMillimeter = actualMillimeter + stepMillimeter) {
//            dataFilePath = String.format(Locale.US, "PDSingleVision/v126/");
            if(lensID>9){
                dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"NE%drightzero.txt",lensID);
            } else  dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"NE0%drightzero.txt",lensID);
//            dataFilePath = String.format(Locale.US, "PDSingleVision/v126/" +"zero.txt",lensID);

            relativeMillimeter = actualMillimeter- 12 ;
            frameZero = ByteArrayReadWrite.readFromTestAssets(dataFilePath);
            processor.feedFrame(frameZero);
            processor.generateZeroValues();


            if(lensID>9) {
                rightFilePath = String.format(Locale.US, "PDSingleVision/v126/" + "NE%drightframe%d.txt", lensID, actualMillimeter);
            }else rightFilePath = String.format(Locale.US, "PDSingleVision/v126/" + "NE0%drightframe%d.txt", lensID, actualMillimeter);
            frameRight = ByteArrayReadWrite.readFromTestAssets(rightFilePath);
            processor.feedFrame(frameRight);
            processor.runGridFinder(isRightLens);

            if (processor.calculatePrescription(isRightLens) != null){
                rightPrescription = processor.calculatePrescription(isRightLens);
                if (rightPrescription.dPartialPD!=null){
                    System.out.println(relativeMillimeter);
                    rightPDData.addValue(rightPrescription.dPartialPD);
                    testGlassesForDiopterRight.add(rightPrescription);
                }
                else{
                    failedRightPD++;
                    System.out.println("error, " + relativeMillimeter + "mm,");
                }
            }
            else{
                failedRightPD++;
                System.out.println("error, " + relativeMillimeter + "mm,");
            }

        }
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
//            System.out.println(error.format(calculateDiopterValueAt180(testGlassesForDiopterLeft.get(index))));
//        }
//        System.out.println("Right Lenses, Diopter value");
//        for (index = 0; index < (validLenses); index ++){
//            System.out.println(error.format(calculateDiopterValueAt180(testGlassesForDiopterRight.get(index))));
//        }
//
//        System.out.println(failedLeftPD + "# Errors (NullPD) || " + "Left NE_" + lensID + ", " + Mean.format(leftPDData.getMean()) + "\u00B1" + StdDev.format(leftPDData.getStandardDeviation()));
//        System.out.println(failedRightPD + "# Errors (NullPD) || " + "Right NE_" + lensID + ", " + Mean.format(rightPDData.getMean()) + "\u00B1" + StdDev.format(rightPDData.getStandardDeviation()));
    }

    @Test
    public void calculatePD_NE_09() throws IOException {
        int lensID = 9, stepMillimeter = 1, firstMillimeter = 8, lastMillimiter=16;
        double leftActualPD = 32.5, rightActualPD = 32;
        calculatePDforTests(lensID,rightActualPD,leftActualPD,stepMillimeter,firstMillimeter, lastMillimiter);
    }

}
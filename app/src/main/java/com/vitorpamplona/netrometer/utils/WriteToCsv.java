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

import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriteToCsv {

    private static final String DIRECTORY = "/debug";
    private static final String EXTENSION = ".csv";

    private String fileName;
    private File mFileX, mFileY, mFileValid;
    private CSVWriter mWriterX, mWriterY, mWriterValid;

    public WriteToCsv(String f) {

        fileName = f;

        load();
    }

    public boolean load() {
        File root = new File(NetrometerApplication.get().getLocalToExportPath());
        File dir = new File (root.getAbsolutePath() + DIRECTORY);
        dir.mkdirs();

        mFileX = new File(dir, fileName+"X"+EXTENSION);
        mFileY = new File(dir, fileName+"Y"+EXTENSION);
        mFileValid = new File(dir, fileName+"Valid"+EXTENSION);

        try {
            mWriterX = new CSVWriter(new FileWriter(mFileX));
            mWriterY = new CSVWriter(new FileWriter(mFileY));
            mWriterValid = new CSVWriter(new FileWriter(mFileValid));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            mWriterX.close();
            mWriterY.close();
            mWriterValid.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveGridToFile(GridResult result) {

        List<String[]> dataX = new ArrayList<String[]>();
        List<String[]> dataY = new ArrayList<String[]>();
        List<String[]> dataValid = new ArrayList<String[]>();

        for (int row = 0; row < result.pointsOnGrid.length; row++) {

            String[] rowX = new String[result.pointsOnGrid.length];
            String[] rowY = new String[result.pointsOnGrid.length];
            String[] rowValid = new String[result.pointsOnGrid.length];

            for (int column = 0; column < result.pointsOnGrid[row].length; column++) {
                rowX[column] = String.valueOf(result.pointsOnGrid[row][column].x);
                rowY[column] = String.valueOf(result.pointsOnGrid[row][column].y);
                rowValid[column] = String.valueOf(result.pointsOnGrid[row][column].isValid);
            }

            dataX.add(rowX);
            dataY.add(rowY);
            dataValid.add(rowValid);
        }

        mWriterX.writeAll(dataX, false);
        mWriterY.writeAll(dataY, false);
        mWriterValid.writeAll(dataValid,false);

        close();
    }
}

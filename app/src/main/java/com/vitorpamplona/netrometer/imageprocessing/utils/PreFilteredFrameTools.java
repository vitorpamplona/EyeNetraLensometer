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


import android.util.Log;

import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.imageprocessing.model.Point2DGrid;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Clock;
import com.vitorpamplona.netrometer.utils.Point2D;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;

public class PreFilteredFrameTools {

	public PreFilteredFrameTools() {
	}

    protected boolean isGradientUp(int[][] data, Point2D refinedPoint) {

        int searchX = 4;
        int searchY = 10;
        int sumGradient = 0;
        int count = 0;
        int sumGradientPre = 0;
        int countPre = 0;

        for (int x = (int) refinedPoint.x - searchX; x < refinedPoint.x + searchX; x++) {
            for (int y = (int) refinedPoint.y - searchY; y < refinedPoint.y + searchY; y++) {
                if (x - 4 > 0 && y > 0 && x + 4 < Params.PREVIEW_FRAME_WIDTH && y < Params.PREVIEW_FRAME_HEIGHT) {
                    int Yval = data[x+1][y];    // 0xff & (int) frame.getY(data, x + 1, y);
                    int YvalPre = data[x-1][y]; // 0xff & (int) frame.getY(data, x - 1, y);
                    sumGradient = Math.abs(Yval - YvalPre) + sumGradient;
                    count++;
//                    Log.e("FrameTools", "delta frame: "+Math.abs(Yval - YvalPre));
                }
            }
        }

        for (int x = (int) refinedPoint.x + 18 * searchX; x < refinedPoint.x + 20 * searchX; x++) {
            for (int y = (int) refinedPoint.y - searchY; y < refinedPoint.y + searchY; y++) {
                if (x - 4 > 0 && y > 0 && x + 4 < Params.PREVIEW_FRAME_WIDTH && y < Params.PREVIEW_FRAME_HEIGHT) {
                    int Yval = data[x+1][y];    // 0xff & (int) frame.getY(data, x + 1, y);
                    int YvalPre = data[x-1][y]; // 0xff & (int) frame.getY(data, x - 1, y);
                    sumGradientPre = Math.abs(Yval - YvalPre) + sumGradientPre;
                    countPre++;
                }
            }
        }
//        Log.e("FrameTools","refined ("+refinedPoint.x+", "+refinedPoint.y+") , isGradient: "+ (sumGradient/(count+0.000000001))+", "+ sumGradient+ "/ "+count+"isGradientPre: "+ (sumGradientPre/(countPre+0.000000001))+", "+ sumGradientPre+ "/ "+countPre);

        if(sumGradient/(count+0.000000001) < 0.96*sumGradientPre/(countPre+0.000000001)){
            return true;
        } else return false;
    }

    protected boolean isGradientDown(int[][] data, Point2D refinedPoint) {

        int searchX = 4;
        int searchY = 10;
        int sumGradient = 0;
        int count = 0;
        int sumGradientPre = 0;
        int countPre =0;

        for(int x=(int)refinedPoint.x-searchX; x< refinedPoint.x+searchX; x++){
            for(int y=(int)refinedPoint.y-searchY; y< refinedPoint.y+searchY; y++){
                if(x-4>0 && y>0 && x+4< Params.PREVIEW_FRAME_WIDTH && y< Params.PREVIEW_FRAME_HEIGHT){
                    int Yval = data[x+1][y];    // 0xff & (int) frame.getY(data, x + 1, y);
                    int YvalPre = data[x-1][y]; // 0xff & (int) frame.getY(data, x - 1, y);
                    sumGradient = Math.abs(Yval-YvalPre) + sumGradient;
                    count++;
                }
            }
        }

        for(int x=(int)refinedPoint.x-20*searchX; x< refinedPoint.x-18*searchX; x++){
            for(int y=(int)refinedPoint.y-searchY; y< refinedPoint.y+searchY; y++){
                if(x-4>0 && y>0 && x+4< Params.PREVIEW_FRAME_WIDTH && y< Params.PREVIEW_FRAME_HEIGHT){
                    int Yval = data[x+1][y];    // 0xff & (int) frame.getY(data, x + 1, y);
                    int YvalPre = data[x-1][y]; // 0xff & (int) frame.getY(data, x - 1, y);
                    sumGradientPre = Math.abs(Yval-YvalPre) + sumGradientPre;
                    countPre++;
                }
            }
        }

        if(sumGradient/(count+0.000000001) < 0.92*sumGradientPre/(countPre+0.000000001)){
            return true;
        } else return false;

    }

    // NEW Algorithm

    public void refinePositionAndUpdateCenter(int[][] data, Point2DGrid center, int refineDistance, int step_size) {

        if (center == null) return;

        findGeneralCenterOfMassAndUpdateObject(data,
                (int)(center.x - refineDistance), // Search box.
                (int)(center.y - refineDistance), // Search box.
                (int)(center.x + refineDistance), // Search box.
                (int)(center.y + refineDistance) // Search box.
                , step_size, center);
    }

    public void findGeneralCenterOfMassAndUpdateObject(int[][] data, int left, int top, int right, int bottom, int pixelStepSize, Point2DGrid toUpdate) {

        int moment_x = 0, moment_y = 0;
        double totalMass = 0;
        int s = pixelStepSize;
        int count=0;


        for (int y=top; y<=bottom; y+=s) {
            for (int x=left; x<=right; x+=s) {
                if (y<0) continue;
                if (x<0) continue;
                if (y>=Params.PREVIEW_FRAME_HEIGHT) continue;
                if (x>=Params.PREVIEW_FRAME_WIDTH) continue;

                if(data[x][y]!=0x00){
                    count++;
                }
                moment_x += data[x][y] * x;
                moment_y += data[x][y] * y;
                totalMass += data[x][y];
            }
        }

        if(count > .95*((bottom-top+1)*(right-left+1)/ (s*s))){
            toUpdate.isValid = false;
            return;
        }

        if (totalMass < 0.00000001) {
            toUpdate.isValid = false;
        } else {
            toUpdate.x = (float) (moment_x / totalMass);
            toUpdate.y = (float) (moment_y / totalMass);
            toUpdate.isValid = true;
        }
    }




    public void gridData2(int[][] data, Point2DGrid[][] grid, Point2D centerPosition,
                                    int rowDotCount,    int columnDotCount,
                                    int rowCenterPoint, int columnCenterPoint, Point2D steps, ArrayList<Point2D> anchors) {

        Clock clock = new Clock("GridData2");

        grid[rowCenterPoint][columnCenterPoint] = new Point2DGrid(centerPosition,true);

        //Adding the anchor points to the grid
        if(anchors.get(0)!=null & rowCenterPoint - 9 >= 0 & columnCenterPoint + 9 < columnDotCount ) {
            grid[rowCenterPoint - 9][columnCenterPoint + 9].x = anchors.get(0).x;
            grid[rowCenterPoint - 9][columnCenterPoint + 9].y = anchors.get(0).y;
            grid[rowCenterPoint - 9][columnCenterPoint + 9].isValid = true;
        }
        if(anchors.get(1)!=null & rowCenterPoint + 9 < rowDotCount & columnCenterPoint + 9 < columnDotCount) {
            grid[rowCenterPoint + 9][columnCenterPoint + 9].x = anchors.get(1).x;
            grid[rowCenterPoint + 9][columnCenterPoint + 9].y = anchors.get(1).y;
            grid[rowCenterPoint + 9][columnCenterPoint + 9].isValid = true;
        }
        if(anchors.get(2)!=null & rowCenterPoint + 9 < rowDotCount & columnCenterPoint - 9 >=0) {
            grid[rowCenterPoint + 9][columnCenterPoint - 9].x = anchors.get(2).x;
            grid[rowCenterPoint + 9][columnCenterPoint - 9].y = anchors.get(2).y;
            grid[rowCenterPoint + 9][columnCenterPoint - 9].isValid = true;
        }
        if(anchors.size()>3){
            if(anchors.get(3)!=null & rowCenterPoint - 9 >=0 & columnCenterPoint - 9 >=0) {
                grid[rowCenterPoint - 9][columnCenterPoint - 9].x = anchors.get(3).x;
                grid[rowCenterPoint - 9][columnCenterPoint - 9].y = anchors.get(3).y;
                grid[rowCenterPoint - 9][columnCenterPoint - 9].isValid = true;
            }
        }
        clock.capture("Variables Set");

        Thread myCenterLeft = new Thread(new SearchLeftCenterRow(data, grid, centerPosition, rowDotCount, columnDotCount, rowCenterPoint, columnCenterPoint, steps), "PreFiltered Center Left");
        Thread myCenterRight = new Thread(new SearchRightCenterRow(data, grid, centerPosition, rowDotCount, rowCenterPoint, columnCenterPoint, steps), "PreFiltered Center Right");

        myCenterLeft.start();
        myCenterRight.start();

        try {
            myCenterLeft.join();
            myCenterRight.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clock.capture("Center Grid Line");

        Log.i("Processors", "Available Processors " + Runtime.getRuntime().availableProcessors());

        Thread myUpLeft = new Thread(new SearchUpFromCenterRow(data, grid, centerPosition, rowCenterPoint, steps, 0, columnDotCount/2), "PreFiltered Up Left");
        Thread myUpRight = new Thread(new SearchUpFromCenterRow(data, grid, centerPosition, rowCenterPoint, steps, columnDotCount/2, columnDotCount), "PreFiltered Up Right");

        Thread myDownLeft = new Thread(new SearchDownFromCenterRow(data, grid, centerPosition, rowDotCount, rowCenterPoint, steps, 0, columnDotCount/2), "PreFiltered Down Left");
        Thread myDownRight = new Thread(new SearchDownFromCenterRow(data, grid, centerPosition, rowDotCount, rowCenterPoint, steps, columnDotCount/2, columnDotCount), "PreFiltered Down Right");

        myUpLeft.start();
        myUpRight.start();
        myDownLeft.start();
        myDownRight.start();


        try {
            myUpLeft.join();
            myUpRight.join();
            myDownLeft.join();
            myDownRight.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clock.capture("Rest of Grid");


        refineGrid(grid);
        clock.capture("Refinement of Grid");
        clock.log();
    }


    DescriptiveStatistics bottomRowStatCache;
    DescriptiveStatistics topRowStatCache;
    float[] bottomRowCache;
    float[] topRowCache;
    boolean[] flagFirstInvalidBottomCache;
    boolean[] flagFirstInvalidTopCache;

    protected void refineGrid(Point2DGrid[][] grid){

        if (topRowStatCache == null) topRowStatCache = new DescriptiveStatistics();
        if (bottomRowStatCache == null) bottomRowStatCache = new DescriptiveStatistics();

        this.bottomRowStatCache.clear();
        this.topRowStatCache.clear();

        int rowTotal = grid.length;
        int columnTotal = grid[0].length;
        int centerX = rowTotal/2;

        if (bottomRowCache == null) bottomRowCache = new float[columnTotal];
        for (int y=0; y<columnTotal;y++){
            bottomRowCache[y]=grid[rowTotal-1][y].x;
        }

        if (topRowCache == null) topRowCache = new float[columnTotal];
        for (int y=0; y<columnTotal;y++){
            topRowCache[y]=grid[0][y].x;
        }

        if (flagFirstInvalidBottomCache == null) flagFirstInvalidBottomCache = new boolean[columnTotal];
        if (flagFirstInvalidTopCache == null) flagFirstInvalidTopCache = new boolean[columnTotal];

        Arrays.fill(flagFirstInvalidBottomCache, false);
        Arrays.fill(flagFirstInvalidTopCache, false);

        // Collect boundaries
        for(int y=0; y<columnTotal;y++){
            flagFirstInvalidBottomCache[y]=false;
            for (int x= centerX; x< rowTotal; x++){
                if(!flagFirstInvalidBottomCache[y] && !grid[x-1][y].isValid && !grid[x][y].isValid ){
                    bottomRowCache[y] = grid[x-1][y].x;
                    bottomRowStatCache.addValue(x-1);
                    flagFirstInvalidBottomCache[y] = true;
                } else if(x==(rowTotal-1) && !flagFirstInvalidBottomCache[y]){
                    bottomRowStatCache.addValue(x);
                    bottomRowCache[y] = grid[x][y].x;
                }
            }
        }
        //Change boundaries which are outliers to medianBottomDot value
        int medianBottomDot = (int) bottomRowStatCache.getPercentile(80);
        for(int y=0; y<columnTotal;y++){
            if(bottomRowStatCache.getN()>0 && bottomRowStatCache.getElement(y)> medianBottomDot+10){
//                Log.e("gauss", "y = "+y+", bottomRowStatCache.getElement(y) = "+bottomRowStatCache.getElement(y)+", " + "; medianBottomDot = " + medianBottomDot);
                for(int x = medianBottomDot; x<rowTotal; x++){
                    grid[x][y].isValid=false;
                }
            } else if(bottomRowCache[y] > grid[medianBottomDot][y].x+80 ){
                for(int x = medianBottomDot; x<rowTotal; x++){
                    grid[x][y].isValid=false;
                }
            }
        }

        //TOP boundaries

        // Collect boundaries
        for(int y=0; y<columnTotal;y++){
            flagFirstInvalidTopCache[y]=false;
            for (int x= centerX; x>= 0; x--){
                if(!flagFirstInvalidTopCache[y] && !grid[x][y].isValid && !grid[x+1][y].isValid ){
                    topRowCache[y] = grid[x+1][y].x;
                    topRowStatCache.addValue(x+1);
                    flagFirstInvalidTopCache[y] = true;
                } else if(x==0 && !flagFirstInvalidTopCache[y]){
                    topRowStatCache.addValue(0);
                    topRowCache[y] = grid[x][y].x;
                }
            }
        }
        //Change boundaries which are outliers
        int medianTopDot = (int) topRowStatCache.getPercentile(20);
        for(int y=0; y<columnTotal;y++){
            if(topRowStatCache.getN()>0 && topRowStatCache.getElement(y)< medianTopDot-10){
                for(int x = medianTopDot; x>=0; x--){
                    grid[x][y].isValid=false;
                }
            } else if(topRowCache[y] < grid[medianTopDot][y].x-80 ){
                for(int x = medianTopDot; x>=0; x--){
                    grid[x][y].isValid=false;
                }
            }
        }
    }

    protected void refinePoint(int[][] data, float step, Point2DGrid refinedPoint, Point2D lastPoint, boolean yStep) {
        // Broad search
        refinePositionAndUpdateCenter(data, refinedPoint, Math.round(step / 2), 1); // step/2 should NOT BE CHANGED
        // Refined search on neighbors
        refinePositionAndUpdateCenter(data, refinedPoint, Math.round(step / 4), 1); // step/4 should NOT BE CHANGED

        float stepNew = 0;
        if (yStep)
            stepNew = Math.abs(refinedPoint.y - lastPoint.y);
        else
            stepNew = Math.abs(refinedPoint.x - lastPoint.x);

        if(refinedPoint.isValid) {
            if (!isValidStep(step, stepNew)) {
                refinedPoint.isValid = false;
            }
        }
    }

    private boolean isValidStep(float step, float stepAux) {
        return (stepAux > (step * 0.7f)) && (stepAux < (step * 1.45f));
    }

    public class SearchLeftCenterRow implements Runnable {
        // OUTPUT
        Point2DGrid[][] grid;

        // CANNOT BE CHANGED
        int[][] data;
        Point2D centerPosition;
        int rowDotCount;
        int columnDotCount;
        int rowCenterPoint;
        int columnCenterPoint;
        Point2D steps;

        public SearchLeftCenterRow(int[][] data, Point2DGrid[][] grid, Point2D centerPosition,
                               int rowDotCount, int columnDotCount, int rowCenterPoint, int columnCenterPoint, Point2D steps) {
            this.data = data;
            this.grid = grid;
            this.centerPosition = centerPosition;
            this.rowDotCount = rowDotCount;
            this.columnDotCount = columnDotCount;
            this.rowCenterPoint = rowCenterPoint;
            this.columnCenterPoint = columnCenterPoint;
            this.steps = steps;
        }

        public void run() {
            Point2D searchVector = new Point2D(0, steps.y);
            Point2D lastPoint = grid[rowCenterPoint][columnCenterPoint];

            // find dots located on the left of the center
            for (int y=columnCenterPoint+1; y<columnDotCount; y++) {
                grid[rowCenterPoint][y].x = lastPoint.x + searchVector.x;
                grid[rowCenterPoint][y].y = lastPoint.y + searchVector.y;

                refinePoint(data, searchVector.y, grid[rowCenterPoint][y], lastPoint, true);

                if (grid[rowCenterPoint][y].isValid) {
                    searchVector.y = Math.abs(grid[rowCenterPoint][y].y - lastPoint.y);
                    searchVector.x =         (grid[rowCenterPoint][y].x - lastPoint.x);
                }

                lastPoint = grid[rowCenterPoint][y];
            }
        }
    };

    public class SearchRightCenterRow implements Runnable {
        // OUTPUT
        Point2DGrid[][] grid;

        // CANNOT BE CHANGED
        int[][] data;
        Point2D centerPosition;
        int rowDotCount;
        int rowCenterPoint;
        int columnCenterPoint;
        Point2D steps;

        public SearchRightCenterRow(int[][] data, Point2DGrid[][] grid, Point2D centerPosition,
                                       int rowDotCount, int rowCenterPoint, int columnCenterPoint, Point2D steps) {
            this.data = data;
            this.grid = grid;
            this.centerPosition = centerPosition;
            this.rowDotCount = rowDotCount;
            this.rowCenterPoint = rowCenterPoint;
            this.columnCenterPoint = columnCenterPoint;
            this.steps = steps;
        }

        public void run() {
            Point2D searchVector = new Point2D(0, steps.y);
            Point2D lastPoint = grid[rowCenterPoint][columnCenterPoint];

            // find dots located on the right of the center
            for (int y=columnCenterPoint-1; y>=0; y--) {
                grid[rowCenterPoint][y].x = lastPoint.x + searchVector.x;
                grid[rowCenterPoint][y].y = lastPoint.y - searchVector.y;

                refinePoint(data, searchVector.y, grid[rowCenterPoint][y], lastPoint, true);

                if (grid[rowCenterPoint][y].isValid) {
                    searchVector.y = Math.abs(grid[rowCenterPoint][y].y - lastPoint.y);
                    searchVector.x =        -(grid[rowCenterPoint][y].x - lastPoint.x);
                }

                lastPoint = grid[rowCenterPoint][y];
            }
        }
    };

    public class SearchUpFromCenterRow implements Runnable {
        // OUTPUT
        Point2DGrid[][] grid;

        // CANNOT BE CHANGED
        int[][] data;
        Point2D centerPosition;
        int rowCenterPoint;
        Point2D steps;
        int startingColumn;
        int endColumn;

        public SearchUpFromCenterRow(int[][] data, Point2DGrid[][] grid, Point2D centerPosition,
                                      int rowCenterPoint, Point2D steps,
                                     int startingColumn, int endColumn) {
            this.data = data;
            this.grid = grid;
            this.centerPosition = centerPosition;
            this.rowCenterPoint = rowCenterPoint;
            this.steps = steps;
            this.startingColumn = startingColumn;
            this.endColumn = endColumn;
        }

        public void run() {
            Point2D searchVector = new Point2D();
            Point2D lastPoint;

            // After main row, find dots up and down for each column
            for(int y=startingColumn; y<endColumn; y++) {

                searchVector.y = 0;
                searchVector.x = steps.x;

                lastPoint = grid[rowCenterPoint][y];
                boolean invalidateNextDots = false;

                // find dots up of the main row
                for (int x = rowCenterPoint - 1; x >= 0; x--) {

                    if (invalidateNextDots) {
                        grid[x][y].isValid = false;
                        continue;
                    }

                    if(x==rowCenterPoint-9 & y == (grid[0].length/2)+9 & grid[x][y].isValid){
                        lastPoint = grid[x][y];
                        continue;
                    }
                    if(x==rowCenterPoint+9 & y == (grid[0].length/2)+9 & grid[x][y].isValid){
                        lastPoint = grid[x][y];
                        continue;
                    }
                    if(x==rowCenterPoint+9 & y == (grid[0].length/2)-9 & grid[x][y].isValid){
                        lastPoint = grid[x][y];
                        continue;
                    }

                    if(x==rowCenterPoint-9 & y == (grid[0].length/2)-9 & grid[x][y].isValid){
                        lastPoint = grid[x][y];
                        continue;
                    }

                    grid[x][y].x = lastPoint.x - searchVector.x;
                    grid[x][y].y = lastPoint.y + searchVector.y;

                    PreFilteredFrameTools.this.refinePoint(data, searchVector.x, grid[x][y], lastPoint, false);

                    if (!grid[x][y].isValid){
                        if(isGradientUp(data, new Point2D(lastPoint.x - searchVector.x, lastPoint.y + searchVector.y))){
                            invalidateNextDots = true;
                        }
                    }

                    if (grid[x][y].isValid) {
                        searchVector.y = -(grid[x][y].y - lastPoint.y);
                        searchVector.x = Math.abs(grid[x][y].x - lastPoint.x);
                    }

                    if ((rowCenterPoint > x + 1 && !grid[x + 1][y].isValid) &&
                            rowCenterPoint > x && !grid[x][y].isValid) {
                        invalidateNextDots = true;
                    }

                    lastPoint = grid[x][y];
                }
            }
        }
    };

    public class SearchDownFromCenterRow implements Runnable {
        // OUTPUT
        Point2DGrid[][] grid;

        // CANNOT BE CHANGED
        int[][] data;
        Point2D centerPosition;
        int rowDotCount;
        int rowCenterPoint;
        Point2D steps;
        int startingColumn;
        int endColumn;

        public SearchDownFromCenterRow(int[][] data, Point2DGrid[][] grid, Point2D centerPosition,
                                       int rowDotCount, int rowCenterPoint, Point2D steps,
                                       int startingColumn, int endColumn) {
            this.data = data;
            this.grid = grid;
            this.centerPosition = centerPosition;
            this.rowDotCount = rowDotCount;
            this.rowCenterPoint = rowCenterPoint;
            this.steps = steps;
            this.startingColumn = startingColumn;
            this.endColumn = endColumn;
        }

        public void run() {
            Point2D searchVector = new Point2D();
            Point2D lastPoint;

            for(int y=startingColumn; y<endColumn; y++) {
                searchVector.x = steps.x;
                searchVector.y = 0;
                lastPoint = grid[rowCenterPoint][y];
                boolean invalidateNextDots = false;

                // find dots down of the main row
                for (int x = rowCenterPoint + 1; x < rowDotCount; x++) {

                    if (invalidateNextDots) {
                        grid[x][y].isValid = false;
                        continue;
                    }
                    if(x==rowCenterPoint-9 & y == (grid[0].length/2)+9 & grid[x][y].isValid){
                        lastPoint = grid[x][y];
                        continue;
                    }
                    if(x==rowCenterPoint+9 & y == (grid[0].length/2)+9 & grid[x][y].isValid){
                        lastPoint = grid[x][y];
                        continue;
                    }
                    if(x==rowCenterPoint+9 & y == (grid[0].length/2)-9 & grid[x][y].isValid){
                        lastPoint = grid[x][y];
                        continue;
                    }

                    if(x==rowCenterPoint-9 & y == (grid[0].length/2)-9 & grid[x][y].isValid){
                        lastPoint = grid[x][y];
                        continue;
                    }

                    grid[x][y].x = lastPoint.x + searchVector.x;
                    grid[x][y].y = lastPoint.y + searchVector.y;

                    refinePoint(data, searchVector.x, grid[x][y], lastPoint, false);
                    

                    if (!grid[x][y].isValid ){
                        if(isGradientDown(data, new Point2D(lastPoint.x + searchVector.x, lastPoint.y + searchVector.y))){
                            invalidateNextDots = true;
                        }
                    }

                    if (grid[x][y].isValid) {
                        searchVector.y = (grid[x][y].y - lastPoint.y);
                        searchVector.x = Math.abs(grid[x][y].x - lastPoint.x);
                    }

                    if ((rowCenterPoint - 1 < x - 1 && !grid[x - 1][y].isValid) &&
                         rowCenterPoint - 1 < x && !grid[x][y].isValid) {
                        invalidateNextDots = true;
                    }

                    lastPoint = grid[x][y];
                }
            }
        }
    }
}

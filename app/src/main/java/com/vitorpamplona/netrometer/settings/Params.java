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
package com.vitorpamplona.netrometer.settings;

import android.os.Environment;

import com.vitorpamplona.netrometer.imageprocessing.utils.CoordinateTransform2D;
import com.vitorpamplona.netrometer.imageprocessing.utils.ImageProcessingUtil;
import com.vitorpamplona.netrometer.utils.Rect;
import com.vitorpamplona.netrometer.imageprocessing.utils.YuvFilter;
import com.vitorpamplona.netrometer.imageprocessing.model.VectorNeighbor;
import com.vitorpamplona.netrometer.utils.Point2D;

import java.util.List;


public final class   Params {


    ///// MAIN DASHBOARD FOR NETROMETER //////

    // resolution of displayed image in debug tab
    public final static int PREVIEW_DISPLAY_WIDTH = 1920;
    public final static int PREVIEW_DISPLAY_HEIGHT = 1080;

    // resolution of frame to work on
    public final static int PREVIEW_FRAME_WIDTH = 1280;
    public final static int PREVIEW_FRAME_HEIGHT = 720;

    // Lens plane conversion pixel to milimiters
    public final static float UNIT_CONVERSION_PIXEL2MM = 43.4f;

    // Smart Stage Light Pipe Active Threshold
    public final static double ACTIVE_PIXELS_LIGHT_PIPE = 16000;

    // Hard setting dependent on hardware
    public final static float UNIT_DOT_GRID_DISTANCE_SV = 14.77f;
    public final static float UNIT_DOT_GRID_DISTANCE_PRG = UNIT_DOT_GRID_DISTANCE_SV; //14.80f;

    // conversion between displayed and preview frame sizes
    public final static double FRAME_CONVERSION_X = PREVIEW_DISPLAY_WIDTH / (PREVIEW_FRAME_WIDTH * 1d);
    public final static double FRAME_CONVERSION_Y = PREVIEW_DISPLAY_HEIGHT/ (PREVIEW_FRAME_HEIGHT * 1d);

    // center search box size (sides)
    public static final int CENTER_SEARCH_WIDTH = (int) (PREVIEW_DISPLAY_WIDTH*0.07f); //134 pixels = 3.08mm
    public static final int CENTER_PROGRESSIVE_SEARCH_WIDTH = (int) (2*UNIT_CONVERSION_PIXEL2MM); //87 pixels
    public static final Point2D CENTER_SEARCH_LOCATION = new Point2D(625,368);
    // Anti-clock wise 2nd quadrant, 3rd, 4th, 1st
    public static final Point2D CENTER_ANCHOR_1_SEARCH_LOCATION = new Point2D(CENTER_SEARCH_LOCATION.x - 1*CENTER_SEARCH_WIDTH,CENTER_SEARCH_LOCATION.y + 1*CENTER_SEARCH_WIDTH);
    public static final Point2D CENTER_ANCHOR_2_SEARCH_LOCATION = new Point2D(CENTER_SEARCH_LOCATION.x + 1*CENTER_SEARCH_WIDTH,CENTER_SEARCH_LOCATION.y + 1*CENTER_SEARCH_WIDTH);
    public static final Point2D CENTER_ANCHOR_3_SEARCH_LOCATION = new Point2D(CENTER_SEARCH_LOCATION.x + 1*CENTER_SEARCH_WIDTH,CENTER_SEARCH_LOCATION.y - 1*CENTER_SEARCH_WIDTH);
    public static final Point2D CENTER_ANCHOR_4_SEARCH_LOCATION = new Point2D(CENTER_SEARCH_LOCATION.x - 1*CENTER_SEARCH_WIDTH,CENTER_SEARCH_LOCATION.y - 1*CENTER_SEARCH_WIDTH);



    public static final Rect CENTER_SEARCH_BOX = new Rect(
                                (int)(Params.CENTER_SEARCH_LOCATION.x - Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_SEARCH_LOCATION.y - Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_SEARCH_LOCATION.x + Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_SEARCH_LOCATION.y + Params.CENTER_SEARCH_WIDTH/2));

    public static final Rect CENTER_ANCHOR_1 = new Rect(
                                (int)(Params.CENTER_ANCHOR_1_SEARCH_LOCATION.x - Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_ANCHOR_1_SEARCH_LOCATION.y - Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_ANCHOR_1_SEARCH_LOCATION.x + Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_ANCHOR_1_SEARCH_LOCATION.y + Params.CENTER_SEARCH_WIDTH/2));

    public static final Rect CENTER_ANCHOR_2 = new Rect(
                                (int)(Params.CENTER_ANCHOR_2_SEARCH_LOCATION.x - Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_ANCHOR_2_SEARCH_LOCATION.y - Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_ANCHOR_2_SEARCH_LOCATION.x + Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_ANCHOR_2_SEARCH_LOCATION.y + Params.CENTER_SEARCH_WIDTH/2));

    public static final Rect    CENTER_ANCHOR_3 = new Rect(
                                (int)(Params.CENTER_ANCHOR_3_SEARCH_LOCATION.x - Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_ANCHOR_3_SEARCH_LOCATION.y - Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_ANCHOR_3_SEARCH_LOCATION.x + Params.CENTER_SEARCH_WIDTH/2),
                                (int)(Params.CENTER_ANCHOR_3_SEARCH_LOCATION.y + Params.CENTER_SEARCH_WIDTH/2));

    public static final Rect    CENTER_ANCHOR_4 = new Rect(
            (int)(Params.CENTER_ANCHOR_4_SEARCH_LOCATION.x - Params.CENTER_SEARCH_WIDTH/2),
            (int)(Params.CENTER_ANCHOR_4_SEARCH_LOCATION.y - Params.CENTER_SEARCH_WIDTH/2),
            (int)(Params.CENTER_ANCHOR_4_SEARCH_LOCATION.x + Params.CENTER_SEARCH_WIDTH/2),
            (int)(Params.CENTER_ANCHOR_4_SEARCH_LOCATION.y + Params.CENTER_SEARCH_WIDTH/2));


    public static final Rect CENTER_SEARCH_BOX_PROGRESSIVE = new Rect(
            (int)(Params.CENTER_SEARCH_LOCATION.x - 1.4*(float)Params.CENTER_PROGRESSIVE_SEARCH_WIDTH),
            (int)(Params.CENTER_SEARCH_LOCATION.y - .7*(float)Params.CENTER_PROGRESSIVE_SEARCH_WIDTH),
            (int)(Params.CENTER_SEARCH_LOCATION.x + 1.4*(float)Params.CENTER_PROGRESSIVE_SEARCH_WIDTH),
            (int)(Params.CENTER_SEARCH_LOCATION.y + .7*(float)Params.CENTER_PROGRESSIVE_SEARCH_WIDTH));

    public static final Rect CENTER_PROGRESSIVE_DISTANCE_BOX = new Rect(
            (int)(Params.CENTER_SEARCH_LOCATION.x - 4*UNIT_CONVERSION_PIXEL2MM - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.y - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.x - 4*UNIT_CONVERSION_PIXEL2MM + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.y + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2));

    public static final Rect CENTER_PROGRESSIVE_READER_BOX = new Rect(
            (int)(Params.CENTER_SEARCH_LOCATION.x + 8*UNIT_CONVERSION_PIXEL2MM - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.y - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.x + 8*UNIT_CONVERSION_PIXEL2MM + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.y + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2));

    public static final Rect CENTER_PROGRESSIVE_READER_BOX_LEFT = new Rect(
            (int)(Params.CENTER_SEARCH_LOCATION.x + 8*UNIT_CONVERSION_PIXEL2MM - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.y - 4*UNIT_CONVERSION_PIXEL2MM- Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.x + 8*UNIT_CONVERSION_PIXEL2MM + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.y - 4*UNIT_CONVERSION_PIXEL2MM+ Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2));

    public static final Rect CENTER_PROGRESSIVE_READER_BOX_RIGHT = new Rect(
            (int)(Params.CENTER_SEARCH_LOCATION.x + 8*UNIT_CONVERSION_PIXEL2MM - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.y + 4*UNIT_CONVERSION_PIXEL2MM - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.x + 8*UNIT_CONVERSION_PIXEL2MM + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
            (int)(Params.CENTER_SEARCH_LOCATION.y + 4*UNIT_CONVERSION_PIXEL2MM + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2));

    // trigger search area
    public static final Rect TRIGGER_SEARCH_AREA = new Rect((int)(PREVIEW_FRAME_WIDTH*0),
                                                            (int)(PREVIEW_FRAME_HEIGHT*0.3),
                                                            (int)(PREVIEW_FRAME_WIDTH*0.05),
                                                            (int)(PREVIEW_FRAME_HEIGHT*0.7));

    public static final int TRIGGER_AREA_CUTOFF = 250000;

    public static final double CUT_OFF_THRESHOLD = 255 * 0.53;

    // coordinate transformer (screen to image / image to screen - coordinate transformer)
    public static final CoordinateTransform2D TF = new CoordinateTransform2D(    new Point2D(0,0),
                                                                                        new Point2D(0, PREVIEW_FRAME_HEIGHT-1),
                                                                                        new Point2D(PREVIEW_DISPLAY_HEIGHT-1,0),
                                                                                        new Point2D(0,0));


    // debug directory path
    public final static String DEBUG_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MeridianDebug/";

    // fitting coefficients

    // Gun design:  Thomas Swyst _ Radius [10f,11f]
//    public final static double FITTING_COEFFICIENTS_A  = 34.4649 / 1e6;
//    public final static double FITTING_COEFFICIENTS_B  = 7.3893 / 1e4;
//    public final static double FITTING_COEFFICIENTS_C  = 2.3632 / 1e2;
//    public final static double FITTING_COEFFICIENTS_D  = 1.0095 / 1e0;

    // Gun design:  Thomas Swyst _ Radius [10f,11f]
    public final static double FITTING_COEFFICIENTS_A  = 0;
    public final static double FITTING_COEFFICIENTS_B  = 0;
    public final static double FITTING_COEFFICIENTS_C  = -43.2224;
    public final static double FITTING_COEFFICIENTS_D  = 42.7644;

    // Results Step
    public final static float RESULT_STEP_SPHERE = 0.25f;  // typically 0.25f
    public final static float RESULT_STEP_CYLINDER = 0.25f;  // typically 0.25f
    public final static float RESULT_STEP_AXIS = 1f;  // typically 1f

    // Filters
	public final static YuvFilter NO_FILTER = new YuvFilter(0, 255, 0, 255);
    public final static YuvFilter BLUE_STANDARD_FILTER = new YuvFilter(136,  180,  74,  134);
//    public final static YuvFilter BLUE_STANDARD_FILTER = new YuvFilter(125,  180,  74,  134); //TODO:MOTOZ SPEC
    public final static YuvFilter RED_STANDARD_FILTER = new YuvFilter(  0,  255,(int) (0.53*255),  255);
    public final static YuvFilter RED_FILTER_V2 = new YuvFilter( 0f, 0.6f, 0.53f, 1f);
//    public final static YuvFilter RED_FILTER_V2 = new YuvFilter( 0f, 0.6f, 0.61f, 1f); //TODO:MOTOZ SPEC
    public final static YuvFilter BLUE_AUX_FILTER = new YuvFilter(150,  200,  74,  134);

    // Grid size parameters (odd numbers)
    public final static int ROW_DOT_COUNT = 23;
    public final static int COLUMN_DOT_COUNT = 23;
    public final static int ROW_CENTER_POINT = ROW_DOT_COUNT/2;
    public final static int COLUMN_CENTER_POINT = COLUMN_DOT_COUNT/2;

    public final static float MAX_NEIGHBORS_RADIUS = 11;
    public final static float MIN_NEIGHBORS_RADIUS = 10;
    public final static List<VectorNeighbor> NEIGHBORS = ImageProcessingUtil.defineNeighbors();


    public final static float DEFAULT_GRID_STEP = UNIT_DOT_GRID_DISTANCE_SV;
    public static final float DEFAULT_ANCHORS_DOTS_DISTANCE= 18;

    // Progressives Grid size parameters (odd numbers)
    public final static int ROW_DOT_COUNT_PROGRESSIVE =  75;//89;
    public final static int COLUMN_DOT_COUNT_PROGRESSIVE = 45;
    public final static int ROW_CENTER_POINT_PROGRESSIVE = ROW_DOT_COUNT_PROGRESSIVE/2;
    public final static int COLUMN_CENTER_POINT_PROGRESSIVE = COLUMN_DOT_COUNT_PROGRESSIVE/2;
    public final static int PROGRESSIVE_PROBE_STEP=6;
    public final static int PROGRESSIVE_ROI_PROBE_STEP=1;

    // UI - Measure Fragment to retrieve Optical Center - Progressives Grid size parameters (odd numbers)
    public final static int ROW_DOT_COUNT_PROGRESSIVE_UI =ROW_DOT_COUNT_PROGRESSIVE;// 41;
    public final static int COLUMN_DOT_COUNT_PROGRESSIVE_UI = 15; //COLUMN_DOT_COUNT_PROGRESSIVE/4;//31;
    public final static int ROW_CENTER_POINT_PROGRESSIVE_UI = ROW_DOT_COUNT_PROGRESSIVE_UI/2;
    public final static int COLUMN_CENTER_POINT_PROGRESSIVE_UI = COLUMN_DOT_COUNT_PROGRESSIVE_UI/2;

    // UI - Measure Fragment to retrieve Optical Center - Progressives Grid size parameters (odd numbers)
    public final static int ROW_DOT_COUNT_SINGLEVISION_UI = 23;
    public final static int COLUMN_DOT_COUNT_SINGLEVISION_UI = 23;
    public final static int ROW_CENTER_POINT_SINGLEVISION_UI = ROW_DOT_COUNT_SINGLEVISION_UI/2;
    public final static int COLUMN_CENTER_POINT_SINGLEVISION_UI = COLUMN_DOT_COUNT_SINGLEVISION_UI/2;

    // Holder of eyeglasses frames parameters
    public final static double  FH_RIGHT_MAIN_REF_DISTANCE = 30.5; // distance in mm from middle dot to stage reference
    public final static double  FH_RIGHT_MAJOR_DISTANCE = 9.5; // Right to main ref
    public final static double  FH_RIGHT_MINOR_DISTANCE = 7.2; // Left to main ref

    public final static double  FH_LEFT_MAIN_REF_DISTANCE = 30.5; // distance from middle dot to stage reference
    public final static double  FH_LEFT_MAJOR_DISTANCE = 7.2; // Right to main ref
    public final static double  FH_LEFT_MINOR_DISTANCE = 9.5; // Left to main ref

    public final static double FH_SCALE_RATIO_PLANE = 1; //1.28;


}

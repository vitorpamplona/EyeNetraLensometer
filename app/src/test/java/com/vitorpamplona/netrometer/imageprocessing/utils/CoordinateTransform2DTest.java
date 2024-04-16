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

import android.graphics.Rect;

import com.vitorpamplona.netrometer.BuildConfig;

import com.vitorpamplona.netrometer.activity.CustomRobolectricGradleTestRunner;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Point2D;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(CustomRobolectricGradleTestRunner.class)
@Config(sdk=21)
public class CoordinateTransform2DTest {

    public static final CoordinateTransform2D TF = new CoordinateTransform2D(
            new Point2D(0,0),
            new Point2D(0, Params.PREVIEW_FRAME_HEIGHT-1),
            new Point2D(Params.PREVIEW_DISPLAY_HEIGHT-1,0),
            new Point2D(0,0));

    @Test
    public void coordinateTransformPointFromPoint2D() {

        Point2D p = new Point2D(0,0);
        p = TF.point(p);

        assertEquals( 1079f, p.x, 0);
        assertEquals( 0f, p.y, 0);

        p = new Point2D(1,1);
        p = TF.point(p);

        assertEquals( 1077f, p.x, 0.5);
        assertEquals( 1.5f, p.y, 0.5);

        p = new Point2D(150, 300);
        p = TF.point(p);

        assertEquals( 629f, p.x, 0.5);
        assertEquals( 225f, p.y, 0.5);

        p = new Point2D(-1, -1);
        p = TF.point(p);

        assertEquals( 1081f, p.x, 0.5);
        assertEquals( -2f, p.y, 0.5);
    }

    @Test
    public void coordinateTransformPointFromFloat() {

        Point2D p;
        p = TF.point(0,0);

        assertEquals(1079f, p.x, 0);
        assertEquals(0f, p.y, 0);

        p = TF.point(1,1);

        assertEquals( 1077f, p.x, 0.5);
        assertEquals( 1.5f, p.y, 0.5);

        p = TF.point(150, 300);

        assertEquals( 629f, p.x, 0.5);
        assertEquals( 225f, p.y, 0.5);

        p = TF.point(-1, -1);

        assertEquals( 1081f, p.x, 0.5);
        assertEquals( -2f, p.y, 0.5);
    }

    @Test
    public void coordinateTransformRect() {

        Rect rect = new Rect(0,1,1,0);
        rect = TF.rect(rect);

        assertEquals(1077, rect.left);
        assertEquals(0, rect.top);
        assertEquals(1079, rect.right);
        assertEquals(1, rect.bottom);


        rect = new Rect(100,200,200,100);
        rect = TF.rect(rect);

        assertEquals(778, rect.left);
        assertEquals(150, rect.top);
        assertEquals(928, rect.right);
        assertEquals(300, rect.bottom);


        rect = new Rect(1,1,1,1);
        rect = TF.rect(rect);

        assertEquals(1077, rect.left);
        assertEquals(1, rect.top);
        assertEquals(1077, rect.right);
        assertEquals(1, rect.bottom);
    }
}

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
package com.vitorpamplona.netrometer.model;

import com.vitorpamplona.netrometer.BuildConfig;

import com.vitorpamplona.netrometer.activity.CustomRobolectricGradleTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(CustomRobolectricGradleTestRunner.class)
@Config(sdk=21)
public class PrescriptionTest {


    @Test
    public void testCylModes() {
        Prescription p = new Prescription(-2f, -0.5f, 180f, RefractionType.NETRA);

        p.putInNegativeCilinder();

        assertEquals(-2, p.getSphere(), 0.01);
        assertEquals(-0.5, p.getCylinder(), 0.01);
        assertEquals(180, p.getAxis(), 0.01);

        p.putInPositiveCilinder();

        assertEquals(-2.5, p.getSphere(), 0.01);
        assertEquals(+0.5, p.getCylinder(), 0.01);
        assertEquals(90, p.getAxis(), 0.01);

        p.putInNegativeCilinder();

        assertEquals(-2, p.getSphere(), 0.01);
        assertEquals(-0.5, p.getCylinder(), 0.01);
        assertEquals(180, p.getAxis(), 0.01);
    }

    public void testCopyConstructor() {
        Prescription p = new Prescription(1f, 2f, 3f, RefractionType.NETRA);
        p.setAddLens(4f);
        p.setId(UUID.randomUUID());
        p.setCyclo(true);
        p.setVaCorrected(0.76f);

        Prescription pCopy = new Prescription(p);

        assertFalse(p == pCopy);
        assertEquals(p.getSphere(), pCopy.getSphere(), 0.01);
        assertEquals(p.getCylinder(), pCopy.getCylinder(), 0.01);
        assertEquals(p.getAxis(), pCopy.getAxis(), 0.01);
        assertEquals(p.getAddLens(), pCopy.getAddLens(), 0.01);
        assertEquals(p.getCyclo(), pCopy.getCyclo());
        assertEquals(p.getId(), pCopy.getId());
        assertEquals(p.getProcedure(), pCopy.getProcedure());
    }

    @Test
    public void testCopyCylModes() {
        Prescription p = new Prescription(-2f, -0.5f, 180f, RefractionType.NETRA);

        Prescription pNeg = p.copyInNegativeCilinder();

        assertEquals(-2, pNeg.getSphere(), 0.01);
        assertEquals(-0.5, pNeg.getCylinder(), 0.01);
        assertEquals(180, pNeg.getAxis(), 0.01);

        assertEquals(-2, p.getSphere(), 0.01);
        assertEquals(-0.5, p.getCylinder(), 0.01);
        assertEquals(180, p.getAxis(), 0.01);

        Prescription pPos = p.copyInPositiveCilinder();

        assertEquals(-2.5, pPos.getSphere(), 0.01);
        assertEquals(+0.5, pPos.getCylinder(), 0.01);
        assertEquals(90, pPos.getAxis(), 0.01);

        assertEquals(-2, pNeg.getSphere(), 0.01);
        assertEquals(-0.5, pNeg.getCylinder(), 0.01);
        assertEquals(180, pNeg.getAxis(), 0.01);

        assertEquals(-2, p.getSphere(), 0.01);
        assertEquals(-0.5, p.getCylinder(), 0.01);
        assertEquals(180, p.getAxis(), 0.01);

        Prescription pNeg2 = p.copyInNegativeCilinder();

        assertEquals(-2, pNeg2.getSphere(), 0.01);
        assertEquals(-0.5, pNeg2.getCylinder(), 0.01);
        assertEquals(180, pNeg2.getAxis(), 0.01);

        assertEquals(-2.5, pPos.getSphere(), 0.01);
        assertEquals(+0.5, pPos.getCylinder(), 0.01);
        assertEquals(90, pPos.getAxis(), 0.01);

        assertEquals(-2, pNeg.getSphere(), 0.01);
        assertEquals(-0.5, pNeg.getCylinder(), 0.01);
        assertEquals(180, pNeg.getAxis(), 0.01);

        assertEquals(-2, p.getSphere(), 0.01);
        assertEquals(-0.5, p.getCylinder(), 0.01);
        assertEquals(180, p.getAxis(), 0.01);
    }


}

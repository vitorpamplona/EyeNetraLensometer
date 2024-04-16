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
package com.vitorpamplona.netrometer.activity.fragments;

import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.imageprocessing.listeners.CenterFinderListener;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;

public abstract class AbstractMeasuringFragment extends AbstractNetrometerFragment implements CenterFinderListener {

    @Override
    public void onResume() {
        super.onResume();
        if (getImageProcessor() != null) getImageProcessor().registerCenterFinderListener(this);
        getNetActivity().hidePrinterButton();
        getNetActivity().hideNewCustomReadingButton();
        getNetActivity().hideCameraPreviewButton();
        getNetActivity().enableToolbarView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getImageProcessor() != null) getImageProcessor().registerCenterFinderListener(null);
    }

    public boolean onBackPressed() {
        if (getImageProcessor() != null) getImageProcessor().registerCenterFinderListener(null);
        getNetActivity().finishImageProcessing();
        getNetActivity().loadStartFragment();
        return true;
    }

    public void done() {
        if (getImageProcessor() != null) getImageProcessor().registerCenterFinderListener(null);
        if (getActivity() != null) ((NetrometerActivity) getActivity()).finishImageProcessing();
    }

    public void enableDisplayingProgress() {
        if (getNetActivity() != null)
            getNetActivity().enableProgress();
    }

    public void disableDisplayingProgress() {
        if (getNetActivity() != null)
            getNetActivity().disableProgress();
    }

    public void resetTexts() {

    }

    public void drawDebug(GridResult result, GridResult zero) {

    }
}
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

import android.app.Fragment;

import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.imageprocessing.NetrometerImageProcessing;
import com.vitorpamplona.netrometer.settings.AppSettings;

public abstract class AbstractNetrometerFragment extends Fragment {

//    protected SingleVisionImageProcessing getImageProcessor() {
//        return ((NetrometerActivity) getActivity()).getImageProcessor();
//    }

    protected NetrometerActivity getNetActivity() {
        return ((NetrometerActivity) getActivity());
    }

    protected NetrometerImageProcessing getImageProcessor() {
        if (((NetrometerActivity) getActivity()) == null)
            return null;

        return ((NetrometerActivity) getActivity()).getImageProcessor();
    }

    protected AppSettings getSettings() {
        return ((NetrometerApplication) getActivity().getApplication()).getSettings();
    }

    protected boolean isDebugMode() {
        return ((NetrometerApplication) getActivity().getApplication()).isDebuggable();
    }

    public boolean onBackPressed() {
        return false;
    }

    public boolean shouldShowActionBar() {
        return true;
    }

    public void afterSync() {

    }

}

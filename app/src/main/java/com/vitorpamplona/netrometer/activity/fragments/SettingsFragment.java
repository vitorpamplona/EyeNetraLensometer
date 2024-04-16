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

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.utils.LanguageHelper;

import java.util.Locale;

import static com.vitorpamplona.netrometer.utils.LanguageHelper.callAppLanguagePopUp;
import static com.vitorpamplona.netrometer.utils.LanguageHelper.loadSelectedAppLanguageSettingsPage;

/**
 * Created by grant on 12/25/14.
 */
public class SettingsFragment extends AbstractNetrometerFragment implements LanguageHelper.SettingsCallback {

    private TextView mInstructions1;

    private RadioButton rbSphCyl;
    private RadioButton rbSphEq;
    private RadioButton rbMinusCyl;
    private RadioButton rbPlusCyl;
    private RadioButton rbAcuityImperial;
    private RadioButton rbAcuityMetric;

    private ImageButton imAppLanguage;
    private TextView txAppLanguage;
    private TextView txInstructions1;

    private LinearLayout llAppLanguage;

    private Button mHome;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_nav_settings, container, false);

        txInstructions1 = (TextView) view.findViewById(R.id.instructions_1);

        rbSphCyl = (RadioButton) view.findViewById(R.id.rbSphCyl);
        rbSphEq = (RadioButton) view.findViewById(R.id.rbSphEq);;
        rbMinusCyl = (RadioButton) view.findViewById(R.id.rbMinusCyl);;
        rbPlusCyl = (RadioButton) view.findViewById(R.id.rbPlusCyl);
        rbAcuityImperial = (RadioButton) view.findViewById(R.id.rbAcuityImperial);
        rbAcuityMetric = (RadioButton) view.findViewById(R.id.rbAcuityMetric);

        imAppLanguage = (ImageButton) view.findViewById(R.id.imAppLocale);
        txAppLanguage = (TextView) view.findViewById(R.id.txAppLanguageText);
        llAppLanguage = (LinearLayout) view.findViewById(R.id.llAppLanguage);


        mHome = (Button) view.findViewById(R.id.back_button);
        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNetActivity().loadStartFragment();
            }
        });

        rbSphCyl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSettings().setSpherocylindricalMode(true);
            }
        });
        rbSphEq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSettings().setSpherocylindricalMode(false);
            }
        });
        rbMinusCyl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSettings().setNegativeCylModel(true);
            }
        });
        rbPlusCyl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSettings().setNegativeCylModel(false);
            }
        });
        rbAcuityImperial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSettings().setImperialSystem(true);
            }
        });
        rbAcuityMetric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSettings().setImperialSystem(false);
            }
        });
        llAppLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAppLanguagePopUp(SettingsFragment.this.getNetActivity(), view, SettingsFragment.this);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        getNetActivity().showMenu();
        getNetActivity().hidePrinterButton();
        getNetActivity().hideNewCustomReadingButton();
        getNetActivity().hideCameraPreviewButton();
        getNetActivity().animateToHideCamera();
        getNetActivity().enableToolbarView();

        rbSphCyl.setChecked(getSettings().isSpherocylindricalMode());
        rbSphEq.setChecked(!getSettings().isSpherocylindricalMode());

        rbMinusCyl.setChecked(getSettings().isNegativeCylModel());
        rbPlusCyl.setChecked(!getSettings().isNegativeCylModel());

        rbAcuityImperial.setChecked(getSettings().isImperialSystem());
        rbAcuityMetric.setChecked(!getSettings().isImperialSystem());

        loadSelectedAppLanguageSettingsPage(this);

        super.onResume();
    }

    public void setLanguage(int chekingImg, int checkingTxt) {
        imAppLanguage.setImageResource(chekingImg);
        txAppLanguage.setText(checkingTxt);
    }

    //This changes the language of the whole app. Should be done on the settings instead.
    public void setAppLocale(Locale myLocale) {
        getSettings().setAppLocale(myLocale.getLanguage(), myLocale.getCountry());

        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        getNetActivity().recreate();

        loadSelectedAppLanguageSettingsPage(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onBackPressed() {
        getNetActivity().loadStartFragment();
        return true;
    }

}
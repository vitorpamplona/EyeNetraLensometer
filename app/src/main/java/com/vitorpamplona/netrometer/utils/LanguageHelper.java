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

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;


public class LanguageHelper {

    public static void loadSelectedAppLanguageSettingsPage(SettingsCallback act) {
        Locale appLocale = NetrometerApplication.get().getSettings().getAppLocale();
        checkAppLanguageAndSet(act, appLocale, Locale.US, R.drawable.usa, R.string.english_us);
        checkAppLanguageAndSet(act, appLocale, Locale.FRANCE, R.drawable.france, R.string.french_FR);
        checkAppLanguageAndSet(act, appLocale, Locale.GERMANY, R.drawable.germany, R.string.german_DE);
        checkAppLanguageAndSet(act, appLocale, new Locale("nl", "NL"), R.drawable.netherlands, R.string.dutch_NL);
        checkAppLanguageAndSet(act, appLocale, new Locale("ja", "JP"), R.drawable.japan, R.string.japanese);
        checkAppLanguageAndSet(act, appLocale, Locale.ITALY, R.drawable.italy, R.string.italy_IT);
        checkAppLanguageAndSet(act, appLocale, new Locale("zh", "CN"), R.drawable.china, R.string.chinese_Sim);
        checkAppLanguageAndSet(act, appLocale, new Locale("es", "ES"), R.drawable.spain, R.string.spanish_ES);
        checkAppLanguageAndSet(act, appLocale, new Locale("es", "US"), R.drawable.mexico, R.string.spanish_us);
        checkAppLanguageAndSet(act, appLocale, new Locale("pt", "BR"), R.drawable.brazil, R.string.portuguese_BR);
        //checkAppLanguageAndSet(act, appLocale, new Locale("sw", "TZ"), R.drawable.tanzania, R.string.swahili_TZ);
    }

    public static void languagePopUpReturnSettingsPage(int itemId, SettingsCallback act) {
        switch (itemId) {
            case R.id.lang_english_US: act.setAppLocale(Locale.US); break;
            case R.id.lang_chinese_Simplified: act.setAppLocale(new Locale("zh", "CN")); break;
            case R.id.lang_french_France: act.setAppLocale(Locale.FRANCE); break;
            case R.id.lang_spanish_Spain: act.setAppLocale(new Locale("es", "ES")); break;
            case R.id.lang_dutch_Netherlands: act.setAppLocale(new Locale("nl", "NL")); break;
            case R.id.lang_spanish_US: act.setAppLocale(new Locale("es", "US")); break;
            case R.id.lang_portuguese_Brasil: act.setAppLocale(new Locale("pt", "BR")); break;
            case R.id.lang_japanese: act.setAppLocale(new Locale("ja", "JP")); break;
            case R.id.lang_german_Germany: act.setAppLocale(Locale.GERMANY); break;
            case R.id.lang_italian_Italy: act.setAppLocale(Locale.ITALY); break;
            //case R.id.lang_swahili_Tanzania: act.setAppLocale(new Locale("sw", "TZ")); break;
        }
    }

    public static void callAppLanguagePopUp(Context ctx, View view, final SettingsCallback act) {
        final PopupMenu popup = new PopupMenu(ctx, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.language, popup.getMenu());

        // Enabling Icons
        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            Class<?> cls = Class.forName("com.android.internal.view.menu.MenuPopupHelper");
            Method method = cls.getDeclaredMethod("setForceShowIcon", new Class[]{boolean.class});
            method.setAccessible(true);
            method.invoke(menuPopupHelper, new Object[]{true});
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                languagePopUpReturnSettingsPage(item.getItemId(), act);
                return true;
            }
        });
        popup.show();
    }

    public static void checkAppLanguageAndSet(SettingsCallback act, Locale appLocale, Locale checking, int chekingImg, int checkingTxt) {
        if (appLocale.getLanguage().equals(checking.getLanguage()) && appLocale.getCountry().equals(checking.getCountry())) {
            act.setLanguage(chekingImg, checkingTxt);
        }
    }

    public static interface SettingsCallback {
        public void setLanguage(int chekingImg, int checkingTxt);
        public void setAppLocale(Locale l);
    }

}



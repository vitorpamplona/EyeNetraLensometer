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


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

public abstract class AppSettingsBase {

    private SharedPreferences appSharedPrefs = null;
    private SharedPreferences.Editor prefsEditor = null;

    protected AppSettingsBase(Context context) {
        appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    private SharedPreferences.Editor getEditor() {
        if (prefsEditor == null) prefsEditor = appSharedPrefs.edit();
        return prefsEditor;
    }

    protected String getString(String key, String defaultValue) {
        return appSharedPrefs.getString(key, defaultValue);
    }

    protected boolean getBoolean(String key, boolean defaultValue) {
        return appSharedPrefs.getBoolean(key, defaultValue);
    }

    protected float getFloat(String key, float defaultValue) {
        return appSharedPrefs.getFloat(key, defaultValue);
    }

    protected double getDouble(String key, double defaultValue) {
        return Double.longBitsToDouble(appSharedPrefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    protected long getLong(String key, long defaultValue) {
        return appSharedPrefs.getLong(key, defaultValue);
    }

    protected UUID getUUID(String key) {
        return UUID.fromString(appSharedPrefs.getString(key, null));
    }

    protected int getInt(String key, int defaultValue) {
        return appSharedPrefs.getInt(key, defaultValue);
    }

    protected void setBoolean(String key, boolean value) {
        getEditor().putBoolean(key, value);
        getEditor().commit();
    }

    protected void setFloat(String key, float value) {
        getEditor().putFloat(key, value);
        getEditor().commit();
    }

    protected void setInt(String key, int value) {
        getEditor().putInt(key, value);
        getEditor().commit();
    }

    void setDouble(String key, double value) {
        getEditor().putLong(key, Double.doubleToRawLongBits(value));
        getEditor().commit();
    }

    protected void setLong(String key, long value) {
        getEditor().putLong(key, value);
        getEditor().commit();
    }

    protected void setString(String key, String value) {
        getEditor().putString(key, value);
        getEditor().commit();
    }

    protected void setUUID(String key, UUID value) {
        if (value != null) {
            getEditor().putString(key, value.toString());
        } else {
            getEditor().remove(key);
        }
        getEditor().commit();
    }



}

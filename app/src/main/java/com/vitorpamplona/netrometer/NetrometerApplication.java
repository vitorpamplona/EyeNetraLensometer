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
package com.vitorpamplona.netrometer;

import android.Manifest;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.vitorpamplona.netrometer.imageprocessing.hardware.AbstractDevice;
import com.vitorpamplona.netrometer.imageprocessing.hardware.DeviceDataset;
import com.vitorpamplona.netrometer.model.ExamResults;
import com.vitorpamplona.netrometer.model.db.SQLiteHelper;
import com.vitorpamplona.netrometer.settings.AppSettings;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonemetadata;
import com.google.i18n.phonenumbers.Phonenumber;
import com.vitorpamplona.netrometer.utils.DeviceModelParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NetrometerApplication extends Application{

    public static boolean DEVELOPER_MODE = BuildConfig.DEBUG;

    private static NetrometerApplication mSingleton;

    private AppSettings mAppSettings;
    boolean mIsDebuggable;

    private SQLiteHelper mSqliteHelper, mDevSqliteHelper;

    public String getStorageRoot() {
        return Environment.getExternalStorageDirectory() + "/EyeNetra/";
    }

    public String getLocalFilePath() {
        return getStorageRoot() + getSettings().getOrgName() + "/";
    }

    public String getLocalMeasurementsPath() {
        return getLocalFilePath() + "Measurements/";
    }

    public String getLocalLastMeasurementsPath() {
        return getLocalFilePath() + "Last Measurement/";
    }

    public String getLocalToExportPath() {
        return getLocalFilePath() + "Export/";
    }



    @Override
    public void onCreate() {
        super.onCreate();

        mSingleton = this;

        mAppSettings = new AppSettings(this);
        mIsDebuggable = false; //( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );

        mSqliteHelper = new SQLiteHelper(this, false);
        mDevSqliteHelper = new SQLiteHelper(this, true);

        resetDefaultNumbers();

        restoreAppLocale();
    }

    public void resetDefaultNumbers() {
        PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();
        try {
            Method getMetadata = PhoneNumberUtil.class.getDeclaredMethod("getMetadataForRegion", String.class);
            getMetadata.setAccessible(true);

            for (String region : mPhoneUtil.getSupportedRegions()) {
                Phonenumber.PhoneNumber exampleNumber = mPhoneUtil.getExampleNumberForType(region, PhoneNumberUtil.PhoneNumberType.MOBILE);
                String logExample = null;
                if (exampleNumber != null)
                    logExample = mPhoneUtil.format(exampleNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

                Phonemetadata.PhoneMetadata desc = (Phonemetadata.PhoneMetadata) getMetadata.invoke(mPhoneUtil, region);
                String example = desc.getMobile().getExampleNumber();
                String newExample = example;
                newExample = newExample.replaceAll("123456789", "000000000");
                newExample = newExample.replaceAll("12345678", "00000000");
                newExample = newExample.replaceAll("1234567", "0000000");
                newExample = newExample.replaceAll("123456", "000000");
                newExample = newExample.replaceAll("12345", "00000");
                newExample = newExample.replaceAll("1234", "0000");
                newExample = newExample.replaceAll("123", "000");
                newExample = newExample.replaceAll("12", "00");
                //String newExample = example.replaceAll("[0-9]", "0");
                desc.getMobile().setExampleNumber(newExample);

                Phonenumber.PhoneNumber newExampleNumber = mPhoneUtil.getExampleNumberForType(region, PhoneNumberUtil.PhoneNumberType.MOBILE);
                String logNewExample = null;
                if (newExampleNumber != null)
                    logNewExample = mPhoneUtil.format(
                            newExampleNumber,
                            PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

                Log.v("Default Numbers", region + ": " + logExample + " -> " + logNewExample);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void restoreAppLocale() {
        // restore original locale
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        conf.locale = getSettings().getAppLocale();;
        res.updateConfiguration(conf, null);
    }

    public AppSettings getSettings() {
        return mAppSettings;
    }
    public boolean isDebuggable() {
        return mIsDebuggable;
    }

    public SQLiteHelper getSqliteHelper() {
        return getSqliteHelper(DEVELOPER_MODE);
    }

    public SQLiteHelper getSqliteHelper(boolean isDev) {
        return (isDev) ? mDevSqliteHelper : mSqliteHelper;
    }

    public static NetrometerApplication get() {
        return mSingleton;
    }

    public String getVersionName() {
        String versionName = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            //TODO?
        }
        return versionName;
    }


    public AbstractDevice getDevice() {
        return DeviceDataset.get(getSettings().getHardwareId());
    }

    public ExamResults buildNewExamResults() {
        AppSettings settings = getSettings();
        ExamResults e = new ExamResults(UUID.randomUUID());
        e.setExamDate(new Date());
        e.setStudyName(settings.getStudyName());
        if (e.getSequenceNumber() == 0) {
            e.setSequenceNumber(settings.getSequenceNumber());
            settings.setSequenceNumber(settings.getSequenceNumber() + 1);
        }
        if (NetrometerApplication.DEVELOPER_MODE) {
            e.setEnvironment("Netrometer Prog Dev");
        } else {
            e.setEnvironment("Netrometer Prog");
        }

        e.setUserToken(settings.getLoggedInUserToken());
        e.setUserName(settings.getLoggedInUsername());
        e.setDevice(settings.getHardwareId());
        e.setAppVersion(getVersionName());

        return e;
    }

    String[] SUPPORTED_S4s = new String[] {
        "Samsung GT-I9505G","Samsung GT-I9505", "Samsung GT-I9500",
        "Samsung GT-I9508", "Samsung SAMSUNG-SGH-I337", "Samsung SGH-I337M",
        "Samsung GT-I9515L", "Samsung SCH-I545", "Samsung SGH-M919N", "Samsung SGH-M919"
    };

    public boolean isSamsungS4() {
        String deviceModel = DeviceModelParser.getDeviceName();

        List<String> listS4s = Arrays.asList(SUPPORTED_S4s);

        return listS4s.contains(deviceModel);
    }
}
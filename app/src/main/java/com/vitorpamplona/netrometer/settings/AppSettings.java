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

import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.imageprocessing.utils.FittingCoefficients;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppSettings extends AppSettingsBase {

    private static String SETTING_LOGGED_IN_USER_TOKEN = "account.id";
    private static String SETTING_LOGGED_IN_USER_USERNAME = "account.username";

    private static String SETTING_FITTING_COEFFICIENT_A = "coefficient_a";
    private static String SETTING_FITTING_COEFFICIENT_B = "coefficient_b";
    private static String SETTING_FITTING_COEFFICIENT_C = "coefficient_c";
    private static String SETTING_FITTING_COEFFICIENT_D = "coefficient_d";

    private static String SETTING_HARDWARE_ID = "hardware_id";
    public static long SETTING_HARDWARE_ID_DEFAULT = -1;

    public static String SETTING_STUDY_NAME = "study_name";
    private static String SETTING_SEQUENCE_NUMBER = "sequence_number";

    private final String SETTING_APP_LOCALE_LANG = "app_locale_language";
    private final String SETTING_APP_LOCALE_COUNTRY = "app_locale_country";

    private final String SETTING_NEGATIVE_CYLS = "negative_cyls";
    private final String SETTING_IMPERIAL_SYSTEM = "imperial_system";
    private final String SETTING_SPHEROCYLINDRICAL_CORRECTIONS = "spherocylindrical";

    private final String SETTING_WHEN_TO_RECONNECT = "when_to_reconnect";

    private final String SETTING_USING_SMART_STAGE = "using_smart_stage";

    private final String SETTING_FIRST_NAME = "user_first_name";
    private final String SETTING_LAST_NAME = "user_last_name";
    private final String SETTING_CAN_PRESCRIBE = "user_can_prescribe";
    private final String SETTING_ORG_NAME = "user_org_name";
    private final String SETTING_ACTIVE_ACCOUNT = "active_account";
    private final String SETTING_CAMERA_PREVIEW_ACTIVE = "camera_preview";

    private final String SETTING_LOGO_REMOTE_PATH = "user_logo_remote_path";
    private final String SETTING_LOGO_LOCAL_PATH = "user_logo_local_path";
    private final String SETTING_LOGO_LAST_MODIFIED = "user_logo_last_modified";
    private final String SETTING_LOGO_ACTIVE = "user_logo_active";

    private final String SETTING_LAST_FLAG = "last_flag";


    public AppSettings(Context context) {
        super(context);
    }

    public FittingCoefficients getFittingCoefficients() {

        return new FittingCoefficients(
                getFloat(SETTING_FITTING_COEFFICIENT_A, (float) Params.FITTING_COEFFICIENTS_A),
                getFloat(SETTING_FITTING_COEFFICIENT_B, (float) Params.FITTING_COEFFICIENTS_B),
                getFloat(SETTING_FITTING_COEFFICIENT_C, (float) Params.FITTING_COEFFICIENTS_C),
                getFloat(SETTING_FITTING_COEFFICIENT_D, (float) Params.FITTING_COEFFICIENTS_D)
                );
    }

    public void setFittingCoefficients(FittingCoefficients f) {
        setFloat(SETTING_FITTING_COEFFICIENT_A, (float) f.A);
        setFloat(SETTING_FITTING_COEFFICIENT_B, (float) f.B);
        setFloat(SETTING_FITTING_COEFFICIENT_C, (float) f.C);
        setFloat(SETTING_FITTING_COEFFICIENT_D, (float) f.D);
    }

    public long getHardwareId(){
        long id = getLong(SETTING_HARDWARE_ID, SETTING_HARDWARE_ID_DEFAULT);

        // On updates, it deletes the shared preferences.
        if (id == SETTING_HARDWARE_ID_DEFAULT && getLoggedInUsername() != null) {
            // Look for a good Id in the database.
            id = NetrometerApplication.get().getSqliteHelper().findLastUsedID(getLoggedInUsername());
            setHardwareId(id);
        }

        return id;
    }

    public boolean isDeviceKnown() {
        return getHardwareId() > 0;
    }

    public void setHardwareId(long hardwareId) {
        setLong(SETTING_HARDWARE_ID, hardwareId);
    }

    public String getLoggedInUserToken() {
        return getString(SETTING_LOGGED_IN_USER_TOKEN, null);
    }

    public void setLoggedInUserToken(String userId) {
        setString(SETTING_LOGGED_IN_USER_TOKEN, userId);
    }

    public String getLoggedInUsername() {
        return getString(SETTING_LOGGED_IN_USER_USERNAME, "guest");
    }

    public void setLoggedInUsername(String username) {
        setString(SETTING_LOGGED_IN_USER_USERNAME, username);
    }

    public String getStudyName() {
        return getString(SETTING_STUDY_NAME, "");
    }

    public void setStudyName(String s) {
        if (getString(SETTING_STUDY_NAME, "").compareTo(s) !=0) {
            setSequenceNumber(1);
        }
        setString(SETTING_STUDY_NAME, s);
    }

    public int getSequenceNumber() {
        int i = getInt(SETTING_SEQUENCE_NUMBER, 1);
        return (i > 0) ? i : 1;
    }

    public void setSequenceNumber(int i) {
        if (i > 0) {
            setInt(SETTING_SEQUENCE_NUMBER, i);
        } else {
            setInt(SETTING_SEQUENCE_NUMBER, 1);
        }
    }

    public boolean isAskingToRelogin() {
        Long datetimeToRelogin = getLong(SETTING_WHEN_TO_RECONNECT, 0L);
        if (datetimeToRelogin == 0) return false;

        Date now = Calendar.getInstance().getTime();
        Date dateToRelogin = new Date(datetimeToRelogin);

        if (now.after(dateToRelogin)) {
            return true;
        } else {
            return false;
        }
    }
    public boolean willNotAskToReloginInTheNext(int timeInHours) {
        Calendar pretendToBe = Calendar.getInstance();
        pretendToBe.add(Calendar.HOUR, timeInHours);

        Long datetimeToRelogin = getLong(SETTING_WHEN_TO_RECONNECT, 0L);
        if (datetimeToRelogin == 0) return true;

        Date dateToRelogin = new Date(datetimeToRelogin);

        if (pretendToBe.getTime().after(dateToRelogin)) {
            return false;
        } else {
            return true;
        }
    }

    public void resetTimerAskRelogin() {
        askToReloginIn(744); // 1 month.
    }

    public void askToReloginIn(int timeInHours) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, timeInHours);
        setLong(SETTING_WHEN_TO_RECONNECT, now.getTimeInMillis());
    }

    public void requestRelogin() {
        if (willNotAskToReloginInTheNext(48)) {
            askToReloginIn(0);
        }
    }

    public boolean isCanPrescribe() {
        return getBoolean(SETTING_CAN_PRESCRIBE, false);
    }
    public void setCanPrescribe(boolean b) {
        setBoolean(SETTING_CAN_PRESCRIBE, b);
    }


    public boolean isSpherocylindricalMode() {
        return getBoolean(SETTING_SPHEROCYLINDRICAL_CORRECTIONS, true);
    }
    public void setSpherocylindricalMode(boolean b) {
        setBoolean(SETTING_SPHEROCYLINDRICAL_CORRECTIONS, b);
    }

    public void toggleSpherocylindricalMode() {
        setSpherocylindricalMode(!isSpherocylindricalMode());
    }

    public boolean isImperialSystem() {
        return getBoolean(SETTING_IMPERIAL_SYSTEM, true);
    }
    public void setImperialSystem(boolean b) {
        setBoolean(SETTING_IMPERIAL_SYSTEM, b);
    }
    public void toggleImperialSystem() {
        setImperialSystem(!isImperialSystem());
    }

    public boolean isNegativeCylModel() {
        return getBoolean(SETTING_NEGATIVE_CYLS, true);
    }
    public void setNegativeCylModel(boolean b) {
        setBoolean(SETTING_NEGATIVE_CYLS, b);
    }

    public void toggleCylModel() {
        setNegativeCylModel(!isNegativeCylModel());
    }

    public void setLastSmartStageChoice(boolean usingSmartStage) { setBoolean(SETTING_USING_SMART_STAGE, usingSmartStage); }
    public boolean wasUsingSmartStage() { return getBoolean(SETTING_USING_SMART_STAGE, false); }

    public void setAppLocale(String lang, String country) {
        setString(SETTING_APP_LOCALE_LANG, lang);
        setString(SETTING_APP_LOCALE_COUNTRY, country);
    }

    public Locale getAppLocale() {
        return new Locale(
                getString(SETTING_APP_LOCALE_LANG, Locale.getDefault().getLanguage()),
                getString(SETTING_APP_LOCALE_COUNTRY, Locale.getDefault().getCountry()));
    }

    public String getLastFlag() {
        return getString(SETTING_LAST_FLAG, null);
    }
    public void setLastFlag(String s) {
        setString(SETTING_LAST_FLAG, s);
    }

    public String getUserFirstName() {
        return getString(SETTING_FIRST_NAME, "");
    }
    public void setUserFirstName(String s) {
        setString(SETTING_FIRST_NAME, s);
    }

    public String getLogoRemotePath() {
        return getString(SETTING_LOGO_REMOTE_PATH, "");
    }
    public void setLogoRemotePath(String s) {
        setString(SETTING_LOGO_REMOTE_PATH, s);
    }

    public String getLogoLocalPath() {
        return getString(SETTING_LOGO_LOCAL_PATH, "");
    }
    public void setLogoLocalPath(String s) {
        setString(SETTING_LOGO_LOCAL_PATH, s);
    }

    public long getLogoLastModified() {
        return getLong(SETTING_LOGO_LAST_MODIFIED, 0);
    }
    public void setLogoLastModified(long s) {
        setLong(SETTING_LOGO_LAST_MODIFIED, s);
    }

    public boolean isLogoActive() {
        return getBoolean(SETTING_LOGO_ACTIVE, true);
    }
    public void setLogoActive(boolean b) {
        setBoolean(SETTING_LOGO_ACTIVE, b);
    }

    public String getUserLastName() {
        return getString(SETTING_LAST_NAME, "");
    }
    public void setUserLastName(String s) {
        setString(SETTING_LAST_NAME, s);
    }

    public String getOrgName() {
        return getString(SETTING_ORG_NAME, "");
    }
    public void setOrgName(String s) {
        setString(SETTING_ORG_NAME, s);
    }

    public boolean isCameraPreviewActive() {
        return getBoolean(SETTING_CAMERA_PREVIEW_ACTIVE, false);
    }
    public void setCameraPreviewActive(boolean s) {
        setBoolean(SETTING_CAMERA_PREVIEW_ACTIVE, s);
    }

}

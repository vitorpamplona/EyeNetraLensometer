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

import com.vitorpamplona.netrometer.utils.DataUtil;
import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.model.RefractionType;
import com.vitorpamplona.netrometer.model.db.objects.DebugExam;
import com.vitorpamplona.netrometer.model.db.objects.Refraction;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportLastReading {

    public File newFile() throws IOException {
        File direct = new File(NetrometerApplication.get().getLocalLastMeasurementsPath());

        if (!direct.exists()) {
            direct.mkdirs();
        }

        File jsonFile = new File(
                NetrometerApplication.get().getLocalLastMeasurementsPath() + "anonymous_result.json");

        return jsonFile;
    }

    public void save(DebugExam e) {
        try {
            FileWriter writer = new FileWriter(newFile());
            writer.append(buildAnonymizedJson(e));
            writer.flush();
            writer.close();
        } catch (Exception e1) {
        }
    }

    public String buildAnonymizedJson(DebugExam exam) {
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonReadings = new JSONObject();

        DataUtil.put(jsonReadings, "uuid", exam.getSyncId().toString());
        DataUtil.put(jsonReadings, "exam_date", exam.getTested());
        DataUtil.put(jsonReadings, "latitude", exam.getLatitude());
        DataUtil.put(jsonReadings, "longitude", exam.getLongitude());
        DataUtil.put(jsonReadings, "app_version", exam.getAppVersion());
        DataUtil.put(jsonReadings, "device_id", exam.getDeviceId());

        Refraction netra = exam.getRefraction(RefractionType.ENTERING_RX);
        Refraction netrometer = exam.getRefraction(RefractionType.ENTERING_RX);
        Refraction subjective = exam.getRefraction(RefractionType.SUBJECTIVE);

        if (netra == null && netrometer == null && subjective != null) {
            DataUtil.put(jsonReadings, "test_method", "Netrometer");
            addRefraction(exam, jsonReadings, subjective, "adj_results");
        }

        if (netra != null) {
            DataUtil.put(jsonReadings, "test_method", "Netra");
            addRefraction(exam, jsonReadings, netra, "results");
            addRefraction(exam, jsonReadings, subjective, "adj_results");
        }

        if (netrometer != null) {
            DataUtil.put(jsonReadings, "test_method", "Netrometer");
            addRefraction(exam, jsonReadings, netrometer, "results");
            addRefraction(exam, jsonReadings, subjective, "adj_results");
        }

        DataUtil.put(jsonRoot, "readings", jsonReadings);

        return jsonRoot.toString();
    }

    private void addRefraction(DebugExam exam, JSONObject json, Refraction ref, String resultsField) {
        if (ref != null) {

            JSONObject jsonResults = new JSONObject();

            DataUtil.put(jsonResults, "distance_type", "Distance");
            DataUtil.put(jsonResults, "va", ref.getBinocularAcuity());

            JSONObject jsonRightEye = new JSONObject();

            DataUtil.put(jsonRightEye, "eye", "Right");
            DataUtil.put(jsonRightEye, "sphere",    ref.getRightSphere());
            DataUtil.put(jsonRightEye, "cylinder",  ref.getRightCylinder());
            DataUtil.put(jsonRightEye, "axis",      ref.getRightAxis());
            DataUtil.put(jsonRightEye, "add",       ref.getRightAdd());
            DataUtil.put(jsonRightEye, "pd",        ref.getRightPd());
            DataUtil.put(jsonRightEye, "va",        ref.getRightAcuity());
            DataUtil.put(jsonRightEye, "confidence", exam.getFittingQualityRight());

            DataUtil.put(jsonResults, "right_eye", jsonRightEye);

            JSONObject jsonLeftEye = new JSONObject();

            DataUtil.put(jsonLeftEye, "eye", "Left");
            DataUtil.put(jsonLeftEye, "sphere",     ref.getLeftSphere());
            DataUtil.put(jsonLeftEye, "cylinder",   ref.getLeftCylinder());
            DataUtil.put(jsonLeftEye, "axis",       ref.getLeftAxis());
            DataUtil.put(jsonLeftEye, "add",        ref.getLeftAdd());
            DataUtil.put(jsonLeftEye, "pd",         ref.getLeftPd());
            DataUtil.put(jsonLeftEye, "va",         ref.getLeftAcuity());
            DataUtil.put(jsonLeftEye, "confidence", exam.getFittingQualityLeft());

            DataUtil.put(jsonResults, "left_eye",  jsonLeftEye);

            DataUtil.put(json, resultsField, jsonResults);

        }
    }

}

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
package com.vitorpamplona.netrometer.model.db.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vitorpamplona.netrometer.utils.DataUtil;
import com.vitorpamplona.netrometer.model.RecommendedUseType;
import com.vitorpamplona.netrometer.model.db.Column;
import com.vitorpamplona.netrometer.model.db.Column.ColumnType;
import com.vitorpamplona.netrometer.model.db.SQLiteHelper;
import com.vitorpamplona.netrometer.model.db.objects.DebugExam;
import com.vitorpamplona.netrometer.model.db.objects.SQLiteModel;
import com.vitorpamplona.netrometer.settings.AppSettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DebugExamTable extends Table {

	public static final String TABLE_NAME = "debug_exam";

	public static final String TESTED = "tested";

	public static final String STUDY_NAME = "study_name";
	public static final String SEQUENCE_NUMBER = "sequence_number";
	public static final String ENVIRONMENT = "environment";
	public static final String SHARE_WITH = "share_with";
	public static final String SERVER_USER_TOKEN = "server_user_token";
	public static final String SERVER_USER_NAME = "server_user_name";

	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String SMART_STAGE = "smartstage";
	public static final String FITTING_QUALITY_LEFT = "fitting_quality_left";
	public static final String FITTING_QUALITY_RIGHT = "fitting_quality_right";

	public static final String DISTANCE_PREFERENCE = "distance_preference";
	public static final String NEAR_PREFERENCE = "near_preference";

	public static final String DATE_OF_BIRTH = "date_of_birth";

	public static final String DEVICE_ID = "device_id";
	public static final String APP_VERSION = "app_version";
	public static final String STATUS = "status";

	public static final String PRESCRIPTION_EXPIRATION_DATE = "prescription_expiration_date";
	public static final String PRESCRIPTION_EMAIL = "prescription_email";
	public static final String PRESCRIPTION_PHONE = "prescription_phone";
	public static final String PRESCRIPTION_RECOMMENDED_USE = "prescription_recommended_use";
	public static final String PRESCRIPTION_SYNC_ID = "prescription_sync_id";


	public DebugExamTable(SQLiteHelper helper) {
		super(helper, new Column[]{

				new Column(ID, ColumnType.INTEGER, true, true, true, null),
				new Column(SYNC_ID, ColumnType.TEXT),

				new Column(CREATED, ColumnType.TIMESTAMP),
				new Column(UPDATED, ColumnType.TIMESTAMP),
				new Column(TESTED, ColumnType.TIMESTAMP),
				new Column(SYNCED, ColumnType.TIMESTAMP),

				new Column(TO_SYNC_DEBUG, ColumnType.BOOLEAN),
				new Column(TO_SYNC_INSIGHT, ColumnType.BOOLEAN),
				new Column(CAN_DELETE, ColumnType.BOOLEAN),
				new Column(SMART_STAGE, ColumnType.BOOLEAN),

				new Column(STUDY_NAME, ColumnType.TEXT),
				new Column(SEQUENCE_NUMBER, ColumnType.INTEGER),
				new Column(ENVIRONMENT, ColumnType.TEXT),
				new Column(SHARE_WITH, ColumnType.TEXT),
				new Column(SERVER_USER_TOKEN, ColumnType.TEXT),
				new Column(SERVER_USER_NAME, ColumnType.TEXT),

				new Column(DEVICE_ID, ColumnType.INTEGER),
				new Column(APP_VERSION, ColumnType.TEXT),
				new Column(STATUS, ColumnType.TEXT),

				new Column(LATITUDE, ColumnType.REAL),
				new Column(LONGITUDE, ColumnType.REAL),

				new Column(DISTANCE_PREFERENCE, ColumnType.TEXT),
				new Column(NEAR_PREFERENCE, ColumnType.TEXT),

				new Column(DATE_OF_BIRTH, ColumnType.DATE),
				new Column(FITTING_QUALITY_LEFT, ColumnType.REAL),
				new Column(FITTING_QUALITY_RIGHT, ColumnType.REAL),

				new Column(PRESCRIPTION_EXPIRATION_DATE, ColumnType.DATE),
				new Column(PRESCRIPTION_EMAIL, ColumnType.TEXT),
				new Column(PRESCRIPTION_PHONE, ColumnType.TEXT),
				new Column(PRESCRIPTION_RECOMMENDED_USE, ColumnType.TEXT),
				new Column(PRESCRIPTION_SYNC_ID, ColumnType.INTEGER)


				});
	}

	@Override
	public String getName() {
		return DebugExamTable.TABLE_NAME;
	}

	public Cursor findAll(String username) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		String selection = SERVER_USER_NAME + "=? AND (" + STATUS + " IS NULL OR " + STATUS + "!=?)";
		String[] selectionArgs = new String[]{ username, "archived" };
		String[] columns = new String[]{ getIdName(), TESTED, STATUS };

		Cursor c = db.query(getName(), columns, selection, selectionArgs, null, null, getIdName() + " DESC");

		return c;
	}

	public long findLastUsedID(String username) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		String selection = SERVER_USER_NAME + "=? AND " + DEVICE_ID + " != " + AppSettings.SETTING_HARDWARE_ID_DEFAULT;
		String[] selectionArgs = new String[]{ username };
		String[] columns = new String[]{ DEVICE_ID };

		Cursor c = db.query(getName(), columns, selection, selectionArgs, null, null, CREATED + " DESC");

		Long lastId = AppSettings.SETTING_HARDWARE_ID_DEFAULT;

		if (c.moveToFirst()) {
			Long lastIdTemp = DataUtil.getLong(c, DEVICE_ID);

			if (lastIdTemp != null) {
				lastId = lastIdTemp;
			}
		}

		c.close();

		return lastId;
	}

	public int countToSyncIds(String username) {
		List<Long> ids = new ArrayList<Long>();

		String selection = SERVER_USER_NAME + "=? AND (" +
				TO_SYNC_INSIGHT + "=? or " + TO_SYNC_DEBUG + "=? or " + PRESCRIPTION_SYNC_ID + "=?)"
				+ " AND (" + STATUS + " IS NULL OR " + STATUS + "!=?)";
		String[] selectionArgs = new String[]{ username, "1", "1", "-1" , "archived" };

		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), new String[]{ getIdName() }, selection, selectionArgs, null, null, null);
		int ret = c.getCount();
		c.close();

		//db.close();

		return ret;
	}

	public int countMeasurements(String username) {
		List<Long> ids = new ArrayList<Long>();

		String selection = SERVER_USER_NAME + "=?" + " AND (" + STATUS + " IS NULL OR " + STATUS + "!=?)";
		String[] selectionArgs = new String[]{ username, "archived" };

		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), new String[]{ getIdName() }, selection, selectionArgs, null, null, null);
		int ret = c.getCount();
		c.close();

		//db.close();

		return ret;
	}

	public int countPrescriptions(String username) {
		List<Long> ids = new ArrayList<Long>();

		String selection = SERVER_USER_NAME + "=? AND " + PRESCRIPTION_SYNC_ID + " IS NOT NULL " + "AND (" + STATUS + " IS NULL OR " + STATUS + "!=?)";
		String[] selectionArgs = new String[]{ username, "archived" };

		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), new String[]{ getIdName() }, selection, selectionArgs, null, null, null);
		int ret = c.getCount();
		c.close();

		//db.close();

		return ret;
	}


	public void setToSyncPrescription(SQLiteModel m) {
		ContentValues cv = new ContentValues();
		cv.put(PRESCRIPTION_SYNC_ID, -1);
		save(m, cv);
	}

	public void setSyncedPrescription(SQLiteModel m, Integer prescriptionId) {
		ContentValues cv = new ContentValues();
		cv.put(PRESCRIPTION_SYNC_ID, prescriptionId);
		save(m, cv);
	}

	public List<Long> findToSyncPrescriptionIds() {
		List<Long> ids = new ArrayList<Long>();

		String selection = PRESCRIPTION_SYNC_ID + "=?";
		String[] selectionArgs = new String[]{ "-1" };

		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), new String[]{ getIdName() }, selection, selectionArgs, null, null, null);

		while (c.moveToNext()) {
			ids.add(DataUtil.getLong(c, getIdName()));
		}

		c.close();

		//db.close();

		return ids;
	}

	public void resetUserTokenInsight(SQLiteModel m, String newToken) {
		m.setSynced(new Date());
		ContentValues cv = new ContentValues();
		cv.put(SERVER_USER_TOKEN, newToken);
		save(m, cv);
	}

	public DebugExam findLastResult() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), null, null, null, null, null, getIdName() + " DESC", "1");

		DebugExam ret = null;
		if (c.moveToFirst()) {
			ret = new DebugExam();
			ret.setId(DataUtil.getLong(c, getIdName()));
			ret.updateFromCursor(c);
		}

		c.close();

		return ret;
	}

	public void saveStudySequenceShare(DebugExam m) {
		ContentValues cv = new ContentValues();
		cv.put(STUDY_NAME, m.getStudyName());
		cv.put(SEQUENCE_NUMBER, m.getSequenceNumber());
		cv.put(SHARE_WITH, m.getShareWith());
		cv.put(SERVER_USER_TOKEN, m.getUserToken());
		save(m, cv);
	}


	public void delete(Long id) {
		String[] selectionArgs = new String[]{ id.toString() };

		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		String selection = RefractionTable.DEBUG_EXAM_ID + "=?";

		db.delete(RefractionTable.TABLE_NAME, selection, selectionArgs);

		selection = DebugExamTable.ID + "=?";

		db.delete(getName(), selection, selectionArgs);

		//db.close();
	}

	public void save(DebugExam m) {
		ContentValues cv = new ContentValues();

		//cv.put(ID, m.getId());

		cv.put(SYNC_ID, DataUtil.uuidToString(m.getSyncId()));

		//cv.put(CREATED, DataUtil.dateToTimestampString(m.getCreated()));
		//cv.put(UPDATED, DataUtil.dateToTimestampString(m.getUpdated()));
		//cv.put(SYNCED, DataUtil.dateToTimestampString(m.getSynced()));
		cv.put(TESTED, DataUtil.dateToTimestampString(m.getTested()));

		//cv.put(TO_SYNC_DEBUG, m.getToSync());
		//cv.put(TO_SYNC_INSIGHT, m.getToSyncInsight());

		cv.put(DEVICE_ID, m.getDeviceId());
		cv.put(APP_VERSION, m.getAppVersion());
		cv.put(STATUS, m.getStatus());

		cv.put(STUDY_NAME, m.getStudyName());
		cv.put(SERVER_USER_TOKEN, m.getUserToken());
		cv.put(SERVER_USER_NAME, m.getUserName());

		cv.put(SEQUENCE_NUMBER, m.getSequenceNumber());
		cv.put(ENVIRONMENT, m.getEnvironment());
		cv.put(SHARE_WITH, m.getShareWith());

		cv.put(LATITUDE, m.getLatitude());
		cv.put(LONGITUDE, m.getLongitude());
		cv.put(SMART_STAGE, m.getSmartStage());

		cv.put(PRESCRIPTION_EMAIL, m.getPrescriptionEmail());
		cv.put(PRESCRIPTION_PHONE, m.getPrescriptionPhone());
		cv.put(PRESCRIPTION_EXPIRATION_DATE, DataUtil.dateToDateString(m.getPrescriptionExpiration()));
		cv.put(PRESCRIPTION_RECOMMENDED_USE, DataUtil.enumListToJsonString(RecommendedUseType.class, m.getPrescriptionRecommendedUse()));

		cv.put(DISTANCE_PREFERENCE, DataUtil.enumToString(m.getDistancePreference()));
		cv.put(NEAR_PREFERENCE, DataUtil.enumToString(m.getNearPreference()));

		cv.put(DATE_OF_BIRTH, DataUtil.dateToDateString(m.getDateOfBirth()));
		cv.put(FITTING_QUALITY_LEFT, m.getFittingQualityLeft());
		cv.put(FITTING_QUALITY_RIGHT, m.getFittingQualityRight());

		save(m, cv);
	}
}

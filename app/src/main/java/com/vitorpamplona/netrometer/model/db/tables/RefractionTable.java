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
import com.vitorpamplona.netrometer.model.RefractionType;
import com.vitorpamplona.netrometer.model.db.Column;
import com.vitorpamplona.netrometer.model.db.Column.ColumnType;
import com.vitorpamplona.netrometer.model.db.SQLiteHelper;
import com.vitorpamplona.netrometer.model.db.objects.DebugExam;
import com.vitorpamplona.netrometer.model.db.objects.Refraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefractionTable extends Table {
	
protected DebugExam mDebugExam;
	
	protected RefractionType mRefractionType;
	
	protected Float mBinocularAcuity;
	
	protected Float mRightPd;
	protected Float mRightSphere;
	protected Float mRightCylinder;
	protected Float mRightAxis;
	protected Float mRightAdd;
	protected Float mRightAcuity;
	
	protected String mRightOriginalData;
	protected String mRightHistory;
	
	protected Float mLeftPd;
	protected Float mLeftSphere;
	protected Float mLeftCylinder;
	protected Float mLeftAxis;
	protected Float mLeftAdd;
	protected Float mLeftAcuity;
	
	protected String mLeftOriginalData;
	protected String mLeftHistory;
	
	public static final String TABLE_NAME = "refraction";
	
	public static final String DEBUG_EXAM_ID = "debug_exam_id";
	public static final String REFRACTION_TYPE = "refraction_type";
	public static final String BINOCULAR_ACUITY = "binocular_acuity";
	
	public static final String RIGHT_PD = "right_pd";
	public static final String RIGHT_SPHERE = "right_sphere";
	public static final String RIGHT_CYLINDER = "right_cylinder";
	public static final String RIGHT_AXIS = "right_axis";
	public static final String RIGHT_ADD = "right_add";
	public static final String RIGHT_ACUITY = "right_acuity";
	public static final String RIGHT_ORIGINAL_DATA = "right_original_data";
	public static final String RIGHT_HISTORY = "right_history";
	public static final String RIGHT_NETRO = "right_netro";
	
	public static final String LEFT_PD = "left_pd";
	public static final String LEFT_SPHERE = "left_sphere";
	public static final String LEFT_CYLINDER = "left_cylinder";
	public static final String LEFT_AXIS = "left_axis";
	public static final String LEFT_ADD = "left_add";
	public static final String LEFT_ACUITY = "left_acuity";
	public static final String LEFT_ORIGINAL_DATA = "left_original_data";
	public static final String LEFT_HISTORY = "left_history";
	public static final String LEFT_NETRO = "left_netro";
	
	public RefractionTable(SQLiteHelper helper) {
		super(helper, new Column[]{
				
				new Column(ID, ColumnType.INTEGER, true, true, true, null),
				new Column(SYNC_ID, ColumnType.TEXT),
				
				new Column(CREATED, ColumnType.TIMESTAMP),
				new Column(UPDATED, ColumnType.TIMESTAMP),
				new Column(SYNCED, ColumnType.TIMESTAMP),
				
				new Column(TO_SYNC_DEBUG, ColumnType.BOOLEAN),
				new Column(TO_SYNC_INSIGHT, ColumnType.BOOLEAN),
				new Column(CAN_DELETE, ColumnType.BOOLEAN),
				
				new Column(DEBUG_EXAM_ID, ColumnType.INTEGER),
				new Column(REFRACTION_TYPE, ColumnType.TEXT),
				new Column(BINOCULAR_ACUITY, ColumnType.REAL),
				
				new Column(RIGHT_PD, ColumnType.REAL),
				new Column(RIGHT_SPHERE, ColumnType.REAL),
				new Column(RIGHT_CYLINDER, ColumnType.REAL),
				new Column(RIGHT_AXIS, ColumnType.REAL),
				new Column(RIGHT_ADD, ColumnType.REAL),
				new Column(RIGHT_ACUITY, ColumnType.REAL),
				new Column(RIGHT_ORIGINAL_DATA, ColumnType.TEXT),
				new Column(RIGHT_HISTORY, ColumnType.TEXT),
				new Column(RIGHT_NETRO, ColumnType.TEXT),
				
				new Column(LEFT_PD, ColumnType.REAL),
				new Column(LEFT_SPHERE, ColumnType.REAL),
				new Column(LEFT_CYLINDER, ColumnType.REAL),
				new Column(LEFT_AXIS, ColumnType.REAL),
				new Column(LEFT_ADD, ColumnType.REAL),
				new Column(LEFT_ACUITY, ColumnType.REAL),
				new Column(LEFT_ORIGINAL_DATA, ColumnType.TEXT),
				new Column(LEFT_HISTORY, ColumnType.TEXT),
				new Column(LEFT_NETRO, ColumnType.TEXT)
				
				});
	}

	@Override
	public String getName() {
		return RefractionTable.TABLE_NAME;
	}
	
	public Map<RefractionType, Refraction> findRefractions(DebugExam e) {
		Map<RefractionType, Refraction> rm = new HashMap<RefractionType, Refraction>();
		List<Long> ids = new ArrayList<Long>();
		
		String selection = DEBUG_EXAM_ID + "=?";
		String[] selectionArgs = new String[]{ e.getId().toString() };
		
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), new String[]{ getIdName() }, selection, selectionArgs, null, null, null);
		
		while (c.moveToNext()) {
			ids.add(DataUtil.getLong(c, getIdName()));
		}
		
		c.close();
		
		//db.close();
		
		for (Long id : ids) {
			Refraction r = new Refraction();
			r.setId(id);
			find(r);
			r.setDebugExam(e);
			rm.put(r.getRefractionType(), r);
		}
		
		return rm;
	}
	
	public void save(Refraction m) {
		ContentValues cv = new ContentValues();
		
		//cv.put(ID, m.getId());
		
		cv.put(SYNC_ID, DataUtil.uuidToString(m.getSyncId()));
		
		//cv.put(CREATED, DataUtil.dateToTimestampString(m.getCreated()));
		//cv.put(UPDATED, DataUtil.dateToTimestampString(m.getUpdated()));
		//cv.put(SYNCED, DataUtil.dateToTimestampString(m.getSynced()));
		
		//cv.put(TO_SYNC_DEBUG, m.getToSync());
		//cv.put(TO_SYNC_INSIGHT, m.getToSync());
		cv.put(DEBUG_EXAM_ID, m.getDebugExam().getId());
		cv.put(REFRACTION_TYPE, DataUtil.enumToString(m.getRefractionType()));
		cv.put(BINOCULAR_ACUITY, m.getBinocularAcuity());
		
		cv.put(RIGHT_PD, m.getRightPd());
		cv.put(RIGHT_SPHERE, m.getRightSphere());
		cv.put(RIGHT_CYLINDER, m.getRightCylinder());
		cv.put(RIGHT_AXIS, m.getRightAxis());
		cv.put(RIGHT_ADD, m.getRightAdd());
		cv.put(RIGHT_ACUITY, m.getRightAcuity());
		cv.put(RIGHT_ORIGINAL_DATA, DataUtil.compress(m.getRightOriginalData()));
		cv.put(RIGHT_HISTORY, DataUtil.compress(m.getRightHistory()));
		cv.put(RIGHT_NETRO, DataUtil.compress(m.getRightNetro()));

		cv.put(LEFT_PD, m.getLeftPd());
		cv.put(LEFT_SPHERE, m.getLeftSphere());
		cv.put(LEFT_CYLINDER, m.getLeftCylinder());
		cv.put(LEFT_AXIS, m.getLeftAxis());
		cv.put(LEFT_ADD, m.getLeftAdd());
		cv.put(LEFT_ACUITY, m.getLeftAcuity());
		cv.put(LEFT_ORIGINAL_DATA, DataUtil.compress(m.getLeftOriginalData()));
		cv.put(LEFT_HISTORY, DataUtil.compress(m.getLeftHistory()));
		cv.put(LEFT_NETRO, DataUtil.compress(m.getLeftNetro()));
		
		save(m, cv);
	}
}

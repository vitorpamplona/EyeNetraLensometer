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
import com.vitorpamplona.netrometer.model.db.Column;
import com.vitorpamplona.netrometer.model.db.SQLiteHelper;
import com.vitorpamplona.netrometer.model.db.objects.SQLiteModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Table {
	
	public static final String ID = "id";
	public static final String SYNC_ID = "sync_id";
	public static final String CUSTOMER_ID = "customer_id";
	
	public static final String CREATED = "created";
	public static final String UPDATED = "updated";
	public static final String DELETED = "deleted";
	public static final String SYNCED = "synced";
	
	public static final String TO_SYNC_DEBUG = "to_sync";
	public static final String TO_SYNC_INSIGHT = "to_sync_insight";
	public static final String CAN_DELETE = "can_delete";
	
	public static final String DATA = "data"; // for encrypted intake form / exam data
	
	protected final Map<String, Column> mColumnMap = new HashMap<String, Column>();
	protected final List<Column> mColumns = new ArrayList<Column>();
	protected final List<String> mColumnNames = new ArrayList<String>();
	
	protected SQLiteHelper mDbHelper;
	
	public Table(SQLiteHelper helper, Column...columns) {
		mDbHelper = helper;
		
		for (int i = 0; i < columns.length; i++) {
		    Column c = columns[i];
		    mColumns.add(c);
		    mColumnNames.add(c.name);
		    mColumnMap.put(c.name, c);
		}
	}
	
	public abstract String getName();
	
	public String getIdName() {
		return ID;
	}
	
	public String getSyncIdName() {
		return SYNC_ID;
	}
	
	public String getCustomerIdName() {
		return CUSTOMER_ID;
	}
	
	public List<Column> getColumns() {
		return mColumns;
	}
	
	public List<String> getColumnNames() {
		return mColumnNames;
	}
	
	public Column getColumn(String columnName) {
		return mColumnMap.get(columnName);
	}
	
	public void find(SQLiteModel m) {	
		if (m.getId() != null) {
			selectById(m);
		} else if (m.getSyncId() != null) {
			selectBySyncId(m);
		} else if (m.getCustomerId() != null) {
			selectByCustomerId(m);
		}
	}

	public Cursor findAll() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), new String[]{ getIdName() }, null, null, null, null, getIdName() + " DESC");

		return c;
	}

	public List<Long> findSyncedIdsThatCanBeDeleted() {
		List<Long> ids = new ArrayList<Long>();

		String selection = TO_SYNC_DEBUG + "=? and " + TO_SYNC_INSIGHT + "=? and " + CAN_DELETE + "=?";
		String[] selectionArgs = new String[]{ "0", "0", "1" };

		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), new String[]{ getIdName() }, selection, selectionArgs, null, null, null);

		while (c.moveToNext()) {
			ids.add(DataUtil.getLong(c, getIdName()));
		}

		c.close();

		//db.close();

		return ids;
	}

	public List<Long> findToSyncDebugIds() {
		List<Long> ids = new ArrayList<Long>();

		String selection = TO_SYNC_DEBUG + "=?";
		String[] selectionArgs = new String[]{ "1" };

		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), new String[]{ getIdName() }, selection, selectionArgs, null, null, null);

		while (c.moveToNext()) {
			ids.add(DataUtil.getLong(c, getIdName()));
		}

		c.close();

		//db.close();

		return ids;
	}

	public List<Long> findToSyncInsightIds() {
		List<Long> ids = new ArrayList<Long>();

		String selection = TO_SYNC_INSIGHT + "=?";
		String[] selectionArgs = new String[]{ "1" };

		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), new String[]{ getIdName() }, selection, selectionArgs, null, null, null);

		while (c.moveToNext()) {
			ids.add(DataUtil.getLong(c, getIdName()));
		}

		c.close();

		//db.close();

		return ids;
	}
	
	public void setSyncedDebug(SQLiteModel m) {
		m.setSynced(new Date());
		ContentValues cv = new ContentValues();
		cv.put(TO_SYNC_DEBUG, false);
		cv.put(SYNCED, DataUtil.dateToTimestampString(m.getSynced()));
		save(m, cv);
	}

	public void setSyncedInsight(SQLiteModel m) {
		m.setSynced(new Date());
		ContentValues cv = new ContentValues();
		cv.put(TO_SYNC_INSIGHT, false);
		cv.put(SYNCED, DataUtil.dateToTimestampString(m.getSynced()));
		save(m, cv);
	}
	
	//should not be called after every model change
	public void setToSyncDebug(SQLiteModel m) {
		m.setToSync(true);
		ContentValues cv = new ContentValues();
		cv.put(TO_SYNC_DEBUG, true);
		save(m, cv);
	}

	//should not be called after every model change
	public void setReadyToDeleteWhenSync(SQLiteModel m) {
		m.setToSync(true);
		ContentValues cv = new ContentValues();
		cv.put(CAN_DELETE, true);
		save(m, cv);
	}

	//should not be called after every model change
	public void setToSyncInsight(SQLiteModel m) {
		m.setToSync(true);
		ContentValues cv = new ContentValues();
		cv.put(TO_SYNC_INSIGHT, true);
		save(m, cv);
	}
	
	public void saveCustomerId(SQLiteModel m) {
		ContentValues cv = new ContentValues();
		cv.put(CUSTOMER_ID, m.getCustomerId());
		save(m, cv);
	}
	
	public void save(SQLiteModel m, ContentValues cv) {
		m.setUpdated(new Date());
		cv.put(UPDATED, DataUtil.dateToTimestampString(m.getUpdated()));
		
		// in the sqlite db already
		if (m.getId() != null) {
			// m.getSyncId() should not be null in this case
			update(m, cv);
			
		// syncing from server, *might* be in the sqlite db
		} else if (m.getSyncId() != null) {
			if (updateBySyncId(m, cv) == 0) {
				insert(m, cv);
			} else {
				setIdFromSyncId(m);
			}
			
		// not in the sqlite db or on the server
		} else {
			m.setSyncId(UUID.randomUUID());
			cv.put(SYNC_ID, m.getSyncId().toString());
			insert(m, cv);
		}
		//mDbHelper.printTable(this);
		//print content values?
	}
	
	protected void selectById(SQLiteModel m) {
		
		String selection = getIdName() + "=?";
		String[] selectionArgs = new String[]{ m.getId().toString() };
		
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), null, selection, selectionArgs, null, null, null);
	
		if (c.getCount() > 0) {
			c.moveToFirst();
			m.updateFromCursor(c);
		}
		
		c.close();
		
		//db.close();
	}
	
	protected void selectByCustomerId(SQLiteModel m) {
		
		String selection = getCustomerIdName() + "=?";
		String[] selectionArgs = new String[]{ m.getCustomerId().toString() };
		
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), null, selection, selectionArgs, null, null, null);

		//TODO: this shouldn't be > 1, but if it is, throw a fit?
		if (c.getCount() > 0) {
			c.moveToFirst();
			m.updateFromCursor(c);
		}
		
		c.close();
		
		//db.close();
	}
	
	protected void selectBySyncId(SQLiteModel m) {
		
		String selection = getSyncIdName() + "=?";
		String[] selectionArgs = new String[]{ m.getSyncId().toString() };
		
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), null, selection, selectionArgs, null, null, null);
	
		if (c.getCount() > 0) {
			c.moveToFirst();
			m.updateFromCursor(c);
		} 
		
		c.close();
		
		//db.close();
	}

	protected void setIdFromSyncId(SQLiteModel m) {

		String selection = getSyncIdName() + "=?";
		String[] selectionArgs = new String[]{ m.getSyncId().toString() };

		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(getName(), new String[]{getIdName()}, selection, selectionArgs, null, null, null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			m.setId(c.getLong(c.getColumnIndex(getIdName())));
		}

		c.close();

		//db.close();
	}
	
	protected long insert(SQLiteModel m, ContentValues cv) {
		if (m.getCreated() == null) {
			m.setCreated(new Date());
			cv.put(CREATED, DataUtil.dateToTimestampString(m.getCreated()));
		}
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		long result = db.insert(getName(), null, cv);
		
		//db.close();
		
		if (result >= 0) {
			m.setId(result);
		}
		
		return result;
	}
	
	protected int update(SQLiteModel m, ContentValues cv) {
		String whereClause = getIdName() + "=?";
		String[] whereArgs = new String[]{ m.getId().toString() };
		
		return update(m, cv, whereClause, whereArgs);
	}

	protected int updateBySyncId(SQLiteModel m, ContentValues cv) {
		String whereClause = getSyncIdName() + "=?";
		String[] whereArgs = new String[]{ m.getSyncId().toString() };
		
		return update(m, cv, whereClause, whereArgs);
	}
	
	protected int update(SQLiteModel m, ContentValues cv, String whereClause, String[] whereArgs) {
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		int result = db.update(getName(), cv, whereClause, whereArgs);
		
		//db.close();
		
		return result;
	}
	

	
}

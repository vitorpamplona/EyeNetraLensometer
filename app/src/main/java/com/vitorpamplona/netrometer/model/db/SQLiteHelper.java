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
package com.vitorpamplona.netrometer.model.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.model.db.objects.DebugExam;
import com.vitorpamplona.netrometer.model.db.objects.Refraction;
import com.vitorpamplona.netrometer.model.db.tables.DebugExamTable;
import com.vitorpamplona.netrometer.model.db.tables.RefractionTable;
import com.vitorpamplona.netrometer.model.db.tables.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SQLiteHelper extends SQLiteOpenHelper {

	public static final String TAG = "SQLiteHelper";

	public static final String DATABASE_NAME = "telerx";
	public static final String DEV_DATABASE_NAME = "debug_telerx";
	public static final int DATABASE_VERSION = 25;

	public static final String TEMP_SUFFIX = "_temp";

	protected NetrometerApplication mApp;

	protected List<Table> mTables = new ArrayList<Table>();

	public final DebugExamTable debugExamTable;
	public final RefractionTable refractionTable;

	public SQLiteHelper(NetrometerApplication app, boolean isDev) {
		super(app, (isDev ? DEV_DATABASE_NAME : DATABASE_NAME), null, DATABASE_VERSION);
		mApp = app;

		debugExamTable = new DebugExamTable(this);
		mTables.add(debugExamTable);

		refractionTable = new RefractionTable(this);
		mTables.add(refractionTable);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		for(Iterator<Table> i = mTables.iterator(); i.hasNext();) {
			createTable(db, i.next());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		try {

			db.beginTransaction();

			for (Table t : mTables) {
				upgradeTable(db, t);
			}

			db.setTransactionSuccessful();

		} catch (SQLiteException e) {
			throw e;
		} finally {
			db.endTransaction();
		}
	}

	public DebugExam findDebugExam(UUID id) {
		DebugExam e = new DebugExam();
		e.setSyncId(id);
		debugExamTable.find(e);
		e.setRefractions(refractionTable.findRefractions(e));

		return e;
	}

	public DebugExam findDebugExam(Long id) {
		DebugExam e = new DebugExam();
		e.setId(id);
		debugExamTable.find(e);
		e.setRefractions(refractionTable.findRefractions(e));

		return e;
	}

	public DebugExam lastDebugExam() {
		DebugExam e = debugExamTable.findLastResult();
		if (e != null)
			e.setRefractions(refractionTable.findRefractions(e));

		return e;
	}

	public class UsageStats {
		public int measurements;
		public int prescriptions;
		public int readyToSync;
	}

	public UsageStats stats(String username) {
		UsageStats stats = new UsageStats();

		if (username == null) return stats;

		stats.measurements = debugExamTable.countMeasurements(username);
		stats.prescriptions = debugExamTable.countPrescriptions(username);
		stats.readyToSync = debugExamTable.countToSyncIds(username);
		return stats;
	}


	public Cursor allIds(String username) {
		return debugExamTable.findAll(username);
	}


	public long findLastUsedID(String username) {
		return debugExamTable.findLastUsedID(username);
	}

	//** DEBUG SAVE **//

	public void saveDebugExam(DebugExam e) {
		debugExamTable.save(e);

		for (Refraction r : e.getRefractions().values()) {
			refractionTable.save(r);
		}
	}

	public void deleteAll(List<Long> ids) {
		for (Long id : ids) {
			debugExamTable.delete(id);
		}
	}


	//** SCHEMA CREATION/MANAGEMENT **/

	protected void createTable(SQLiteDatabase db, Table t) {

		String s = "DROP TABLE IF EXISTS " + t.getName() + ";";
		db.execSQL(s);

		s = "CREATE TABLE " + t.getName() + " (";
		for(Iterator<Column> i = t.getColumns().iterator(); i.hasNext(); ) {
			Column c = i.next();
			s += c.name + " " + c.type.getSQLiteType();

			if (c.primaryKey) {
				s += " " + Column.PRIMARY_KEY;
			}

			if (c.autoincrement) {
				s += " " + Column.AUTOINCREMENT;
			}

			if (c.notNull) {
				s += " " + Column.NOT_NULL;
			}

			if (c.defaultValue != null) {
				s += " " + Column.DEFAULT + " " + c.defaultValue;
			}

			if (i.hasNext()) {
				s += ",";
			}
		}
		s += ");";

		db.execSQL(s);
	}



	//TODO: only equipped to handle new columns; modified columns may crash
	protected void upgradeTable(SQLiteDatabase db, Table t) {

		if (tableExists(db, t.getName())) {
			String tempName = t.getName() + TEMP_SUFFIX;

			dropTable(db, tempName);
			renameTable(db, t.getName(), tempName);
			createTable(db, t);

			HashMap<String, String> oldColumns = getColumnInfo(db, tempName);
			HashMap<String, String> newColumns = getColumnInfo(db, t.getName());

			newColumns.keySet().retainAll(oldColumns.keySet());

			//test for type match
			ArrayList<String> columns = new ArrayList<String>();
			for (String key : newColumns.keySet()) {
				if (oldColumns.get(key).contentEquals(newColumns.get(key))) {
					columns.add(key);
				}
			}

			String colString = "";
			for (String col : columns) {
				colString += col + ",";
			}
			if (colString.length() > 0) {

				colString = colString.substring(0, colString.length() - 1);

				db.execSQL("INSERT INTO " + t.getName() + " (" + colString + ") SELECT " + colString + " FROM " + tempName + ";");
			}

			//printTable(db, tempName);
			//printTable(db, tableName);

			dropTable(db, tempName);
		} else {
			createTable(db, t);
		}
	}

	protected boolean tableExists(SQLiteDatabase db, String tableName) {
		boolean tableExists = false;

		Cursor c = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = ?", new String[]{ tableName });
		if (c.getCount() > 0) {
			tableExists = true;
		}

		c.close();
		return tableExists;
	}

	protected void renameTable(SQLiteDatabase db, String tableName, String newTableName) {
		db.execSQL("ALTER TABLE '" + tableName + "' RENAME TO '" + newTableName + "'");
	}

	protected void dropTable(SQLiteDatabase db, String tableName) {
		db.execSQL("DROP TABLE IF EXISTS '" + tableName + "'");
	}

	//TODO: use columns notnull(3), dflt_value(4), pk(5)?
	protected HashMap<String, String> getColumnInfo(SQLiteDatabase db, String tableName) {
		HashMap<String, String> map = new HashMap<String, String>();

		Cursor c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);

		while (c.moveToNext()) {
			//1: name, 2: type
			map.put(c.getString(1), c.getString(2));
		}
		c.close();

		return map;
	}

	//*** DEBUG ***//

	public void printTable(Table t) {
		Log.d(TAG, "==============================");
		Log.d(TAG, "--- " + t.getName() + " ---");
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(t.getName(), null, null, null, null, null, null);
		printCursor(c);
		c.close();
		//db.close();
	}

    public void printLastRow(Table table) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(table.getName(), null, null, null, null, null, table.getIdName() + " desc", "1");

        Log.d(TAG, "==============================");
        Log.d(TAG, "--- " + table.getName() + " ---");
        printCursor(c);

        c.close();

       /// db.close();
    }

    public void printRow(Table table, long id) {

        String selection = table.getIdName() + "=?";
        String[] selectionArgs = new String[]{ String.valueOf(id) };

        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(table.getName(), null, selection, selectionArgs, null, null, null);

        Log.d(TAG, "==============================");
        Log.d(TAG, "--- " + table.getName() + " ---");
        printCursor(c);

        c.close();

       // db.close();
    }

	public void printCursor(Cursor c) {
		if (c.getCount() == 0) return;

		c.moveToFirst();

		String[] names = c.getColumnNames();
		Integer[] types = new Integer[names.length];
		for (int i = 0; i < names.length; i++) {
			types[i] = c.getType(i);
		}

		Log.d(TAG, "==============================");

		c.moveToPosition(-1);

		String line = "";
		while (c.moveToNext()) {
			for (int i = 0; i < names.length; i++) {
				line = names[i] + ": ";

				switch(types[i]) {
				case Cursor.FIELD_TYPE_STRING:
					line += c.getString(i);
					break;
				case Cursor.FIELD_TYPE_INTEGER:
					line += c.getInt(i);
					break;
				case Cursor.FIELD_TYPE_FLOAT:
					line += c.getFloat(i);
					break;
				case Cursor.FIELD_TYPE_NULL:
					line += "NULL";
					break;
				case Cursor.FIELD_TYPE_BLOB:
					line += "[BLOB]";
					break;
				}

				Log.d(TAG, line);
			}
			if (!c.isLast()) {
				Log.d(TAG, "------------------------------");
			}
		}

		Log.d(TAG, "==============================");
	}


}

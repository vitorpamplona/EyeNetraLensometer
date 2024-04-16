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

public class Column {
	
	public enum ColumnType {
		BOOLEAN("boolean"),
		DATE("date"),
		INTEGER("integer"),
		REAL("real"),
		TEXT("text"),
		TIMESTAMP("timestamp");
		
		private final String mSQLiteType;
		private ColumnType(String sqliteType) {
			mSQLiteType = sqliteType;
		}
		public String getSQLiteType() {
			return mSQLiteType;
		}
	}
	
	public static final String PRIMARY_KEY = "primary key";
	public static final String AUTOINCREMENT = "autoincrement";
	public static final String NOT_NULL = "not null";
	public static final String DEFAULT = "default";
	
	public String name;
	public ColumnType type;
	public boolean primaryKey;
	public boolean autoincrement;
	public boolean notNull;
	public String defaultValue;
	
	public Column(String name, ColumnType type) {
		init(name, type, false, false, false, null);
	}
	
	public Column(String name, ColumnType type, boolean primary, boolean autoincrement, boolean notNull, String defaultValue) {
		init(name, type, primary, autoincrement, notNull, defaultValue);
	}
	
	protected void init(String name, ColumnType type, boolean primary, boolean autoincrement, boolean notNull, String defaultValue) {
		this.name = name;
		this.type = type;
		this.primaryKey = primary;
		this.notNull = notNull;
		this.defaultValue = defaultValue;
	}
}

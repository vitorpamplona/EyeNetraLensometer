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
package com.vitorpamplona.netrometer.model.db.objects;

import android.database.Cursor;

import com.vitorpamplona.netrometer.utils.DataUtil;
import com.vitorpamplona.netrometer.model.db.tables.Table;

import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

public abstract class SQLiteModel {
	
	protected Long mId, mCustomerId;
	protected UUID mSyncId;
	
	protected Date mCreated, mUpdated, mDeleted, mSynced;
	
	protected boolean mToSync;
	
	public Long getId() {
		return mId;
	}
	public void setId(Long id) {
		mId = id;
	}
	
	public UUID getSyncId() {
		return mSyncId;
	}
	public void setSyncId(UUID syncId) {
		mSyncId = syncId;
	}
	
	public Long getCustomerId() {
		return mCustomerId;
	}
	public void setCustomerId(Long id) {
		mCustomerId = id;
	}
	
	public Date getCreated() {
		return mCreated;
	}
	public void setCreated(Date d) {
		mCreated = d;
	}
	public Date getUpdated() {
		return mUpdated;
	}
	public void setUpdated(Date d) {
		mUpdated = d;
	}
	public Date getDeleted() {
		return mDeleted;
	}
	public void setDeleted(Date d) {
		mDeleted = d;
	}
	public Date getSynced() {
		return mSynced;
	}
	public void setSynced(Date d) {
		mSynced = d;
	}
	
	public boolean getToSync() {
		return mToSync;
	}
	public void setToSync(boolean b) {
		mToSync = b;
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		
		DataUtil.put(json, Table.SYNC_ID, DataUtil.uuidToString(getSyncId()));
		return json;
	}
	
	public void updateFromCursor(Cursor c) {
		mId = DataUtil.getLong(c, Table.ID);
	
		mSyncId = DataUtil.stringToUuid(DataUtil.getString(c, Table.SYNC_ID));
	}
	
	public void updateFromJson(JSONObject json) {
		//id (long) should not get received from the server

		mSyncId = DataUtil.stringToUuid(DataUtil.getString(json, Table.SYNC_ID));
	}
	
}

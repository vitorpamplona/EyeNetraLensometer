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

import com.vitorpamplona.netrometer.utils.AngleDiff;
import com.vitorpamplona.netrometer.utils.DataUtil;
import com.vitorpamplona.netrometer.model.RefractionType;
import com.vitorpamplona.netrometer.model.db.tables.RefractionTable;

import org.json.JSONObject;


public class Refraction extends SQLiteModel {
	
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
	protected String mRightNetro;
	
	protected Float mLeftPd;
	protected Float mLeftSphere;
	protected Float mLeftCylinder;
	protected Float mLeftAxis;
	protected Float mLeftAdd;
	protected Float mLeftAcuity;
	
	protected String mLeftOriginalData;
	protected String mLeftHistory;
	protected String mLeftNetro;
	
	public Refraction() {
		
	}
	public Refraction(Cursor c) {
		updateFromCursor(c);
	}
	/*
	public Refraction(JSONObject json) {
		updateFromJson(json);
	}
	*/

	public void checkAxisOutOfBounds() {
		if (mLeftAxis != null && mLeftAxis>180)
			mLeftAxis = AngleDiff.angle0to180(mLeftAxis);
		if (mRightAxis != null && mRightAxis>180)
			mRightAxis = AngleDiff.angle0to180(mRightAxis);
	}

	/**
	 * Positive Notation to Negative Notation.
	 */
	public void putInNegativeCilinder() {
		if (mLeftCylinder != null && mLeftCylinder > 0.001) {
			if (mLeftSphere == null) mLeftSphere = 0.0f;
			mLeftSphere = mLeftSphere + mLeftCylinder;
			mLeftCylinder = -mLeftCylinder;
			if (mLeftAxis == null) mLeftAxis = 0.0f;
			mLeftAxis = mLeftAxis + 90;
		}
		if (mRightCylinder != null && mRightCylinder > 0.001) {
			if (mRightSphere == null) mRightSphere = 0.0f;
			mRightSphere = mRightSphere + mRightCylinder;
			mRightCylinder = -mRightCylinder;
			if (mRightAxis == null) mRightAxis = 0.0f;
			mRightAxis = mRightAxis + 90;
		}
		checkAxisOutOfBounds();
	}

	/**
	 * Positive Notation to Negative Notation.
	 */
	public void putInPositiveCilinder() {
		if (mLeftCylinder != null && mLeftCylinder < -0.001) {
			if (mLeftSphere == null) mLeftSphere = 0.0f;
			mLeftSphere = mLeftSphere + mLeftCylinder;
			mLeftCylinder = -mLeftCylinder;
			if (mLeftAxis == null) mLeftAxis = 0.0f;
			mLeftAxis = mLeftAxis + 90;
		}
		if (mRightCylinder != null && mRightCylinder < -0.001) {
			if (mRightSphere == null) mRightSphere = 0.0f;
			mRightSphere = mRightSphere + mRightCylinder;
			mRightCylinder = -mRightCylinder;
			if (mRightAxis == null) mRightAxis = 0.0f;
			mRightAxis = mRightAxis + 90;
		}

		checkAxisOutOfBounds();
	}

	public DebugExam getDebugExam() {
		return mDebugExam;
	}
	
	public void setDebugExam(DebugExam d) {
		mDebugExam = d;
	}
	
	public RefractionType getRefractionType() {
		return mRefractionType;
	}
	public void setRefractionType(RefractionType r) {
		mRefractionType = r;
	}
	
	public Float getBinocularAcuity() {
		return mBinocularAcuity;
	}
	public void setBinocularAcuity(Float f) {
		mBinocularAcuity = f;
	}
	
	public Float getRightPd() {
		return mRightPd;
	}
	public void setRightPd(Float f) {
		mRightPd = f;
	}
	public Float getRightSphere() {
		return mRightSphere;
	}
	public void setRightSphere(Float f) {
		mRightSphere = f;
	}
	public Float getRightCylinder() {
		return mRightCylinder;
	}
	public void setRightCylinder(Float f) {
		mRightCylinder = f;
	}
	public Float getRightAxis() {
		return mRightAxis;
	}
	public void setRightAxis(Float f) {
		mRightAxis = f;
	}
	public Float getRightAdd() {
		return mRightAdd;
	}
	public void setRightAdd(Float f) {
		mRightAdd = f;
	}
	public Float getRightAcuity() {
		return mRightAcuity;
	}
	public void setRightAcuity(Float f) {
		mRightAcuity = f;
	}
	
	public String getRightOriginalData() {
		if (mRightOriginalData == null) {
			mRightOriginalData = "";
		}
		return mRightOriginalData;
	}
	public void setRightOriginalData(String s) {
		mRightOriginalData = s;
	}
	public String getRightHistory() {
		if (mRightHistory == null) {
			mRightHistory = "";
		}
		return mRightHistory;
	}
	public void setRightHistory(String s) {
		mRightHistory = s;
	}

	public String getRightNetro() {
		if (mRightNetro == null) {
			mRightNetro = "";
		}
		return mRightNetro;
	}
	public void setRightNetro(String s) {
		mRightNetro = s;
	}

	public Float getSumOfPds() {
		if (mLeftPd == null || mRightPd == null) return null;
		return mLeftPd + mRightPd;
	}

	public Float getLeftPd() {
		return mLeftPd;
	}
	public void setLeftPd(Float f) {
		mLeftPd = f;
	}
	public Float getLeftSphere() {
		return mLeftSphere;
	}
	public void setLeftSphere(Float f) {
		mLeftSphere = f;
	}
	public Float getLeftCylinder() {
		return mLeftCylinder;
	}
	public void setLeftCylinder(Float f) {
		mLeftCylinder = f;
	}
	public Float getLeftAxis() {
		return mLeftAxis;
	}
	public void setLeftAxis(Float f) {
		mLeftAxis = f;
	}
	public Float getLeftAdd() {
		return mLeftAdd;
	}
	public void setLeftAdd(Float f) {
		mLeftAdd = f;
	}
	public Float getLeftAcuity() {
		return mLeftAcuity;
	}
	public void setLeftAcuity(Float f) {
		mLeftAcuity = f;
	}
	
	public String getLeftOriginalData() {
		if (mLeftOriginalData == null) {
			mLeftOriginalData = "";
		}
		return mLeftOriginalData;
	}
	public void setLeftOriginalData(String s) {
		mLeftOriginalData = s;
	}
	public String getLeftHistory() {
		if (mLeftHistory == null) {
			mLeftHistory = "";
		}
		return mLeftHistory;
	}
	public void setLeftHistory(String s) {
		mLeftHistory = s;
	}

	public String getLeftNetro() {
		if (mLeftNetro == null) {
			mLeftNetro = "";
		}
		return mLeftNetro;
	}
	public void setLeftNetro(String s) {
		mLeftNetro = s;
	}

	public Float getPd() {
		if (mLeftPd != null && mRightPd != null) {
			return mLeftPd + mRightPd;
		} else if (mLeftPd != null) {
			return mLeftPd;
		} else if (mRightPd != null) {
			return mRightPd;
		} else {
			return null;
		}
	}

	public Float getHalfPd() {
		if (mLeftPd != null && mRightPd != null) {
			return (mLeftPd + mRightPd)/2;
		} else if (mLeftPd != null) {
			return mLeftPd;
		} else if (mRightPd != null) {
			return mRightPd;
		} else {
			return null;
		}
	}
	
	
	@Override
	public void updateFromCursor(Cursor c) {
		super.updateFromCursor(c);
		
		//mDebugExam set elsewhere
		
		setRefractionType(DataUtil.stringToEnum(RefractionType.class, DataUtil.getString(c, RefractionTable.REFRACTION_TYPE)));
		
		setBinocularAcuity(DataUtil.getFloat(c, RefractionTable.BINOCULAR_ACUITY));
		
		setRightPd(DataUtil.getFloat(c, RefractionTable.RIGHT_PD));
		setRightSphere(DataUtil.getFloat(c, RefractionTable.RIGHT_SPHERE));
		setRightCylinder(DataUtil.getFloat(c, RefractionTable.RIGHT_CYLINDER));
		setRightAxis(DataUtil.getFloat(c, RefractionTable.RIGHT_AXIS));
		setRightAdd(DataUtil.getFloat(c, RefractionTable.RIGHT_ADD));
		setRightAcuity(DataUtil.getFloat(c, RefractionTable.RIGHT_ACUITY));
		
		setRightOriginalData(DataUtil.decompress(DataUtil.getByteArray(c, RefractionTable.RIGHT_ORIGINAL_DATA)));
		setRightHistory(DataUtil.decompress(DataUtil.getByteArray(c, RefractionTable.RIGHT_HISTORY)));
		setRightNetro(DataUtil.decompress(DataUtil.getByteArray(c, RefractionTable.RIGHT_NETRO)));

		setLeftPd(DataUtil.getFloat(c, RefractionTable.LEFT_PD));
		setLeftSphere(DataUtil.getFloat(c, RefractionTable.LEFT_SPHERE));
		setLeftCylinder(DataUtil.getFloat(c, RefractionTable.LEFT_CYLINDER));
		setLeftAxis(DataUtil.getFloat(c, RefractionTable.LEFT_AXIS));
		setLeftAdd(DataUtil.getFloat(c, RefractionTable.LEFT_ADD));
		setLeftAcuity(DataUtil.getFloat(c, RefractionTable.LEFT_ACUITY));
		
		setLeftOriginalData(DataUtil.decompress(DataUtil.getByteArray(c, RefractionTable.LEFT_ORIGINAL_DATA)));
		setLeftHistory(DataUtil.decompress(DataUtil.getByteArray(c, RefractionTable.LEFT_HISTORY)));
		setLeftNetro(DataUtil.decompress(DataUtil.getByteArray(c, RefractionTable.LEFT_NETRO)));
		
	}
	
	@Override
	public void updateFromJson(JSONObject json) {
		if (json == null) {
			return;
		}
		
		super.updateFromJson(json);
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		
		return json;
	}

	public boolean equalsRight (Refraction o) {
		if (o==null) return false;
		boolean sphEquals = (getRightSphere() == null && o.getRightSphere() == null) || (getRightSphere() != null && o.getRightSphere() != null && (Math.abs(getRightSphere() - o.getRightSphere()) < 0.1));
		boolean cylEquals = (getRightCylinder() == null && o.getRightCylinder() == null) || (getRightCylinder() != null && o.getRightCylinder() != null && (Math.abs(getRightCylinder() - o.getRightCylinder()) < 0.1));
		boolean axisEquals = (getRightAxis() == null && o.getRightAxis() == null) || (getRightAxis() != null && o.getRightAxis() != null && (Math.abs(getRightAxis() - o.getRightAxis()) < 0.1));

		return (sphEquals && cylEquals && axisEquals);
	}

	public boolean equalsLeft (Refraction o) {
		if (o==null) return false;
		boolean sphEquals = (getLeftSphere() == null && o.getLeftSphere() == null) || (getLeftSphere() != null && o.getLeftSphere() != null && (Math.abs(getLeftSphere() - o.getLeftSphere()) < 0.1));
		boolean cylEquals = (getLeftCylinder() == null && o.getLeftCylinder() == null) || (getLeftCylinder() != null && o.getLeftCylinder() != null && (Math.abs(getLeftCylinder() - o.getLeftCylinder()) < 0.1));
		boolean axisEquals = (getLeftAxis() == null && o.getLeftAxis() == null) || (getLeftAxis() != null && o.getLeftAxis() != null && (Math.abs(getLeftAxis() - o.getLeftAxis()) < 0.1));

		return (sphEquals && cylEquals && axisEquals);
	}
}

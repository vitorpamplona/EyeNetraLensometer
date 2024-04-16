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
package com.vitorpamplona.netrometer.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class ExamResults implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int RIGHT = 0;
    public static final int LEFT = 1;

    private UUID id;

    private long device;
    private String appVersion;
    
    private Date examDate;
    private String studyName;
    private String shareWith;
    private int sequenceNumber;

    private String userToken;
    private String userName;

    private double latitude;
    private double longitude;

    private Eye leftEye;
    private Eye rightEye;

    private boolean smartStage;

    private int cloudID;
    private String environment;

    public ExamResults() {
        setDefaults();
    }

    public ExamResults(UUID id) {
        this.id = id;
        setDefaults();
    }

    /**
     * Set defaults for results here
     */
    private void setDefaults() {
    	cloudID = -1;
    }


    /**
     * Lazy Initialization
     */
    public Eye leftEye() {
        if (leftEye == null) leftEye = new Eye();
        return leftEye;
    }

    /**
     * Lazy Initialization
     */
    public Eye rightEye() {
        if (rightEye == null) rightEye = new Eye();
        return rightEye;
    }

    public Eye getLeftEye() {
        return leftEye();
    }

    public Eye getRightEye() {
        return rightEye();
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID i) {
        id = i;
    }

    public Date getExamDate() {
        if (null == examDate) {
            examDate = Calendar.getInstance().getTime();
        }
        return examDate;
    }

    public String getFormattedExamDate() {
        if (null == examDate) {
            examDate = Calendar.getInstance().getTime();
        }
        return examDate.getMonth() + "/" + examDate.getDay() + "/" + (examDate.getYear() + 1900);
    }

    public void setExamDate(Date d) {
        examDate = d;
    }
    
    public String getStudyName() {
    	return studyName;
    }
    
    public void setStudyName(String s) {
    	studyName = s;
    }
    
    public int getSequenceNumber() {
    	return sequenceNumber;
    }
    
    public void setSequenceNumber(int i) {
    	sequenceNumber = i;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLeftEye(Eye leftEye) {
        this.leftEye = leftEye;
    }

    public void setRightEye(Eye rightEye) {
        this.rightEye = rightEye;
    }

	public long getDevice() {
		return device;
	}

	public void setDevice(long device) {
		this.device = device;
	}

	public String getShareWith() {
		return shareWith;
	}

	public void setShareWith(String shareWith) {
		this.shareWith = shareWith;
	}
	
	public void setReadyToSubmit() {
		cloudID = 0;
	}
	
	public boolean isReadyToSubmit() {
		return cloudID == 0;
	}
	
	public boolean wasPushed() {
		return cloudID > 0;
	}
	
	public void setCloudID(int id) {
		cloudID = id;
	}
	
	public int getCloudID() {
		return cloudID;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean getSmartStage() {
        return smartStage;
    }

    public void setSmartStage(boolean f) {
        smartStage = f;
    }
}

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
package com.vitorpamplona.netrometer.imageprocessing.hardware;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.TreeMap;

public class DeviceDataset implements Serializable {

	public enum PreviewFrameSize { // valid resolutions for Galaxy S4
		R1920x1080 	(1920,1080),
		R1440x1080 	(1440,1080),
		R1280x720 	(1280,720),
		R1056x864 	(1056,864),
		R960x720 	(960,720),
		R720x480 	(720,480),
		R640x480 	(640,480),
		R320x240 	(320,240),
		R176x144 	(176,144);

		public final int WIDTH;
		public final int HEIGHT;

		PreviewFrameSize(int width, int height) {
	        this.WIDTH = width;
	        this.HEIGHT = height;
		}
	}

	private static final long serialVersionUID = 1L;

	private static NavigableMap<Long, AbstractDevice> DEVICES = new TreeMap<Long, AbstractDevice>() {{
	    // 201 - 3D printed MVP with APD
		put(1l,  new DeviceV100(1l,   "CNCed", 		"MVP", 	 "LaunchPad"));

		put(20l, new DeviceV1011(20l,  "Injection",  "V1.011", "LaunchPad"));

		put(126l, new DeviceV1012(126l,  "Injection",  "V1.012", "LaunchPad"));

		put(4000l,  new DeviceV200(4000l,   "HousePrinter", 		"MVPMotoZ", 	 "LaunchPad"));
	}};

	private static AbstractDevice DEFAULT_DEVICE = internalGet(20);

	public static AbstractDevice get(long id) {
		if (id < 0) return DEFAULT_DEVICE;

		return internalGet(id);
	}

	private static AbstractDevice internalGet(long id) {
		// DEVICE ranges are not uniform and there are 'holes'.
		// In such cases, return the device of the last existing ID before the requested ID.
		if (DEVICES.floorEntry(id) == null) return null;
		AbstractDevice baseModel = DEVICES.floorEntry(id).getValue();
		if (baseModel == null) return null;
		AbstractDevice dynamicInstance = baseModel.clone();
		dynamicInstance.id = id;
		return dynamicInstance;
	}


}

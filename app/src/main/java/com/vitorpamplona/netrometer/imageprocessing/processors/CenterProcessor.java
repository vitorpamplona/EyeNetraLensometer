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
package com.vitorpamplona.netrometer.imageprocessing.processors;

import com.vitorpamplona.netrometer.imageprocessing.utils.FrameTools;
import com.vitorpamplona.netrometer.utils.Rect;
import com.vitorpamplona.netrometer.imageprocessing.utils.YuvFilter;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Point2D;

public class CenterProcessor {
		
	private final int previewWidth;
	private final int previewHeight;
	
	private Rect searchBox;
	private int refineDistance = 10;
	private YuvFilter filter = Params.NO_FILTER;
    private FrameTools tools;
	
	public CenterProcessor(int imWidth, int imHeight) {

		previewWidth = imWidth;
		previewHeight = imHeight;

		searchBox = new Rect(
				(int)(previewWidth/2 - previewWidth*0.05f),
				(int)(previewHeight/2 - previewHeight*0.05f),
				(int)(previewWidth/2 + previewWidth*0.05f),
				(int)(previewHeight/2 + previewHeight*0.05f));

        tools = new FrameTools(Params.PREVIEW_FRAME_WIDTH,Params.PREVIEW_FRAME_HEIGHT);

	}

	public Point2D run(byte[] data) {
		
		Point2D center;
		center = tools.findGeneralCenterOfMass(data, searchBox, 1, filter);
        if (center == null) return null;

		return tools.refinePosition(data, center, refineDistance, 1, filter);

	}

	public CenterProcessor setFilter(YuvFilter f) {
		filter = f;
        return this;
	}
	
	public CenterProcessor setRefineDistance(int d) {
		refineDistance = d;
        return this;
	}
	
	public CenterProcessor setSearchBox(Rect b) {
		searchBox = b;
		return this;
	}

}

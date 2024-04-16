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
package com.vitorpamplona.netrometer.activity.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.utils.Point2D;

public class CrosshairView extends View {

	private Paint mPaint = new Paint();
	private Point2D mGuideVector;
	private boolean mIsPositiveDiopters = false;
	private boolean mIsInsideValidZone = false;
    private boolean mIsInsideMaxRadius = false;
    private int mMaxRadius = 50;
    private int mZoneRadius = 10;
    private int mPosX = 0;
    private int mPosY = 0;

    public CrosshairView(Context context) {
		super(context);
	}

	public CrosshairView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public CrosshairView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onDraw(Canvas canvas) {
        if (mGuideVector == null) return;
        checkIfInsideValidZone();

        drawTargetCircleFill(canvas);

        drawTargetCircleStroke(canvas);
        drawDotPosition(canvas);
	}

    private void checkIfInsideValidZone() {
        float magnitude = mGuideVector.magnitude();
        mIsInsideValidZone = magnitude < mZoneRadius;
        mIsInsideMaxRadius = magnitude < mMaxRadius;
    }

    private void drawTargetCircleFill(Canvas canvas) {
        mPaint.reset();
        mPaint.setStyle(Paint.Style.FILL);

        if (mIsInsideValidZone) {
            mPaint.setColor(getResources().getColor(R.color.crosshair_circle_fill));
        } else {
            mPaint.setColor(getResources().getColor(android.R.color.transparent));
        }

        canvas.drawCircle(mPosX, mPosY, mZoneRadius, mPaint);
    }

    private void drawTargetCircleStroke(Canvas canvas) {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        if (mIsInsideValidZone) {
            mPaint.setColor(getResources().getColor(R.color.crosshair_circle_stroke));
            mPaint.setStrokeWidth(getResources().getInteger(R.integer.crosshair_circle_stroke_inside));
        } else {
            mPaint.setColor(getResources().getColor(R.color.crosshair_circle_stroke_wrong));
            mPaint.setStrokeWidth(getResources().getInteger(R.integer.crosshair_circle_stroke));
        }
        canvas.drawCircle(mPosX, mPosY, mZoneRadius, mPaint);
    }
	
	private void drawDotPosition(Canvas canvas) {
        mPaint.reset();

        // don't draw if outside max radius
        if (!mIsInsideMaxRadius) return;

        // rotate by 'alpha' radians to mimic glasses direction
        double alpha;
        if (mIsPositiveDiopters) {
            alpha = +Math.PI/2;
        } else {
            alpha = -Math.PI/2;
        }
        float rotX = (float) (mGuideVector.x*Math.cos(alpha) - mGuideVector.y*Math.sin(alpha));
        float rotY = (float) (mGuideVector.x*Math.sin(alpha) + mGuideVector.y*Math.cos(alpha));
        mGuideVector = new Point2D(rotX,rotY);

        mPaint.setColor(getResources().getColor(R.color.crosshair_center));
        canvas.drawCircle(mPosX+mGuideVector.x, mPosY+mGuideVector.y, 15, mPaint);
	}

    public void reset() {
        mPosX = 0;
        mPosY = 0;
        mGuideVector = null;
        mZoneRadius = 10;
    }

	public void setCenterGuide(Point2D guideVector, boolean positiveDiopters) {
		mGuideVector = guideVector;
		mIsPositiveDiopters = positiveDiopters;
        invalidate();
	}
    
    public void setGuideCenterPosition(int x, int y) {
        mPosX = x;
        mPosY = y;
    }
    
    public void setValidZoneRadius(int zoneRadius) {
        mZoneRadius = zoneRadius;
    }

    public void setMaxRadius(int maxRadius) {
        mMaxRadius = maxRadius;
    }

    public void setGuideVectorNull() {
        mIsInsideValidZone= false;
    }

    public boolean isInsideValidZone() {
        return mIsInsideValidZone;
    }

    public boolean isInsideMaxRadius() {
        return mIsInsideMaxRadius;
    }

}
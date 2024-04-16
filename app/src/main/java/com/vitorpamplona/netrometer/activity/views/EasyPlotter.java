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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.vitorpamplona.netrometer.utils.Point2D;

import java.util.ArrayList;
import java.util.List;

public class EasyPlotter extends View{
	
	private List<SettingsWrapper> plotItems = new ArrayList<SettingsWrapper>();
	private Paint paint = new Paint();

	public EasyPlotter(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public EasyPlotter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public EasyPlotter(Context context) {
		super(context);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		
		// unwrap and draw to canvas
		for (SettingsWrapper w : plotItems) {
			w.drawToCanvas(canvas);
		}

	}
	
	
	///// draw actions
	
	public EasyPlotter clearData() {
		plotItems.clear();
        return this;
	}
	
	public EasyPlotter plotNow() {
		invalidate();
        return this;
	}
	
	///// setters
	
	public EasyPlotter point(Point2D point, String style) {
		plotItems.add(new PointSettings(point, parseStyleSettings(style)));
        return this;
	}

	public EasyPlotter point(float x, float y, String style) {
		point(new Point2D(x, y), style);
        return this;
	}

	public EasyPlotter rectangle(Rect rect, String style, float strokeWidth) {
		plotItems.add(new RectangleSettings(rect, parseStyleSettings(style), strokeWidth));
        return this;
	}
	
	public EasyPlotter rectangle(Rect rect, String style) {
		rectangle(rect, style, 1);
        return this;
	}

    public EasyPlotter rectangle(RectF rect, String style, float strokeWidth) {
        plotItems.add(new RectangleSettings(rect, parseStyleSettings(style), strokeWidth));
        return this;
    }

    public EasyPlotter rectangle(RectF rect, String style) {
        rectangle(rect, style, 1);
        return this;
    }

	public EasyPlotter circle(Point2D center, float rad, String style, float strokeWidth) {
		plotItems.add(new CircleSettings(center, rad, parseStyleSettings(style), strokeWidth));
        return this;
	}
	
	public EasyPlotter circle(Point2D center, float rad, String style) {
		circle(center, rad, style, 1);
        return this;
	}

	public EasyPlotter line(Point2D p1, Point2D p2, String style, float strokeWidth) {
		plotItems.add(new LineSettings(p1, p2, parseStyleSettings(style), strokeWidth));
        return this;
	}
	
	public EasyPlotter line(Point2D p1, Point2D p2, String style) {
		line(p1, p2, style, 1);
        return this;
	}
	
	public EasyPlotter line(Path p, String style, float strokeWidth) {
		plotItems.add(new PathSettings(p, parseStyleSettings(style), strokeWidth));
        return this;
	}
	
	public EasyPlotter line(Path p, String style) {
		line(p, style, 1);
        return this;
	}

    public EasyPlotter text(Point2D location, String text, String style, float fontSize) {
        plotItems.add(new TextSettings(location, text, parseStyleSettings(style), fontSize));
        return this;
    }

    public EasyPlotter text(Point2D location, String text, String style) {
        text(location, text, style, 15);  // TODO what is the default text size on canvas?
        return this;
    }
	
	///// worker methods
	
	private StyleSettings parseStyleSettings(String style) {
		
		int len = style.length();
		int colorStroke = Color.BLACK;  // default
		int colorFill = Color.TRANSPARENT;  // default
		boolean defineStroke = true;
    	int color = Color.BLACK;

		for (int i=0; i<len; i++){
										    
	    	switch (style.charAt(i)) {
	    	
		    	// check hex values
		    	case '#':
		    		if ((len-i)>8) {
		    			String sub = style.substring(i+1,i+9);
		    			if (sub.matches("[0-9A-Fa-f]+")) {
		    				color = Color.parseColor("#" + sub);
		    			}
		    		}
	    			i+=8;
	    			break;
	    	
	    		// android standard colors
		    	case 't': color = Color.TRANSPARENT; break;
		    	case 'k': color = Color.BLACK; break;
		    	case 'b': color = Color.BLUE; break;
		    	case 'c': color = Color.CYAN; break;
		    	case 'g': color = Color.GREEN; break;
		    	case 'm': color = Color.MAGENTA; break;
		    	case 'r': color = Color.RED; break;
		    	case 'w': color = Color.WHITE; break;
		    	case 'y': color = Color.YELLOW; break;
		    	// custom colors
		    	case 'o': color = Color.parseColor("#ffff4444"); break; // orange
		    	case 'l': color = Color.parseColor("#ff44ff44"); break; // lime
		    	case 'i': color = Color.parseColor("#ff4444ff"); break; // cream blue
                case 'p': color = Color.parseColor("#ffff69b4"); break; // sexy pink
		    	
		    	// if no valid character, try next
		    	default: continue;
		    	
	    	}
		    
	    	if (defineStroke) {
	    		colorStroke = color;
	    		defineStroke = false;
	    	} else {
	    		colorFill = color;
	    		break; // got both, quit
	    	}
		    
		}
		
		return new StyleSettings(colorStroke, colorFill);
	}
	
	
	
	///// utility classes
	
	private class PointSettings implements SettingsWrapper {
		public Point2D point;
		public StyleSettings styleSettings;
		
		public PointSettings(Point2D point, StyleSettings styleSettings) {
			this.point = point;
			this.styleSettings = styleSettings;
		}
		
		@Override
		public void drawToCanvas(Canvas c) {
			// plot points
			paint.reset();
			// stroke
			paint.setColor(styleSettings.colorStroke);
			c.drawPoint(point.x, point.y, paint);
		}
	}
	
	private class RectangleSettings implements SettingsWrapper {
		public RectF rectangle;
		public StyleSettings styleSettings;
		public float strokeWidth;
		
		public RectangleSettings(Rect rectangle, StyleSettings styleSettings, float strokeWidth) {
			this(new RectF(rectangle), styleSettings, strokeWidth);
		}

        public RectangleSettings(RectF rectangle, StyleSettings styleSettings, float strokeWidth) {
            this.rectangle = rectangle;
            this.styleSettings = styleSettings;
            this.strokeWidth = strokeWidth;
        }

		@Override
		public void drawToCanvas(Canvas c) {
			// plot rectangles
			paint.reset();
			// background
			paint.setStyle(Paint.Style.FILL);
			if (styleSettings.colorFill != Color.TRANSPARENT) {
				paint.setColor(styleSettings.colorFill);
				c.drawRect(rectangle, paint);
			}
			// stroke
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(styleSettings.colorStroke);
			paint.setStrokeWidth(strokeWidth);
			c.drawRect(rectangle, paint);
		}
	}
	
	private class CircleSettings implements SettingsWrapper {
		public Point2D center;
		public float radius;
		public StyleSettings styleSettings;
		public float strokeWidth;
		
		public CircleSettings(Point2D center, float radius, StyleSettings styleSettings, float strokeWidth) {
			this.center = center;
			this.radius = radius;
			this.styleSettings = styleSettings;
			this.strokeWidth = strokeWidth;
		}
		
		@Override
		public void drawToCanvas(Canvas c) {
			// plot circles
			paint.reset();
			// background
			paint.setStyle(Paint.Style.FILL);
			if (styleSettings.colorFill != Color.TRANSPARENT) {
				paint.setColor(styleSettings.colorFill);
				c.drawCircle(center.x, center.y, radius, paint);
			}
			// stroke
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(styleSettings.colorStroke);
			paint.setStrokeWidth(strokeWidth);
			c.drawCircle(center.x, center.y, radius, paint);
		}
	}
	
	private class LineSettings implements SettingsWrapper {
		public Point2D point1;
		public Point2D point2;
		public StyleSettings styleSettings;
		public float strokeWidth;
		
		public LineSettings(Point2D point1, Point2D point2, StyleSettings styleSettings, float strokeWidth) {
			this.point1 = point1;
			this.point2 = point2;
			this.styleSettings = styleSettings;
			this.strokeWidth = strokeWidth;
		}
		
		@Override
		public void drawToCanvas(Canvas c) {
			// plot lines
			paint.reset();
			// stroke
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(styleSettings.colorStroke);
			paint.setStrokeWidth(strokeWidth);
			c.drawLine(point1.x, point1.y, point2.x, point2.y, paint);
		}
	}
	
	private class PathSettings implements SettingsWrapper {
		public Path path;
		public StyleSettings styleSettings;
		public float strokeWidth;
		
		public PathSettings(Path path, StyleSettings styleSettings, float strokeWidth) {
			this.path = path;
			this.styleSettings = styleSettings;
			this.strokeWidth = strokeWidth;
		}
		
		@Override
		public void drawToCanvas(Canvas c) {
			// plot paths
			paint.reset();
			// stroke
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(styleSettings.colorStroke);
			paint.setStrokeWidth(strokeWidth);
			c.drawPath(path, paint);
		}
	}

    private class TextSettings implements SettingsWrapper {
        public Point2D location;
        public String text;
        public StyleSettings styleSettings;
        public float fontSize;

        public TextSettings(Point2D location, String text, StyleSettings style, float fontSize) {
            this.location = location;
            this.text = text;
            this.styleSettings = style;
            this.fontSize = fontSize;
        }

        @Override
        public void drawToCanvas(Canvas c) {
            // plot texts
            paint.reset();
            // stroke
            paint.setColor(styleSettings.colorStroke);
            paint.setTextSize(fontSize);
            c.drawText(text, location.x, location.y, paint);
        }
    }


	// Style settings:
	private class StyleSettings {
		public int colorStroke;
		public int colorFill;
		
		public StyleSettings(int colorStroke, int colorFill) {
			this.colorStroke = colorStroke;
			this.colorFill = colorFill;
		}
	}
	
	
	public interface SettingsWrapper {
		void drawToCanvas(Canvas c);
	}



	
}

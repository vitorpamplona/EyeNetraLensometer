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
package com.vitorpamplona.netrometer.utils;

import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataUtil {

	protected static final SimpleDateFormat mTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public static Boolean ifNull(Boolean value, boolean ifNull) {
		if (value == null) {
			return ifNull;
		} else {
			return value;
		}
	}
	
	//TODO: remove copied json code
	public static <E extends Enum<E>> String enumListToJsonString(Class<E> c, List<E> l) {
		String s = "[]";
		
		JSONArray a = new JSONArray();
		for (E e : l) {
			try {
				a.put(e.name());
			} catch (Exception exc) {
	    		exc.printStackTrace();
			}
		}
			
		s = a.toString();
		
		return s;
	}
	
	public static <E extends Enum<E>> List<E> jsonStringToEnumList(Class<E> c, String s) {
		List<E> l = new ArrayList<E>();
		
		JSONArray a = null;
		if (s != null) {
			try {
				 a = new JSONArray(s);
			} catch (JSONException exc) {
	    		exc.printStackTrace();
			}
		}
		
		if (a != null) {
			for (int i = 0; i < a.length(); i++) {
				try {
					l.add((E)Enum.valueOf(c, a.getString(i)));
				} catch (Exception exc) {
		    		exc.printStackTrace();
				}
			}
		}
		
		return l;
	}
	
	public static <E extends Enum<E>> JSONArray enumListToJsonArray(Class<E> c, List<E> l) {

		JSONArray a = new JSONArray();
		for (E e : l) {
			try {
				a.put(e.name());
			} catch (Exception exc) {
	    		exc.printStackTrace();
			}
		}
		
		return a;
	}
	
	public static <E extends Enum<E>> List<E> jsonArrayToEnumList(Class<E> c, JSONArray j) {
		List<E> l = new ArrayList<E>();

		if (j != null) {
			for (int i = 0; i < j.length(); i++) {
				try {
					l.add((E)Enum.valueOf(c, j.getString(i)));
				} catch (Exception exc) {
		    		exc.printStackTrace();
				}
			}
		}
		
		return l;
	}
	
	public static <E extends Enum<E>, F extends Enum<F>> String enumMapToJsonString(Map<E, F> m) {
		return enumMapToJsonObject(m).toString();
	}
	
	public static <E extends Enum<E>, F extends Enum<F>> JSONObject enumMapToJsonObject(Map<E, F> m) {
		JSONObject o = new JSONObject();
		
		for (E e : m.keySet()) {
			try {
				o.put(e.name(), m.get(e).name());
			} catch (Exception exc) {
	    		exc.printStackTrace();
			}
		}
		
		return o;
	}
	
	public static <E extends Enum<E>, F extends Enum<F>> Map<E, F> jsonStringToEnumMap(Class<E> e, Class<F> f, String s) {
		JSONObject o = null;
		
		if (s != null) {
			try {
				 o = new JSONObject(s);
			} catch (JSONException exc) {
	    		exc.printStackTrace();
			}
		}

		return jsonObjectToEnumMap(e, f, o);
	}
	
	public static <E extends Enum<E>, F extends Enum<F>> Map<E, F> jsonObjectToEnumMap(Class<E> e, Class<F> f, JSONObject j) {
		Map<E, F> m = new HashMap<E, F>();

		if (j != null) {
			
			Iterator<?> keys = j.keys();

	        while (keys.hasNext()) {
	        	try {
	        		
		            String key = (String)keys.next();
		            String value = j.getString(key);
		            
		            m.put((E)Enum.valueOf(e, key), (F)Enum.valueOf(f, value));
		            
	        	} catch (Exception exc) {
		    		exc.printStackTrace();
	        	}
	        }
		}
		
		return m;
	}
	
	public static <E extends Enum<E>> String enumToString(E e) {
		String result = null;
		
		if (e != null) {
			result = e.name();
		}
		
		return result;
	}
	
	public static <E extends Enum<E>> E stringToEnum(Class<E> c, String s) {
		E result = null;
		
		if (s != null && s.length() > 0) {	
			try {
				result = (E)Enum.valueOf(c, s);
			} catch (Exception exc) {
			    exc.printStackTrace();
			}
		}
		
		return result;
	}
	
	public static String uuidToString(UUID uuid) {
		String result = null;
		
		if (uuid != null) {
			result = uuid.toString();
		}
		
		return result;
	}
	
	public static UUID stringToUuid(String s) {
		UUID result = null;
		
		if (s != null) {
			try {
				result = UUID.fromString(s);
			} catch (Exception e) {
			    e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public static boolean isValidDay(int day) {
		if (day >0 && day <=31) {
			return true;
		}
		return false;
	}
	
	public static boolean isValidMonth(int month) {
		if (month >0 && month <=12) {
			return true;
		}
		return false;
	}
	
	public static boolean isValidYear(int year) {
		Calendar c = Calendar.getInstance(); 
		if (year >0 && year <=c.get(Calendar.YEAR)) {
			return true;
		}
		return false;
	}
	
	public static Date timestampStringToDate(String s) {
		if (s == null) {
			return null;
		}
		
		Date d = null;
		try {
			d = mTimestampFormat.parse(s);
		} catch (Exception e) {
    		e.printStackTrace();
		}
		
		return d;
	}
	
	public static String dateToTimestampString(Date d) {
		if (d == null) {
			return null;
		} else {
			return mTimestampFormat.format(d);
		}
	}
	
	public static Date dateStringToDate(String s) {
		if (s == null) {
			return null;
		}
		
		Date d = null;
		try {
			d = mDateFormat.parse(s);
		} catch (Exception e) {
    		e.printStackTrace();
		}
		
		return d;
	}
	
	public static String dateToDateString(Date d) {
		if (d == null) {
			return null;
		} else {
			return mDateFormat.format(d);
		}
	}

	public static String calendarToDateString(Calendar c) {
		String date = null;
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		if (c != null) {
			date = sdf.format(c.getTime());
		}
		return date;
	}

    public static JSONObject toJsonObject(String string) {
        JSONObject result = null;
        if (string==null) return result;

        try {
            result = new JSONObject(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONArray toJsonArray(String string) {
        JSONArray result = null;
        if (string==null) return result;
        
        try {
            result = new JSONArray(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

	public static JSONObject getJsonObject(JSONObject json, String key) {
		JSONObject result = null;
		try {
			if (json.has(key) && !json.isNull(key)) {
				result = json.getJSONObject(key);
			}
		} catch (Exception e) {
    		e.printStackTrace();
		}
		return result;
	}

    public static JSONObject getJsonObject(JSONArray json, int index) {
        JSONObject result = null;
        try {
            if (index < json.length()) {
                result = json.getJSONObject(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONArray getJsonArray(JSONObject json, String key) {
        JSONArray result = null;
        try {
            if (json.has(key) && !json.isNull(key)) {
                result = json.getJSONArray(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
	
	public static String getString(JSONObject json, String key) {
		String result = null;
		try {
			if (json.has(key) && !json.isNull(key)) {
				result = json.getString(key);
			}
		} catch (Exception e) {
    		e.printStackTrace();
		}
		return result;
	}
	
	public static Integer getInteger(JSONObject json, String key) {
		Integer result = null;
		try {
			if (json.has(key) && !json.isNull(key)) {
				result = json.getInt(key);
			}
		} catch (Exception e) {
    		e.printStackTrace();
		}
		return result;
	}
	
	public static Long getLong(JSONObject json, String key) {
		Long result = null;
		try {
			if (json.has(key) && !json.isNull(key)) {
				result = json.getLong(key);
			}
		} catch (Exception e) {
    		e.printStackTrace();
		}
		return result;
	}
	
	public static Float getFloat(JSONObject json, String key) {
		Float result = null;
		try {
			//TODO: for other safegets
			if (json.has(key) && !json.isNull(key)) {
				double d = json.getDouble(key);
				result = Float.valueOf((float)d);
			}
		} catch (Exception e) {
    		e.printStackTrace();
		}
		return result;
	}
	
	public static Boolean getBoolean(JSONObject json, String key) {
		Boolean result = null;
		try {
			if (json.has(key) && !json.isNull(key)) {
				Integer i = json.getInt(key);
				if (i != null) {
					result = (i == 1);
				}
			}
		} catch (Exception e) {
    		e.printStackTrace();
		}
		return result;
	}
	
	public static void put(JSONObject json, String name, Object value) {
		try {
			json.put(name, value);
		} catch (Exception e) {
    		e.printStackTrace();
		}
	}
	
	public static void put(JSONArray json, JSONObject obj) {
		try {
			json.put(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getString(Cursor cursor, String columnName) {
		String result = null;
		int columnIndex = cursor.getColumnIndex(columnName);
		if (!cursor.isNull(columnIndex)) {
			result = cursor.getString(columnIndex);
		}
		
		return result;
	}
	
	public static Float getFloat(Cursor cursor, String columnName) {
		Float result = null;
		int columnIndex = cursor.getColumnIndex(columnName);
		if (!cursor.isNull(columnIndex)) {
			result = cursor.getFloat(columnIndex);
		}
		
		return result;
	}
	
	public static Long getLong(Cursor cursor, String columnName) {
		Long result = null;
		int columnIndex = cursor.getColumnIndex(columnName);
		if (!cursor.isNull(columnIndex)) {
			result = cursor.getLong(columnIndex);
		}
		
		return result;
	}
	
	public static Integer getInteger(Cursor cursor, String columnName) {
		Integer result = null;
		int columnIndex = cursor.getColumnIndex(columnName);
		if (!cursor.isNull(columnIndex)) {
			result = cursor.getInt(columnIndex);
		}
		
		return result;
	}
	
	public static Boolean getBoolean(Cursor cursor, String columnName) {
		Boolean result = null;
		Integer i = getInteger(cursor, columnName);
		if (i != null) {
			result = (i == 1);
		}
		return result;
	}
	
	public static byte[] getByteArray(Cursor cursor, String columnName) {
		byte[] result = null;
		int columnIndex = cursor.getColumnIndex(columnName);
		if (!cursor.isNull(columnIndex)) {
			result = cursor.getBlob(columnIndex);
		}
		
		return result;
	}
	
	public static byte[] compress(String data) {
		if (data == null) {
			return null;
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzos = null;
		byte[] ret = null;

		try {
			gzos = new GZIPOutputStream(baos);
			gzos.write(data.getBytes("UTF-8"));
			gzos.finish();

			ret = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (gzos != null)
					gzos.close();

				if (baos != null)
					baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ret;
	}
	
	public static String decompress(byte[] originalZippedData) {
		if (originalZippedData == null) {
			return null;
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(originalZippedData);
		InputStream ungzippedResponse = null;
		String ret = null;
		try {
			ungzippedResponse = new GZIPInputStream(bais);
			InputStreamReader reader = new InputStreamReader(ungzippedResponse, "UTF-8");

			StringWriter writer = new StringWriter();

			char[] buffer = new char[10240];
			for (int length = 0; (length = reader.read(buffer)) > 0;) {
				writer.write(buffer, 0, length);
			}
			ret = writer.toString();
		} catch (IOException e) {
			try {
				if (bais != null)
					bais.close();
				if (ungzippedResponse != null)
					ungzippedResponse.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		return ret;
	}
}

package com.unicorn.mobile.common;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ReportTimeManager {
	private static Map<String, Long> reportimeMap = new HashMap<String, Long>();

	public final static String USR_ADD_REPORT = "usr_add_report";
	public final static String USR_ACTIVE_REPORT = "usr_active_report";
	public final static String EVENT_REPORT = "event_report";
	public final static String CRASH_REPORT = "crash_report";

	public static boolean needAddUsrReport(Context ctx) {
		long curTime = System.currentTimeMillis();
		long interval;
		if (reportimeMap.containsKey(USR_ADD_REPORT)) {
			interval = curTime - reportimeMap.get(USR_ADD_REPORT);
		} else {
			SharedPreferences sPref = ctx.getSharedPreferences(
					Constant.REPORT_RECORD, Context.MODE_PRIVATE);
			interval = curTime - sPref.getLong(USR_ADD_REPORT, 0L);
		}

		return !(interval > 0 && interval < Constant.ADD_REPORT_INTERVAL);
	}
	
	public static boolean needActiveUsrReport(Context ctx) {
		long curTime = System.currentTimeMillis();
		long interval;
		if (reportimeMap.containsKey(USR_ACTIVE_REPORT)) {
			interval = curTime - reportimeMap.get(USR_ACTIVE_REPORT);
		} else {
			SharedPreferences sPref = ctx.getSharedPreferences(
					Constant.REPORT_RECORD, Context.MODE_PRIVATE);
			interval = curTime - sPref.getLong(USR_ACTIVE_REPORT, 0L);
		}
		
		return !(interval > 0 && interval < Constant.ACTIVE_REPORT_INTERVAL);
	}

	public static boolean needEventReport(Context ctx) {
		long curTime = System.currentTimeMillis();
		long interval;
		if (reportimeMap.containsKey(EVENT_REPORT)) {
			interval = curTime - reportimeMap.get(EVENT_REPORT);
		} else {
			SharedPreferences sPref = ctx.getSharedPreferences(Constant.REPORT_RECORD, Context.MODE_PRIVATE);
			interval = curTime - sPref.getLong(EVENT_REPORT, 0L);
		}

		return !(interval > 0 && interval < Constant.EVENT_REPORT_INTERVAL);
	}

	public static boolean needCrashReport(Context ctx) {
		long curTime = System.currentTimeMillis();
		long interval;
		SharedPreferences sPref = ctx.getSharedPreferences(Constant.REPORT_RECORD,
				Context.MODE_PRIVATE);
		interval = curTime - sPref.getLong(CRASH_REPORT, 0L);

		return !(interval > 0 && interval < Constant.CRASH_REPORT_INTERVAL);
	}

	public static void recordTime(Context ctx, String key, Long value) {
		reportimeMap.put(key, value);
		SharedPreferences sPref = ctx.getSharedPreferences(Constant.REPORT_RECORD,
				Context.MODE_PRIVATE);
		Editor editor = sPref.edit();
		editor.putLong(key, value);
		editor.commit();
	}
}

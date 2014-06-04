package com.unicorn.mobile.common;

public class Constant {
	
	public final static String TAG = "CyouAgent";
	public final static String USER_RECORD = "usr_record";
	public final static String CRASH_RECORD = "crash_record";
	public final static String REPORT_RECORD = "report_record";

	public final static int USER_NEW_REPORT = 0;
	public final static int USER_ACTIVE_REPORT = 1;
	public final static int CRASH_REPORT = 2;
	public final static int REAL_EVENT_REPORT = 3;
	public final static int PRESERVE_EVENT_REPORT = 4;
	
	public final static long CRASH_REPORT_INTERVAL = 30 * 60 * 1000; // 30min
	public final static long EVENT_REPORT_INTERVAL = 5 * 60 * 1000; // 1h
	public final static long ACTIVE_REPORT_INTERVAL = 3 * 60 * 60 * 1000; // 3h
	public final static long ADD_REPORT_INTERVAL = 5 * 60 * 1000; // 5min

	public static final String BASE_URL = "BASE_URL";
	public final static String APPKEY = "CYOU_APPKEY";
	public final static String CHANNEL = "CYOU_CHANNEL";

	public final static String DEFAULT_CHARSET = "UTF-8";
	
	public final static String COMMON_DATE_FORMAT = "yyyy-MM-dd";
	public final static String CRASH_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss";
	
}

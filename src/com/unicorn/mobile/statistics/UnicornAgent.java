package com.unicorn.mobile.statistics;

import java.util.Map;

import com.unicorn.mobile.common.Constant;
import com.unicorn.mobile.common.DeviceInfo;
import com.unicorn.mobile.common.DeviceUtil;
import com.unicorn.mobile.common.ReportTimeManager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.HandlerThread;

public class UnicornAgent {
	private static Context sContext;
	private static HandlerThread sHandlerThread;
	private static UnicornHandler sHandler;

    // @TODO some configurations from server

	public static String getDeviceUuid(Context ctx) {
		return DeviceUtil.getUuid(ctx);
	}

	public static void onConfig(Application app) {
		sContext = app;
		sHandlerThread = new HandlerThread(Constant.TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
		sHandlerThread.start();
		sHandler = UnicornHandler.getInstance(sHandlerThread.getLooper());
		sHandler.init(app);
		
		// updateServerConfig();
	}

	public static void onCrash(Application app) {
		UnicornCrashHandler crashHandler = UnicornCrashHandler.getInstance();
		crashHandler.init(app, sHandler);
		// occasion: crash report on the startup
		crashHandler.reportCrash();
	}

	public static void onResume(Activity ac) {
		sContext = ac.getApplicationContext();
		revive();
		doUserUpload();
	}

	public static void onPause(Activity ac) {
		sContext = ac.getApplicationContext();
		revive();
		doUserUpload();
	}

	/**
	 * 
	 * @param ctx
	 * @param eventId
	 * @param eventMap
	 */
	public static void onEvent(Context ctx, int eventId, Map<String, String> eventMap) {
		onEvent(ctx, eventId, eventMap, false);
	}
	
	/**
	 * 
	 * @param ctx
	 * @param eventId
	 * @param eventMap
	 * @param mode true--real-time false--otherwise
	 */
	public static void onEvent(Context ctx, int eventId, Map<String, String> eventMap, boolean mode) {
		sContext = ctx.getApplicationContext();
		revive();
		if (mode) {
			sHandler.sendMessage(sHandler.obtainMessage(Constant.REAL_EVENT_REPORT, eventId, 0, eventMap));
		} else {
			sHandler.sendMessage(sHandler.obtainMessage(Constant.PRESERVE_EVENT_REPORT, eventId, 0, eventMap));
		}
	}

	/**
	 * update server configuration
	 */
	/*
	 * private void updateServerConfig() {
	 * 
	 * }
	 */

	private static void revive() {
		if (sHandler == null || sHandler.getLooper() == null) {
			sHandlerThread = new HandlerThread(Constant.TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
			sHandlerThread.start();
			sHandler = UnicornHandler.getInstance(sHandlerThread.getLooper());
		}
		sHandler.init(sContext);
	}

	private static void doUserUpload() {
		SharedPreferences sPref = sContext.getSharedPreferences(
				Constant.USER_RECORD, Context.MODE_PRIVATE);
		DeviceInfo deviceInfo = new DeviceInfo(sContext);
		boolean valid = sPref.getBoolean("valid", false);
		if (!valid) {
			if (ReportTimeManager.needAddUsrReport(sContext)) {
				deviceInfo.build();
				Map<String, String> model = deviceInfo.parse();
				sHandler.sendMessage(sHandler.obtainMessage(Constant.USER_NEW_REPORT, model));
			}
		} else if (ReportTimeManager.needActiveUsrReport(sContext)) {
			Map<String, String> record = deviceInfo.recover();
			sHandler.sendMessage(sHandler.obtainMessage(Constant.USER_ACTIVE_REPORT, record));
		}
	}
}

package com.unicorn.mobile.statistics;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.os.Message;

import com.unicorn.mobile.common.Constant;
import com.unicorn.mobile.common.DeviceInfo;
import com.unicorn.mobile.common.DeviceUtil;
import com.unicorn.mobile.common.ReportTimeManager;

public class UnicornAgent {
	private static Context sContext;
	private static HandlerThread sHandlerThread;
	private static UnicornHandler sHandler;

    // @TODO some configurations from server
	
	/**
	 * a method to get the device's uuid
	 * 
	 * @param ctx --- context from the application
	 * @return
	 */
	public static String getDeviceUuid(Context ctx) {
		return DeviceUtil.getUuid(ctx);
	}

	public static void onConfig(Context ctx) {
		sContext = ctx.getApplicationContext();
		sHandlerThread = new HandlerThread(Constant.TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
		sHandlerThread.start();
		sHandler = UnicornHandler.getInstance(sContext, sHandlerThread.getLooper());
		
		// updateServerConfig();
	}

	public static void onCrash(Context ctx) {
		UnicornCrashHandler crashHandler = UnicornCrashHandler.getInstance();
		crashHandler.init(ctx.getApplicationContext(), sHandler);
		// occasion: crash report on the startup
		crashHandler.reportCrash();
	}

	public static void onResume(Activity ac) {
		sContext = ac.getApplicationContext();
		revive();

		if (ReportTimeManager.needAddUsrReport(sContext)) {
			doUserUpload();
		}
		
	}

	public static void onPause(Activity ac) {
		sContext = ac.getApplicationContext();
		revive();

		if (ReportTimeManager.needAddUsrReport(sContext)) {
			doUserUpload();
		}
	}

	/**
	 * 
	 * @param ctx
	 * @param eventId
	 * @param eventMap
	 */
	public static void onEvent(Context ctx, int eventId, Map<String, String> eventMap) {
		sContext = ctx.getApplicationContext();
		onEvent(ctx, eventId, eventMap, false);
	}
	
	/**
	 * 
	 * @param ctx
	 * @param eventId
	 * @param eventMap
	 * @param mode
	 *            true--"real-time", false--"non-real-time"
	 */
	public static void onEvent(Context ctx, int eventId, Map<String, String> eventMap, boolean mode) {
		sContext = ctx.getApplicationContext();
		revive();
		
		Message eventMsg;
		if (mode) {
			eventMsg = sHandler.obtainMessage(Constant.REAL_EVENT_REPORT, eventId, 0, eventMap);
			sHandler.sendMessage(eventMsg);
		} else {
			eventMsg = sHandler.obtainMessage(Constant.PRESERVE_EVENT_REPORT, eventId, 0, eventMap);
			sHandler.sendMessage(eventMsg);
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
	
	/**
	 * recover the handler thread of the report process
	 */
	private static void revive() {
		if (sHandler == null || sHandler.getLooper() == null) {
			sHandlerThread = new HandlerThread(Constant.TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
			sHandlerThread.start();
			sHandler = UnicornHandler.getInstance(sContext, sHandlerThread.getLooper());
		}
	}

	private static void doUserUpload() {
		DeviceInfo deviceInfo = new DeviceInfo(sContext);
		SharedPreferences sPref = sContext.getSharedPreferences(Constant.USER_RECORD, Context.MODE_PRIVATE);
		boolean valid = sPref.getBoolean("valid", false);
		if (!valid) {
			deviceInfo.build();
			Map<String, String> model = deviceInfo.parse();
			Message msg = sHandler.obtainMessage(Constant.USER_NEW_REPORT, model);
			sHandler.sendMessage(msg);
		} else {
			Map<String, String> record = deviceInfo.recover();
			Message msg = sHandler.obtainMessage(Constant.USER_ACTIVE_REPORT, record);
			sHandler.sendMessage(msg);
		}
	}
	
}

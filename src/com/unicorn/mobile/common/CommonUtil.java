package com.unicorn.mobile.common;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;

public final class CommonUtil {

	/**
	 * check task active status
	 * 
	 * @param ctx
	 * @param pkgName
	 * @return
	 */
	public static boolean isApplicationTaskRunning(Context ctx, String pkgName) {
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> appTask = am.getRunningTasks(Integer.MAX_VALUE);
		for (RunningTaskInfo runningTaskInfo : appTask) {
			if (runningTaskInfo.baseActivity.getPackageName().equals(pkgName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * retrieve the running Launcher process.
	 * 
	 * @param ctx
	 * @return
	 */
	public static List<String> getRunningLaunchers(Context ctx) {
		List<String> results = new ArrayList<String>();
		List<String> launcherList = getAllLaunchers(ctx);
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcess = am.getRunningAppProcesses();
		for (RunningAppProcessInfo process : appProcess) {
			if (launcherList.contains(process.processName)) {
				results.add(process.processName);
			}
		}

		return results;
	}

	/**
	 * retrieve all the Launcher packages
	 * 
	 * @param ctx
	 * @return
	 */
	public static List<String> getAllLaunchers(Context ctx) {
		List<String> launcherList;
		PackageManager pm = ctx.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> rl = pm.queryIntentActivities(intent, 0);
		launcherList = new ArrayList<String>(rl.size());
		for (ResolveInfo ri : rl) {
			String pkgName = ri.activityInfo.packageName;
			launcherList.add(pkgName);
		}

		return launcherList;
	}

	/**
	 * Checks if external storage is available for read and write
	 * 
	 * @return
	 */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * check network available
	 * 
	 * @param ctx
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * get file path on the storage space
	 * 
	 * @param ctx
	 * @param relativePath
	 * @param filename
	 * @return filePath: Absolute Path
	 */
	public static File getStoragePath(Context ctx, String relativePath,
			String filename) {

		File fileDir;
		boolean sdCardAvailable = isExternalStorageWritable();
		if (sdCardAvailable) {
			// external storage space
			fileDir = new File(Environment.getExternalStorageDirectory()
					.getPath() + File.separator + relativePath);
		} else {
			// internal storage space
			fileDir = ctx.getDir(relativePath, Context.MODE_PRIVATE);
		}

		File file = null;
		if (fileDir.isDirectory() || fileDir.mkdirs()) {
			file = new File(fileDir, filename);
		}

		return file;
	}

	/**
	 * get an installed application's uid
	 * 
	 * @param ctx
	 * @param pkgName
	 * @return
	 */
	public static int getInstalledAppUid(Context ctx, String pkgName) {
		final PackageManager pm = ctx.getPackageManager();
		List<ApplicationInfo> pkgs = pm
				.getInstalledApplications(PackageManager.GET_META_DATA);
		int uid = 0;
		for (ApplicationInfo packageInfo : pkgs) {
			if (packageInfo.packageName.equals(pkgName)) {
				// get the uid for the pkgname
				uid = packageInfo.uid;
			}
		}

		return uid;
	}

	/**
	 * get data from "meta-data" in manifest.xml
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getBaseUrl(Context ctx) {
		String url = "";
		PackageManager pm = ctx.getPackageManager();
		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
			url = appInfo.metaData.getString(Constant.BASE_URL);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		if (!url.endsWith("/")) {
			url += "/";
		}
		
		return url;
	}

	/**
	 * get channel from "meta-data" in manifest.xml
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getChannel(Context ctx) {
		String channel = "";
		try {
			PackageManager pm = ctx.getPackageManager();
			ApplicationInfo appInfo = pm.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
			channel = Integer.toString(appInfo.metaData.getInt(Constant.CHANNEL));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return channel;
	}

	/**
	 * get appkey from "meta-data" in manifest.xml
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getAppKey(Context ctx) {
		String appkey = "";
		try {
			PackageManager pm = ctx.getPackageManager();
			ApplicationInfo appInfo = pm.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
			appkey = appInfo.metaData.getString(Constant.APPKEY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return appkey;
	}

	/**
	 * get client time
	 * 
	 * @param format
	 * @param date
	 * @return
	 */
	public static String getClientTime(String format, Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
		dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		return dateFormat.format(date);
	}

	/**
	 * get the current networking
	 * 
	 * @param ctx
	 * @return network type
	 */
	public static String getNetworkType(Context ctx) {
		TelephonyManager manager = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		int typeCode = manager.getNetworkType();
		String type = "UNKNOWN";
		if (typeCode == TelephonyManager.NETWORK_TYPE_CDMA) {
			type = "CDMA";
		}
		if (typeCode == TelephonyManager.NETWORK_TYPE_EDGE) {
			type = "EDGE";
		}
		if (typeCode == TelephonyManager.NETWORK_TYPE_EVDO_0) {
			type = "EVDO_0";
		}
		if (typeCode == TelephonyManager.NETWORK_TYPE_EVDO_A) {
			type = "EVDO_A";
		}
		if (typeCode == TelephonyManager.NETWORK_TYPE_GPRS) {
			type = "GPRS";
		}
		if (typeCode == TelephonyManager.NETWORK_TYPE_HSDPA) {
			type = "HSDPA";
		}
		if (typeCode == TelephonyManager.NETWORK_TYPE_HSPA) {
			type = "HSPA";
		}
		if (typeCode == TelephonyManager.NETWORK_TYPE_HSUPA) {
			type = "HSUPA";
		}
		if (typeCode == TelephonyManager.NETWORK_TYPE_UMTS) {
			type = "UMTS";
		}
		if (typeCode == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
			type = "UNKNOWN";
		}
		if (typeCode == TelephonyManager.NETWORK_TYPE_1xRTT) {
			type = "1xRTT";
		}
		if (typeCode == 11) {
			type = "iDen";
		}
		if (typeCode == 12) {
			type = "EVDO_B";
		}
		if (typeCode == 13) {
			type = "LTE";
		}
		if (typeCode == 14) {
			type = "eHRPD";
		}
		if (typeCode == 15) {
			type = "HSPA+";
		}

		return type;
	}

	/**
	 * get current connectivity type
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getConnType(Context ctx) {
		String conn = null;
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null) {
			conn = netInfo.getTypeName();
		}

		return conn;
	}

	/**
	 * format Http Get params from map to String
	 * 
	 * @param params
	 * @return
	 */
	public static String formatParams(Map<String, String> params) {
		StringBuilder builder = new StringBuilder();
		String separator = "";
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String value = entry.getValue() == null ? "" : String
						.valueOf(entry.getValue());
				builder.append(separator);
				builder.append(URLEncoder.encode(entry.getKey(),
						Constant.DEFAULT_CHARSET));
				builder.append('=');
				builder.append(URLEncoder.encode(value,
						Constant.DEFAULT_CHARSET));
				separator = "&";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	public static String makeStrMd5(String str) {
		String md5Str = "";
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(str.getBytes());
			byte[] bytes = md5.digest();
			md5Str = byteArrayToHex(bytes);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return md5Str;
	}

	private static String byteArrayToHex(byte[] bytes) {
		char[] hexReferChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[bytes.length * 2];
		int index = 0;
		for (byte b : bytes) {
			hexChars[index++] = hexReferChars[b >>> 4 & 0xf];
			hexChars[index++] = hexReferChars[b & 0xf];
		}
		return new String(hexChars);
	}


	/**
	 * format map to JSONObj String
	 * 
	 * @param map
	 * @return
	 */
	public static String formatToJsonObj(Map<String, String> map) {
		JSONObject obj = new JSONObject();
		try {
			for (Entry<String, String> entry : map.entrySet()) {
				obj.put(entry.getKey(), entry.getValue());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return obj.toString();
	}
	

	/**
	 * format map to JSONArray String---crash message
	 * 
	 * @param crashMap
	 * @return
	 */
	public static String formatToJsonArr(Map<String, String> crashMap) {
		JSONArray arr = new JSONArray();
		for (Entry<String, String> crashRecord : crashMap.entrySet()) {
			JSONObject obj = new JSONObject();
			try {
				obj.put(crashRecord.getKey(), crashRecord.getValue());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			arr.put(obj);
		}
		return arr.toString();
	}
	
	/**
	 * return basename without suffix of the file
	 * 
	 * @param filename
	 * @return
	 */
	public static String getFileBasename(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >- 1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
	}

}

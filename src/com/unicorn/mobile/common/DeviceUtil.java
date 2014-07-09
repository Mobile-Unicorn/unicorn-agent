package com.unicorn.mobile.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.UUID;

public class DeviceUtil {
	private final static String UUID_FILE_NAME = "uuid.md";
	
	public static int getApplicationVersion(Context ctx) {
		int versionCode = 0;
		PackageManager pm = ctx.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
			versionCode = pi.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return versionCode;
	}

	public static int getDeviceDpi(Context ctx) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = ctx.getResources().getDisplayMetrics();
		return dm.densityDpi;
	}

	public static String getDeviceResolution(Context ctx) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = ctx.getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		String resolution;
		if (width > height) {
			resolution = width + "x" + height;
		} else {
			resolution = height + "x" + width;
		}
		return resolution;
	}

	public static String getLocale() {
		Locale locale = Locale.getDefault();
		return locale.getLanguage() + "_" + locale.getCountry();
	}

	public static String getSysVersion() {
		return Build.VERSION.RELEASE;
	}

	public static String getDeviceModel() {
		return Build.MODEL;
	}

	public static String getAndroidId(Context ctx) {
		String androidId = Secure.getString(ctx.getContentResolver(),
				Secure.ANDROID_ID);
		return androidId;
	}

	public static String getMacSerial(Context ctx) {
		String macSerial = ((WifiManager) ctx
				.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo()
				.getMacAddress();
		return macSerial;
	}

	public static String getDeviceId(Context ctx) {
		String deviceId = ((TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		return deviceId;
	}

	public static String getUuid(Context ctx) {
		String uuidStr = "";
		// fetch uuid from file
		try {
			uuidStr = fetchUuid(ctx);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// fetch failure then create a new one
		if (TextUtils.isEmpty(uuidStr)) {
			UUID uuid = createUuid(ctx);
			uuidStr = uuid.toString().replaceAll("-", "");
			saveUuid(ctx, uuidStr);
		}

		return uuidStr;
	}

	// ---------------------------local method start--------------------------- //
	private static String fetchUuid(Context ctx) throws IOException {
		File file = new File(ctx.getFilesDir(), UUID_FILE_NAME);
		if (!file.exists())	return null;
		RandomAccessFile f = new RandomAccessFile(file, "r");
		byte[] bytes = new byte[(int) f.length()];
		f.readFully(bytes);
		f.close();
		String uuidStr = new String(bytes);
		return TextUtils.isEmpty(uuidStr) ? "" : uuidStr;
	}

	private static UUID createUuid(Context ctx) {
		StringBuilder builder = new StringBuilder();
		builder.append(getAndroidId(ctx));
		builder.append(getMacSerial(ctx));
		builder.append(getDeviceId(ctx));
		UUID comp = str2Uuid(builder.toString());
		return comp == null ? UUID.randomUUID() : comp;
	}
	
	private static UUID str2Uuid(String str) {
		UUID uuid = null;
		try {
			uuid = UUID.nameUUIDFromBytes(str.getBytes(Constant.DEFAULT_CHARSET));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return uuid;
	}

	private static void saveUuid(Context ctx, String uuidStr) {
		File file = new File(ctx.getFilesDir(), UUID_FILE_NAME);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(uuidStr.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	// ---------------------------local method end--------------------------- //
	
}

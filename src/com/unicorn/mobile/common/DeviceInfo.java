package com.unicorn.mobile.common;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class DeviceInfo implements ICollector {

	private Context mContext;

	private String uuid;
	private String android_id;
	private String mac_serial;
	private String device_id;
	private String locale;
	private int density;
	private String resolution;
	private String model;
	private String os_version;
	private int app_version;
	private String networkType;
	private String connType;

	public DeviceInfo(Context ctx) {
		mContext = ctx;
	}

	public void build() {
		uuid = DeviceUtil.getUuid(mContext);
		android_id = DeviceUtil.getAndroidId(mContext);
		mac_serial = DeviceUtil.getMacSerial(mContext);
		device_id = DeviceUtil.getDeviceId(mContext);
		locale = DeviceUtil.getLocale();
		density = DeviceUtil.getDeviceDpi(mContext);
		resolution = DeviceUtil.getDeviceResolution(mContext);
		model = DeviceUtil.getDeviceModel();
		os_version = DeviceUtil.getSysVersion();
		app_version = DeviceUtil.getApplicationVersion(mContext);
		networkType = CommonUtil.getNetworkType(mContext);
		connType = CommonUtil.getConnType(mContext);
	}

	@Override
	public Map<String, String> parse() {
		Map<String, String> deviceMap = new HashMap<String, String>();
		deviceMap.put("uuid", uuid);
		deviceMap.put("android_id", format(android_id));
		deviceMap.put("mac_serial", format(mac_serial));
		deviceMap.put("device_id", format(device_id));
		deviceMap.put("locale", format(locale));
		deviceMap.put("density", format(Integer.toString(density)));
		deviceMap.put("resolution", format(resolution));
		deviceMap.put("model", format(model));
		deviceMap.put("os_version", format(os_version));
		deviceMap.put("app_version", format(Integer.toString(app_version)));
		deviceMap.put("network", format(networkType));
		deviceMap.put("conn", format(connType));

		return deviceMap;
	}

	@Override
	public Map<String, String> recover() {
		SharedPreferences sPref = mContext.getSharedPreferences(
				Constant.USER_RECORD, Context.MODE_PRIVATE);
		Map<String, String> deviceMap = new HashMap<String, String>();
		deviceMap.put("uuid", sPref.getString("uuid", "null"));
		deviceMap.put("android_id", sPref.getString("android_id", "null"));
		deviceMap.put("mac_serial", sPref.getString("mac_serial", "null"));
		deviceMap.put("device_id", sPref.getString("device_id", "null"));
		deviceMap.put("locale", sPref.getString("locale", "null"));
		deviceMap.put("density", sPref.getString("density", "null"));
		deviceMap.put("resolution", sPref.getString("resolution", "null"));
		deviceMap.put("model", sPref.getString("model", "null"));
		deviceMap.put("os_version", sPref.getString("os_version", "null"));
		deviceMap.put("app_version", sPref.getString("app_version", "null"));
		deviceMap.put("network", sPref.getString("network", "null"));
		deviceMap.put("conn", sPref.getString("conn", "null"));

		return deviceMap;
	}

	private String format(String rawStr) {
		return TextUtils.isEmpty(rawStr) ? "null" : rawStr;
	}

}

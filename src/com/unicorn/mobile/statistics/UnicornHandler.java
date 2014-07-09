package com.unicorn.mobile.statistics;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.unicorn.mobile.common.CommonUtil;
import com.unicorn.mobile.common.Constant;
import com.unicorn.mobile.common.ReportTimeManager;
import com.unicorn.mobile.dao.UnicornDao;
import com.unicorn.mobile.dao.EventModel;
import com.unicorn.mobile.net.NetworkClient;
import com.unicorn.mobile.net.NetworkClientFactory;
import com.unicorn.mobile.net.ResponseModel;

public class UnicornHandler extends Handler {
    private static UnicornHandler sCyouHandler;
    private Context mContext;
    private String mAppKey;
    private String mChannel;
    private String mBaseUrl;

    private UnicornHandler(Looper looper) {
        super(looper);
    }

    public static synchronized UnicornHandler getInstance(Context context, Looper looper) {
        if (sCyouHandler == null) {
            sCyouHandler = new UnicornHandler(looper);
            sCyouHandler.init(context);
        }
        return sCyouHandler;
    }
    
    private void init(Context context) {
    	mContext = context;
    	mAppKey = CommonUtil.getAppKey(context);
        mChannel = CommonUtil.getChannel(context);
        mBaseUrl = CommonUtil.getBaseUrl(context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.USER_NEW_REPORT:
                Map<String, String> deviceMap = (Map<String, String>) msg.obj;
                uploadNewUser(deviceMap);
                break;
            case Constant.USER_ACTIVE_REPORT:
                Map<String, String> recordMap = (Map<String, String>) msg.obj;
                uploadActiveUser(recordMap);
                break;
            case Constant.REAL_EVENT_REPORT:
                Map<String, String> sEventMap = (Map<String, String>) msg.obj;
                uploadEvent(msg.arg1, sEventMap);
                break;
            case Constant.CRASH_REPORT:
                Map<String, String> crashMap = (Map<String, String>) msg.obj;
                uploadCrash(crashMap);
                break;
            case Constant.PRESERVE_EVENT_REPORT:
                Map<String, String> eventMap = (Map<String, String>) msg.obj;
                preserveEvent(msg.arg1, eventMap);
                break;
            default:
                Log.d(Constant.TAG, "Unknown Message Type");
        }
    }

    private void uploadNewUser(Map<String, String> map) {
        if (!CommonUtil.isNetworkAvailable(mContext))
            return;

        map.put("app_key", mAppKey);
        map.put("channel", mChannel);
        String clientTime = CommonUtil.getClientTime(
                Constant.COMMON_DATE_FORMAT, new Date());
        map.put("date", clientTime);

        NetworkClient netClient = NetworkClientFactory.createNetworkClient();
        String param = CommonUtil.formatParams(map);
        String url = mBaseUrl + "client/adduser/add.do";

        ResponseModel response = null;
        try {
            response = netClient.getInputStream(url + "?" + param);
            if (response.status == 200) {
                saveDeviceInfo(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            netClient.close();
        }
    }

    private void uploadActiveUser(Map<String, String> deviceMap) {
        if (!CommonUtil.isNetworkAvailable(mContext))
            return;

        deviceMap.put("app_key", mAppKey);
        deviceMap.put("channel", mChannel);
        String clientTime = CommonUtil.getClientTime(
                Constant.COMMON_DATE_FORMAT, new Date());
        deviceMap.put("date", clientTime);

        NetworkClient netClient = NetworkClientFactory.createNetworkClient();
        String param = CommonUtil.formatParams(deviceMap);
        String url = mBaseUrl + "client/activeuser/add.do";

        try {
            netClient.getInputStream(url + "?" + param);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            netClient.close();
            ReportTimeManager.recordTime(mContext,
                    ReportTimeManager.USR_ACTIVE_REPORT,
                    System.currentTimeMillis());
        }
    }

    private void uploadEvent(int eventId, Map<String, String> eventMap) {
        if (!CommonUtil.isNetworkAvailable(mContext))
            return;
        eventMap.put("eid", Integer.toString(eventId));
        eventMap.put("app_key", mAppKey);
        eventMap.put("channel", mChannel);
        String clientTime = CommonUtil.getClientTime(
                Constant.COMMON_DATE_FORMAT, new Date());
        eventMap.put("date", clientTime);

        NetworkClient netClient = NetworkClientFactory.createNetworkClient();
        String param = CommonUtil.formatParams(eventMap);
        String url = mBaseUrl + "client/event/add.do";

        try {
            netClient.getInputStream(url + "?" + param);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            netClient.close();
            ReportTimeManager.recordTime(mContext, ReportTimeManager.EVENT_REPORT, System.currentTimeMillis());
        }
    }

    private void uploadCrash(Map<String, String> crashMap) {
        if (!CommonUtil.isNetworkAvailable(mContext))
            return;

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("app_key", mAppKey);
        paramMap.put("channel", mChannel);
        paramMap.put("uuid", UnicornAgent.getDeviceUuid(mContext));
        String clientTime = CommonUtil.getClientTime(
                Constant.CRASH_DATE_FORMAT, new Date());
        paramMap.put("date", clientTime);
        paramMap.put("record", CommonUtil.formatToJsonArr(crashMap));

        NetworkClient netClient = NetworkClientFactory.createNetworkClient();
        String param = CommonUtil.formatParams(paramMap);
        String url = mBaseUrl + "client/crash/add.do";

        ResponseModel resModel = null;
        try {
            resModel = netClient.getInputStream(url + "?" + param);
            if (resModel.status / 100 == 2) {
                SharedPreferences sPref = mContext.getSharedPreferences(Constant.CRASH_RECORD, Context.MODE_PRIVATE);
                Editor editor = sPref.edit();
                for (String key : crashMap.keySet()) {
                    editor.putLong(key, 0L);
                }
                editor.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            netClient.close();
            ReportTimeManager.recordTime(mContext, ReportTimeManager.CRASH_REPORT, System.currentTimeMillis());
        }
    }

    private void preserveEvent(int eventId, Map<String, String> eventMap) {
        EventModel eventModel = new EventModel(eventId, CommonUtil.formatToJsonObj(eventMap));
        UnicornDao cDao = new UnicornDao(mContext);
        cDao.insert(eventModel);
    }

    private void saveDeviceInfo(Map<String, String> deviceMap) {
        SharedPreferences sPref = mContext.getSharedPreferences(Constant.USER_RECORD, Context.MODE_PRIVATE);
        Editor editor = sPref.edit();
        editor.putBoolean("valid", true);
        editor.putString("date", deviceMap.get("date"));
        editor.putString("uuid", deviceMap.get("uuid"));
        editor.putString("android_id", deviceMap.get("android_id"));
        editor.putString("mac_serial", deviceMap.get("mac_serial"));
        editor.putString("device_id", deviceMap.get("device_id"));
        editor.putString("locale", deviceMap.get("locale"));
        editor.putString("density", deviceMap.get("density"));
        editor.putString("resolution", deviceMap.get("resolution"));
        editor.putString("model", deviceMap.get("model"));
        editor.putString("os_version", deviceMap.get("os_version"));
        editor.putString("app_version", deviceMap.get("app_version"));
        editor.putString("network", deviceMap.get("network"));
        editor.putString("conn", deviceMap.get("conn"));

        editor.commit();
    }

}

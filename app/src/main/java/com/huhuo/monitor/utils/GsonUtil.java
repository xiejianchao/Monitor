package com.huhuo.monitor.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.model.CommonDataModel;
import com.huhuo.monitor.model.BaseJsonModel;
import com.huhuo.monitor.model.ListDataModel;
import com.huhuo.monitor.model.MonitorModel;

import org.json.JSONObject;

/**
 * Created by xiejc on 16/1/21.
 */
public class GsonUtil {

    private static final Gson gson = new Gson();

    public static MonitorModel parseMonitor(String json) {
        try {
            MonitorModel model = gson.fromJson(json,MonitorModel.class);
            return  model;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static BaseJsonModel parseMonitorNew(String json) {
        try {

            BaseJsonModel model = null;
            final JSONObject jsonObject = new JSONObject(json);
            final String type = jsonObject.getString("type");
            if (!TextUtils.isEmpty(type)) {
                switch (type) {
                    case Constants.LIST:
                        model = gson.fromJson(json,ListDataModel.class);
                        break;
                    case Constants.ADD:
                        model = gson.fromJson(json, CommonDataModel.class);
                        break;
                    case Constants.DEL:
                        model = gson.fromJson(json, CommonDataModel.class);
                        break;
                    case Constants.STATUS:
                        model = gson.fromJson(json, CommonDataModel.class);
                        break;
                    case Constants.UPDATE:
                        model = gson.fromJson(json, CommonDataModel.class);
                        break;
                    case Constants.CLEAR:
                        model = gson.fromJson(json, CommonDataModel.class);
                        break;
                }
            }
            return model;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

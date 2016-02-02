package com.huhuo.monitor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.huhuo.monitor.MonitorApplication;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.model.MonitorModel;

public class SPUtil {

	private static final String TAG = SPUtil.class.getSimpleName();
	private static String name = Constants.PREFERENCES_NAME;
	private static Context context = MonitorApplication.getInstance().getApplicationContext();

	private SPUtil() {
	}

	/**
	 * 获取SharedPreferences实例对象
	 * 
	 * @param
	 * @return
	 */
	private static SharedPreferences getSharedPreference() {
		return context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
	}

	/**
	 * 保存一个Boolean类型的值！
	 * 
	 * @param
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean putBoolean(String key, Boolean value) {
		SharedPreferences sharedPreference = getSharedPreference();
		Editor editor = sharedPreference.edit();
		editor.putBoolean(key, value);
		return editor.commit();
	}

	/**
	 * 保存一个int类型的值！
	 * 
	 * @param
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean putInt(String key, int value) {
		SharedPreferences sharedPreference = getSharedPreference();
		Editor editor = sharedPreference.edit();
		editor.putInt(key, value);
		return editor.commit();
	}

	/**
	 * 保存一个float类型的值！
	 * 
	 * @param
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean putFloat(String key, float value) {
		SharedPreferences sharedPreference = getSharedPreference();
		Editor editor = sharedPreference.edit();
		editor.putFloat(key, value);
		return editor.commit();
	}

	/**
	 * 保存一个long类型的值！
	 * 
	 * @param
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean putLong(String key, long value) {
		SharedPreferences sharedPreference = getSharedPreference();
		Editor editor = sharedPreference.edit();
		editor.putLong(key, value);
		return editor.commit();
	}

	/**
	 * 保存一个String类型的值！
	 * 
	 * @param
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean putString(String key, String value) {
		SharedPreferences sharedPreference = getSharedPreference();
		Editor editor = sharedPreference.edit();
		editor.putString(key, value);
		return editor.commit();
	}

	/**
	 * 获取String的value
	 * 
	 * @param
	 * @param key
	 *            名字
	 * @param defValue
	 *            默认值
	 * @return
	 */
	public static String getString(String key, String defValue) {
		SharedPreferences sharedPreference = getSharedPreference();
		return sharedPreference.getString(key, defValue);
	}

	/**
	 * 获取int的value
	 * 
	 * @param
	 * @param key
	 *            名字
	 * @param defValue
	 *            默认值
	 * @return
	 */
	public static int getInt(String key, int defValue) {
		SharedPreferences sharedPreference = getSharedPreference();
		return sharedPreference.getInt(key, defValue);
	}

	/**
	 * 获取float的value
	 * 
	 * @param
	 * @param key
	 *            名字
	 * @param defValue
	 *            默认值
	 * @return
	 */
	public static float getFloat(String key, Float defValue) {
		SharedPreferences sharedPreference = getSharedPreference();
		return sharedPreference.getFloat(key, defValue);
	}

	/**
	 * 获取boolean的value
	 * 
	 * @param
	 * @param key
	 *            名字
	 * @param defValue
	 *            默认值
	 * @return
	 */
	public static boolean getBoolean(String key, Boolean defValue) {
		SharedPreferences sharedPreference = getSharedPreference();
		return sharedPreference.getBoolean(key, defValue);
	}

	/**
	 * 获取long的value
	 * 
	 * @param
	 * @param key
	 *            名字
	 * @param defValue
	 *            默认值
	 * @return
	 */
	public static long getLong(String key, long defValue) {
		SharedPreferences sharedPreference = getSharedPreference();
		return sharedPreference.getLong(key, defValue);
	}

	public static boolean removeKey(String key) {
		SharedPreferences sharedPreference = getSharedPreference();
		final Editor edit = sharedPreference.edit();
		edit.remove(key);
		final boolean commit = edit.commit();
		return commit;
	}

	public static void saveMonitorInfo(MonitorModel model) {
		SPUtil.putString(model.getId() + "_" + Constants.Key.NAME,model.getName());
		SPUtil.putString(model.getId() + "_" + Constants.Key.DESC, model.getDetail());
		SPUtil.putString(model.getId() + "_" + Constants.Key.CONTENT,model.getContent());
	}

	public static void clearMonitorInfo(MonitorModel model) {
		removeKey(model.getId() + "_" + Constants.Key.NAME);
		removeKey(model.getId() + "_" + Constants.Key.DESC);
		removeKey(model.getId() + "_" + Constants.Key.CONTENT);
	}

	public static void saveMobile(String mobile) {
		putString(Constants.Key.MOBILE,mobile);
	}

	public static String getMobile() {
		return getString(Constants.Key.MOBILE,"");
	}



}
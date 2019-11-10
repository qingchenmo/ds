package com.ds.utils;


import android.content.Context;
import android.content.SharedPreferences;
import com.ds.GlobalContext;


public class Setting {
	private static Setting _instance;
	private static final String TAG = Setting.class.getSimpleName();

	private Setting() {

	}

	public static Setting instance() {
		if (null == _instance) {
			_instance = new Setting();
		}

		return _instance;
	}

	public void saveBoolen(String key, boolean value) {
		SharedPreferences cfg = GlobalContext.getInstance().getContext().getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = cfg.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	public boolean getBoolean(String key, boolean defaultValue) {
		SharedPreferences cfg = GlobalContext.getInstance().getContext().getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		return cfg.getBoolean(key, defaultValue);
	}
	public void remove(String key) {
		SharedPreferences cfg = GlobalContext.getInstance().getContext().getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = cfg.edit();
		editor.remove(key);
		editor.commit();
	}

	public void saveData(String key, String value) {
		SharedPreferences cfg = GlobalContext.getInstance().getContext().getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = cfg.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void saveIntData(String key, int value) {
		SharedPreferences cfg = GlobalContext.getInstance().getContext().getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = cfg.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public String getData(String key, String defaultValue) {
		SharedPreferences cfg = GlobalContext.getInstance().getContext().getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		return cfg.getString(key, defaultValue);
	}

	public int getInt(String key, int defaultValue) {
		SharedPreferences cfg = GlobalContext.getInstance().getContext().getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		return cfg.getInt(key, defaultValue);
	}
	public double getFloat(String key, float defaultValue) {
		SharedPreferences cfg = GlobalContext.getInstance().getContext().getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		return cfg.getFloat(key, defaultValue);
	}
	public void saveFloat(String key, float value) {
		SharedPreferences cfg = GlobalContext.getInstance().getContext().getSharedPreferences(TAG,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = cfg.edit();
		editor.putFloat(key, value);
		editor.commit();
	}
}

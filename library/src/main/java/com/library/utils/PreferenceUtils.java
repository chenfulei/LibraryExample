package com.library.utils;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * PreferenceManager的静态函数
 * @author chen_fulei
 *
 */
public class PreferenceUtils {
	
	/**
	 * 对String类型操作
	 * @param mContext
	 * @param key key值
	 * @param value 
	 * @return
	 */
	public static String getPrefString(Context mContext, String key, String value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		return setting.getString(key, value);
	}
	
	public static void setPrefString(Context mContext, String key , String value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		setting.edit().putString(key, value).commit();
	}
	
	/**
	 * 对int 类型操作
	 * @param mContext
	 * @param key
	 * @param value
	 * @return
	 */
	public static int getPrefInt(Context mContext, String key, int value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		return setting.getInt(key, value);
	}
	
	public static void setPrefInt(Context mContext, String key, int value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		setting.edit().putInt(key, value).commit();
	}
	
	/**
	 * 对float 类型操作
	 * @param mContext
	 * @param key
	 * @param value
	 * @return
	 */
	public static float getPrefFloat(Context mContext, String key, float value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		return setting.getFloat(key, value);
	}
	
	public static void setPrefFloat(Context mContext, String key, float value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		setting.edit().putFloat(key, value).commit();
	}
	
	/**
	 * 对boolean 类型操作
	 * @param mContext
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean getPrefBoolean(Context mContext, String key , boolean value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		return setting.getBoolean(key, value);
	}
	
	public static void setPrefBoolean(Context mContext, String key, boolean value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		setting.edit().putBoolean(key, value).commit();
	}
	
	/**
	 * 对long 操作
	 * @param mContext
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long getPrefLong(Context mContext , String key , long value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		return setting.getLong(key, value);
	}
	
	public static void setPrefLong(Context mContext, String key , long value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		setting.edit().putLong(key, value).commit();
	}
	
	/**
	 * 对set<String> 操作
	 * @param mContext
	 * @param key
	 * @param value
	 * @return
	 */
	public static Set<String> getPrefSet(Context mContext, String key , Set<String> value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		return setting.getStringSet(key, value);
	}
	
	public static void setPrefSet(Context mContext, String key, Set<String> value){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		setting.edit().putStringSet(key, value).commit();
	}
	
	/**
	 * 判断SharePreference 是否含有key
	 * @param mContext
	 * @param key
	 * @return
	 */
	public static boolean hasKey(Context mContext, String key){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);
		return setting.contains(key);
	}
	
	/**
	 * 清理
	 * @param share
	 */
	public static void clearPreference(final SharedPreferences share){
		Editor editor = share.edit();
		editor.clear();
		editor.commit();
	}
}

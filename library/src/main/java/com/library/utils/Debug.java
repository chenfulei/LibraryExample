package com.library.utils;

import android.util.Log;

/**
 * 全局的debug管理
 * <p/>
 * Created by chen_fulei on 2015/7/17.
 */
public class Debug {

    private static boolean DEBUG = true;

    /**
     * 设置debug开关
     *
     * @param enable
     */
    public static void setEnableDebug(boolean enable) {
        DEBUG = enable;
    }

    /**
     * 默认打印logcat  Info
     *
     * @param message logcat 信息
     */
    public static void Log(String message) {
        Log("", message);
    }

    public static void Log(String tag, String message) {
        if (DEBUG) {
            Log.i(tag, message);
        }
    }

    /**
     * 打印异常错误信息
     *
     * @param e
     */
    public static void Log(Throwable e) {
        LogE(Log.getStackTraceString(e));
    }

    public static void Log(String tag, Throwable e) {
        LogE(tag, Log.getStackTraceString(e));
    }

    /**
     * 打印错误信息 Error
     *
     * @param message
     */
    public static void LogE(String message) {
        LogE("", message);
    }

    public static void LogE(String tag, String message) {
        if (DEBUG) {
            Log.e(tag, message);
        }
    }

    /**
     * 打印wrong logcat
     *
     * @param message
     */
    public static void LogW(String message) {
        LogW("", message);
    }

    public static void LogW(String tag, String message) {
        if (DEBUG) {
            Log.w(tag, message);
        }
    }

    public static void Log(String msg, Object... format) {
        Log(String.format(msg, format));
    }
}

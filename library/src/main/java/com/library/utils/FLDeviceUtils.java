package com.library.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.UUID;

/**
 * 手机基本信息
 *
 * Created by chen_fulei on 2015/8/21.
 */
public class FLDeviceUtils {

    /**
     * 机器的mac地址
     * 注意添加权限access_wifi_state
     * @param mContext
     * @return
     */
    public static String getMediaMac(Context mContext) {
        WifiManager wifiManager = (WifiManager) mContext
                .getSystemService(mContext.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifi = wifiManager.getConnectionInfo();
            return wifi.getMacAddress();
        }

        return null;
    }

    /**
     * 获取当前网络状态
     * @param mContext
     * @return
     */
    public static boolean getNetStatus(Context mContext){
        ConnectivityManager conn = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = conn.getActiveNetworkInfo();
        if (net == null || !net.isConnected()) {
            return false;
        }else{
            return true;
        }
    }

    /**
     * 设备是否能够上网
     * @param mContext
     * @return
     */
    public static boolean isNetWorkAvailable(Context mContext){
        ConnectivityManager conn = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = conn.getActiveNetworkInfo();
        if(net !=null){
            return net.isAvailable();
        }else{
            return false;
        }
    }

    /**
     * 判断是否使用的是WIFI
     * @param mContext
     * @return
     */
    public static boolean checkNetWiFi(Context mContext){
        ConnectivityManager conn = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(net.isAvailable()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获得当前app的版本versionName
     *
     * @param mContext
     * @return
     */
    public static String getVersion(Context mContext) {
        try {
            return mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 64).versionName;
        } catch (Exception e) {
           e.printStackTrace();
        }

        return null;
    }

    /**
     * 获得当前app版本的versionCode
     *
     * @param mContext
     * @return
     */
    public static int getVersionCode(Context mContext) {
        try {
            return mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 64).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 获得kernel 版本信息
     *
     * @param mContext
     * @return
     */
    public static String getKernelInfo(Context mContext) {
        String procVersionStr = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    "/proc/version"), 256);
            procVersionStr = reader.readLine();
            reader.close();
            return procVersionStr;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return procVersionStr;
    }


    /**
     * 内置的存储卡号
     *
     * @return
     */
    public static String getEMMCID() {
        String procVersionStr = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    "/sys/block/mmcblk0/device/cid"));
            procVersionStr = reader.readLine();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return procVersionStr;
    }

    /**
     * 获取当前设备的唯一序号
     *
     * @param mContext
     * @return
     */
    public static String getDeviceId(Context mContext) {
        TelephonyManager tm = (TelephonyManager) mContext
                .getSystemService(mContext.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     * 获取UUID
     *
     * @param mContext
     * @return
     */
    public static String getUUID(Context mContext) {
        TelephonyManager tm = (TelephonyManager) mContext
                .getSystemService(mContext.TELEPHONY_SERVICE);
        String androidId = ""
                + android.provider.Settings.Secure.getString(
                mContext.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tm
                .getDeviceId().hashCode() << 32) | tm.getDeviceId().hashCode());

        return deviceUuid.toString();
    }


    /**
     * 获取当前android系统版本
     *
     * @return
     */
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取当前设备型号
     *
     * @return
     */
    public static String getDeviceMode() {
        return Build.BRAND + "_" + Build.MODEL;
    }




}

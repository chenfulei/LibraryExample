package com.library.http;

import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.library.constants.FLConstants;
import com.library.utils.FLFileUtils;

import java.io.File;

/**
 * Created by chen_fulei on 2015/8/24.
 */
public class FLHttpConfig {

    /** 缓存文件夹 **/
    public static String CACHEPATH = FLConstants.cacheFolder+"/cache";
    /** 线程池大小 **/
    public static int NETWORK_POOL_SIZE = 4;
    /** Http请求超时时间 **/
    public static int TIMEOUT = 5000;

    /** 磁盘缓存大小 */
    public static int DISK_CACHE_SIZE = 5 * 1024 * 1024;
    /** 缓存有效时间: 默认5分钟 */
    public int cacheTime = 5;

    /** 在Http请求中，如果服务器也声明了对缓存时间的控制，那么是否优先使用服务器设置: 默认false */
    public static boolean useServerControl = false;

    /** 为了更真实的模拟网络请求。如果启用，在读取完成以后，并不立即返回而是延迟500毫秒再返回 */
    public boolean useDelayCache = false;
    /** 如果启用了useDelayCache，本属性才有效。单位:ms */
    public long delayTime = 500;

    /** 同时允许多少个下载任务，建议不要太大(注意：本任务最大值不能超过NETWORK_POOL_SIZE) */
    public static int MAX_DOWNLOAD_TASK_SIZE = 2;

    /** 缓存器 **/
    public FLCache mCache;
    /** 网络请求执行器 **/
    public FLNetwork mNetwork;
    /** Http响应的分发器 **/
    public FLDelivery mDelivery;
    /** 下载控制器队列，对每个下载任务都有一个控制器负责控制下载 */
    public FLDownloadTaskQueue mController;
    /** 全局的cookie，如果每次Http请求都需要传递固定的cookie，可以设置本项 */
    public static String sCookie;

    public FLHttpConfig() {
        File folder = FLFileUtils.getSaveFolder(CACHEPATH);
        mCache = new FLDiskCache(folder, DISK_CACHE_SIZE);
        mNetwork = new FLNetwork(httpStackFactory());
        mDelivery = new FLDeliveryExecutor(new Handler(Looper.getMainLooper()));
        mController = new FLDownloadTaskQueue(FLHttpConfig.MAX_DOWNLOAD_TASK_SIZE);
    }

    /**
     * 创建HTTP请求端的生产器(将抽象工厂缩减为方法)
     *
     * @return
     */
    public FLHttpStack httpStackFactory() {
        if (Build.VERSION.SDK_INT >= 11) {
            return new FLHttpConnectStack();
        } else {
            return new FLHttpClientStack(AndroidHttpClient.newInstance("volley/0"));
        }
    }

    public void setCookieString(String cookie) {
        sCookie = cookie;

    }

    public String getCookieString() {
        return sCookie;
    }

}

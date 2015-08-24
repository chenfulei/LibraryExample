package com.library.http;

import com.library.constants.FLConstants;

/**
 * Created by chen_fulei on 2015/8/24.
 */
public class FLHttpConfig {

    public static boolean DEBUG = true;

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

}

package com.library.bitmap;

import com.library.constants.FLConstants;
import com.library.http.FLCache;
import com.library.http.FLDiskCache;
import com.library.utils.FLFileUtils;

import java.io.File;

/**
 * Bitmap配置器
 *
 * Created by chen_fulei on 2015/8/26.
 */
public class FLBitmapConfig {
    public static String CACHEPATH = FLConstants.cacheFolder+"/image";

    /** 磁盘缓存大小 */
    public static int DISK_CACHE_SIZE = 5 * 1024 * 1024;
    /** 磁盘缓存器 **/
    public static FLCache mCache;
    public static FLImageDisplayer.ImageCache mMemoryCache;

    public int cacheTime = 1440000;

    // 为了防止网速很快的时候速度过快而造成先显示加载中图片，然后瞬间显示网络图片的闪烁问题
    public long delayTime = 100;

    public FLBitmapConfig() {
        File folder = FLFileUtils.getSaveFolder(CACHEPATH);
        if (mCache == null) {
            mCache = new FLDiskCache(folder, DISK_CACHE_SIZE);
            mMemoryCache = new FLBitmapMemoryCache((int)(Runtime.getRuntime().maxMemory() / 8));
        }
    }

}

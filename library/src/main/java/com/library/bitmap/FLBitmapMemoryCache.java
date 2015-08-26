package com.library.bitmap;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.library.utils.FLSystemUtils;

/**
 * 使用lru算法的Bitmap内存缓存池<br>
 *
 * Created by chen_fulei on 2015/8/26.
 */
public final class FLBitmapMemoryCache implements FLImageDisplayer.ImageCache {

    private FLMemoryLruCache<String, Bitmap> cache;

    public FLBitmapMemoryCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        init(maxMemory / 8);
    }

    /**
     * @param maxSize
     *            使用内存缓存的内存大小，单位：kb
     */
    public FLBitmapMemoryCache(int maxSize) {
        init(maxSize);
    }

    /**
     * @param maxSize
     *            使用内存缓存的内存大小，单位：kb
     */
    @SuppressLint("NewApi")
    private void init(int maxSize) {
        cache = new FLMemoryLruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                super.sizeOf(key, value);
                if (FLSystemUtils.getSDKVersion() >= 12) {
                    return value.getByteCount();
                } else {
                    return value.getRowBytes() * value.getHeight();
                }
            }
        };
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void removeAll() {
        cache.removeAll();
    }

    /**
     * 已过期，请使用putBitmap(String key, Bitmap bitmap)
     *
     * @param key
     *            图片的地址
     * @param bitmap
     *            要缓存的bitmap
     */
    @Deprecated
    public void put(String key, Bitmap bitmap) {
        if (this.get(key) == null) {
            cache.put(key, bitmap);
        }
    }

    /**
     * 已过期，请使用gutBitmap(String key)
     *
     * @param key
     *            图片的地址
     * @return
     */
    @Deprecated
    public Bitmap get(String key) {
        return cache.get(key);
    }

    /**
     * @param url
     *            图片的地址
     * @return
     */
    @Override
    public Bitmap getBitmap(String url) {
        return cache.get(url);
    }

    /**
     * @param url
     *            图片的地址
     * @param bitmap
     *            要缓存的bitmap
     */
    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        if (this.getBitmap(url) == null) {
            cache.put(url, bitmap);
        }
    }
}

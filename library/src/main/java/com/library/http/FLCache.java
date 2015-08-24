package com.library.http;

import java.util.Collections;
import java.util.Map;

/**
 * 一个缓存接口协议，其中包含了缓存的bean原型
 * Created by chen_fulei on 2015/8/24.
 */
public interface FLCache {
    public Entry get(String key);

    public void put(String key, Entry entry);

    public void remove(String key);

    public void clear();

    /**
     * 执行在线程中
     */
    public void initialize();

    /**
     * 让一个缓存过期
     *
     * @param key
     *            Cache key
     * @param fullExpire
     *            True to fully expire the entry, false to soft expire
     */
    public void invalidate(String key, boolean fullExpire);

    /**
     * cache真正缓存的数据bean，这个是会被保存的缓存对象
     */
    public static class Entry {
        public byte[] data;
        public String etag; // 为cache标记一个tag

        public long serverDate; // 本次请求成功时的服务器时间
        public long ttl; // 有效期,System.currentTimeMillis()

        public Map<String, String> responseHeaders = Collections.emptyMap();

        /**
         * 是否已过期
         */
        public boolean isExpired() {
            return this.ttl < System.currentTimeMillis();
        }
    }
}

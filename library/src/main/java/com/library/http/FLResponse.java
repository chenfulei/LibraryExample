package com.library.http;

import java.util.Map;

/**
 * Http响应封装类，包含了本次响应的全部信息
 *
 * Created by chen_fulei on 2015/8/24.
 */
public class FLResponse<T> {
    /**
     * Http响应的类型
     */
    public final T result;

    /**
     * 本次响应的缓存对象，如果失败则为null
     */
    public final FLCache.Entry cacheEntry;

    public final FLHttpException error;

    public final Map<String, String> headers;

    public boolean isSuccess() {
        return error == null;
    }

    private FLResponse(T result, Map<String, String> headers,
                       FLCache.Entry cacheEntry) {
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.error = null;
        this.headers = headers;
    }

    private FLResponse(FLHttpException error) {
        this.result = null;
        this.cacheEntry = null;
        this.headers = null;
        this.error = error;
    }

    /**
     * 返回一个成功的HttpRespond
     *
     * @param result
     *            Http响应的类型
     * @param cacheEntry
     *            缓存对象
     */
    public static <T> FLResponse<T> success(T result,
                                          Map<String, String> headers, FLCache.Entry cacheEntry) {
        return new FLResponse<T>(result, headers, cacheEntry);
    }

    /**
     * 返回一个失败的HttpRespond
     *
     * @param error
     *            失败原因
     */
    public static <T> FLResponse<T> error(FLHttpException error) {
        return new FLResponse<T>(error);
    }
}

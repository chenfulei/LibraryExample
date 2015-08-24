package com.library.http;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Http请求端
 * Created by chen_fulei on 2015/8/24.
 */
public interface FLHttpStack {

    /**
     * 让Http请求端去发起一个Request
     *
     * @param request
     *            一次实际请求集合
     * @param additionalHeaders
     *            Http请求头
     * @return 一个Http响应
     */
    public HttpResponse performRequest(FLRequest<?> request,
                                       Map<String, String> additionalHeaders) throws IOException;
}

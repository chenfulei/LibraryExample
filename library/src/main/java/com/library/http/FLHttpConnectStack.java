package com.library.http;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * HttpUrlConnection方式实现
 * Created by chen_fulei on 2015/8/24.
 */
public class FLHttpConnectStack implements FLHttpStack {

    @Override
    public HttpResponse performRequest(FLRequest<?> request, Map<String, String> additionalHeaders) throws IOException {
        return null;
    }
}

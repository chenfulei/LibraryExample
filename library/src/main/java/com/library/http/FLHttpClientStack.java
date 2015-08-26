package com.library.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.util.Map;

/**
 * HttpClient请求端实现
 * Created by chen_fulei on 2015/8/24.
 */
public class FLHttpClientStack implements FLHttpStack{

    protected final HttpClient mClient;

    private final static String HEADER_CONTENT_TYPE = "Content-Type";

    public FLHttpClientStack(HttpClient client) {
        mClient = client;
    }

    @Override
    public HttpResponse performRequest(FLRequest<?> request, Map<String, String> additionalHeaders) throws IOException {
        return null;
    }
}

package com.library.http;

import com.library.utils.Debug;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.cookie.DateUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络请求执行器，将传入的Request使用HttpStack客户端发起网络请求，并返回一个NetworkRespond结果
 *
 * Created by chen_fulei on 2015/8/25.
 */
public class FLNetwork {
    protected static final boolean DEBUG = FLHttpConfig.DEBUG;
    protected final FLHttpStack mHttpStack;

    public FLNetwork(FLHttpStack httpStack) {
        mHttpStack = httpStack;
    }

    /**
     * 实际执行一个请求的方法
     *
     * @param request
     *            一个请求任务
     * @return 一个不会为null的响应
     * @throws FLHttpException
     */
    public FLNetworkResponse performRequest(FLRequest<?> request)
            throws FLHttpException {
        while (true) {
            HttpResponse httpResponse = null;
            byte[] responseContents = null;
            Map<String, String> responseHeaders = new HashMap<String, String>();
            try {
                // 标记Http响应头在Cache中的tag
                Map<String, String> headers = new HashMap<String, String>();
                addCacheHeaders(headers, request.getCacheEntry());
                httpResponse = mHttpStack.performRequest(request, headers);

                StatusLine statusLine = httpResponse.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                responseHeaders = convertHeaders(httpResponse.getAllHeaders());
                if (statusCode == HttpStatus.SC_NOT_MODIFIED) { // 304
                    return new FLNetworkResponse(HttpStatus.SC_NOT_MODIFIED,
                            request.getCacheEntry() == null ? null : request
                                    .getCacheEntry().data,
                            responseHeaders, true);
                }

                if (httpResponse.getEntity() != null) {
                    if (request instanceof FLFileRequest) {
                        responseContents = ((FLFileRequest) request)
                                .handleResponse(httpResponse);
                    } else {
                        responseContents = entityToBytes(httpResponse
                                .getEntity());
                    }
                } else {
                    responseContents = new byte[0];
                }

                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
                return new FLNetworkResponse(statusCode, responseContents,
                        responseHeaders, false);
            } catch (SocketTimeoutException e) {
                throw new FLHttpException(new SocketTimeoutException(
                        "socket timeout"));
            } catch (ConnectTimeoutException e) {
                throw new FLHttpException(new SocketTimeoutException(
                        "socket timeout"));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {
                int statusCode = 0;
                FLNetworkResponse networkResponse = null;
                if (httpResponse != null) {
                    statusCode = httpResponse.getStatusLine().getStatusCode();
                } else {
                    throw new FLHttpException("NoConnection error", e);
                }
                Debug.Log("Unexpected response code %d for %s", statusCode,
                        request.getUrl());
                if (responseContents != null) {
                    networkResponse = new FLNetworkResponse(statusCode,
                            responseContents, responseHeaders, false);
                    if (statusCode == HttpStatus.SC_UNAUTHORIZED
                            || statusCode == HttpStatus.SC_FORBIDDEN) {
                        throw new FLHttpException("auth error");
                    } else {
                        throw new FLHttpException(
                                "server error, Only throw ServerError for 5xx status codes.",
                                networkResponse);
                    }
                } else {
                    throw new FLHttpException(networkResponse);
                }
            }
        }
    }

    /**
     * 标记Respondeader响应头在Cache中的tag
     *
     * @param headers
     * @param entry
     */
    private void addCacheHeaders(Map<String, String> headers, FLCache.Entry entry) {
        if (entry == null) {
            return;
        }
        if (entry.etag != null) {
            headers.put("If-None-Match", entry.etag);
        }
        if (entry.serverDate > 0) {
            Date refTime = new Date(entry.serverDate);
            headers.put("If-Modified-Since", DateUtils.formatDate(refTime));
        }
    }

    /**
     * 把HttpEntry转换为byte[]
     *
     * @param entity
     * @return
     * @throws IOException
     * @throws FLHttpException
     */
    private byte[] entityToBytes(HttpEntity entity) throws IOException,
            FLHttpException {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(
                ByteArrayPool.get(), (int) entity.getContentLength());
        byte[] buffer = null;
        try {
            InputStream in = entity.getContent();
            if (in == null) {
                throw new FLHttpException("server error");
            }
            buffer = ByteArrayPool.get().getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            try {
                entity.consumeContent();
            } catch (IOException e) {
                Debug.Log("Error occured when calling consumingContent");
            }
            ByteArrayPool.get().returnBuf(buffer);
            bytes.close();
        }
    }

    /**
     * 转换RespondHeader为Map类型
     */
    private static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
}
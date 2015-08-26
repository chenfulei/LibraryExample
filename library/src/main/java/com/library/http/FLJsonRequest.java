package com.library.http;

import com.library.utils.Debug;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 用来发起application/json格式的请求的，我们平时所使用的是form表单提交的参数，而使用JsonRequest提交的是json参数。
 *
 * Created by chen_fulei on 2015/8/26.
 */
public class FLJsonRequest extends FLRequest<byte[]> {
    private static final String PROTOCOL_CHARSET = "utf-8";
    private static final String PROTOCOL_CONTENT_TYPE = String.format(
            "application/json; charset=%s", PROTOCOL_CHARSET);

    private final String mRequestBody;
    private final FLHttpParams mParams;

    public FLJsonRequest(int method, String url, FLHttpParams params,
                       FLHttpCallBack callback) {
        super(method, url, callback);
        mRequestBody = params.getJsonParams();
        mParams = params;
    }

    @Override
    public Map<String, String> getHeaders() {
        return mParams.getHeaders();
    }

    @Override
    protected void deliverResponse(Map<String, String> headers, byte[] response) {
        if (mCallback != null) {
            mCallback.onSuccess(headers, response);
        }
    }

    @Override
    public FLResponse<byte[]> parseNetworkResponse(FLNetworkResponse response) {
        return FLResponse.success(response.data, response.headers,
                FLHttpHeaderParser.parseCacheHeaders(mConfig, response));
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody
                    .getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            Debug.Log(
                    "Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }
}

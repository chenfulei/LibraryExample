package com.library.http;

import com.library.utils.Debug;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Form表单形式的Http请求
 *
 * Created by chen_fulei on 2015/8/26.
 */
public class FLFormRequest extends FLRequest<byte[]> {

    private final FLHttpParams mParams;

    public FLFormRequest(String url, FLHttpCallBack callback) {
        this(HttpMethod.GET, url, null, callback);
    }

    public FLFormRequest(int httpMethod, String url, FLHttpParams params,
                         FLHttpCallBack callback) {
        super(httpMethod, url, callback);
        if (params == null) {
            params = new FLHttpParams();
        }
        this.mParams = params;
    }

    @Override
    public String getBodyContentType() {
        return mParams.getContentType().getValue();
    }

    @Override
    public Map<String, String> getHeaders() {
        return mParams.getHeaders();
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mParams.writeTo(bos);
        } catch (IOException e) {
            Debug.LogE("", "IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    public FLResponse<byte[]> parseNetworkResponse(FLNetworkResponse response) {
        return FLResponse.success(response.data, response.headers,
                FLHttpHeaderParser.parseCacheHeaders(mConfig, response));
    }

    @Override
    protected void deliverResponse(Map<String, String> headers, byte[] response) {
        if (mCallback != null) {
            mCallback.onSuccess(headers, response);
        }
    }
}

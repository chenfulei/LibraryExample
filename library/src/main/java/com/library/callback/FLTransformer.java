package com.library.callback;

/**
 * 转换器
 * Created by chen_fulei on 2015/7/28.
 */
public interface FLTransformer {

    <T> T transform(String url, Class<T> type, String encoding, byte[] data, FLAjaxStatus status);
}

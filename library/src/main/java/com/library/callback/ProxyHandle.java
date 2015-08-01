package com.library.callback;

import org.apache.http.HttpRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.Proxy;

/**
 *异步代理
 *
 * Created by chen_fulei on 2015/8/1.
 */
public abstract class ProxyHandle {
    public abstract void applyProxy(AbstractAjaxCallback<?, ?> cb, HttpRequest request, DefaultHttpClient client);
    public abstract Proxy makeProxy(AbstractAjaxCallback<?, ?> cb);
}

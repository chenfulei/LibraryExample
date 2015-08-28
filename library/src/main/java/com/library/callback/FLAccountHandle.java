package com.library.callback;

import android.content.Context;

import org.apache.http.HttpRequest;

import java.net.HttpURLConnection;
import java.util.LinkedHashSet;

/**
 * Created by chen_fulei on 2015/8/17.
 */
public abstract class FLAccountHandle {

    private LinkedHashSet<FLAbstractAjaxCallback<?, ?>> callbacks;

    public synchronized void auth(FLAbstractAjaxCallback<?, ?> cb){

        if(callbacks == null){
            callbacks = new LinkedHashSet<FLAbstractAjaxCallback<?,?>>();
            callbacks.add(cb);
            auth();
        }else{
            callbacks.add(cb);
        }

    }

    public abstract boolean authenticated();

    protected synchronized void success(Context context){

        if(callbacks != null){

            for(FLAbstractAjaxCallback<?, ?> cb: callbacks){
                cb.async(context);
            }

            callbacks = null;
        }

    }

    protected synchronized void failure(Context context, int code, String message){

        if(callbacks != null){

            for(FLAbstractAjaxCallback<?, ?> cb: callbacks){
                cb.failure(code, message);
            }

            callbacks = null;
        }

    }


    protected abstract void auth();

    public abstract boolean expired(FLAbstractAjaxCallback<?, ?> cb, FLAjaxStatus status);

    public abstract boolean reauth(FLAbstractAjaxCallback<?, ?> cb);

    public void applyToken(FLAbstractAjaxCallback<?, ?> cb, HttpRequest request){
    }

    public void applyToken(FLAbstractAjaxCallback<?, ?> cb, HttpURLConnection conn){
    }

    public String getNetworkUrl(String url){
        return url;
    }

    public String getCacheUrl(String url){
        return url;
    }

    public void unauth(){
    }

}

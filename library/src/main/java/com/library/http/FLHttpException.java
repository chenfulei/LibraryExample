package com.library.http;

/**
 * Created by chen_fulei on 2015/8/24.
 */
public class FLHttpException extends Exception {
    public final FLNetworkResponse networkResponse;

    public FLHttpException() {
        networkResponse = null;
    }

    public FLHttpException(FLNetworkResponse response) {
        networkResponse = response;
    }

    public FLHttpException(String exceptionMessage) {
        super(exceptionMessage);
        networkResponse = null;
    }

    public FLHttpException(String exceptionMessage, FLNetworkResponse response) {
        super(exceptionMessage);
        networkResponse = response;
    }

    public FLHttpException(String exceptionMessage, Throwable reason) {
        super(exceptionMessage, reason);
        networkResponse = null;
    }

    public FLHttpException(Throwable cause) {
        super(cause);
        networkResponse = null;
    }
}

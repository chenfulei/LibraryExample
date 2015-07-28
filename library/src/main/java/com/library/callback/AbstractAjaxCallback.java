package com.library.callback;

import android.app.Activity;

import com.library.constants.FLConstants;

import org.apache.http.client.methods.HttpUriRequest;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * ajax 回调处理程序核心类
 * The core class of ajax callback handler.
 * <p/>
 * Created by chen_fulei on 2015/7/28.
 */
public abstract class AbstractAjaxCallback<T, K> implements Runnable {

    private static int NET_TIMEOUT = 30000; //默认网络请求超时时间(毫秒)
    private static String AGENT = null; // 默认设置网络代理
    private static int NETWORK_POOL = 6;// 线程池中最大线程数
    private static boolean GZIP = true; // 默认数据打包
    private static boolean REUSE_CLIENT = true;
    private static boolean SIMULATE_ERROR = false; //模拟网络错误
    /**
     * 设置静态的转换器， 这个转换器是无状态的，如果必须有状态，那要使用AjaxCallback.transformer或 AjaxUtility.transformer
     * 转换器状态选择：1. Native 2. instance transformer() 3. static setTransformer()
     */
    private static Transformer st;
    protected AccountHandle ah;
    private Class<T> type; //返回的class
    private Reference<Object> whandler; //在java中指的是指针
    private Object handler; //基类
    private String callback; // 反射基类的方法
    private WeakReference<Object> progress; // 回收Object垃圾集合
    private Transformer transformer;
    private WeakReference<Activity> act;
    private int method = FLConstants.METHOD_DETECT;
    private HttpUriRequest request;

    /**
     * 设置网络请求超时时间
     *
     * @param timeout
     */
    public static void setTimeout(int timeout) {
        NET_TIMEOUT = timeout;
    }

    /**
     * 设置网络代理
     *
     * @param agent
     */
    public static void setAgent(String agent) {
        AGENT = agent;
    }

    /**
     * 设置压缩打包
     *
     * @param gZip
     */
    public static void setGZip(boolean gZip) {
        GZIP = gZip;
    }

    /**
     * 设置模拟网络错误
     *
     * @param error
     */
    public static void setSimulateError(boolean error) {
        SIMULATE_ERROR = error;
    }

    /**
     * 设置转换器
     *
     * @param transformer 默认转换器将原始数据转换成指定的类型
     */
    public static void setTransformer(Transformer transformer) {
        st = transformer;
    }

    /**
     * 获取自身对象
     *
     * @return
     */
    private K self() {

        return (K) this;
    }

    /**
     * 清除
     */
    private void clear() {
        whandler = null;
        handler = null;
        progress = null;
        request = null;
        transformer = null;
        ah = null;
        act = null;
    }

    /**
     * 获取返回的class 对象
     * Gets the ajax response type.
     *
     * @return
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * 用弱引用设置一个回调处理程序。使用弱处理程序,如果你不希望ajax回调处理程序对象的垃圾收集。
     *
     * @param handler
     * @param callback
     * @return
     */
    public K weakHandler(Object handler, String callback) {
        this.whandler = new WeakReference<Object>(handler);
        this.callback = callback;
        this.handler = null;
        return self();
    }



    /**
     * 线程执行（内部使用）
     */
    @Override
    public void run() {

    }
}

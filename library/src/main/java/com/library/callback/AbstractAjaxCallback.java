package com.library.callback;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import com.library.constants.FLConstants;
import com.library.utils.Debug;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

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

    private Class<T> type; //返回的class
    private Reference<Object> whandler; //在java中指的是指针
    private Object handler; //基类
    private String callback; // 反射基类的方法
    private WeakReference<Object> progress; // 回收Object垃圾集合

    private String url; // 网络请求的url
    private String networkUrl; //
    protected Map<String, Object> params; // 参数
    protected Map<String, String> headers; // 头文件参数
    protected Map<String, String> cookies; // cookie参数

    private Transformer transformer;
    protected T result;

    private int policy = FLConstants.CACHE_DEFAULT;
    private File cacheDir;
    private File targetFile;
    protected AccountHandle ah;

    protected AjaxStatus status;

    protected boolean fileCache; //是否文件缓存
    protected boolean memCache; //
    private boolean refresh; // 刷新
    private int timeout = 0; // 超时时间
    private boolean redirect = true;

    private long expire;
    private String encoding = "UTF-8";
    private WeakReference<Activity> act;

    private int method = FLConstants.METHOD_DETECT;
    private HttpUriRequest request;

    private boolean uiCallback = true;
    private int retry = 0;



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
     * 设置静态的转换器， 这个转换器是无状态的，如果必须有状态，那要使用AjaxCallback.transformer或 AjaxUtility.transformer
     * 转换器状态选择：1. Native 2. instance transformer() 3. static setTransformer()
     */
    private static Transformer st;
    /**
     * 设置转换器
     *
     * @param transformer 默认转换器将原始数据转换成指定的类型
     */
    public static void setTransformer(Transformer transformer) {
        st = transformer;
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
     * Set a callback handler. See weakHandler for handler objects, such as Activity, that should not be held from garbaged collected.
     * @param handler
     * @param callback
     * @return
     */
    public K handler(Object handler, String callback){
        this.handler = handler;
        this.callback = callback;
        this.whandler = null;
        return self();
    }

    /**
     * 设置url
     * @param url
     * @return
     */
    public K url(String url){
        this.url = url;

        return self();
    }

    public K networkUrl(String url){
        this.networkUrl = url;
        return self();
    }

    /**
     * 设置所需的ajax响应类型。在ajax回调函数类型参数是必需的,否则不会发生。
     * Current supported type: JSONObject.class, String.class, byte[].class, Bitmap.class, XmlDom.class
     * @param type
     * @return
     */
    public K type(Class<T> type){
        this.type = type;
        return self();
    }

    public K method(int method){
        this.method = method;
        return self();
    }

    public K timeout(int timeout){
        this.timeout = timeout;
        return self();
    }

    /**
     * 如果http请求应该遵循重定向。默认是true
     * @param redirect
     * @return
     */
    public K redirect(boolean redirect){
        this.redirect = redirect;
        return self();
    }

    public K retry(int retry){
        this.retry = retry;
        return self();
    }

    /**
     * 设置转换器
     * @param transformer
     * @return
     */
    public K transformer(Transformer transformer){
        this.transformer = transformer;
        return self();
    }

    /**
     * 设置是否文件缓存
     * Set ajax request to be file cached.
     *
     * @param cache
     * @return
     */
    public K fileCache(boolean cache){
        this.fileCache = cache;
        return self();
    }

    /**
     * Indicate ajax request to be memcached. Note: The default ajax handler does not supply a memcache.
     * Subclasses such as BitmapAjaxCallback can provide their own memcache.
     *
     * @param cache the cache
     * @return self
     */
    public K memCache(boolean cache){
        this.memCache = cache;
        return self();
    }

    public K policy(int policy){
        this.policy = policy;
        return self();
    }

    /**
     * 表明ajax请求应该使用回调的主ui线程。默认是true
     * Indicate the ajax request should use the main ui thread for callback. Default is true.
     * @param refresh
     * @return
     */
    public K refresh(boolean refresh){
        this.refresh = refresh;
        return self();
    }

    /**
     * Indicate the ajax request should use the main ui thread for callback. Default is true.
     *
     * @param uiCallback use the main ui thread for callback
     * @return self
     */
    public K uiCallback(boolean uiCallback){
        this.uiCallback = uiCallback;
        return self();
    }

    /**
     * The expire duation for filecache. If a cached copy will be served if a cached file exists within current time minus expire duration.
     *
     * @param expire the expire
     * @return self
     */
    public K expire(long expire){
        this.expire = expire;
        return self();
    }

    /**
     * 设置请求头字段
     * Set the header fields for the http request.
     * @param name
     * @param value
     * @return
     */
    public K header(String name, String value){
        if(headers == null){
            headers = new HashMap<String, String>();
        }
        headers.put(name, value);
        return self();
    }

    /**
     * Set the header fields for the http request.
     *
     * @param headers the header
     * @return self
     */

    public K headers(Map<String, String> headers){
        this.headers = (Map<String, String>) headers;
        return self();
    }

    /**
     * Set the cookies for the http request.
     *
     * @param name the name
     * @param value the value
     * @return self
     */
    public K cookie(String name, String value){
        if(cookies == null){
            cookies = new HashMap<String, String>();
        }
        cookies.put(name, value);
        return self();
    }

    /**
     * 设置http request cookies
     * @param cookies
     * @return
     */
    public K cookies(Map<String, String> cookies){
        this.cookies = (Map<String, String>) cookies;
        return self();
    }

    /**
     * 设置http request 的字符集
     * 默认是：utf-8
     * @param encoding
     * @return
     */
    public K encoding(String encoding){
        this.encoding = encoding;
        return self();
    }

    private HttpHost proxy;
    public K proxy(String host, int port){
        proxy = new HttpHost(host, port);
        return self();
    }

    public K proxy(String host, int port, String user, String password){

        proxy(host, port);
        String authHeader = makeAuthHeader(user, password);

        Debug.Log("proxy auth", authHeader);

        return header("Proxy-Authorization", authHeader);

    }

    private static String makeAuthHeader(String username, String password){

        String cred = username + ":" + password;
        byte[] data = cred.getBytes();

        String auth = "Basic " + new String(AjaxUtility.encode64(data, 0, data.length));
        return auth;
    }

    /**
     * 目标文件
     * @param file
     * @return
     */
    public K targetFile(File file){
        this.targetFile = file;
        return self();
    }

    /**
     * 设置参数
     *  Set http POST params. If params are set, http POST method will be used.
     * The UTF-8 encoded value.toString() will be sent with POST.
     *
     * Header field "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" will be added if no Content-Type header field presents.
     *
     * @param name
     * @param value
     * @return
     */
    public K param(String name, Object value){
        if(params == null){
            params = new HashMap<String, Object>();
        }
        params.put(name, value);
        return self();
    }

    /**
     * Set the http POST params. See param(String name, Object value).
     *
     * @param params the params
     * @return self
     */
    @SuppressWarnings("unchecked")
    public K params(Map<String, ?> params){
        this.params = (Map<String, Object>) params;
        return self();
    }

    /**
     * 设置加载滚动条
     * Set the progress view (can be a progress bar or any view) to be shown (VISIBLE) and hide (GONE) depends on progress.
     * @param view the progress view
     * @return self
     */
    public K progress(View view){
        return progress((Object) view);
    }

    /**
     * 设置进度条
     *
     * Set the dialog to be shown and dismissed depends on progress.
     * @param dialog
     * @return self
     */
    public K progress(Dialog dialog){
        return progress((Object) dialog);
    }

    /**
     * 设置进度条
     * @param progress
     * @return
     */
    public K progress(Object progress){
        if(progress != null){
            this.progress = new WeakReference<Object>(progress);
        }
        return self();
    }

    //返回的对象类型
    private static final Class<?>[] DEFAULT_SIG = {String.class, Object.class, AjaxStatus.class};
    //是否完成
    private boolean completed;

    void callback(){

    }

    /**
     * 唤醒所有锁
     */
    private void wake(){

        if(!blocked) return;

        synchronized(this){
            try{
                notifyAll();
            }catch(Exception e){
            }
        }

    }

    private boolean blocked; //处理同步异步之间锁 唤醒和睡眠

    /**
     * 线程锁
     * Block the current thread until the ajax call is completed. Returns immediately if ajax is already completed.
     * Exception will be thrown if this method is called in main thread.
     */
    public void block(){

        if(AjaxUtility.isUIThread()){
            throw new IllegalStateException("Cannot block UI thread.");
        }

        if(completed) return;

        try{
            synchronized(this){
                blocked = true;
                //wait at most the network timeout plus 5 seconds, this guarantee thread will never be blocked forever
                this.wait(NET_TIMEOUT + 5000);
            }
        }catch(Exception e){
        }

    }

    /**
     * The callback method to be overwritten for subclasses.
     *
     * @param url the url
     * @param object the object
     * @param status the status
     */
    public void callback(String url, T object, AjaxStatus status){

    }

    protected void skip(String url, T object, AjaxStatus status){

    }

//    protected T fileGet(String url, File file, AjaxStatus status){
//
//        try {
//            byte[] data = null;
//
//            if(isStreamingContent()){
//                status.file(file);
//            }else{
//                data = AQUtility.toBytes(new FileInputStream(file));
//            }
//
//            return transform(url, data, status);
//        } catch(Exception e) {
//            Debug.Log(e);
//            return null;
//        }
//    }

    protected boolean isStreamingContent(){
        return File.class.equals(type) || XmlPullParser.class.equals(type) || InputStream.class.equals(type) || XmlDom.class.equals(type);
    }

    protected T datastoreGet(String url){

        return null;

    }

    /**
     * 线程执行（内部使用）
     */
    @Override
    public void run() {

    }
}

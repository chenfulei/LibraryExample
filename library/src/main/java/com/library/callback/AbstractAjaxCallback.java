package com.library.callback;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Xml;
import android.view.View;

import com.library.constants.FLConstants;
import com.library.utils.Debug;
import com.library.utils.FLDataUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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
    private static int lastStatus = 200; // 最后网络请求的状态

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
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl(){
        return url;
    }

    /**
     * Gets the callback method name.
     *
     * @return the callback
     */
    public String getCallback() {
        return callback;
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

        String auth = "Basic " + new String(FLDataUtils.encode64(data, 0, data.length));
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

    /**
     * 获取 html 字符集
     * @param html
     * @return
     */
    private String getCharset(String html){
        String pattern = "<meta [^>]*http-equiv[^>]*\"Content-Type\"[^>]*>"; //正则表达式
        Pattern p = Pattern.compile(pattern , Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);

        if (!m.find()) return  null; // 没有设置字符集
        String tag = m.group(); // 匹配正则表达的字符串

        return parseCharset(tag);
    }

    /**
     * 匹配 charset
     * @param tag
     * @return
     */
    private String parseCharset(String tag){
        if(tag == null) return null;
        int i = tag.indexOf("charset");
        if(i == -1) return null;

        int e = tag.indexOf(";", i) ;
        if(e == -1) e = tag.length();

        String charset = tag.substring(i + 7, e).replaceAll("[^\\w-]", "");
        return charset;
    }

    /**
     * 更改字符集(改为utf-8)
     * @param data
     * @param target
     * @param status
     * @return
     */
    private String correctEncoding(byte[] data , String target , AjaxStatus status){
        String result = null;
        try {
            //如果是utf-8的字符就直接返回
            if(!"utf-8".equalsIgnoreCase(target)){
                return new String(data, target);
            }

            String header = parseCharset(status.getHeader("Content-Type"));
            Debug.Log("parsing header", header);
            if(header != null){
                return new String(data, header);
            }

            result = new String(data, "utf-8");

            String charset = getCharset(result);

            Debug.Log("parsing needed", charset);

            if(charset != null && !"utf-8".equalsIgnoreCase(charset)){
                Debug.Log("correction needed", charset);
                result = new String(data, charset);
                status.data(result.getBytes("utf-8"));
            }

        }catch (Exception e){
            e.printStackTrace();
            Debug.Log(e);
        }

        return result;
    }

    /**
     * Gets the handler.
     * @return
     */
    public Object getHandler(){
        if (handler != null) return handler;
        if (whandler ==null) return null;

        return whandler.get();
    }

    protected static int getLastStatus(){
        return lastStatus;
    }

    /**
     * Gets the result. Can be null if ajax is not completed or the ajax call failed.
     * This method should only be used after the block() method.
     *
     * @return the result
     */
    public T getResult(){
        return result;
    }

    /**
     * Gets the ajax status.
     * This method should only be used after the block() method.
     *
     * @return the status
     */

    public AjaxStatus getStatus(){
        return status;
    }

    /**
     * Gets the encoding. Default is UTF-8.
     *
     * @return the encoding
     */
    public String getEncoding(){
        return encoding;
    }

    private boolean abort;

    /**
     * Abort the http request that will interrupt the network transfer.
     * This method currently doesn't work with multi-part post.
     *
     * If no network transfer is involved (eg. response is file cached), this method has no effect.
     *
     */

    public void abort(){
        abort = true;

        if(request != null && !request.isAborted()){
            request.abort();
        }
    }

    /**
     * 判断activity是否注销
     * @return
     */
    private boolean isActive(){
        if(act == null) return true;
        Activity a = act.get();
        if(a == null || a.isFinishing()){
            return false;
        }
        return true;
    }

    /**
     * 获取缓存url
     * @return
     */
    private String getCacheUrl(){

        return url;
    }

    /**
     * 获取url
     * @param url
     * @return
     */
    private String getNetworkUrl(String url){

        String result = url;

        if(networkUrl != null){
            result = networkUrl;
        }
        return result;
    }

    /**
     * 获取缓存文件
     * @return
     */
    protected File getCacheFile(){
        return AjaxUtility.getCacheFile(cacheDir, getCacheUrl());
    }

    /**
     * 判断缓存文件是否有效
     * @param context
     * @return
     */
    protected boolean cacheAvailable(Context context){
        return fileCache && AjaxUtility.getExistedCacheByUrl(AjaxUtility.getCacheDir(context, policy), url) != null;
    }

    //返回的对象类型
    private static final Class<?>[] DEFAULT_SIG = {String.class, Object.class, AjaxStatus.class};
    //是否完成
    private boolean completed;

    void callback(){
        showProgress(false);

        completed = true;

        if(isActive()){

            if(callback != null) {
                Object handler = getHandler();
                Class<?>[] AJAX_SIG = {String.class, type, AjaxStatus.class};
                AjaxUtility.invokeMethod(handler, callback, true, AJAX_SIG, DEFAULT_SIG, url, result, status);
            }else{
                try{
                    callback(url, result, status);
                }catch(Exception e){
                    e.printStackTrace();
                    Debug.Log(e);
                }
            }

        }else{
            skip(url, result, status);
        }

        filePut();
        if(!blocked){
            status.close();
        }

        wake();
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

    protected T fileGet(String url, File file, AjaxStatus status){

        try {
            byte[] data = null;

            if(isStreamingContent()){
                status.file(file);
            }else{
                data = AjaxUtility.toBytes(new FileInputStream(file));
            }

            return transform(url, data, status);
        } catch(Exception e) {
            Debug.Log(e);
            return null;
        }
    }

    protected boolean isStreamingContent(){
        return File.class.equals(type) || XmlPullParser.class.equals(type) || InputStream.class.equals(type) || XmlDom.class.equals(type);
    }

    protected T datastoreGet(String url){

        return null;

    }

    protected T memGet(String url){
        return null;
    }


    protected void memPut(String url, T object){
    }

    private File getPreFile(){

        boolean pre = isStreamingContent();

        File result = null;

        if(pre){

            if(targetFile != null){
                result = targetFile;
            }else if(fileCache){
                result = getCacheFile();
            }else{
                File dir = AjaxUtility.getTempDir();

                if(dir == null) dir = cacheDir;
                result = AjaxUtility.getCacheFile(dir, url);
            }
        }

        if(result != null && !result.exists()){
            try{
                result.getParentFile().mkdirs();
                result.createNewFile();

            }catch(Exception e){
                e.printStackTrace();
                Debug.Log(e);
                return null;
            }
        }

        return result;
    }

    protected void filePut(String url, T object, File file, byte[] data){

        if(file == null || data == null) return;

        AjaxUtility.storeAsync(file, data, 0);
    }

    /**
     * 保存数据到文件
     */
    private void filePut(){

        if(result != null && fileCache){

            byte[] data = status.getData();

            try{
                if(data != null && status.getSource() == AjaxStatus.NETWORK){

                    File file = getCacheFile();
                    if(!status.getInvalid()){
                        filePut(url, result, file, data);
                    }else{
                        if(file.exists()){
                            file.delete();
                        }
                    }

                }
            }catch(Exception e){
                e.printStackTrace();
                Debug.Log(e);
            }
            status.data(null);
        }else if(status.getCode() == AjaxStatus.TRANSFORM_ERROR){

            File file = getCacheFile();

            if(file.exists()){
                file.delete();
                Debug.Log("invalidated cache due to transform error");
            }

        }
    }

    /**
     * 缓存(根据url缓存数据)
     * @param cacheDir
     * @param url
     * @return
     */
    protected File accessFile(File cacheDir, String url){

        if(expire < 0) return null;

        File file = AjaxUtility.getExistedCacheByUrl(cacheDir, url);

        if(file != null && expire != 0){
            long diff = System.currentTimeMillis() - file.lastModified();
            if(diff > expire){
                return null;
            }
        }

        return file;
    }

    /**
     * 显示进度条
     * @param show
     */
    protected void showProgress(final boolean show){
        final Object p  = progress == null ? null : progress.get();

        if (p != null){
            if (AjaxUtility.isUIThread()){
                Common.showProgress(p , url , show);
            }else {
                AjaxUtility.post(new Runnable() {
                    @Override
                    public void run() {
                        Common.showProgress(p , url , show);
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected T transform(String url, byte[] data, AjaxStatus status){

        if(type == null){
            return null;
        }

        File file = status.getFile();

        if(data != null){

            if(type.equals(Bitmap.class)){ //如果是图片就转换为图片类
                return (T) BitmapFactory.decodeByteArray(data, 0, data.length);
            }

            if(type.equals(JSONObject.class)){ // json

                JSONObject result = null;
                String str = null;
                try {
                    str = new String(data, encoding);
                    result = (JSONObject) new JSONTokener(str).nextValue();
                } catch (Exception e) {
                    e.printStackTrace();
                    Debug.Log(e);
                    Debug.Log(str);
                }
                return (T) result;
            }

            if(type.equals(JSONArray.class)){ //数组

                JSONArray result = null;

                try {
                    String str = new String(data, encoding);
                    result = (JSONArray) new JSONTokener(str).nextValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return (T) result;
            }

            if(type.equals(String.class)){ // 字符串

                String result = null;

                if(status.getSource() == AjaxStatus.NETWORK){
                    Debug.Log("network");
                    result = correctEncoding(data, encoding, status);
                }else{
                    Debug.Log("file");
                    try {
                        result = new String(data, encoding);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return (T) result;
            }

            if(type.equals(byte[].class)){ // 数据类
                return (T) data;
            }

            if(transformer != null){
                return transformer.transform(url, type, encoding, data, status);
            }
            if(st != null){
                return st.transform(url, type, encoding, data, status);
            }
        }else if(file != null){

            if(type.equals(File.class)){
                return (T) file;
            }
            if(type.equals(XmlDom.class)){ // xml

                XmlDom result = null;
                try {
                    FileInputStream fis = new FileInputStream(file);
                    result = new XmlDom(fis);
                    status.closeLater(fis);
                } catch (Exception e) {
                    e.printStackTrace();
                    Debug.Log(e);
                    return null;
                }
                return (T) result;
            }

            if(type.equals(XmlPullParser.class)){

                XmlPullParser parser = Xml.newPullParser();
                try{

                    FileInputStream fis = new FileInputStream(file);
                    parser.setInput(fis, encoding);
                    status.closeLater(fis);
                }catch(Exception e) {
                    e.printStackTrace();
                    Debug.Log(e);
                    return null;
                }
                return (T) parser;
            }

            if(type.equals(InputStream.class)){
                try{
                    FileInputStream fis = new FileInputStream(file);
                    status.closeLater(fis);
                    return (T) fis;
                }catch(Exception e) {
                    e.printStackTrace();
                    Debug.Log(e);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 线程执行网络请求
     *
     * Starts the async process.
     *
     * If activity is passed, the callback method will not be invoked if the activity is no longer in use.
     * Specifically, isFinishing() is called to determine if the activity is active.
     *
     * @param activity
     */
    public void async(Activity activity){
        //如果activity已经注销
        if (activity.isFinishing()){
            Debug.LogE("Possible memory leak. Calling ajax with a terminated activity.");
        }

        if (type == null){
            Debug.LogE("type() is not called with response type.");
        }

        this.act = new WeakReference<Activity>(activity);
        async((Context) activity);
    }

    /**
     * 线程执行网络请求
     *
     * Starts the async process.
     * @param context
     */
    public void async(Context context){
        if(status == null){
            status = new AjaxStatus();
            status.redirect(url).refresh(refresh);
        }else if(status.getDone()){
            status.reset();
            result = null;
        }

        showProgress(true); //显示进度条

        work(context);
    }

    /**
     * 执行线程
     * @param context
     */
    private void work(Context context){

        T object = memGet(url);

        if(object != null){
            result = object;
            status.source(AjaxStatus.MEMORY).done();
            callback();
        }else{

            cacheDir = AjaxUtility.getCacheDir(context, policy);
            execute(this);
        }
    }

    /**
     * 线程池
     */
    private static ExecutorService fetchExe;
    public static void execute(Runnable job){

        if(fetchExe == null){
            fetchExe = Executors.newFixedThreadPool(NETWORK_POOL);
        }

        fetchExe.execute(job);
    }

    /**
     * 获取正在执行的线程数
     *
     *  Return the number of active ajax threads. Note that this doesn't necessarily correspond to active network connections.
     * Ajax threads might be reading a cached url from file system or transforming the response after a network transfer.
     * @return
     */
    public static int getActiveCount(){
        int result = 0;
        if(fetchExe instanceof ThreadPoolExecutor){
            result = ((ThreadPoolExecutor) fetchExe).getActiveCount();
        }

        return result;
    }

    /**
     * 设置线程池数 （最大显示25）
     * Sets the simultaneous network threads limit. Highest limit is 25.
     *
     * @param limit the new network threads limit
     */
    public static void setNetworkLimit(int limit){

        NETWORK_POOL = Math.max(1, Math.min(25, limit));
        fetchExe = null;

        Debug.Log("setting network limit", NETWORK_POOL + "");
    }

    /**
     * 取消所有线程活动
     * Cancel ALL ajax tasks.
     *
     * Warning: Do not call this method unless you are exiting an application.
     */
    public static void cancel(){

        if(fetchExe != null){
            fetchExe.shutdownNow();
            fetchExe = null;
        }
        BitmapAjaxCallback.clearTasks();
    }

    /**
     * 线程执行（内部使用）
     */
    @Override
    public void run() {
        if(!status.getDone()){

            try{
                backgroundWork();
            }catch(Throwable e){
                e.printStackTrace();
                status.code(AjaxStatus.NETWORK_ERROR).done();
            }

            if(!status.getReauth()){
                //if doesn't need to reauth
                if(uiCallback){
                    AjaxUtility.post(this);
                }else{
                    afterWork();
                }
            }
        }else{
            afterWork();
        }
    }

    /**
     * 异步底层执行
     */
    private void backgroundWork(){
        if(!refresh){

            if(fileCache){
                fileWork();
            }
        }

        if(result == null){
            datastoreWork();
        }

        if(result == null){
            networkWork();
        }
    }

    /**
     * 执行失败
     * @param code
     * @param message
     */
    public void failure(int code , String message){
        if(status != null){
            status.code(code).message(message).done();

            if(uiCallback){
                AjaxUtility.post(this);
            }else{
                afterWork();
            }
        }
    }

    private void afterWork(){

        if(url != null && memCache){
            memPut(url, result);
        }
        callback();
        clear();
    }

    /**
     * 文件缓存处理
     */
    private void fileWork(){

        File file = accessFile(cacheDir, getCacheUrl());

        //if file exist
        if(file != null){
            //convert
            status.source(AjaxStatus.FILE);
            result = fileGet(url, file, status);

            //if result is ok
            if(result != null){
                status.time(new Date(file.lastModified())).done();
            }
        }
    }

    /**
     * 数据保存处理
     */
    private void datastoreWork(){

        result = datastoreGet(url);

        if(result != null){
            status.source(AjaxStatus.DATASTORE).done();
        }
    }

    /**
     * 网络请求
     */
    private boolean reauth;
    private void networkWork(){
        if(url == null){
            status.code(AjaxStatus.NETWORK_ERROR).done();
            return;
        }
        byte[] data = null;

        try{
            network(retry + 1);

            data = status.getData();

        }catch(IOException e){

            Debug.LogE("IOException");

            //work around for IOException when 401 is returned
            //reference: http://stackoverflow.com/questions/11735636/how-to-get-401-response-without-handling-it-using-try-catch-in-android
            String message = e.getMessage();
            if(message != null && message.contains("No authentication challenges found")){
                status.code(401).message(message);
            }else{
                status.code(AjaxStatus.NETWORK_ERROR).message("network error");
            }

        }catch(Exception e){
            e.printStackTrace();
            Debug.Log(e);
            status.code(AjaxStatus.NETWORK_ERROR).message("network error");
        }
        try{
            result = transform(url, data, status);
        }catch(Exception e){
           e.printStackTrace();
            Debug.Log(e);
        }
        if(result == null && data != null){
            status.code(AjaxStatus.TRANSFORM_ERROR).message("transform error");
        }

        lastStatus = status.getCode();
        status.done();
    }

    //added retry logic
    /**
     * 网络请求 （递归方式）
     * @param attempts
     * @throws IOException
     */
    private void network(int attempts) throws IOException{
        if(attempts <= 1){
            network();
            return;
        }
        for(int i = 0; i < attempts; i++){
            try{
                network();
                return;
            }catch(IOException e){
                if(i == attempts - 1){
                    throw e;
                }
            }
        }
    }
    private void network() throws IOException{
        String url = this.url;
        Map<String, Object> params = this.params;

        //convert get to post request, if url length is too long to be handled on web
        if(params == null && url.length() > 2000){
            Uri uri = Uri.parse(url);
            url = extractUrl(uri);
            params = extractParams(uri);
        }

        url = getNetworkUrl(url);

        //不同请求方式
        if(FLConstants.METHOD_DELETE == method){
            httpDelete(url, status);
        }else if(FLConstants.METHOD_PUT == method){
            httpPut(url, params, status);
        }else{
            if(FLConstants.METHOD_POST == method && params == null){
                params = new HashMap<String, Object>();
            }
            if(params == null){
                httpGet(url, status);
            }else{
                if(isMultiPart(params)){
                    httpMulti(url, params, status);
                }else{
                    httpPost(url, params, status);
                }
            }
        }
    }
    private static String extractUrl(Uri uri){

        String result = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();

        String fragment = uri.getFragment();
        if(fragment != null) result += "#" + fragment;

        return result;
    }
    private static Map<String, Object> extractParams(Uri uri){

        Map<String, Object> params = new HashMap<String, Object>();
        String[] pairs = uri.getQuery().split("&");

        for(String pair: pairs){
            String[] split = pair.split("=");
            if(split.length >= 2){
                params.put(split[0], split[1]);
            }else if(split.length == 1){
                params.put(split[0], "");
            }
        }
        return params;
    }

    private static String patchUrl(String url){
        url = url.replaceAll(" ", "%20").replaceAll("\\|", "%7C");
        return url;
    }


    private static ProxyHandle proxyHandle;
    public static void setProxyHandle(ProxyHandle handle){
        proxyHandle = handle;
    }

    private static SocketFactory ssf;
    private static final String lineEnd = "\r\n";
    private static final String twoHyphens = "--";
    private static final String boundary = "*****";

    private void httpGet(String url, AjaxStatus status) throws IOException{

        Debug.Log("get", url);
        url = patchUrl(url);

        HttpGet get = new HttpGet(url);

        httpDo(get, url, status);
    }

    private void httpDelete(String url, AjaxStatus status) throws IOException{

        Debug.Log("get", url);
        url = patchUrl(url);

        HttpDelete del = new HttpDelete(url);

        httpDo(del, url, status);
    }

    private void httpPost(String url, Map<String, Object> params, AjaxStatus status) throws ClientProtocolException, IOException{

        Debug.Log("post", url);

        HttpEntityEnclosingRequestBase req = new HttpPost(url);

        httpEntity(url, req, params, status);
    }

    private void httpPut(String url, Map<String, Object> params, AjaxStatus status) throws ClientProtocolException, IOException{

        Debug.Log("put", url);

        HttpEntityEnclosingRequestBase req = new HttpPut(url);

        httpEntity(url, req, params, status);
    }

    private void httpEntity(String url, HttpEntityEnclosingRequestBase req, Map<String, Object> params, AjaxStatus status) throws ClientProtocolException, IOException{

        //This setting seems to improve post performance
        //http://stackoverflow.com/questions/3046424/http-post-requests-using-httpclient-take-2-seconds-why
        req.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        HttpEntity entity = null;
        Object value = params.get(FLConstants.POST_ENTITY);

        if(value instanceof HttpEntity){
            entity = (HttpEntity) value;
        }else{
            List<NameValuePair> pairs = new ArrayList<>();

            for(Map.Entry<String, Object> e: params.entrySet()){
                value = e.getValue();
                if(value != null){
                    pairs.add(new BasicNameValuePair(e.getKey(), value.toString()));
                }
            }

            entity = new UrlEncodedFormEntity(pairs, "UTF-8");
        }

        if(headers != null  && !headers.containsKey("Content-Type")){
            headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        }

        req.setEntity(entity);
        httpDo(req, url, status); // 执行
    }

    /**
     * 网络请求核心部分
     * @param hr
     * @param url
     * @param status
     * @throws ClientProtocolException
     * @throws IOException
     */
    private void httpDo(HttpUriRequest hr, String url, AjaxStatus status) throws ClientProtocolException, IOException{

        DefaultHttpClient client = getClient();

        if(proxyHandle != null){
            proxyHandle.applyProxy(this, hr, client);
        }

        if(AGENT != null){
            hr.addHeader("User-Agent", AGENT);
        }else if(AGENT == null && GZIP){
            hr.addHeader("User-Agent", "gzip");
        }


        if(headers != null){
            for(String name: headers.keySet()){
                hr.addHeader(name, headers.get(name));
            }
        }

        if(GZIP && (headers == null || !headers.containsKey("Accept-Encoding"))){
            hr.addHeader("Accept-Encoding", "gzip");
        }
        String cookie = makeCookie();
        if(cookie != null){
            hr.addHeader("Cookie", cookie);
        }

        HttpParams hp = hr.getParams();
        if(proxy != null) hp.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);


        if(timeout > 0){
            hp.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
            hp.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
        }

        if(!redirect){
            hp.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        }

        HttpContext context = new BasicHttpContext();
        CookieStore cookieStore = new BasicCookieStore();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        request = hr;

        if(abort){
            throw new IOException("Aborted");
        }

        if(SIMULATE_ERROR){
            throw new IOException("Simulated Error");
        }

        HttpResponse response = null;

        try{
            //response = client.execute(hr, context);
            response = execute(hr, client, context);
        }catch(HttpHostConnectException e){

            //if proxy is used, automatically retry without proxy
            if(proxy != null){
                Debug.LogE("proxy failed, retrying without proxy");
                hp.setParameter(ConnRoutePNames.DEFAULT_PROXY, null);
                //response = client.execute(hr, context);
                response = execute(hr, client, context);
            }else{
                throw e;
            }
        }

        byte[] data = null;
        String redirect = url;

        int code = response.getStatusLine().getStatusCode();
        String message = response.getStatusLine().getReasonPhrase();
        String error = null;

        HttpEntity entity = response.getEntity();

        File file = null;
        File tempFile = null;

        if(code < 200 || code >= 300){
            InputStream is = null;
            try{
                if(entity != null){
                    is = entity.getContent();
                    byte[] s = toData(getEncoding(entity), is);

                    error = new String(s, "UTF-8");

                    Debug.LogE("error", error);
                }
            }catch(Exception e){
                e.printStackTrace();
                Debug.Log(e);
            }finally{
                AjaxUtility.close(is);
            }
        }else{

            HttpHost currentHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
            redirect = currentHost.toURI() + currentReq.getURI();

            int size = Math.max(32, Math.min(1024 * 64, (int) entity.getContentLength()));

            OutputStream os = null;
            InputStream is = null;

            try{
                file = getPreFile();

                if(file == null){
                    os = new PredefinedBAOS(size);
                }else{
                    //file.createNewFile();
                    tempFile = makeTempFile(file);
                    os = new BufferedOutputStream(new FileOutputStream(tempFile));
                }
                is = entity.getContent();

                boolean gzip = "gzip".equalsIgnoreCase(getEncoding(entity));

                if(gzip){
                    is = new GZIPInputStream(is);
                }

                int contentLength = (int) entity.getContentLength();

                copy(is, os, contentLength, tempFile, file);
                if(file == null){
                    data = ((PredefinedBAOS) os).toByteArray();
                }else{
                    if(!file.exists() || file.length() == 0){
                        file = null;
                    }
                }

            }finally{
                AjaxUtility.close(is);
                AjaxUtility.close(os);
            }
        }

        Debug.Log("response:", code+"");
        if(data != null){
            Debug.Log( data.length+" :"+ url);
        }
        status.code(code).message(message).error(error).redirect(redirect).time(new Date()).data(data).file(file).client(client).context(context).headers(response.getAllHeaders());
    }

    /**
     * 获取http client
     */
    private static DefaultHttpClient client;
    private static DefaultHttpClient getClient(){

        if(client == null || !REUSE_CLIENT){

            Debug.Log("creating http client");

            HttpParams httpParams = new BasicHttpParams();

            //httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

            HttpConnectionParams.setConnectionTimeout(httpParams, NET_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, NET_TIMEOUT);

            //ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(NETWORK_POOL));
            ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(25));

            //Added this line to avoid issue at: http://stackoverflow.com/questions/5358014/android-httpclient-oom-on-4g-lte-htc-thunderbolt
            HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", ssf == null ? SSLSocketFactory.getSocketFactory() : ssf, 443));

            ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, registry);
            client = new DefaultHttpClient(cm, httpParams);
        }
        return client;
    }

    //helper method to support underscore subdomain

    /**
     * 执行网络请求
     * @param hr
     * @param client
     * @param context
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    private HttpResponse execute(HttpUriRequest hr, DefaultHttpClient client, HttpContext context) throws ClientProtocolException, IOException{

        HttpResponse response = null;

        if(hr.getURI().getAuthority().contains("_")) {
            URL urlObj = hr.getURI().toURL();
            HttpHost host;
            if(urlObj.getPort() == -1) {
                host = new HttpHost(urlObj.getHost(), 80, urlObj.getProtocol());
            } else {
                host = new HttpHost(urlObj.getHost(), urlObj.getPort(), urlObj.getProtocol());
            }
            response = client.execute(host, hr, context);
        } else {
            response = client.execute(hr, context);
        }
        return response;
    }

    private static boolean isMultiPart(Map<String, Object> params){
        for(Map.Entry<String, Object> entry: params.entrySet()){
            Object value = entry.getValue();
            Debug.Log(entry.getKey(), value.toString());
            if(value instanceof File || value instanceof byte[] || value instanceof InputStream) return true;
        }
        return false;
    }

    private void httpMulti(String url, Map<String, Object> params, AjaxStatus status) throws IOException {

        Debug.Log("multipart", url);

        HttpURLConnection conn = null;
        DataOutputStream dos = null;

        URL u = new URL(url);

        Proxy py = null;

        if(proxy != null){
            Debug.Log("proxy :"+ proxy);
            py = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getHostName(), proxy.getPort()));
        }else if(proxyHandle != null){
            py = proxyHandle.makeProxy(this);
        }

        if(py == null){
            conn = (HttpURLConnection) u.openConnection();
        }else{
            conn = (HttpURLConnection) u.openConnection(py);
        }

        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(NET_TIMEOUT * 4);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data;charset=utf-8;boundary=" + boundary);

        if(headers != null){
            for(String name: headers.keySet()){
                conn.setRequestProperty(name, headers.get(name));
            }
        }

        String cookie = makeCookie();
        if(cookie != null){
            conn.setRequestProperty("Cookie", cookie);
        }

        dos = new DataOutputStream(conn.getOutputStream());
        for(Map.Entry<String, Object> entry: params.entrySet()){

            writeObject(dos, entry.getKey(), entry.getValue());
        }

        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        dos.flush();
        dos.close();

        conn.connect();
        int code = conn.getResponseCode();

        String message = conn.getResponseMessage();

        byte[] data = null;

        String encoding = conn.getContentEncoding();
        String error = null;

        if(code < 200 || code >= 300){
            error = new String(toData(encoding, conn.getErrorStream()), "UTF-8");
            Debug.LogE("error", error);
        }else{
            data = toData(encoding, conn.getInputStream());
        }
        Debug.Log("response :"+code);
        if(data != null){
            Debug.Log(data.length+":"+url);
        }

        status.code(code).message(message).redirect(url).time(new Date()).data(data).error(error).client(null);
    }

    /**
     * 字符流转换为数据
     * @param encoding
     * @param is
     * @return
     * @throws IOException
     */
    private byte[] toData(String encoding, InputStream is) throws IOException{

        boolean gzip = "gzip".equalsIgnoreCase(encoding);

        if(gzip){
            is = new GZIPInputStream(is);
        }

        return AjaxUtility.toBytes(is);
    }

    /**
     * 读写
     * @param dos
     * @param name
     * @param obj
     * @throws IOException
     */
    private static void writeObject(DataOutputStream dos, String name, Object obj) throws IOException{

        if(obj == null) return;

        if(obj instanceof File){

            File file = (File) obj;
            writeData(dos, name, file.getName(), new FileInputStream(file));

        }else if(obj instanceof byte[]){
            writeData(dos, name, name, new ByteArrayInputStream((byte[]) obj));
        }else if(obj instanceof InputStream){
            writeData(dos, name, name, (InputStream) obj);
        }else{
            writeField(dos, name, obj.toString());
        }
    }

    private static void writeData(DataOutputStream dos, String name, String filename, InputStream is) throws IOException {

        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\""+name+"\";"
                + " filename=\"" + filename + "\"" + lineEnd);

        //added to specify type
        dos.writeBytes("Content-Type: application/octet-stream");
        dos.writeBytes(lineEnd);
        dos.writeBytes("Content-Transfer-Encoding: binary");
        dos.writeBytes(lineEnd);

        dos.writeBytes(lineEnd);
        AjaxUtility.copy(is, dos);

        dos.writeBytes(lineEnd);
    }


    private static void writeField(DataOutputStream dos, String name, String value) throws IOException {
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"");
        dos.writeBytes(lineEnd);
        dos.writeBytes(lineEnd);

        byte[] data = value.getBytes("UTF-8");
        dos.write(data);

        dos.writeBytes(lineEnd);
    }

    /**
     * 使用cookie
     * @return
     */
    private String makeCookie(){
        if(cookies == null || cookies.size() == 0) return null;

        Iterator<String> iter = cookies.keySet().iterator();

        StringBuilder sb = new StringBuilder();

        while(iter.hasNext()){
            String key = iter.next();
            String value = cookies.get(key);
            sb.append(key);
            sb.append("=");
            sb.append(value);
            if(iter.hasNext()){
                sb.append("; ");
            }
        }
        return sb.toString();
    }

    /**
     * 将HttpEntity 转换为String
     * @param entity
     * @return
     */
    private String getEncoding(HttpEntity entity){

        if(entity == null) return null;

        Header eheader = entity.getContentEncoding();
        if(eheader == null) return null;

        return eheader.getValue();
    }

    /**
     * 数据缓存
     * @param is
     * @param os
     * @param max
     * @param tempFile
     * @param destFile
     * @throws IOException
     */
    private void copy(InputStream is, OutputStream os, int max, File tempFile, File destFile) throws IOException{
        //if no file operation is involved
        if(destFile == null){
            copy(is, os, max);
            return;
        }
        try{
            copy(is, os, max);
            is.close();
            os.close();
            tempFile.renameTo(destFile);
        }catch(IOException e){

            Debug.Log("copy failed, deleting files");
            //copy is a failure, delete everything
            tempFile.delete();
            destFile.delete();

            AjaxUtility.close(is);
            AjaxUtility.close(os);
            throw e;
        }
    }

    private File makeTempFile(File file) throws IOException{
        File temp = new File(file.getAbsolutePath() + ".tmp");
        temp.createNewFile();

        return temp;

    }

    /**
     *
     * @param is
     * @param os
     * @param max
     * @throws IOException
     */
    private void copy(InputStream is, OutputStream os, int max) throws IOException{
        Object o = null;
        if(progress != null){
            o = progress.get();
        }

        Progress p = null;

        if(o != null){
            p = new Progress(o);
        }
        AjaxUtility.copy(is, os, max, p);
    }
}

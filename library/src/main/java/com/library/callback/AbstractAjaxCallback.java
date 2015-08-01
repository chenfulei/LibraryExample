package com.library.callback;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Xml;
import android.view.View;

import com.library.constants.FLConstants;
import com.library.utils.Debug;
import com.library.utils.FLDataUtils;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if(ah != null){
            return ah.getCacheUrl(url);
        }
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

        if(ah != null){
            result = ah.getNetworkUrl(result);
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
        if(ah != null){
            if(!ah.authenticated()){
                Debug.LogE("auth needed", url);
                ah.auth(this);
                return;
            }
        }

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

    }

}

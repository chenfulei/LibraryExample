package com.library.callback;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.library.constants.FLConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 调用ajax实现方式的入口
 *
 * Created by chen_fulei on 2015/8/4.
 */
public abstract class FLAbstractAjaxMain<T extends FLAbstractAjaxMain<T>>{

    private Context context;
    private Activity activity;

    private View view;
    protected Object progress;
    private static WeakHashMap<Dialog, Void> dialogs = new WeakHashMap<Dialog, Void>();
    private FLTransformer trans;
    private int policy = FLConstants.CACHE_DEFAULT;
    private HttpHost proxy;

    public FLAbstractAjaxMain(Context context){
        this.context = context;
    }

    public FLAbstractAjaxMain(Activity activity){
        this.activity = activity;
    }

    private Context getContext(){
        if (activity != null){
            return activity;
        }
        return context;
    }

    /**
     * 自身对象
     * @return
     */
    protected T self(){
        return (T) this;
    }

    /**
     * 当前需要操作的View
     * @param view
     * @return
     */
    public T findView(View view){
        this.view = view;
        reset();

        return self();
    }

    /**
     * 获取当前操作的View
     * @return
     */
    public View getView(){
        return view;
    }

    /**
     * 创建进度条
     * @param view
     * @return
     */
    public T progress(Object view){
        progress = view;
        return self();
    }

    /**
     * 弹出框进度
     * @param dialog
     * @return
     */
    public T progress(Dialog dialog){
        progress = dialog;
        return self();
    }

    public T show(Dialog dialog){
        try{
            if(dialog != null){
                dialog.show();
                dialogs.put(dialog, null);
            }
        }catch(Exception e){
        }

        return self();
    }

    public T dismiss(Dialog dialog){

        try{
            if(dialog != null){
                dialogs.remove(dialog);
                dialog.dismiss();
            }
        }catch(Exception e){
        }
        return self();
    }

    public T dismiss(){

        Iterator<Dialog> keys = dialogs.keySet().iterator();

        while(keys.hasNext()){

            Dialog d = keys.next();
            try{
                d.dismiss();
            }catch(Exception e){
            }
            keys.remove();
        }
        return self();
    }

    /**
     * Apply the transformer to convert raw data to desired object type for the next ajax request.
     * @param transformer
     * @return
     */
    public T transformer(FLTransformer transformer){
        trans = transformer;
        return self();
    }

    /**
     * Apply the proxy info to next ajax request.
     * @param cachePolicy
     * @return
     */
    public T policy(int cachePolicy){
        policy = cachePolicy;
        return self();
    }
    public T proxy(String host, int port){
        proxy = new HttpHost(host, port);
        return self();
    }

    /**
     * 重置
     */
    protected void reset(){
        progress = null;
        trans = null;
        policy = FLConstants.CACHE_DEFAULT;
        proxy = null;
    }

    /**
     * 清除当前ImageView 的缓存
     * @return
     */
    public T clear(){
        if(view instanceof ImageView) {
            ImageView iv = ((ImageView) view);
            iv.setImageBitmap(null);
            iv.setTag(FLConstants.TAG_URL, null);
        }

        return self();
    }

    /**
     * 网络请求(本地)图片并设置imageView中src
     * @param url 路径
     * @param memCache 缓存
     * @param fileCache 本地文件缓存
     * @param targetWidth 设置图片大小
     * @param fallbackId 设置预置位图 resource id
     * @param preset 设置预置位图。这个位图将立即显示,直到ajax回调返回最终的图像的url。
     * @param animId 动画
     * @param ratio 旋转
     * @param round 原型
     * @param networkUrl 网络url
     * @return
     */
    protected T image(String  url , boolean memCache , boolean fileCache , int targetWidth , int fallbackId , Bitmap preset , int animId , float ratio ,
                      int round , String networkUrl){
        if (view instanceof ImageView){ // 必须是ImageView才能执行
            FLBitmapAjaxCallback.async(activity, getContext(), (ImageView) view, url, memCache, fileCache, targetWidth, fallbackId, preset,
                    animId, ratio, FLConstants.ANCHOR_DYNAMIC, progress, null, policy, round, proxy, networkUrl);
            reset();
        }

        return self();
    }

    public T image(String url, boolean memCache, boolean fileCache, int targetWidth, int fallbackId, Bitmap preset, int animId, float ratio){
        return image(url, memCache, fileCache, targetWidth, fallbackId, preset, animId, ratio, 0, null);
    }

    public T image(String url, boolean memCache, boolean fileCache, int targetWidth, int fallbackId, Bitmap preset, int animId){
        return image(url, memCache, fileCache, targetWidth, fallbackId, preset, animId, 0);
    }

    public T image(String url, boolean memCache, boolean fileCache, int targetWidth, int fallbackId){

        return image(url, memCache, fileCache, targetWidth, fallbackId, null, 0);
    }

    public T image(String url, boolean memCache, boolean fileCache){
        return image(url, memCache, fileCache, 0, 0);
    }

    public T image(String url){
        return image(url, true, true);
    }

    /**
     * 网络请求(本地)图片并设置imageView背景
     * @param url 路径
     * @param memCache 缓存
     * @param fileCache 本地文件缓存
     * @param targetWidth 设置图片大小
     * @param fallbackId 设置预置位图 resource id
     * @param preset 设置预置位图。这个位图将立即显示,直到ajax回调返回最终的图像的url。
     * @param animId 动画
     * @param ratio 旋转
     * @param round 原型
     * @param networkUrl 网络url
     * @return
     */
    protected T background(String  url , boolean memCache , boolean fileCache , int targetWidth , int fallbackId , Bitmap preset , int animId , float ratio ,
                      int round , String networkUrl){
        if (view instanceof ImageView){ // 必须是ImageView才能执行
//            BitmapAjaxCallback.async(activity , getContext() ,(ImageView) view ,  url , memCache , fileCache , targetWidth , fallbackId , preset ,
//                    animId , ratio , FLConstants.ANCHOR_DYNAMIC , progress , policy , round , proxy , true , networkUrl);
            reset();
        }

        return self();
    }

    public T background(String url, boolean memCache, boolean fileCache, int targetWidth, int fallbackId, Bitmap preset, int animId, float ratio){
        return background(url, memCache, fileCache, targetWidth, fallbackId, preset, animId, ratio, 0, null);
    }

    public T background(String url, boolean memCache, boolean fileCache, int targetWidth, int fallbackId, Bitmap preset, int animId){
        return background(url, memCache, fileCache, targetWidth, fallbackId, preset, animId, 0);
    }

    public T background(String url, boolean memCache, boolean fileCache, int targetWidth, int fallbackId){

        return background(url, memCache, fileCache, targetWidth, fallbackId, null, 0);
    }

    public T background(String url, boolean memCache, boolean fileCache){
        return background(url, memCache, fileCache, 0, 0);
    }

    public T background(String url){
        return background(url, true, true);
    }

    /**
     * 网络请求(本地)图片并设置imageView中src
     * @param url
     * @param options 调整图片的参数
     * @param networkUrl
     * @return
     */
    protected T image(String url, FLImageOptions options, String networkUrl){
        if(view instanceof ImageView){
            FLBitmapAjaxCallback.async(activity, getContext(), (ImageView) view, url, progress, null, options, proxy, networkUrl);
            reset();
        }
        return self();
    }

    public T image(String url, FLImageOptions options){
        return image(url, options, null);
    }

    /**
     * 网络请求(本地)图片并设置imageView背景
     * @param url
     * @param options 调整图片的参数
     * @param networkUrl
     * @return
     */
    protected T background(String url, FLImageOptions options, String networkUrl){
        if(view instanceof ImageView){
//            BitmapAjaxCallback.async(activity, getContext(), (ImageView) view, url, progress, options, proxy,networkUrl ,true);
            reset();
        }
        return self();
    }

    public T background(String url, FLImageOptions options){
        return background(url, options, null);
    }

    /**
     *网络请求(本地)图片并设置imageView src
     * @param callback
     * @return
     */
    public T image(FLBitmapAjaxCallback callback){

        if(view instanceof ImageView){
            callback.imageView((ImageView) view);
            invoke(callback);
        }

        return self();
    }

    /**
     *网络请求(本地)图片并设置imageView 背景
     * @param callback
     * @return
     */
    public T background(FLBitmapAjaxCallback callback){

        if(view instanceof ImageView){
            callback.imageView((ImageView) view);
            invoke(callback);
        }

        return self();
    }

    /**
     * 网络请求(本地)图片并设置imageView src
     * @param url
     * @param memCache
     * @param fileCache
     * @param targetWidth
     * @param resId
     * @param callback
     * @return
     */
    public T image(String url, boolean memCache, boolean fileCache, int targetWidth, int resId, FLBitmapAjaxCallback callback){

        callback.targetWidth(targetWidth).fallback(resId)
                .url(url).memCache(memCache).fileCache(fileCache);

        return image(callback);
    }

    /**
     * 网络请求(本地)图片并设置imageView 背景
     * @param url
     * @param memCache
     * @param fileCache
     * @param targetWidth
     * @param resId
     * @param callback
     * @return
     */
    public T background(String url, boolean memCache, boolean fileCache, int targetWidth, int resId, FLBitmapAjaxCallback callback){

//        callback.targetWidth(targetWidth).fallback(resId)
//                .url(url).memCache(memCache).fileCache(fileCache).setBackground(true);

        return image(callback);
    }

    public T image(File file, int targetWidth){
        return image(file, true, targetWidth, null);
    }
    public T background(File file, int targetWidth){
        return background(file, true, targetWidth, null);
    }

    public T image(File file, boolean memCache, int targetWidth, FLBitmapAjaxCallback callback){

        if(callback == null) callback = new FLBitmapAjaxCallback();
        callback.file(file);

        String url = null;
        if(file != null) url = file.getAbsolutePath();
        return image(url, memCache, true, targetWidth, 0, callback);

    }
    public T background(File file, boolean memCache, int targetWidth, FLBitmapAjaxCallback callback){

        if(callback == null) callback = new FLBitmapAjaxCallback();
        callback.file(file);

        String url = null;
        if(file != null) url = file.getAbsolutePath();
        return background(url, memCache, true, targetWidth, 0, callback);

    }

    public T image(Bitmap bm, float ratio){
        FLBitmapAjaxCallback cb = new FLBitmapAjaxCallback();
        cb.ratio(ratio).bitmap(bm);
        return image(cb);
    }

    /**
     * ajax 网络请求
     * @param callback
     * @param <K>
     * @return
     */
    public <K> T ajax(FLAjaxCallback<K> callback){
        return invoke(callback);
    }

    public <K> T ajax(String url, Class<K> type, FLAjaxCallback<K> callback){

        callback.type(type).url(url);
        return ajax(callback);
    }

    public <K> T ajax(String url, Class<K> type, long expire, FLAjaxCallback<K> callback){

        callback.type(type).url(url).fileCache(true).expire(expire);

        return ajax(callback);
    }

    public <K> T ajax(String url, Class<K> type, Object handler, String callback){
        FLAjaxCallback<K> cb = new FLAjaxCallback<K>();
        cb.type(type).weakHandler(handler, callback);

        return ajax(url, type, cb);
    }

    public <K> T ajax(String url, Class<K> type, long expire, Object handler, String callback){
        FLAjaxCallback<K> cb = new FLAjaxCallback<K>();
        cb.type(type).weakHandler(handler, callback).fileCache(true).expire(expire);

        return ajax(url, type, cb);
    }

    public <K> T ajax(String url, Map<String, ?> params, Class<K> type, FLAjaxCallback<K> callback){
        callback.type(type).url(url).params(params);
        return ajax(callback);
    }

    public <K> T ajax(String url, Map<String, ?> params, Class<K> type, Object handler, String callback){
        FLAjaxCallback<K> cb = new FLAjaxCallback<K>();
        cb.type(type).weakHandler(handler, callback);

        return ajax(url, params, type, cb);
    }

    /**
     * Ajax HTTP delete.
     * @param url
     * @param type
     * @param callback
     * @param <K>
     * @return
     */
    public <K> T delete(String url, Class<K> type, FLAjaxCallback<K> callback){
        callback.url(url).type(type).method(FLConstants.METHOD_DELETE);
        return ajax(callback);
    }
    public <K> T delete(String url, Class<K> type, Object handler, String callback){

        FLAjaxCallback<K> cb = new FLAjaxCallback<K>();
        cb.weakHandler(handler, callback);
        return delete(url, type, cb);
    }

    /***
     * Ajax HTTP put
     * @param url
     * @param contentHeader
     * @param entity
     * @param type
     * @param callback
     * @param <K>
     * @return
     */
    public <K> T put(String url, String contentHeader, HttpEntity entity, Class<K> type, FLAjaxCallback<K> callback){
        callback.url(url).type(type).method(FLConstants.METHOD_PUT).header("Content-Type", contentHeader).param(FLConstants.POST_ENTITY, entity);
        return ajax(callback);

    }
    public <K> T put(String url, JSONObject jo, Class<K> type, FLAjaxCallback<K> callback){

        try{
            StringEntity entity = new StringEntity(jo.toString(), "UTF-8");
            return put(url, "application/json", entity, type, callback);
        }catch(UnsupportedEncodingException e){
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Ajax HTTP post
     * @param url
     * @param contentHeader
     * @param entity
     * @param type
     * @param callback
     * @param <K>
     * @return
     */
    public <K> T post(String url, String contentHeader, HttpEntity entity, Class<K> type, FLAjaxCallback<K> callback){

        callback.url(url).type(type).method(FLConstants.METHOD_POST).header("Content-Type", contentHeader).param(FLConstants.POST_ENTITY, entity);
        return ajax(callback);

    }
    public <K> T post(String url, JSONObject jo, Class<K> type, FLAjaxCallback<K> callback){

        try{
            StringEntity entity = new StringEntity(jo.toString(), "UTF-8");
            return post(url, "application/json", entity, type, callback);
        }catch(UnsupportedEncodingException e){
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Ajax call with that block until response is ready. This method cannot be called on UI thread.
     * @param callback
     * @param <K>
     * @return
     */
    public <K> T sync(FLAjaxCallback<K> callback){
        ajax(callback);
        callback.block();
        return self();
    }

    /**
     *  Cache the url to file cache without any callback.
     * @param url
     * @param expire
     * @return
     */
    public T cache(String url, long expire) {
        return ajax(url, byte[].class, expire, null, null);
    }

    /**
     * Stop all ajax activities. Should only be called when app exits.
     * @return
     */
    public T ajaxCancel(){

        FLAjaxCallback.cancel();

        return self();
    }


    /**
     * Return file cached by ajax or image requests. Returns null if url is not cached.
     * @param url
     * @return File
     */
    public File getCachedFile(String url){
        File result = FLAjaxUtility.getExistedCacheByUrl(FLAjaxUtility.getCacheDir(getContext(), FLConstants.CACHE_PERSISTENT), url);
        if(result == null) result = FLAjaxUtility.getExistedCacheByUrl(FLAjaxUtility.getCacheDir(getContext(), FLConstants.CACHE_DEFAULT), url);
        return result;
    }

    /**
     * Delete any cached file for the url.
     *
     * @param url
     * @return self
     */
    public T invalidate(String url){

        File file = getCachedFile(url);
        if (file != null)
            file.delete();

        return self();
    }


    /**
     * Return bitmap cached by image requests. Returns null if url is not cached.
     *
     * @param url
     * @return Bitmap
     */

    public Bitmap getCachedImage(String url){
        return getCachedImage(url, 0);
    }

    /**
     * Return bitmap cached by image requests. Returns null if url is not cached.
     *
     * @param url
     * @param targetWidth The desired downsampled width.
     *
     * @return Bitmap
     */
    public Bitmap getCachedImage(String url, int targetWidth){

        Bitmap result = FLBitmapAjaxCallback.getMemoryCached(url, targetWidth);
        if(result == null){
            File file = getCachedFile(url);
            if(file != null){
                result = FLBitmapAjaxCallback.getResizedImage(file.getAbsolutePath(), null, targetWidth, true, 0);
            }
        }

        return result;
    }

    /**
     * Return cached bitmap with a resourceId. Returns null if url is not cached.
     *
     * Use this method instead of BitmapFactory.decodeResource(getResources(), resId) for caching.
     *
     * @param resId
     *
     * @return Bitmap
     */
    public Bitmap getCachedImage(int resId){
        return FLBitmapAjaxCallback.getMemoryCached(getContext(), resId);
    }

    /**
     * 反射
     * @param method
     * @param sig
     * @param params
     * @return
     */
    public Object invoke(String method, Class<?>[] sig, Object... params){

        Object object = view;
        if (object == null) object = activity;

        return FLAjaxUtility.invokeMethod(object, method, false, sig, params);
    }

    protected <K> T invoke(FLAbstractAjaxCallback<?, K> cb){
        if(progress != null){
            cb.progress(progress);
        }

        if(trans != null){
            cb.transformer(trans);
        }

        //if(policy != null){
        cb.policy(policy);
        //}

        if(proxy != null){
            cb.proxy(proxy.getHostName(), proxy.getPort());
        }

        if(activity != null){
            cb.async(activity);
        }else{
            cb.async(getContext());
        }

        reset();

        return self();
    }
}

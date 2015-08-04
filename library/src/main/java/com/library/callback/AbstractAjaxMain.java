package com.library.callback;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.library.constants.FLConstants;

import org.apache.http.HttpHost;

/**
 * 调用ajax实现方式的入口
 *
 * Created by chen_fulei on 2015/8/4.
 */
public abstract class AbstractAjaxMain<T extends AbstractAjaxMain<T>>{

    private Context context;
    private Activity activity;

    private View view;
    protected Object progress;
    private Transformer trans;
    private int policy = FLConstants.CACHE_DEFAULT;
    private HttpHost proxy;

    public AbstractAjaxMain(Context context){
        this.context = context;
    }

    public AbstractAjaxMain(Activity activity){
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

    /**
     * Apply the transformer to convert raw data to desired object type for the next ajax request.
     * @param transformer
     * @return
     */
    public T transformer(Transformer transformer){
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
            BitmapAjaxCallback.async(activity , getContext() ,(ImageView) view ,  url , memCache , fileCache , targetWidth , fallbackId , preset ,
                    animId , ratio , FLConstants.ANCHOR_DYNAMIC , progress , policy , round , proxy , false , networkUrl);
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
            BitmapAjaxCallback.async(activity , getContext() ,(ImageView) view ,  url , memCache , fileCache , targetWidth , fallbackId , preset ,
                    animId , ratio , FLConstants.ANCHOR_DYNAMIC , progress , policy , round , proxy , true , networkUrl);
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
    protected T image(String url, ImageOptions options, String networkUrl){
        if(view instanceof ImageView){
            BitmapAjaxCallback.async(activity, getContext(), (ImageView) view, url, progress, options, proxy,networkUrl ,false);
            reset();
        }
        return self();
    }

    public T image(String url, ImageOptions options){
        return image(url, options, null);
    }

    /**
     * 网络请求(本地)图片并设置imageView背景
     * @param url
     * @param options 调整图片的参数
     * @param networkUrl
     * @return
     */
    protected T background(String url, ImageOptions options, String networkUrl){
        if(view instanceof ImageView){
            BitmapAjaxCallback.async(activity, getContext(), (ImageView) view, url, progress, options, proxy,networkUrl ,true);
            reset();
        }
        return self();
    }

    public T background(String url, ImageOptions options){
        return background(url, options, null);
    }


}

package com.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.library.bitmap.FLBitmapCallBack;
import com.library.bitmap.FLBitmapConfig;
import com.library.bitmap.FLDiskImageRequest;
import com.library.bitmap.FLImageDisplayer;
import com.library.http.FLCache;
import com.library.http.FLHttpCallBack;
import com.library.utils.Debug;
import com.library.utils.FLFileUtils;
import com.library.utils.FLMatcher;
import com.library.utils.FLScreenUtils;
import com.library.utils.FLSystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * 图片加载
 *
 * Created by chen_fulei on 2015/8/26.
 */
public class FLBitmap {
    private final FLBitmapConfig mConfig;
    private final FLImageDisplayer displayer;

    private final List<View> doLoadingViews;

    public FLBitmap() {
        this(new FLBitmapConfig());
    }

    public FLBitmap(FLBitmapConfig bitmapConfig) {
        this.mConfig = bitmapConfig;
        displayer = new FLImageDisplayer(mConfig);
        doLoadingViews = new LinkedList<View>();
    }

    /**
     * 使用默认配置加载网络图片(屏幕的一半显示图片)
     *
     * @param imageView
     *            要显示图片的控件(ImageView设置src，普通View设置bg)
     * @param imageUrl
     *            图片的URL
     */
    public void display(View imageView, String imageUrl) {
        displayWithDefWH(imageView, imageUrl, null, null, null);
    }

    /**
     * @param imageView
     *            要显示的View
     * @param imageUrl
     *            网络图片地址
     * @param width
     *            要显示的图片的最大宽度
     * @param height
     *            要显示图片的最大高度
     */
    public void display(View imageView, String imageUrl, int width, int height) {
        display(imageView, imageUrl, width, height, null, null, null);
    }

    /**
     *
     * @param imageView
     *            要显示的View
     * @param imageUrl
     *            网络图片地址
     * @param callback
     *            加载过程的回调
     */
    public void display(View imageView, String imageUrl, FLBitmapCallBack callback) {
        displayWithDefWH(imageView, imageUrl, null, null, callback);
    }

    /**
     * 如果内存缓存有图片，则显示内存缓存的图片，否则显示默认图片
     *
     * @param imageView
     *            要显示的View
     * @param imageUrl
     *            网络图片地址
     * @param defaultImage
     *            如果没有内存缓存，则显示默认图片
     */
    public void displayCacheOrDefult(View imageView, String imageUrl,
                                     int defaultImage) {
        Bitmap cache = getMemoryCache(imageUrl);
        if (cache == null) {
            setViewImage(imageView, defaultImage);
        } else {
            setViewImage(imageView, cache);
        }
    }

    /**
     * @param imageView
     *            要显示的View
     * @param imageUrl
     *            网络图片地址
     * @param width
     *            要显示的图片的最大宽度
     * @param height
     *            要显示图片的最大高度
     * @param loadBitmap
     *            加载中图片
     */
    public void display(View imageView, String imageUrl, int width, int height,
                        int loadBitmap) {
        display(imageView, imageUrl, width, height, imageView.getResources()
                .getDrawable(loadBitmap), null, null);
    }

    /**
     *
     * @param imageView
     *            要显示的View
     * @param imageUrl
     *            网络图片地址
     * @param loadBitmap
     *            加载中的图片
     */
    public void displayWithLoadBitmap(View imageView, String imageUrl,
                                      int loadBitmap) {
        displayWithDefWH(imageView, imageUrl, imageView.getResources()
                .getDrawable(loadBitmap), null, null);
    }

    /**
     *
     * @param imageView
     *            要显示的View
     * @param imageUrl
     *            网络图片地址
     * @param errorBitmap
     *            加载出错时设置的默认图片
     */
    public void displayWithErrorBitmap(View imageView, String imageUrl,
                                       int errorBitmap) {
        displayWithDefWH(imageView, imageUrl, null, imageView.getResources()
                .getDrawable(errorBitmap), null);
    }

    /**
     * 如果不指定宽高，则使用默认宽高计算方法
     *
     * @param imageView
     *            要显示的View
     * @param imageUrl
     *            网络图片地址
     * @param loadBitmap
     *            加载中图片
     * @param errorBitmap
     *            加载失败的图片
     * @param callback
     *            加载过程的回调
     */
    public void displayWithDefWH(View imageView, String imageUrl,
                                 Drawable loadBitmap, Drawable errorBitmap, FLBitmapCallBack callback) {
        imageView.measure(0, 0);
        int w = imageView.getMeasuredWidth();
        int h = imageView.getMeasuredHeight();
        if (w < 5) {
            w = FLScreenUtils.getScreenW(imageView.getContext()) / 2;
        }
        if (h < 5) {
            h = FLScreenUtils.getScreenH(imageView.getContext()) / 2;
        }
        display(imageView, imageUrl, w, h, loadBitmap, errorBitmap, callback);
    }

    /**
     * @param imageView
     *            要显示的View
     * @param imageUrl
     *            网络图片地址
     * @param width
     *            要显示的图片的最大宽度
     * @param height
     *            要显示图片的最大高度
     * @param loadOrErrorBitmap
     *            加载中或加载失败都显示这张图片
     * @param callback
     *            加载过程的回调
     */
    public void display(View imageView, String imageUrl, int loadOrErrorBitmap,
                        int width, int height, FLBitmapCallBack callback) {
        display(imageView, imageUrl, width, height, imageView.getResources()
                .getDrawable(loadOrErrorBitmap), imageView.getResources()
                .getDrawable(loadOrErrorBitmap), callback);
    }

    /**
     * 显示网络图片(core)
     *
     * @param imageView
     *            要显示的View
     * @param imageUrl
     *            网络图片地址
     * @param width
     *            要显示的图片的最大宽度
     * @param height
     *            要显示图片的最大高度
     * @param loadBitmap
     *            加载中图片
     * @param errorBitmap
     *            加载失败的图片
     * @param callback
     *            加载过程的回调
     */
    public void display(View imageView, String imageUrl, int width, int height,
                        Drawable loadBitmap, Drawable errorBitmap, FLBitmapCallBack callback) {
        if (imageView == null) {
            showLogIfOpen("imageview is null");
            return;
        }
        if (FLMatcher.isEmpty(imageUrl)) {
            showLogIfOpen("image url is empty");
            return;
        }

        if (loadBitmap == null) {
            loadBitmap = new ColorDrawable(0xFFCFCFCF);
        }
        if (errorBitmap == null) {
            errorBitmap = new ColorDrawable(0xFFCFCFCF);
        }
        if (callback == null) {
            callback = new FLBitmapCallBack() {};
        }
        doDisplay(imageView, imageUrl, width, height, loadBitmap, errorBitmap,
                callback);
    }

    /**
     * 真正去加载一个图片
     */
    private void doDisplay(final View imageView, final String imageUrl,
                           int width, int height, final Drawable loadBitmap,
                           final Drawable errorBitmap, final FLBitmapCallBack callback) {
        checkViewExist(imageView);

        imageView.setTag(imageUrl);

        FLBitmapCallBack mCallback = new FLBitmapCallBack() {
            @Override
            public void onPreLoad() {
                if (callback != null) {
                    callback.onPreLoad();
                }
            }

            @Override
            public void onSuccess(Bitmap bitmap) {
                if (imageUrl.equals(imageView.getTag())) {
                    doSuccess(imageView, bitmap, loadBitmap);
                    if (callback != null) {
                        callback.onSuccess(bitmap);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                doFailure(imageView, errorBitmap);
                if (callback != null) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFinish() {
                try {
                    doLoadingViews.remove(imageView);
                } catch (Exception e) {
                }
                if (callback != null) {
                    callback.onFinish();
                }
            }
        };

        if (imageUrl.startsWith("http")) {
            displayer.get(imageUrl, width, height, mCallback);
        } else {
            new FLDiskImageRequest().load(imageUrl, width, width, mCallback);
        }
    }

    private void doFailure(View view, Drawable errorImage) {
        if (errorImage != null) {
            setViewImage(view, errorImage);
        }
    }

    /**
     * 需要解释一下：如果在本地没有缓存的时候，会首先调用一次onSuccess(null)，此时返回的bitmap是null，
     * 在这个时候我们去设置加载中的图片，当网络请求成功的时候，会再次调用onSuccess(bitmap)，此时才返回网络下载成功的bitmap
     */
    private void doSuccess(View view, Bitmap bitmap, Drawable defaultImage) {
        if (bitmap != null) {
            setViewImage(view, bitmap);
        } else if (defaultImage != null) {
            setViewImage(view, defaultImage);
        }
    }

    /**
     * 移除一个缓存
     *
     * @param url
     *            哪条url的缓存
     */
    public void removeCache(String url) {
        FLBitmapConfig.mCache.remove(url);
    }

    /**
     * 清空缓存
     */
    public void cleanCache() {
        FLBitmapConfig.mCache.clear();
    }

    /**
     * 获取缓存数据
     *
     * @param url
     *            哪条url的缓存
     * @return
     */
    public byte[] getCache(String url) {
        FLCache cache = FLBitmapConfig.mCache;
        cache.initialize();
        FLCache.Entry entry = cache.get(url);
        if (entry != null) {
            return entry.data;
        } else {
            return new byte[0];
        }
    }

    /**
     * 获取内存缓存
     *
     * @param url
     * @return
     */
    public Bitmap getMemoryCache(String url) {
        return FLBitmapConfig.mMemoryCache.getBitmap(url);
    }

    /**
     * 取消一个加载请求
     *
     * @param url
     */
    public void cancle(String url) {
        displayer.cancle(url);
    }

    /**
     * 保存一张图片到本地，并关闭通知图库刷新
     *
     * @param cxt
     * @param url
     * @param path
     */
    public void saveImage(Context cxt, String url, String path){
        saveImage(cxt, url, path, false,true,null);
    }
    /**
     * 保存一张图片到本地，并关闭通知图库刷新
     *
     * @param cxt
     * @param url
     * @param path
     * @param onMainThread 是否运行在主线程
     */
    public void saveImage(Context cxt, String url, String path, boolean onMainThread) {
        saveImage(cxt, url, path, false,onMainThread,null);
    }
    /**
     * 保存一张图片到本地
     *
     * @param url
     * @param path
     * @param cb
     * @param isReFresh 刷新图库
     * @param onMainThread 是否运行在主线程
     * FileDownloader must be invoked from the main thread.
     */
    public void saveImage(final Context cxt, String url, final String path,final boolean isReFresh ,boolean onMainThread,
                          FLHttpCallBack cb) {
        if (cb == null) {
            cb = new FLHttpCallBack() {
                @Override
                public void onSuccess(byte[] t) {
                    super.onSuccess(t);
                    // 刷新图库
                    if(isReFresh){
                        refresh(cxt, path);
                    }
                }
            };
        }
        byte[] data = getCache(url);
        if (data.length == 0) {
            new FLHttp().download(path, url, cb,onMainThread);
        } else {
            File file = new File(path);
            cb.onPreStart();
            File folder = file.getParentFile();
            if (folder != null) {
                folder.mkdirs();
            }
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e1) {
                    cb.onFailure(-1, e1.getMessage()+" 无法创建文件");
                    return;
                }
            }
            OutputStream os = null;
            try {
                os = new FileOutputStream(file);
                os.write(data);
                cb.onSuccess(path);
                cb.onSuccess(data);
                if(isReFresh){
                    refresh(cxt, path);
                }
            } catch (IOException e) {
                cb.onFailure(-1, e.getMessage() +" 无法写入数据");
            } finally {
                FLFileUtils.closeIO(os);
                cb.onFinish();
            }
        }
    }

    /********************* private method *********************/
    /**
     * 刷新图库
     *
     * @param cxt
     * @param path
     */
    private void refresh(Context cxt, String path) {
        String name = "";
        name = path.substring(path.lastIndexOf('/'));
        try {
            MediaStore.Images.Media.insertImage(cxt.getContentResolver(), path,
                    name, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        cxt.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
                .parse("file://" + path)));
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void setViewImage(View view, Bitmap background) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageBitmap(background);
        } else {
            if (FLSystemUtils.getSDKVersion() >= 16) {
                view.setBackground(new BitmapDrawable(view.getResources(),
                        background));
            } else {
                view.setBackgroundDrawable(new BitmapDrawable(view
                        .getResources(), background));
            }
        }
    }

    private void setViewImage(View view, int background) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(background);
        } else {
            view.setBackgroundResource(background);
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void setViewImage(View view, Drawable background) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageDrawable(background);
        } else {
            if (FLSystemUtils.getSDKVersion() >= 16) {
                view.setBackground(background);
            } else {
                view.setBackgroundDrawable(background);
            }
        }
    }

    private void showLogIfOpen(String msg) {
        Debug.Log(getClass().getSimpleName(), msg);
    }

    /**
     * 检测一个View是否已经有任务了，如果是，则取消之前的任务
     *
     * @param view
     */
    private void checkViewExist(View view) {
        for (View v : doLoadingViews) {
            if (v.equals(view)) {
                String url = (String) v.getTag();
                if (!FLMatcher.isEmpty(url)) {
                    cancle(url);
                    break;
                }
            }
        }
        doLoadingViews.add(view);
    }
}

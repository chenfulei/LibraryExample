package com.library.bitmap;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.library.FLHttp;
import com.library.http.FLHttpCallBack;
import com.library.http.FLHttpConfig;
import com.library.http.FLHttpException;
import com.library.http.FLRequest;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * 图片显示器
 *
 * Created by chen_fulei on 2015/8/26.
 */
public class FLImageDisplayer {
    private final FLHttp mKJHttp; // 使用FLHttp的线程池执行队列去加载图片

    private final ImageCache mMemoryCache; // 内存缓存器
    // 为了防止网速很快的时候速度过快而造成先显示加载中图片，然后瞬间显示网络图片的闪烁问题
    private final long mResponseDelayMs;

    private Runnable mRunnable;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // 正在请求的事件
    private final HashMap<String, ImageRequestEven> mRequestsMap = new HashMap<String, ImageRequestEven>();
    // 已经请求完成，待处理的事件
    private final HashMap<String, ImageRequestEven> mResponsesMap = new HashMap<String, ImageRequestEven>();

    /**
     * 创建一个图片显示器
     *
     * @param bitmapConfig
     */
    public FLImageDisplayer(FLBitmapConfig bitmapConfig) {
        FLHttpConfig config = new FLHttpConfig();
        config.mCache = FLBitmapConfig.mCache;
        // 靠，在这里踩了个坑。 最初写的是Integer.MAX_VALUE,
        // 结果把这个值*60000转成毫秒long以后溢出了 这次我给个死的值行不行。1000天，能不能算永久了
        // 其实还有一种解决办法是直接在缓存读取的时候，看到是bitmap缓存不管是否失效都返回，
        // 但是这种不利于自定义扩展，就不用了，有兴趣的可以看CacheDispatcher的109行
        // 记录于2015.4.30
        config.cacheTime = bitmapConfig.cacheTime;
        mKJHttp = new FLHttp(config);
        mMemoryCache = FLBitmapConfig.mMemoryCache;
        mResponseDelayMs = bitmapConfig.delayTime;
    }

    /**
     * 判断指定图片是否已经被缓存
     *
     * @param requestUrl
     *            图片地址
     * @return
     */
    public boolean isCached(String requestUrl) {
        throwIfNotOnMainThread();
        return mMemoryCache.getBitmap(requestUrl) != null;
    }

    /**
     * 加载一张图片
     *
     * @param requestUrl
     *            图片地址
     * @param maxWidth
     *            图片最大宽度(如果网络图片大于这个宽度则缩放至这个大小)
     * @param maxHeight
     *            图片最大高度
     * @param callback
     * @return
     */
    public ImageBale get(String requestUrl, int maxWidth, int maxHeight,
                         FLBitmapCallBack callback) {
        throwIfNotOnMainThread();
        callback.onPreLoad();

        Bitmap cachedBitmap = mMemoryCache.getBitmap(requestUrl);
        if (cachedBitmap != null) {
            ImageBale container = new ImageBale(cachedBitmap, requestUrl, null);
            callback.onSuccess(cachedBitmap);
            callback.onFinish();
            return container;
        } else {
            // 开始加载网络图片的标志
            callback.onSuccess(null);
        }

        ImageBale imageBale = new ImageBale(null, requestUrl, callback);
        ImageRequestEven request = mRequestsMap.get(requestUrl);
        if (request != null) {
            request.addImageBale(imageBale);
            return imageBale;
        }

        FLRequest<Bitmap> newRequest = makeImageRequest(requestUrl, maxWidth, maxHeight);
        newRequest.setConfig(mKJHttp.getConfig());
        mKJHttp.doRequest(newRequest);
        mRequestsMap.put(requestUrl,new ImageRequestEven(newRequest, imageBale));
        return imageBale;
    }

    /**
     * 创建一个网络请求
     */
    protected FLRequest<Bitmap> makeImageRequest(final String requestUrl,
                                                 int maxWidth, int maxHeight) {
        return new FLImageRequest(requestUrl, maxWidth, maxHeight,
                new FLHttpCallBack() {
                    @Override
                    public void onSuccess(Bitmap t) {
                        super.onSuccess(t);
                        onGetImageSuccess(requestUrl, t);
                    }

                    @Override
                    public void onFailure(int errorNo, String strMsg) {
                        super.onFailure(errorNo, strMsg);
                        onGetImageError(requestUrl, new FLHttpException(strMsg));
                    }
                });
    }

    /**
     * 从网络获取bitmap成功时调用
     *
     * @param url
     *            缓存key
     * @param bitmap
     *            获取到的bitmap
     */
    protected void onGetImageSuccess(String url, Bitmap bitmap) {
        mMemoryCache.putBitmap(url, bitmap);
        // 从正在请求的列表中移除这个已完成的请求
        ImageRequestEven request = mRequestsMap.remove(url);

        if (request != null) {
            request.mBitmapRespond = bitmap;
            batchResponse(url, request);
        }
    }

    /**
     * 从网络获取bitmap失败时调用
     *
     * @param url
     *            缓存key
     * @param error
     *            失败原因
     */
    protected void onGetImageError(String url, FLHttpException error) {
        // 从正在请求的列表中移除这个已完成的请求
        ImageRequestEven request = mRequestsMap.remove(url);
        if (request != null) {
            request.setError(error);
            batchResponse(url, request);
        }
    }

    /**************************************************************************/

    /**
     * 对一个图片的封装，包含了这张图片所需要携带的信息
     *
     * @author honey_chen
     *
     */
    public class ImageBale {
        private Bitmap mBitmap;
        private final String mRequestUrl;
        private final FLBitmapCallBack mCallback;

        public ImageBale(Bitmap bitmap, String requestUrl,
                         FLBitmapCallBack callback) {
            mBitmap = bitmap;
            mRequestUrl = requestUrl;
            mCallback = callback;
        }

        public void cancelRequest() {
            if (mCallback == null) {
                return;
            }

            ImageRequestEven request = mRequestsMap.get(mRequestUrl);
            if (request != null) {
                boolean canceled = request.removeBale(this);
                if (canceled) {
                    mRequestsMap.remove(mRequestUrl);
                }
            } else {
                request = mResponsesMap.get(mRequestUrl);
                if (request != null) {
                    request.removeBale(this);
                    if (request.mImageBales.size() == 0) {
                        mResponsesMap.remove(mRequestUrl);
                    }
                }
            }
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public String getRequestUrl() {
            return mRequestUrl;
        }
    }

    /**
     * 图片从网络请求并获取到相应的事件
     *
     * @author honey_chen
     *
     */
    private class ImageRequestEven {
        private final FLRequest<?> mRequest;
        private Bitmap mBitmapRespond;
        private FLHttpException mError;
        private final LinkedList<ImageBale> mImageBales = new LinkedList<ImageBale>();

        public ImageRequestEven(FLRequest<?> request, ImageBale imageBale) {
            mRequest = request;
            mImageBales.add(imageBale);
        }

        public void setError(FLHttpException error) {
            mError = error;
        }

        public FLHttpException getError() {
            return mError;
        }

        public void addImageBale(ImageBale imageBale) {
            mImageBales.add(imageBale);
        }

        public boolean removeBale(ImageBale imageBale) {
            mImageBales.remove(imageBale);
            if (mImageBales.size() == 0) {
                mRequest.cancel();
                return true;
            }
            return false;
        }
    }

    /**************************************************************************/

    /**
     * 分发这次ImageRequest事件的结果
     */
    private void batchResponse(String url, final ImageRequestEven request) {
        mResponsesMap.put(url, request);
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    for (ImageRequestEven even : mResponsesMap.values()) {
                        for (ImageBale imageBale : even.mImageBales) {
                            if (imageBale.mCallback == null) {
                                continue;
                            }
                            if (even.getError() == null) {
                                imageBale.mBitmap = even.mBitmapRespond;
                                imageBale.mCallback
                                        .onSuccess(imageBale.mBitmap);
                            } else {
                                imageBale.mCallback.onFailure(even.getError());
                            }
                            imageBale.mCallback.onFinish();
                        }
                    }
                    mResponsesMap.clear();
                    mRunnable = null;
                }

            };
            mHandler.postDelayed(mRunnable, mResponseDelayMs);
        }
    }

    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException(
                    "ImageLoader must be invoked from the main thread.");
        }
    }

    /**
     * 取消一个加载请求
     *
     * @param url
     */
    public void cancle(String url) {
        mKJHttp.cancel(url);
    }

    /**
     * 内存缓存接口定义
     *
     * @author kymjs
     */
    public interface ImageCache {
        public Bitmap getBitmap(String url);

        public void putBitmap(String url, Bitmap bitmap);
    }
}

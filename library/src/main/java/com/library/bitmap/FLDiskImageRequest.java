package com.library.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.library.http.FLAsyncTask;
import com.library.utils.FLFileUtils;

import java.io.FileInputStream;

/**
 * 本地图片加载
 *
 * Created by chen_fulei on 2015/8/26.
 */
public class FLDiskImageRequest {
    private final Handler handle = new Handler(Looper.getMainLooper());
    private String mPath;

    class DiskImageRequestTask extends FLAsyncTask<Void, Void, byte[]> {
        private final int mMaxWidth;
        private final int mMaxHeight;
        private final FLBitmapCallBack mCallback;

        public DiskImageRequestTask(int maxWidth, int maxHeight,
                                    FLBitmapCallBack callback) {
            mMaxHeight = maxHeight;
            mMaxWidth = maxWidth;
            mCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCallback != null) {
                mCallback.onPreLoad();
            }
        }

        @Override
        protected byte[] doInBackground(Void... params) {
            return loadFromFile(mPath, mMaxWidth, mMaxHeight, mCallback);
        }

        @Override
        protected void onPostExecute(byte[] result) {
            super.onPostExecute(result);
            if (mCallback != null) {
                mCallback.onFinish();
            }
        }
    }

    public void load(String path, int maxWidth, int maxHeight,
                     FLBitmapCallBack callback) {
        mPath = path;
        DiskImageRequestTask task = new DiskImageRequestTask(maxWidth,
                maxHeight, callback);
        task.execute();
    }

    /**
     * 从本地载入一张图片
     * @param path
     * @param maxWidth
     * @param maxHeight
     * @param callback
     * @return
     */
    private byte[] loadFromFile(String path, int maxWidth, int maxHeight,
                                FLBitmapCallBack callback) {
        byte[] data = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            if (fis != null) {
                data = FLFileUtils.input2byte(fis);
            }
            handleBitmap(data, maxWidth, maxHeight, callback);
        } catch (Exception e) {
            doFailure(callback, e);
        } finally {
            FLFileUtils.closeIO(fis);
        }
        return data;
    }

    private Bitmap handleBitmap(byte[] data, int maxWidth, int maxHeight,
                                FLBitmapCallBack callback) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        Bitmap bitmap = null;
        if (maxWidth == 0 && maxHeight == 0) {
            bitmap = BitmapFactory
                    .decodeByteArray(data, 0, data.length, option);
        } else {
            option.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, option);
            int actualWidth = option.outWidth;
            int actualHeight = option.outHeight;

            // 计算出图片应该显示的宽高
            int desiredWidth = getResizedDimension(maxWidth, maxHeight,
                    actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(maxHeight, maxWidth,
                    actualHeight, actualWidth);

            option.inJustDecodeBounds = false;
            option.inSampleSize = findBestSampleSize(actualWidth, actualHeight,
                    desiredWidth, desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0,
                    data.length, option);

            // 做缩放
            if (tempBitmap != null
                    && (tempBitmap.getWidth() > desiredWidth || tempBitmap
                    .getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth,
                        desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }

        if (bitmap == null) {
            doFailure(callback, new RuntimeException("bitmap create error"));
        } else {
            FLBitmapConfig.mMemoryCache.putBitmap(mPath, bitmap);
            doSuccess(callback, bitmap);
        }
        callback.onFinish();
        return bitmap;
    }

    /**
     * 框架会自动将大于设定值的bitmap转换成设定值，所以需要这个方法来判断应该显示默认大小或者是设定值大小。<br>
     * 本方法会根据maxPrimary与actualPrimary比较来判断，如果无法判断则会根据辅助值判断，辅助值一般是主要值对应的。
     * 比如宽为主值则高为辅值
     *
     * @param maxPrimary
     *            需要判断的值，用作主要判断
     * @param maxSecondary
     *            需要判断的值，用作辅助判断
     * @param actualPrimary
     *            真实宽度
     * @param actualSecondary
     *            真实高度
     * @return 获取图片需要显示的大小
     */
    private int getResizedDimension(int maxPrimary, int maxSecondary,
                                    int actualPrimary, int actualSecondary) {
        if (maxPrimary == 0 && maxSecondary == 0) {
            return actualPrimary;
        }
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        if (resized * ratio > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    /**
     * 关于本方法的判断，可以查看我的博客：http://blog.kymjs.com/kjframeforandroid/2014/12/05/02/
     *
     * @param actualWidth
     * @param actualHeight
     * @param desiredWidth
     * @param desiredHeight
     * @return
     */
    static int findBestSampleSize(int actualWidth, int actualHeight,
                                  int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }
        return (int) n;
    }

    private void doSuccess(final FLBitmapCallBack callback, final Bitmap bitmap) {
        if (callback != null) {
            handle.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(bitmap);
                }
            });
        }
    }

    private void doFailure(final FLBitmapCallBack callback, final Exception e) {
        if (callback != null) {
            handle.post(new Runnable() {
                @Override
                public void run() {
                    callback.onFailure(e);
                }
            });
        }
    }
}

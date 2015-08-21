package com.library.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * 图片工具
 *
 * Created by chen_fulei on 2015/8/21.
 */
public class FLImageUtils {





    /**
     * 根据最大的来缩放(等比例变大或者变小)
     * @param bitmap
     * @param ow
     * @param oh
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int ow, int oh){
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            // 等比缩放的设置
            // 计算实际应该得到的大小，选择比例最大的那个参数进行缩放控制参数
            int a = width / ow;
            int b = height / oh;
            if(a == 0 && b !=0){
                if (width % ow != 0) {
                    ++a;
                }
                if (height % oh != 0) {
                    ++b;
                }
                if(width == height){
                    ow = oh / a;
                    oh = oh / a;
                }else{
                    ow = ow / b;
                    oh = oh / a;
                }
            }else if( a != 0 && b==0){
                if (width % ow != 0) {
                    ++a;
                }
                if (height % oh != 0) {
                    ++b;
                }
                if(width == height){
                    ow = oh / b;
                    oh = oh / b;
                }else{
                    ow = ow / b;
                    oh = oh / a;
                }
            }else if(a ==0 && b==0){
                if (width % ow != 0) {
                    ++a;
                }
                if (height % oh != 0) {
                    ++b;
                }
                if(ow > oh){
                    ow = oh / a;
                    oh = oh / b;
                }else{
                    ow = ow / a;
                    oh = ow;
                }
            }else{
                if (width % ow != 0) {
                    ++a;
                }
                if (height % oh != 0) {
                    ++b;
                }
                if (a > b) {
                    ow = width / a;
                    oh = height / a;
                } else {
                    ow = width / b;
                    oh = height / b;
                }
            }
            Matrix matrix = new Matrix();
            float scaleWidht = ((float) ow / width);
            float scaleHeight = ((float) oh / height);
            matrix.postScale(scaleWidht, scaleHeight);
            Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                    matrix, true);
            if(bitmap != null){ // 释放旧bitmap
                bitmap.recycle();
                bitmap = null;
                System.gc();
            }

            return newbmp;
        }catch (Exception e){
            e.printStackTrace();
        }

        return bitmap;
    }

}

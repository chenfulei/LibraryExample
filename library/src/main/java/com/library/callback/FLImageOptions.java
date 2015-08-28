package com.library.callback;

import android.graphics.Bitmap;

import com.library.constants.FLConstants;

/**
 * 图片参数
 * Created by chen_fulei on 2015/8/3.
 */
public class FLImageOptions {

    public boolean memCache = true;
    public boolean fileCache = true;
    public Bitmap preset;
    public int policy;

    public int targetWidth;
    public int fallback;
    public int animation;
    public float ratio;
    public int round;
    public float anchor = FLConstants.ANCHOR_DYNAMIC;
}

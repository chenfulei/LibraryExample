package com.library.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 自定义一些提示
 * Created by chen_fulei on 2015/7/27.
 */
public class FLHintUtil {

    /**
     * 提示
     *
     * @param mContext
     * @param message  提示内容
     */
    public static void toast(Context mContext, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

}

package com.library.callback;

import android.widget.ImageView;

import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Created by chen_fulei on 2015/8/1.
 */
public class BitmapAjaxCallback {

    private static HashMap<String, WeakHashMap<ImageView, BitmapAjaxCallback>> queueMap = new HashMap<String, WeakHashMap<ImageView, BitmapAjaxCallback>>();

    /**
     * 清除所有线程活动
     */
    protected static void clearTasks(){
        queueMap.clear();
    }
}

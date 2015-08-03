package com.library.callback;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.library.constants.FLConstants;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 图片管理核心(下载显示图片 包括缓存)
 *
 * Created by chen_fulei on 2015/8/1.
 */
public class BitmapAjaxCallback extends AbstractAjaxCallback<Bitmap , BitmapAjaxCallback>{

    private static HashMap<String, WeakHashMap<ImageView, BitmapAjaxCallback>> queueMap = new HashMap<String, WeakHashMap<ImageView, BitmapAjaxCallback>>();

    private static int SMALL_MAX = 20;
    private static int BIG_MAX = 20;
    private static int SMALL_PIXELS = 50 * 50;//最小分辨率
    private static int BIG_PIXELS = 400 * 400; //最大分辨率
    private static int BIG_TPIXELS = 1000000;

    private static boolean DELAY_WRITE = false;

    private static Map<String, Bitmap> smallCache; //存储小图
    private static Map<String, Bitmap> bigCache; //存储大图
    private static Map<String, Bitmap> invalidCache;

    private WeakReference<ImageView> v; // 软引用图片
    private int targetWidth;
    private int fallback;
    private File imageFile;
    private Bitmap bm;
    private int animation;
    private Bitmap preset;
    private float ratio;
    private int round;
    private boolean targetDim = true;
    private float anchor = FLConstants.ANCHOR_DYNAMIC;
    private boolean invalid;
    private boolean rotate;
    private boolean isBackground = false;

    /**
     * 初始化
     *
     *  Instantiates a new bitmap ajax callback.
     */
    public BitmapAjaxCallback(){
        type(Bitmap.class).memCache(true).fileCache(true).url("");//初始化一个bitmap 并设定有缓存
    }

    

    /**
     * 清除所有线程活动
     */
    protected static void clearTasks(){
        queueMap.clear();
    }
}

package com.library.constants;

/**
 * 自定义常量
 * <p/>
 * Created by chen_fulei on 2015/7/17.
 */
public class FLConstants {

    public static boolean isAnim = true; // 是否开启activity跳转动画
    public static String isAnimDirection = "right"; // right left



    /********************callback 请求中需要的常量 start***************************/
    public static final int CACHE_DEFAULT = 0; // 缓存指针
    public static final int CACHE_PERSISTENT = 1;// 缓存指针
    public static final String ACTIVE_ACCOUNT = "aq.account";
    public static final String POST_ENTITY = "%entity";
    //callback 里面的tag常量
    public static final int TAG_URL = 0x40FF0001; // url标记
    public static final int TAG_SCROLL_LISTENER = 0x40FF0002;
    public static final int TAG_LAYOUT = 0x40FF0003;
    public static final int TAG_NUM = 0x40FF0004;
    public static final int TAG_1 = 0x40FF0005;
    public static final int TAG_2 = 0x40FF0006;
    public static final int TAG_3 = 0x40FF0007;
    public static final int TAG_4 = 0x40FF0008;
    public static final int TAG_5 = 0x40FF0009;

    //网络请求几种状态
    public static final int METHOD_GET = 0;
    public static final int METHOD_POST = 1;
    public static final int METHOD_DELETE = 2;
    public static final int METHOD_PUT = 3;
    public static final int METHOD_DETECT = 4;

    //图片缓存
    public static final float RATIO_PRESERVE = Float.MAX_VALUE;
    public static final float ANCHOR_DYNAMIC = Float.MAX_VALUE;

    /********************callback 请求中需要的常量 end***************************/
}

package com.library.utils;

/**
 * 自定义一些方法
 * <p/>
 * Created by chen_fulei on 2015/7/17.
 */
public class FLSystemUtils {


    /**
     * 比较两个时间的是否在间隔时间内
     *
     * @param one
     * @param two
     * @param space 间隔时间
     * @return 在间隔时间内返回false 否则返回true
     */
    public static boolean compareTimes(long one, long two, long space) {
        return Math.abs(one - two) > space;
    }

    /**
     * 两次点击之间的时间
     *
     * @param lastClickTime
     * @param space         间隔时间
     * @return 在间隔时间内返回false 否则返回true
     */
    public static boolean isFastDoubleClick(long lastClickTime, long space) {
        return compareTimes(System.currentTimeMillis(), lastClickTime, space);
    }

}

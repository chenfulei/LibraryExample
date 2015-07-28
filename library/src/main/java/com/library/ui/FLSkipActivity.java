package com.library.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 管理activity 跳转
 * <p/>
 * Created by chen_fulei on 2015/7/17.
 */
public interface FLSkipActivity {

    /**
     * activit 跳转 （finish 当前activity）
     *
     * @param activity 当前activity
     * @param cls      下一个activity
     */
    void skipActivity(Activity activity, Class<?> cls);

    /**
     * activity 跳转 (finish 当前activity)
     *
     * @param activity 当前activity
     * @param intent   传参数
     */
    void skipActivity(Activity activity, Intent intent);

    /**
     * activity 跳转 (finish 当前activity)
     *
     * @param activity 当前activity
     * @param cls      下一个activity
     * @param bundle   传参数
     */
    void skipActivity(Activity activity, Class<?> cls, Bundle bundle);

    /**
     * activity 跳转 （没有finish 当前）
     *
     * @param activity 当前activity
     * @param cls      next activity
     */
    void showActivity(Activity activity, Class<?> cls);

    /**
     * activity 跳转 （没有finish 当前）
     *
     * @param activity 当前activity
     * @param cls      next activity
     * @param intent   params
     */
    void showActivity(Activity activity, Intent intent);

    /**
     * activity 跳转 （没有finish 当前）
     *
     * @param activity 当前activity
     * @param bundle   params
     */
    void showActivity(Activity activity, Class<?> cls, Bundle bundle);

    /**
     * 是否启动动画切换
     *
     * @param isAnim params
     */
    void setIsAnim(boolean isAnim);
}

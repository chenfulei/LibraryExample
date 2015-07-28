package com.library.ui;

import android.app.Activity;

import java.util.Stack;

/**
 * 管理所有activity
 * <p/>
 * Created by chen_fulei on 2015/7/17.
 */
public class FLActivityManager {

    private static FLActivityManager manager = null;

    //管理activity的堆栈
    private Stack<FLBaseAct> flStack = new Stack<FLBaseAct>();

    public FLActivityManager() {

    }

    /**
     * 单例实例化
     *
     * @return
     */
    public static FLActivityManager getInstance() {
        if (manager == null) {
            synchronized (FLActivityManager.class) {
                if (manager == null) {
                    manager = new FLActivityManager();
                }
            }
        }

        return manager;
    }

    /**
     * 获得当前堆栈里面activity 个数
     *
     * @return
     */
    public int getCount() {

        return flStack.size();
    }

    /**
     * 添加activity到堆栈里
     */
    public void add(FLBaseAct act) {
        if (flStack == null) {
            flStack = new Stack<>();
        }

        flStack.add(act);
    }

    /**
     * 暂停堆栈最顶部的activity
     *
     * @return
     */
    public Activity stop() {
        if (flStack == null) {
            throw new NullPointerException("Activity Stack is null,Activity must extend HCACtivity");
        }

        if (flStack.isEmpty()) {
            return null;
        }

        return (Activity) flStack.lastElement();
    }

    /**
     * 结束当前的activity (在堆栈最顶部)
     */
    public void finish() {
        FLBaseAct act = flStack.lastElement();
        finish((Activity) act);
    }

    /**
     * 指定结束的activity
     *
     * @param activity
     */
    public void finish(Activity activity) {
        if (activity != null) {
            flStack.remove(activity);

            activity = null;
        }
    }

    /**
     * 指定结束activity
     *
     * @param cls
     */
    public void finish(Class<?> cls) {
        for (FLBaseAct act : flStack) {
            if (act.getClass().equals(cls)) {
                finish((Activity) act);
            }
        }
    }

    /**
     * 结束所有activity
     */
    public void finishAll() {
        for (int i = 0; i < flStack.size(); i++) {
            if (null != flStack.get(i)) {
                ((Activity) flStack.get(i)).finish();
            }
        }

        flStack.clear();
    }

    /**
     * 结束整个app
     */
    public void appExit() {
        try {
            finishAll();
            Runtime.getRuntime().exit(0);
        } catch (Exception e) {
            Runtime.getRuntime().exit(-1);
        }
    }

}

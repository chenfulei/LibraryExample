package com.library.ui;

import android.view.View;

/**
 * 规定activity初始化的接口(方便用堆栈来管理activity)
 * <p/>
 * Created by chen_fulei on 2015/7/17.
 */
public interface FLBaseAct {

    /**
     * 设置layout
     */
    void setRootView();

    /**
     * 初始化控件
     */
    void initWidget();

    /**
     * 初始化数据
     */
    void initData();

    /**
     * 在线程中初始化数据 (这个有点问题)
     */
//    void initDataFromThread();

    /**
     * 监听控件点击 (OnClick())
     *
     * @param v
     */
    void widgetClick(View v);

}

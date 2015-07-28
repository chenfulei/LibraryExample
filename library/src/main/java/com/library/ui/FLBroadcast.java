package com.library.ui;

/**
 * A在activity界面提供注册和注销广播的接口
 * <p/>
 * Created by chen_fulei on 2015/7/16.
 */
public interface FLBroadcast {

    /**
     * 注册广播
     */
    void registerBroadcast();

    /**
     * 注销广播
     */
    void unRegisterBroadcast();

}

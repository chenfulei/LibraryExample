package com.library.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.library.utils.FLSystemUtils;

/**
 * Fragment （v4）
 * Created by chen_fulei on 2015/7/20.
 */
public abstract class FLFrameFragment extends Fragment implements View.OnClickListener, FLBroadcast, FLSkipActivity {

    public static long lastClickTime = 0; // 记录控件点击的时间
    public static long space = 500; // 控件点击之间间隔时间 300毫秒
    private View view;

    //初始化View
    protected abstract View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflaterView(inflater, container, savedInstanceState);
        FLAnnotate.init(this);

        //初始化数据
        initData();
        //初始化控件
        initWidget(view);

        //注册广播
        registerBroadcast();

        return view;
    }

    @Override
    public View getView() {
        return view;
    }

    /**
     * 初始化数据
     */
    protected void initData() {
    }

    /**
     * 初始化界面，当嵌套在外部HCActivity时，初始化外部的控件不可使用@HCBindView进行绑定控件
     *
     * @param parentView
     */
    protected void initWidget(View parentView) {
    }

    /**
     * 控件点击
     *
     * @param v
     */
    protected void widgetClick(View v) {
    }

    /**
     * 所有View点击实现地方
     */
    @Override
    public void onClick(View v) {
        if (FLSystemUtils.isFastDoubleClick(lastClickTime, space)) {
            lastClickTime = System.currentTimeMillis();
            widgetClick(v);
        }
    }

    @Override
    public void registerBroadcast() {
    }

    @Override
    public void unRegisterBroadcast() {
    }

    @Override
    public void onDestroy() {
        //注销广播
        unRegisterBroadcast();
        super.onDestroy();
    }
}

package com.library.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

/**
 *  上下拉刷新 （嵌入listView）
 *
 * Created by chen_fulei on 2015/8/22.
 */
public class FLRefreshLayout extends SwipeRefreshLayout{

    public FLRefreshLayout(Context context){
        super(context);

    }

    public FLRefreshLayout (Context context, AttributeSet attrs){
        super(context, attrs);

    }

    /**
     * 判断是否正在刷新
     * @return
     */
    @Override
    public boolean isRefreshing() {
        return super.isRefreshing();
    }

    /**
     * 改变刷新状态
     * @param refreshing
     */
    @Override
    public void setRefreshing(boolean refreshing) {
        super.setRefreshing(refreshing);
    }

    /**
     * 设置下拉出现进度条颜色
     * @param colors
     */
    @Override
    public void setColorSchemeColors(int... colors) {
        super.setColorSchemeColors(colors);
    }

    /**
     *  监听下拉刷新
     * @param listener
     */
    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        super.setOnRefreshListener(listener);
    }
}

package com.library.widget.refresh;

import android.view.View;

/**
 * 下拉刷新的功能接口
 *
 * Created by chen_fulei on 2015/8/22.
 */
public interface FLPullToRefresh<T extends View> {

    /**
     *  设置当前是否可以下拉刷新
     * @param pullRefreshEnabled
     */
    public void setPullRefreshEnabled(boolean pullRefreshEnabled);

    /**
     *  设置当前是否可以上拉加载更多
     * @param pullLoadEnabled
     */
    public void setPullLoadEnabled(boolean pullLoadEnabled);

    /**
     *  设置当前是否滚动到底部自动加载更多
     * @param scrollLoadEnabled
     */
    public void setScrollLoadEnabled(boolean scrollLoadEnabled);

    /**
     * 判断当前是否可以下拉刷新
     * @return
     */
    public boolean isPullRefreshEnabled();

    /**
     * 判断当前是否可以上拉加载更多
     * @return
     */
    public boolean isPullLoadEnabled();

    /**
     * 判断当前是否可以滚动到底部自动加载更多
     * @return
     */
    public boolean isScrollLoadEnabled();

    /**
     *  设置刷新监听器
     * @param refreshListener
     */
    public void setOnHCRefreshListener(FLPullToRefreshBase.OnFLRefreshListener<T> refreshListener);

    /**
     * 停止下来刷新
     */
    public void onPullDownRefreshComplete();

    /**
     * 停止上拉加载更多
     */
    public void onPullUpRefreshComplete();

    /**
     * 获取可刷新对象
     * @return
     */
    public T getRefreshableView();

    /**
     * 获得头部Header布局对象
     * @return
     */
    public FLLoadingLayout getHeaderLoadingLayout();

    /**
     * 获得底部Footer布局对象
     * @return
     */
    public FLLoadingLayout getFooterLoadingLayout();

    /**
     * 设置最后更新的时间文本
     * @param label
     */
    public void setLastUpdatedLabel(CharSequence label);

}

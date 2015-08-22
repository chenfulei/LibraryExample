package com.library.widget.refresh;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.library.widget.refresh.FLLoading;

/**
 *  自定义 下拉&上拉 Header & Footer
 *
 * Created by chen_fulei on 2015/8/22.
 */
public abstract class FLLoadingLayout extends FrameLayout implements FLLoading{

    /** 容器布局 */
    private View mContainer;
    /** 当前的状态 */
    private FLLoadState mCurState = FLLoadState.NONE;
    /** 前一个状态 */
    private FLLoadState mPreState = FLLoadState.NONE;

    /************构造方法******************/
    public FLLoadingLayout(Context context){
        super(context);
        init(context, null);
    }

    public FLLoadingLayout(Context context , AttributeSet attrs){
        super(context, attrs);
        init(context , attrs);
    }

    public FLLoadingLayout(Context context, AttributeSet attrs, int defStyle){
        super(context , attrs , defStyle);
        init(context, attrs);
    }
    /************构造方法******************/

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        mContainer = createLoadingView(context, attrs);
        if (null == mContainer) {
            throw new NullPointerException("Loading view can not be null.");
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        addView(mContainer, params);
    }

    /**
     * 显示或隐藏这个布局
     *
     * @param show flag
     */
    public void show(boolean show) {
        // If is showing, do nothing.
        if (show == (View.VISIBLE == getVisibility())) {
            return;
        }

        ViewGroup.LayoutParams params = mContainer.getLayoutParams();
        if (null != params) {
            if (show) {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            } else {
                params.height = 0;
            }
            setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * 设置最后更新的时间文本
     *
     * @param label 文本
     */
    public void setLastUpdatedLabel(CharSequence label) {
    }

    /**
     * 设置加载中的图片
     *
     * @param drawable 图片
     */
    public void setLoadingDrawable(Drawable drawable) {
    }

    /**
     * 设置拉动的文本，典型的是“下拉可以刷新”
     *
     * @param pullLabel 拉动的文本
     */
    public void setPullLabel(CharSequence pullLabel) {
    }

    /**
     * 设置正在刷新的文本，典型的是“正在刷新”
     *
     * @param refreshingLabel 刷新文本
     */
    public void setRefreshingLabel(CharSequence refreshingLabel) {
    }

    /**
     * 设置释放的文本，典型的是“松开可以刷新”
     *
     * @param releaseLabel 释放文本
     */
    public void setReleaseLabel(CharSequence releaseLabel) {
    }

    @Override
    public void onPull(float scale) {
    }

    @Override
    public void setState(FLLoadState state) {
        if (mCurState != state) {
            mPreState = mCurState;
            mCurState = state;
            onStateChanged(state, mPreState);
        }
    }

    @Override
    public FLLoadState getState() {
        return mCurState;
    }

    /**
     * 得到当前Layout的内容大小，它将作为一个刷新的临界点
     * @return 高度
     */
    @Override
    public abstract int getContentSize();


    /**
     * 当状态改变时调用
     *
     * @param curState 当前状态
     * @param oldState 老的状态
     */
    protected void onStateChanged(FLLoadState curState, FLLoadState oldState) {
        switch (curState) {
            case RESET:
                onReset();
                break;

            case RELEASE_TO_REFRESH:
                onReleaseToRefresh();
                break;

            case PULL_TO_REFRESH:
                onPullToRefresh();
                break;

            case REFRESHING:
                onRefreshing();
                break;

            case NO_MORE_DATA:
                onNoMoreData();
                break;

            default:
                break;
        }
    }

    /**
     * 当状态设置为{@link com.library.widget.refresh.FLLoading.FLLoadState#RESET}时调用
     */
    protected void onReset() {
    }

    /**
     * 当状态设置为{@link com.library.widget.refresh.FLLoading.FLLoadState#PULL_TO_REFRESH}时调用
     */
    protected void onPullToRefresh() {
    }

    /**
     * 当状态设置为{@link com.library.widget.refresh.FLLoading.FLLoadState#RELEASE_TO_REFRESH}时调用
     */
    protected void onReleaseToRefresh() {
    }

    /**
     * 当状态设置为{@link com.library.widget.refresh.FLLoading.FLLoadState#REFRESHING}时调用
     */
    protected void onRefreshing() {
    }

    /**
     * 当状态设置为{@link com.library.widget.refresh.FLLoading.FLLoadState#NO_MORE_DATA}时调用
     */
    protected void onNoMoreData() {
    }

    /**
     * 创建Loadding 的view
     * @param context
     * @param attrs
     * @return
     */
    protected abstract View createLoadingView(Context context, AttributeSet attrs);
}

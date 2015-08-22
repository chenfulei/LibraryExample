package com.library.widget.refresh;

/**
 *  上下拉时样式 layout
 *
 * Created by chen_fulei on 2015/8/22.
 */
public interface FLLoading {
    /**
     * 当前的状态
     */
    public enum FLLoadState {

        /**
         * 初始状态
         */
        NONE,

        /**
         * 刷新
         */
        RESET,

        /**
         * 当界面正在刷新，没有得到释放
         */
        PULL_TO_REFRESH,

        /**
         * When the UI is being pulled by the user, and <strong>has</strong>
         * been pulled far enough so that it will refresh when released.
         */
        RELEASE_TO_REFRESH,

        /**
         * When the UI is currently refreshing, caused by a pull gesture.
         */
        REFRESHING,

        /**
         * When the UI is currently refreshing, caused by a pull gesture.
         */
        LOADING,

        /**
         * No more data
         */
        NO_MORE_DATA,
    }

    /**
     * 设置当前状态，派生类应该根据这个状态的变化来改变View的变化
     *
     * @param state 状态
     */
    public void setState(FLLoadState state);

    /**
     * 得到当前的状态
     *
     * @return 状态
     */
    public FLLoadState getState();

    /**
     * 得到当前Layout的内容大小，它将作为一个刷新的临界点
     *
     * @return 高度
     */
    public int getContentSize();

    /**
     * 在拉动时调用
     *
     * @param scale 拉动的比例
     */
    public void onPull(float scale);

}

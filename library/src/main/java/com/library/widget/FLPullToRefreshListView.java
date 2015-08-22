package com.library.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ListView;

import com.library.widget.refresh.FLLoading;
import com.library.widget.refresh.FLLoadingLayout;
import com.library.widget.refresh.FLPullToRefreshBase;
import com.library.widget.refresh.FooterLoadingLayout;

/**
 * ListView下拉刷新，上加载更多和滑到底部自动加载
 *
 * Created by chen_fulei on 2015/8/22.
 */
public class FLPullToRefreshListView extends FLPullToRefreshBase<ListView> implements AbsListView.OnScrollListener{

    /** ListView */
    private ListView mListView;
    /** 用于滑到底部自动加载的Footer */
    private FLLoadingLayout mLoadMoreFooterLayout;
    /** 滚动的监听器 */
    private AbsListView.OnScrollListener mScrollListener;

    public FLPullToRefreshListView(Context context){
        super(context);
    }

    public FLPullToRefreshListView(Context context , AttributeSet attrs){
        super(context, attrs);
    }

    public FLPullToRefreshListView(Context context , AttributeSet attrs , int defStyle){
        super(context , attrs , defStyle);
        setPullLoadEnabled(false);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (null != mScrollListener) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
                    totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isScrollLoadEnabled() && hasMoreData()) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                    || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                if (isReadyForPullUp()) {
                    startLoading();
                }
            }
        }

        if (null != mScrollListener) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * 创建View,这里创建的自然是ListView
     * @param context context
     * @param attrs 属性
     * @return
     */
    @Override
    protected ListView createRefreshableView(Context context, AttributeSet attrs) {
        ListView listView = new ListView(context);
        mListView = listView;
        /** 设置滚动监听 */
        listView.setOnScrollListener(this);
        return listView;
    }

    /**
     * 设置是否有更多数据的标志
     *
     * @param hasMoreData
     *            true表示还有更多的数据，false表示没有更多数据了
     */
    public void setHasMoreData(boolean hasMoreData) {
        if (!hasMoreData) {
            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.setState(FLLoading.FLLoadState.NO_MORE_DATA);
            }

            FLLoadingLayout footerLoadingLayout = getFooterLoadingLayout();
            if (null != footerLoadingLayout) {
                footerLoadingLayout.setState(FLLoading.FLLoadState.NO_MORE_DATA);
            }
        }
    }

    /**
     * 设置滑动的监听器
     *
     * @param l
     *            监听器
     */
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mScrollListener = l;
    }

    @Override
    protected void startLoading() {
        super.startLoading();
        if (null != mLoadMoreFooterLayout) {
            mLoadMoreFooterLayout.setState(FLLoading.FLLoadState.REFRESHING);
        }
    }

    @Override
    public void onPullUpRefreshComplete() {
        super.onPullUpRefreshComplete();
        if (null != mLoadMoreFooterLayout) {
            mLoadMoreFooterLayout.setState(FLLoading.FLLoadState.RESET);
        }
    }

    @Override
    public void setScrollLoadEnabled(boolean scrollLoadEnabled) {
        super.setScrollLoadEnabled(scrollLoadEnabled);
        if (scrollLoadEnabled) {
            // 设置Footer
            if (null == mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout = new FooterLoadingLayout(getContext());
            }

            if (null == mLoadMoreFooterLayout.getParent()) {
                mListView.addFooterView(mLoadMoreFooterLayout, null, false);
            }
            mLoadMoreFooterLayout.show(true);
        } else {
            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.show(false);
            }
        }
    }

    @Override
    public FLLoadingLayout getFooterLoadingLayout() {
        // TODO Auto-generated method stub
        if (isScrollLoadEnabled()) {
            return mLoadMoreFooterLayout;
        }
        return super.getFooterLoadingLayout();

    }

    @Override
    protected boolean isReadyForPullDown() {
        return isFirstItemVisible();
    }

    @Override
    protected boolean isReadyForPullUp() {
        return isLastItemVisible();
    }

    /**
     * 是否还有更多数据
     * @return true表示还有更多数据
     */
    private boolean hasMoreData() {
        if ((null != mLoadMoreFooterLayout) && (mLoadMoreFooterLayout.getState() == FLLoading.FLLoadState.NO_MORE_DATA)) {
            return false;
        }

        return true;
    }

    /**
     * 判断第一个child是否完全显示出来
     *
     * @return true完全显示出来，否则false
     */
    private boolean isFirstItemVisible() {
        final Adapter adapter = mListView.getAdapter();

        if (null == adapter || adapter.isEmpty()) {
            return true;
        }

        int mostTop = (mListView.getChildCount() > 0) ? mListView.getChildAt(0)
                .getTop() : 0;
        if (mostTop >= 0) {
            return true;
        }

        return false;
    }

    /**
     * 判断最后一个child是否完全显示出来
     *
     * @return true完全显示出来，否则false
     */
    private boolean isLastItemVisible() {
        final Adapter adapter = mListView.getAdapter();

        if (null == adapter || adapter.isEmpty()) {
            return true;
        }

        final int lastItemPosition = adapter.getCount() - 1;
        final int lastVisiblePosition = mListView.getLastVisiblePosition();

        /**
         * This check should really just be: lastVisiblePosition ==
         * lastItemPosition, but ListView internally uses a FooterView which
         * messes the positions up. For me we'll just subtract one to account
         * for it and rely on the inner condition which checks getBottom().
         */
        if (lastVisiblePosition >= lastItemPosition - 1) {
            final int childIndex = lastVisiblePosition
                    - mListView.getFirstVisiblePosition();
            final int childCount = mListView.getChildCount();
            final int index = Math.min(childIndex, childCount - 1);
            final View lastVisibleChild = mListView.getChildAt(index);
            if (lastVisibleChild != null) {
                return lastVisibleChild.getBottom() <= mListView.getBottom();
            }
        }

        return false;
    }
}

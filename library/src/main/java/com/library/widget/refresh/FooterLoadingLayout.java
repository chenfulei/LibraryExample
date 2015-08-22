package com.library.widget.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.library.R;
import com.library.utils.Debug;

/**
 * 下拉刷新的布局
 *
 * Created by chen_fulei on 2015/8/22.
 */
public class FooterLoadingLayout extends FLLoadingLayout{

    /** 进度条 */
    private ProgressBar mProgressBar;
    /** 显示的文本 */
    private TextView mHintView;
    private View container;

    public FooterLoadingLayout(Context context){
        super(context);
        init(context);
    }

    public FooterLoadingLayout(Context context ,AttributeSet attrs){
        super(context , attrs);
        init(context);
    }

    /**
     *  初始化
     * @param context
     */
    private void init(Context context) {
        mProgressBar = (ProgressBar) container.findViewById(R.id.pull_to_load_footer_progressbar);
        mHintView = (TextView) container.findViewById(R.id.pull_to_load_footer_hint_textview);

        setState(FLLoadState.RESET);
    }

    @Override
    protected View createLoadingView(Context context, AttributeSet attrs) {
        container = LayoutInflater.from(context).inflate(
                R.layout.pull_to_load_footer, null);
        Debug.Log("createLoadingView");
        return container;
    }

    @Override
    public void setLastUpdatedLabel(CharSequence label) {
    }

    @Override
    public int getContentSize() {
        View view = findViewById(R.id.pull_to_load_footer_content);
        if (null != view) {
            return view.getHeight();
        }

        return (int) (getResources().getDisplayMetrics().density * 40);
    }

    @Override
    protected void onStateChanged(FLLoadState curState, FLLoadState oldState) {
        mProgressBar.setVisibility(View.GONE);
        mHintView.setVisibility(View.INVISIBLE);

        super.onStateChanged(curState, oldState);
    }

    @Override
    protected void onReset() {
        mHintView.setText(R.string.pull_to_refresh_header_hint_loading);
    }

    @Override
    protected void onPullToRefresh() {
        mHintView.setVisibility(View.VISIBLE);
        mHintView.setText(R.string.pushmsg_center_pull_up_text);
    }

    @Override
    protected void onReleaseToRefresh() {
        mHintView.setVisibility(View.VISIBLE);
        mHintView.setText(R.string.pushmsg_center_pull_release_refresh);
    }

    @Override
    protected void onRefreshing() {
        mProgressBar.setVisibility(View.VISIBLE);
        mHintView.setVisibility(View.VISIBLE);
        mHintView.setText(R.string.pull_to_refresh_header_hint_loading);
    }

    @Override
    protected void onNoMoreData() {
        mHintView.setVisibility(View.VISIBLE);
        mHintView.setText(R.string.pushmsg_center_no_more_msg);
    }
}

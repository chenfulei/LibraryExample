package com.library.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.library.FLAjaxMain;
import com.library.R;
import com.library.constants.FLConstants;
import com.library.utils.Debug;

/**
 * Fragment 的入口
 * <p/>
 * Created by chen_fulei on 2015/7/17.
 */
public abstract class FLFragment extends FLFrameFragment {

    public Activity activity;
    protected FLAjaxMain ajaxMain; // 异步网络请求等入口

    protected Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Debug.Log(getClass().getName(), "onCreate");
        ajaxMain = new FLAjaxMain(getActivity());

        activity = getActivity();
        super.onCreate(savedInstanceState);

        initHandler();
    }

    /**
     * 初始化Handler
     */
    private void initHandler() {
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                onHeadler(msg);
                super.handleMessage(msg);
            }
        };
    }

    /**
     * 处理消息结果
     *
     * @param msg
     */
    protected abstract void onHeadler(Message msg);

    @Override
    public void onResume() {
        Debug.Log(getClass().getName(), "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Debug.Log(getClass().getName(), "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Debug.Log(getClass().getName(), "onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Debug.Log(getClass().getName(), "onDestroyView");
        super.onDestroyView();
        ajaxMain.dismiss();
    }

    @Override
    public void skipActivity(Activity activity, Class<?> cls) {
        showActivity(activity, cls);
        activity.finish();
    }

    @Override
    public void skipActivity(Activity activity, Intent intent) {
        showActivity(activity, intent);
        activity.finish();
    }

    @Override
    public void skipActivity(Activity activity, Class<?> cls, Bundle bundle) {
        showActivity(activity, cls, bundle);
        activity.finish();
    }

    @Override
    public void showActivity(Activity activity, Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        activity.startActivity(intent);

        if (FLConstants.isAnim) {
            if ("left".equals(FLConstants.isAnimDirection)) {
                activity.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_from_right);

            } else if ("right".equals(FLConstants.isAnimDirection)) {
                activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_from_left);
            }
        }
    }

    @Override
    public void showActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
        if (FLConstants.isAnim) {
            if ("left".equals(FLConstants.isAnimDirection)) {
                activity.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_from_right);

            } else if ("right".equals(FLConstants.isAnimDirection)) {
                activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_from_left);
            }
        }
    }

    @Override
    public void showActivity(Activity activity, Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(activity, cls);
        intent.putExtras(bundle);
        if (FLConstants.isAnim) {
            if ("left".equals(FLConstants.isAnimDirection)) {
                activity.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_from_right);
            } else if ("right".equals(FLConstants.isAnimDirection)) {
                activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_from_left);
            }
        }
    }

    @Override
    public void setIsAnim(boolean isAnim) {
        FLConstants.isAnim = isAnim;
    }
}

package com.library.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.library.R;
import com.library.constants.FLConstants;
import com.library.utils.Debug;
import com.library.utils.FLSystemUtils;

/**
 * activity继承 ，重写activity入口
 * <p/>
 * Created by chen_fulei on 2015/7/16.
 */
public abstract class FLFragmentActivity extends FragmentActivity implements View.OnClickListener, FLBroadcast, FLBaseAct, FLSkipActivity {

    public static long lastClickTime = 0; // 记录控件点击的时间
    public static long space = 500; // 控件点击之间间隔时间 300毫秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRootView(); // 初始化layout

        FLAnnotate.init(this); // 初始化注解绑定

        initData();
        initWidget();

        //注册广播
        registerBroadcast();
    }

    /**
     * 初始化数据
     */
    @Override
    public void initData() {
    }

    /**
     * 初始化控件
     */
    @Override
    public void initWidget() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //注销广播
        unRegisterBroadcast();
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

    /**
     * 注册广播
     */
    @Override
    public void registerBroadcast() {
    }

    /**
     * 注销广播
     */
    @Override
    public void unRegisterBroadcast() {
    }

    /**
     * fragment的时候切换 (每次切换到要刷新)
     *
     * @param resView
     * @param flFragment
     */
    public void changeFragment(int resView, FLFragment flFragment) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (FLConstants.isAnim) {
                transaction.setCustomAnimations(R.anim.slide_in_fade, R.anim.slide_out_fade);
            }

            transaction.replace(resView, flFragment).commitAllowingStateLoss();
        } catch (Exception e) {
            Debug.Log(e);
        }
    }

    public void AddFragment(int resView, FLFragment flFragment) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (FLConstants.isAnim) {
                transaction.setCustomAnimations(R.anim.slide_in_fade, R.anim.slide_out_fade);
            }

            transaction.add(resView, flFragment).commit();
        } catch (Exception e) {
            Debug.Log(e);
        }
    }

    /**
     * fragment相互切换，但不刷新
     *
     * @param resView
     * @param currentFragment
     * @param nextFragment
     */
    public void changeFragment(int resView, FLFragment currentFragment, FLFragment nextFragment) {
        try {
            if (nextFragment == null && currentFragment != null) {
                changeFragment(resView, currentFragment);
                return;
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (currentFragment != nextFragment) {
                //判断是否被added
                if (!nextFragment.isAdded()) {
                    // 隐藏当前的fragment，add下一个到Activity中
                    if (FLConstants.isAnim) {
                        transaction.setCustomAnimations(R.anim.slide_in_fade, R.anim.slide_out_fade);
                    }

                    transaction.hide(currentFragment).add(resView, nextFragment).commit();
                } else {
                    // 隐藏当前的fragment，显示下一个
                    transaction.hide(currentFragment).show(nextFragment).commit();
                }

                currentFragment = nextFragment;
            }
        } catch (Exception e) {
            Debug.Log(e);
        }
    }

}

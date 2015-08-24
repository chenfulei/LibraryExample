package com.library.widget;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.library.R;
import com.library.utils.FLScreenUtils;
import com.library.widget.wheel.OnWheelChangedListener;
import com.library.widget.wheel.OnWheelScrollListener;
import com.library.widget.wheel.WheelView;
import com.library.widget.wheel.adapter.BaseWheelViewAdapter;

import java.util.List;

/**
 * 选择器
 *
 * Created by chen_fulei on 2015/8/24.
 */
public class FLChooseWindow {

    private Activity context;
    private View view;
    private View parent;
    private PopupWindow popupWindow;
    private WheelView wheelView;
    private ImageView iv;
    private Button cancel,sure;
    private Button titleBtn;
    private List<DataResources> list;
    private String title;
    private WheelCallBack cb;
    private int mNewValue;

    public FLChooseWindow(Activity context, View parent,List<DataResources> list,String title,WheelCallBack cb) {
        this.context = context;
        this.parent = parent;
        this.title = title;
        this.list = list;
        this.cb=cb;
        initView();

    }
    public void setCurrentItem(int index){

        if(null != wheelView && index <= list.size()){
            wheelView.setCurrentItem(index-1);
        }
    }
    private void initView() {
        mNewValue = 0;
        RelativeLayout root = new RelativeLayout(context);
        root.setBackgroundColor(Color.TRANSPARENT);
        // 这个是透明黑色背景
        iv = new ImageView(context);
        iv.setBackgroundColor(Color.parseColor("#b4000000"));
        RelativeLayout.LayoutParams params_iv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        root.addView(iv, params_iv);

        view = LayoutInflater.from(context).inflate(R.layout.window_selectpage, null);
        wheelView = (WheelView) view.findViewById(R.id.wheel_page);
        cancel = (Button) view.findViewById(R.id.btn_cancel);
        sure = (Button) view.findViewById(R.id.btn_sure);
        titleBtn = (Button) view.findViewById(R.id.btn_title);
        titleBtn.setText(title);
        wheelView.setCyclic(false);
        wheelView.setWheelBackgroundColor(Color.WHITE);
        wheelView.setCenterDrawable(R.drawable.ios7_wheel_val);
        wheelView.setTopShadowDrawable(new int[] { Color.TRANSPARENT,
                Color.TRANSPARENT, Color.TRANSPARENT });
        wheelView.setBottomShadowDrawable(new int[] { Color.TRANSPARENT,
                Color.TRANSPARENT, Color.TRANSPARENT });
        wheelView.setVisibleItems(3);

        wheelView.setViewAdapter(new WheelAdapter());
        // 设置弹出位置
        DisplayMetrics dm = FLScreenUtils.getScreenMetrics(context);
        int width = dm.widthPixels;
        RelativeLayout.LayoutParams params_wheel = new RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params_wheel.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        root.addView(view, params_wheel);
        // 这2句一定要设置了才能使底层layout响应返回按钮的事件
        // --------------------------------------------------------------//
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);
        // --------------------------------------------------------------//
        popupWindow = new PopupWindow(root, dm.widthPixels, dm.heightPixels);
        popupWindow.setFocusable(true);
        root.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                // TODO Auto-generated method stub
                if (arg2.getAction() == KeyEvent.ACTION_DOWN
                        && arg1 == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                }
                return false;
            }
        });
        wheelView.addScrollingListener(new OnWheelScrollListener() {

            @Override
            public void onScrollingStarted(WheelView arg0) {
                // TODO Auto-generated method stub
                wheelView.invalidateWheel(false);
            }

            @Override
            public void onScrollingFinished(WheelView arg0) {
                // TODO Auto-generated method stub
                wheelView.invalidateWheel(false);
            }
        });
        wheelView.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView arg0, int oldValue, int newValue) {
                mNewValue = newValue;
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dismiss();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(null !=cb){
                    cb.choose(mNewValue);
                    dismiss();
                }
            }
        });

    }

    public interface WheelCallBack {
        public void choose(int index);
    }
    public interface DataResources{
        // public String setValue(String value);
        // public String setKey(String key);
        public String getValue();
        public String getKey();
    }
    class WheelAdapter extends BaseWheelViewAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public View getEmptyItem(View arg0, ViewGroup arg1) {
            View v = LayoutInflater.from(context).inflate(R.layout.window_item,
                    null);
            TextView tv = (TextView) v.findViewById(R.id.tv_text);
            tv.setText("");
            return v;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            View v = LayoutInflater.from(context).inflate(R.layout.window_item,
                    null);
            TextView tv = (TextView) v.findViewById(R.id.tv_text);
            tv.setText(list.get(arg0).getValue());
            if (arg0 == wheelView.getCurrentItem()) {
                tv.setTextColor(context.getResources().getColor(R.color.btn_pressed));
            } else {
                tv.setTextColor(Color.parseColor("#AAAAAA"));
            }
            return v;
        }

    }

    public void show() {
        try {
            Animation fade = AnimationUtils.loadAnimation(context,
                    R.anim.fade_in);
            Animation slide = AnimationUtils.loadAnimation(context,
                    R.anim.slide_in_from_bottom);
            fade.setFillAfter(true);
            slide.setFillAfter(true);
            iv.startAnimation(fade);
            view.startAnimation(slide);
            popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void dismiss() {
        try {
            final Animation fade = AnimationUtils.loadAnimation(context,
                    R.anim.fade_out);
            Animation slide = AnimationUtils.loadAnimation(context,
                    R.anim.slide_out_from_bottom);
            fade.setFillAfter(true);
            slide.setFillAfter(true);
            slide.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // TODO Auto-generated method stub
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            popupWindow.dismiss();
                        }
                    });

                }
            });
            iv.startAnimation(fade);
            view.startAnimation(slide);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

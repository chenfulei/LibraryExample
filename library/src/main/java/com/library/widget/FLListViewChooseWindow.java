package com.library.widget;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.library.R;
import com.library.utils.FLScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定義的選擇器，區分左中右位置
 *
 * Created by chen_fulei on 2015/8/24.
 */
public class FLListViewChooseWindow {

    private PopupWindow popupWindow;
    private View view,left_view,right_view;
    private View parent;
    private ImageView iv;
    private Activity activity;
    //private Context context;
    private DisplayMetrics dm;
    private boolean cancel = true;
    private List<DataResources> beans = new ArrayList<DataResources>();
    private ListView listView;
    private OnShowingListener onShowListener;
    private MyAdapter adapter;
    private LayoutInflater inflater;
    private Place place;
    public enum Place{
        left,center,right,show
    }
    public FLListViewChooseWindow(Activity activity, View parent, List<DataResources> beans,Place place) {
        this(activity, parent, true, beans,place);
    }

    public FLListViewChooseWindow(Activity activity, View parent, boolean cancel,
                                  List<DataResources> beans,Place place) {
        this.activity = activity;
        //this.context = activity;
        this.parent = parent;
        this.cancel = cancel;
        this.place =  place;
        if (activity == null) {
            return;
        }
        inflater = LayoutInflater.from(activity);
        this.beans = beans;
        dm = FLScreenUtils.getScreenMetrics(activity);
        initWindow();
    }

    @SuppressWarnings("deprecation")
    private void initWindow() {
        if (activity == null) {
            return;
        }


        view = inflater.inflate(R.layout.popwin_choose, null);
        RelativeLayout root = new RelativeLayout(activity);
        root.setBackgroundColor(Color.TRANSPARENT);
        // 这个是透明黑色背景
        iv = new ImageView(activity);
        iv.setBackgroundColor(Color.parseColor("#b4000000"));
        RelativeLayout.LayoutParams params_iv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        root.addView(iv, params_iv);


        listView = (ListView) view.findViewById(R.id.choose_lv);
        left_view =  view.findViewById(R.id.left_view);
        right_view =  view.findViewById(R.id.right_view);

        if(place==Place.left){
            left_view.setVisibility(View.GONE);
            right_view.setVisibility(View.VISIBLE);
        }else if(place==Place.right){
            left_view.setVisibility(View.VISIBLE);
            right_view.setVisibility(View.GONE);
        }else if(place==Place.show){
            right_view.setVisibility(View.VISIBLE);
            left_view.setVisibility(View.VISIBLE);
        }else{
            right_view.setVisibility(View.GONE);
            left_view.setVisibility(View.GONE);
        }
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
        popupWindow = new PopupWindow(root, dm.widthPixels, dm.heightPixels);
        popupWindow.setFocusable(true);
        // 点击外部自动消失
        if (cancel) {
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.setOutsideTouchable(true);
        }

        RelativeLayout.LayoutParams params_wheel = new RelativeLayout.LayoutParams(dm.widthPixels, RelativeLayout.LayoutParams.WRAP_CONTENT);
        root.addView(view,params_wheel);

        // 这2句一定要设置了才能使底层layout响应返回按钮的事件
        // --------------------------------------------------------------//
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);
        // --------------------------------------------------------------//
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                // TODO Auto-generated method stub
                if(null !=onShowListener){
                    onShowListener.isShowing(popupWindow.isShowing());
                }
            }
        });
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

    }

    public void setBeansAndParent(List<DataResources> beans) {
        this.beans = beans;

        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        if (null != listView) {
            listView.setOnItemClickListener(listener);
        }
    }
    public void setOnShowingListener(OnShowingListener onShowListener){
        this.onShowListener = onShowListener;
    }
    public void show() {
        try {
            Animation fade = AnimationUtils.loadAnimation(activity,
                    R.anim.fade_in);
            Animation slide = AnimationUtils.loadAnimation(activity,
                    R.anim.top_in);
            fade.setFillAfter(true);
            slide.setFillAfter(true);
            iv.startAnimation(fade);
            view.startAnimation(slide);
            //popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
            if(place == Place.show){
                popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
            }else{
                popupWindow.showAsDropDown(parent);
            }
            if(null !=onShowListener){
                onShowListener.isShowing(popupWindow.isShowing());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //	public interface OnClickCallBack {
//		public void choose(int index);
//	}
    public interface OnShowingListener{
        public void isShowing(boolean isShow);
    }
    public interface DataResources{
        public String getValue();
        public String getKey();
    }
    public void dismiss() {
        try {
            final Animation fade = AnimationUtils.loadAnimation(activity,
                    R.anim.fade_out);
            Animation slide = AnimationUtils.loadAnimation(activity,
                    R.anim.top_out);
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

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            return beans.size();
        }

        @Override
        public Object getItem(int position) {
            return beans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView t;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.item_listview_choose, null);
                t = (TextView) convertView.findViewById(R.id.content_et);
                convertView.setTag(t);
            } else {
                t = (TextView) convertView.getTag();
            }
            t.setText(beans.get(position).getValue());
            return convertView;
        }
    }

}

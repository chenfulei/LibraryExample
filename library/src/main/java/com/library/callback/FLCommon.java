package com.library.callback;

import android.app.Activity;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Gallery;
import android.widget.ListAdapter;
import android.widget.ProgressBar;

import com.library.FLAjaxMain;
import com.library.constants.FLConstants;
import com.library.utils.Debug;

import java.io.File;
import java.util.Comparator;

/**
 * 一个共享的监听器类
 * A shared listener class to reduce the number of classes.
 *
 * Created by chen_fulei on 2015/7/30.
 */
public class FLCommon implements Comparator<File>, Runnable, View.OnClickListener, View.OnLongClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener, AdapterView.OnItemSelectedListener, TextWatcher{
    private Object handler;
    private String method;
    private Object[] params;
    private boolean fallback;
    private Class<?>[] sig;
    private int methodId;

    public static final int STORE_FILE = 1;
    public static final int CLEAN_CACHE = 2;

    public FLCommon forward(Object handler, String callback, boolean fallback, Class<?>[] sig){

        this.handler = handler;
        this.method = callback;
        this.fallback = fallback;
        this.sig = sig;

        return this;
    }

    /**
     * 反射的参数
     * @param methodId
     * @param params
     * @return
     */
    public FLCommon method(int methodId, Object... params){

        this.methodId = methodId;
        this.params = params;

        return this;
    }

    /**
     * 反射
     * @param args
     * @return
     */
    private Object invoke(Object... args){

        if(method != null){

            Object[] input = args;
            if(params != null){
                input = params;
            }

            Object cbo = handler;
            if(cbo == null){
                cbo = this;
            }

            Object result = FLAjaxUtility.invokeMethod(cbo, method, fallback, sig, input);
            return result;
        }else if(methodId != 0){

            switch(methodId){

                case CLEAN_CACHE:
                    FLAjaxUtility.cleanCache((File) params[0], (Long) params[1], (Long) params[2]);
                    break;
                case STORE_FILE:
                    FLAjaxUtility.store((File) params[0], (byte[]) params[1]);
                    break;

            }


        }

        return null;
    }

    /**
     * 文件比较  (比较文件最后修改的时间)
     * @param lhs
     * @param rhs
     * @return
     */
    @Override
    public int compare(File lhs, File rhs) {
        long m1= lhs.lastModified(); // 最后修改的时间
        long m2 = rhs.lastModified();

        if (m2 > m1){
            return 1;
        }else  if (m2 == m1){
            return 0;
        }else {
            return -1;
        }
    }

    @Override
    public void run() {
        invoke(); // 执行反射方法(Common内部)
    }

    @Override
    public void onClick(View v) {
        invoke(v);//执行被点击的方法
    }

    @Override
    public boolean onLongClick(View v) {
        Object object = invoke(v); //长按点击事件
        if (object instanceof Boolean){
            return (Boolean) object;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        invoke(parent , view , position , id); // 执行item点击
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Boolean result = (Boolean) invoke(parent , view , position , id); // 执行item长按事件
        return result;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        checkScrolledBottom(view, scrollState);
        if (onScrollListener !=null){
            onScrollListener.onScroll(view , firstVisibleItem , visibleItemCount , totalItemCount);
        }
    }

    /**
     * 滑动状态改变处理
     * @param view
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
        checkScrolledBottom(view, scrollState);

        if (view instanceof  ExpandableListView){
            onScrollStateChangedOne((ExpandableListView)view , scrollState);
        }else {
            onScrollStateChangedTwo(view , scrollState);
        }
    }

    /**********监听EditView的内容变化***************/
    @Override
    public void afterTextChanged(Editable s) {
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        invoke(s , start , before , count);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        invoke(parent, view, position, id);

        if(galleryListener != null){
            galleryListener.onItemSelected(parent, view, position, id);
        }

        if(galleryListen){

            Integer selected = (Integer) parent.getTag(FLConstants.TAG_NUM);

            if(selected != position){

                Adapter adapter = parent.getAdapter();
                parent.setTag(FLConstants.TAG_NUM, position);

                int count = parent.getChildCount();
                Debug.Log("redrawing" ,count +"");

                int first = parent.getFirstVisiblePosition();

                for(int i = 0; i < count; i++){
                    View convertView = parent.getChildAt(i);

                    int drawPos = first + i;

                    Integer lastDrawn = (Integer) convertView.getTag(FLConstants.TAG_NUM);

                    if(lastDrawn != null && lastDrawn.intValue() == drawPos){
                        Debug.Log("skip" ,drawPos +"");
                    }else{
                        Debug.Log("redraw" ,drawPos +"");
                        adapter.getView(drawPos, convertView, parent);
                    }
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if(galleryListener != null){
            galleryListener.onNothingSelected(parent);
        }
    }

    private AdapterView.OnItemSelectedListener galleryListener;
    private  boolean galleryListen = false;

    public void listen(Gallery gallery){
        galleryListener = gallery.getOnItemSelectedListener();
        galleryListen = true;

        gallery.setOnItemSelectedListener(this);
    }

    /***********************滑动监听处理*****************************/
    private int scrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;//滑动的状态
    private AbsListView.OnScrollListener onScrollListener; // 滑动监听
    private int lastBottom;

    /**
     * 获取滑动状态
     * @return
     */
    public int getScrollState(){
        return scrollState;
    }

    /**
     * 获取监听器
     * @param listener
     */
    public void forward(AbsListView.OnScrollListener listener){
        this.onScrollListener = listener;
    }

    /**
     * 检查滑动是否到最底部
     * @param view
     * @param scrollState
     */
    private void checkScrolledBottom(AbsListView view , int scrollState ){
        int cc = view.getCount();
        int last = view.getLastVisiblePosition();

        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && cc == last + 1){
            if(last != lastBottom){
                lastBottom = last;
                invoke(view, scrollState);
            }

        }else{
            lastBottom = -1;
        }
    }

    /**
     * 滑动状态改变
     * @param expandableListView
     * @param scrollState
     */
    private void onScrollStateChangedOne(ExpandableListView expandableListView, int scrollState){
        expandableListView.setTag(FLConstants.TAG_NUM , scrollState);

        if (scrollState == SCROLL_STATE_IDLE){
            int first = expandableListView.getFirstVisiblePosition();
            int last = expandableListView.getLastVisiblePosition();

            int count = last - first;
            ExpandableListAdapter expandableListAdapter = expandableListView.getExpandableListAdapter();
            for (int i = 0 ; i < count ; i++){
                long packed = expandableListView.getExpandableListPosition(i + first);

                int group = expandableListView.getPackedPositionGroup(packed);
                int child = expandableListView.getPackedPositionChild(packed);

                if(group >= 0){

                    View convertView = expandableListView.getChildAt(i);
                    Long targetPacked = (Long) convertView.getTag(FLConstants.TAG_NUM);

                    if(targetPacked != null && targetPacked.longValue() == packed){

                        if(child == -1){

                            expandableListAdapter.getGroupView(group, expandableListView.isGroupExpanded(group), convertView, expandableListView);
                        }else{
                            expandableListAdapter.getChildView(group, child, child == expandableListAdapter.getChildrenCount(group) - 1, convertView, expandableListView);
                        }
                        convertView.setTag(FLConstants.TAG_NUM, null);
                    }else{
                        Debug.Log("skip");
                    }
                }
            }
        }
    }

    /**
     * 滑动状态改变
     * @param view
     * @param scrollState
     */
    private void onScrollStateChangedTwo(AbsListView view , int scrollState){
        view.setTag(FLConstants.TAG_NUM, scrollState);

        if(scrollState == SCROLL_STATE_IDLE){

            int first = view.getFirstVisiblePosition();
            int last = view.getLastVisiblePosition();

            int count = last - first;

            ListAdapter la = view.getAdapter();

            for(int i = 0; i <= count; i++){

                long packed = i + first;

                View convertView = view.getChildAt(i);
                Number targetPacked = (Number) convertView.getTag(FLConstants.TAG_NUM);

                if(targetPacked != null){
                    la.getView((int) packed, convertView, view);
                    convertView.setTag(FLConstants.TAG_NUM, null);
                }else{
                   Debug.Log("skip");
                }

            }

        }
    }

    /**
     * 显示进度条
     * @param p
     * @param url
     * @param show
     */
    public static void showProgress(Object p, String url, boolean show){
        if(p != null){

            if(p instanceof View){

                View pv = (View) p;
                ProgressBar pbar = null;
                if(p instanceof ProgressBar){
                    pbar = (ProgressBar) p;
                }
                if(show){
                    pv.setTag(FLConstants.TAG_URL, url);
                    pv.setVisibility(View.VISIBLE);
                    if(pbar != null){
                        pbar.setProgress(0);
                        pbar.setMax(100);
                    }

                }else{
                    Object tag = pv.getTag(FLConstants.TAG_URL);
                    if(tag == null || tag.equals(url)){
                        pv.setTag(FLConstants.TAG_URL, null);

                        if(pbar == null || pbar.isIndeterminate()){
                            pv.setVisibility(View.GONE);
                        }
                    }
                }
            }else if(p instanceof Dialog){

                Dialog pd = (Dialog) p;
                FLAjaxMain ajaxMain = new FLAjaxMain(pd.getContext());

                if(show){
                    ajaxMain.show(pd);
                }else{
                    ajaxMain.dismiss(pd);
                }

            }else if(p instanceof Activity){

                Activity act = (Activity) p;;
                act.setProgressBarIndeterminateVisibility(show);
                act.setProgressBarVisibility(show);

                if(show){
                    act.setProgress(0);
                }
            }
        }
    }
}

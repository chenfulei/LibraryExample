package com.library.callback;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.io.File;
import java.util.Comparator;

/**
 * 一个共享的监听器类
 * A shared listener class to reduce the number of classes.
 *
 * Created by chen_fulei on 2015/7/30.
 */
public class Common implements Comparator<File>, Runnable, View.OnClickListener, View.OnLongClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener, AdapterView.OnItemSelectedListener, TextWatcher{
    private Object handler;
    private String method;
    private Object[] params;
    private boolean fallback;
    private Class<?>[] sig;
    private int methodId;

    protected static final int STORE_FILE = 1;
    protected static final int CLEAN_CACHE = 2;

    public Common forward(Object handler, String callback, boolean fallback, Class<?>[] sig){

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
    public Common method(int methodId, Object... params){

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

            Object result = AjaxUtility.invokeMethod(cbo , method , fallback , sig , input);
            return result;
        }else if(methodId != 0){

            switch(methodId){

                case CLEAN_CACHE:
                    AjaxUtility.cleanCache((File) params[0], (Long) params[1], (Long) params[2]);
                    break;
                case STORE_FILE:
                    AjaxUtility.store((File) params[0], (byte[]) params[1]);
                    break;

            }


        }

        return null;
    }

    @Override
    public int compare(File lhs, File rhs) {
        return 0;
    }

    @Override
    public void run() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

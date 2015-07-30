package com.library.callback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.ProgressBar;

import com.library.constants.FLConstants;

/**
 * 滚动条 / 进度条显示和隐藏
 * Created by chen_fulei on 2015/7/30.
 */
public class Progress implements Runnable {

    private ProgressBar pb;
    private ProgressDialog pd;
    private Activity act;
    private View view;
    private boolean unknown;
    private int bytes;
    private int current;
    private String url;

    //初始化对象
    public Progress(Object p){

        if(p instanceof ProgressBar){
            pb = (ProgressBar) p;
        }else if(p instanceof ProgressDialog){
            pd = (ProgressDialog) p;
        }else if(p instanceof Activity){
            act = (Activity) p;
        }else if(p instanceof View){
            view = (View) p;
        }

    }

    /**
     * 重置
     */
    public void reset(){

        if(pb != null){
            pb.setProgress(0);
            pb.setMax(10000);
        }
        if(pd != null){
            pd.setProgress(0);
            pd.setMax(10000);
        }

        if(act != null){
            act.setProgress(0);
        }

        unknown = false;
        current = 0;
        bytes = 10000;
    }

    /**
     * 设置数据
     * @param bytes
     */
    public void setBytes(int bytes){

        if(bytes <= 0){
            unknown = true;
            bytes = 10000;
        }

        this.bytes = bytes;

        if(pb != null){
            pb.setProgress(0);
            pb.setMax(bytes);
        }
        if(pd != null){
            pd.setProgress(0);
            pd.setMax(bytes);
        }
    }

    /**
     * 过程
     * @param delta
     */
    public void increment(int delta){

        if(pb != null){
            pb.incrementProgressBy(unknown ? 1 : delta);
        }

        if(pd != null){
            pd.incrementProgressBy(unknown ? 1 : delta);
        }

        if(act != null){
            int p;
            if(unknown){
                p = current++;
            }else{
                current+= delta;
                p = (10000 * current) / bytes;
            }
            if(p > 9999){
                p = 9999;
            }
            act.setProgress(p);
        }
    }

    /**
     * 完成
     */
    public void done(){
        if(pb != null){
            pb.setProgress(pb.getMax());
        }
        if(pd != null){
            pd.setProgress(pd.getMax());
        }

        if(act != null){
            act.setProgress(9999);
        }
    }


    @Override
    public void run() {

    }

    public void show(String url){

        reset();

        if(pd != null){
            pd.show();
        }

        if(act != null){
            act.setProgressBarIndeterminateVisibility(true);
            act.setProgressBarVisibility(true);
        }

        if(pb != null){
            pb.setTag(FLConstants.TAG_URL, url);
            pb.setVisibility(View.VISIBLE);
        }

        if(view != null){
            view.setTag(FLConstants.TAG_URL, url);
            view.setVisibility(View.VISIBLE);
        }

    }

    public void hide(String url){

        if(AjaxUtility.isUIThread()){
            dismiss(url);
        }else{
            this.url = url;
            AjaxUtility.post(this);
        }

    }

    private void dismiss(String url){

        if(pd != null){
           pd.dismiss();
        }

        if(act != null){
            act.setProgressBarIndeterminateVisibility(false);
            act.setProgressBarVisibility(false);
        }

        if(pb != null){
            pb.setTag(FLConstants.TAG_URL, url);
            pb.setVisibility(View.VISIBLE);
        }

        View pv = pb;
        if(pv == null){
            pv = view;
        }

        if(pv != null){

            Object tag = pv.getTag(FLConstants.TAG_URL);
            if(tag == null || tag.equals(url)){
                pv.setTag(FLConstants.TAG_URL, null);

                if(pb != null && pb.isIndeterminate()){
                    pv.setVisibility(View.GONE);
                }
            }
        }

    }
}

package com.library;

import android.app.Activity;
import android.content.Context;

import com.library.callback.AbstractAjaxMain;

/**
 * ajax 入口
 *
 * Created by chen_fulei on 2015/8/4.
 */
public class AjaxMain extends AbstractAjaxMain{

    public AjaxMain(Context context){
        super(context);
    }

    public AjaxMain(Activity activity){
        super(activity);
    }

}

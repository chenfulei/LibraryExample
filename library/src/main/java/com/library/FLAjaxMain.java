package com.library;

import android.app.Activity;
import android.content.Context;

import com.library.callback.FLAbstractAjaxMain;

/**
 * ajax 入口
 *
 * Created by chen_fulei on 2015/8/4.
 */
public class FLAjaxMain extends FLAbstractAjaxMain {

    public FLAjaxMain(Context context){
        super(context);
    }

    public FLAjaxMain(Activity activity){
        super(activity);
    }

}

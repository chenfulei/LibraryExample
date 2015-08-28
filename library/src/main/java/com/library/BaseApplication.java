package com.library;

import android.app.Application;

import com.library.callback.FLBitmapAjaxCallback;

/**
 * application
 *
 * Created by chen_fulei on 2015/8/5.
 */
public class BaseApplication extends Application{


    @Override
    public void onLowMemory() {
        FLBitmapAjaxCallback.clearCache();
    }
}

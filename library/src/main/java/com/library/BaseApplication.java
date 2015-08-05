package com.library;

import android.app.Application;

import com.library.callback.BitmapAjaxCallback;

/**
 * application
 *
 * Created by chen_fulei on 2015/8/5.
 */
public class BaseApplication extends Application{


    @Override
    public void onLowMemory() {
        BitmapAjaxCallback.clearCache();
    }
}

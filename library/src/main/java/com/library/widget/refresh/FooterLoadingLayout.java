package com.library.widget.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chen_fulei on 2015/8/22.
 */
public class FooterLoadingLayout extends FLLoadingLayout{

    public FooterLoadingLayout(Context context){
        super(context);

    }

    public FooterLoadingLayout(Context context ,AttributeSet attrs){
        super(context , attrs);
    }



    @Override
    public int getContentSize() {
        return 0;
    }

    @Override
    protected View createLoadingView(Context context, AttributeSet attrs) {
        return null;
    }
}

package com.library.widget.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chen_fulei on 2015/8/22.
 */
public class HeaderLoadingLayout extends FLLoadingLayout {

    public HeaderLoadingLayout(Context context){
        super(context);
    }

    public HeaderLoadingLayout(Context context ,AttributeSet attrs){
        super(context , attrs);
    }

    @Override
    public void setLastUpdatedLabel(CharSequence label) {
        super.setLastUpdatedLabel(label);
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

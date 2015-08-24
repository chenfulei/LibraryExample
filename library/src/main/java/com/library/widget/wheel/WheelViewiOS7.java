package com.library.widget.wheel;

import android.content.Context;
import android.graphics.Color;

import com.library.R;

/**
 * 类似IOS7选择器的模样
 *
 * Created by chen_fulei on 2015/8/4.
 */
public class WheelViewiOS7 extends WheelView {

	public WheelViewiOS7(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setCenterDrawable(R.drawable.ios7_wheel_val);
		setWheelBackgroundColor(Color.WHITE);
		int colors[] = { Color.TRANSPARENT, Color.TRANSPARENT,
				Color.TRANSPARENT };
		setTopShadowDrawable(colors);
		setBottomShadowDrawable(colors);
	}

}

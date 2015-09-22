package com.library.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.library.R;

/**
 * 进度条（滚动的）
 * @author chen_fulei
 *
 */
public class BKProgressBar extends Dialog{

	public BKProgressBar(Context context) {
		super(context , R.style.MyDialog);
	}

	private BKProgressBar(Context context , int theme){
		super(context, theme);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setCanceledOnTouchOutside(false);
		setContentView(R.layout.dialog_progressbar);
	}

}

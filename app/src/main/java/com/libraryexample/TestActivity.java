package com.libraryexample;

import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.library.ui.FLActivity;
import com.library.ui.FLBindView;

/**
 * Created by chen_fulei on 2015/7/28.
 */
public class TestActivity extends FLActivity {

    @FLBindView(id = R.id.btn_back , click = true)
    private Button btn_back;

    @Override
    public void setRootView() {
        setContentView(R.layout.act_test);
    }

    @Override
    public void initWidget() {
        super.initWidget();
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
                finish();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onHeadler(Message msg) {

    }
}

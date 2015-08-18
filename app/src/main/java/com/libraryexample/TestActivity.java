package com.libraryexample;

import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.library.callback.AjaxCallback;
import com.library.callback.AjaxStatus;
import com.library.ui.FLActivity;
import com.library.ui.FLBindView;
import com.library.utils.Debug;

/**
 * Created by chen_fulei on 2015/7/28.
 */
public class TestActivity extends FLActivity {

    @FLBindView(id = R.id.btn_back , click = true)
    private Button btn_back;

    @FLBindView(id = R.id.image_src )
    private ImageView image_src;

    @FLBindView(id = R.id.image_background)
    private ImageView image_background;

    @FLBindView(id = R.id.text)
    private TextView text;

//    private AQuery aQuery;

    @Override
    public void setRootView() {
        setContentView(R.layout.act_test);
//        aQuery = new AQuery(this);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        Debug.LogE("this is test!");
    }

    @Override
    public void initData() {
        super.initData();
//        ajaxMain.findView(image_src).image("http://a.hiphotos.baidu.com/exp/w=500/sign=97c510127a310a55c424def487474387/6f061d950a7b02081b07b50061d9f2d3562cc801.jpg", true, true);

    }

    public void callbackJson(){
//        ajaxMain.ajax("http://bapi.baby-kingdom.com/index.php?mod=misc&op=reportcustom&ver=2.0.0&app=android" ,String.class , new AjaxCallback<String>(){
//
//            public void callback(String url, String object, AjaxStatus status) {
//                Debug.LogE("callback status : "+status.getCode() +"  "+object);
//                text.setText(object);
//            }
//        });

        String im = "http://www.baby-kingdom.com/static/image/smiley/default/bb72.gif";
        ajaxMain.findView(image_src).image(im, false, false, 0, R.mipmap.ic_launcher);
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
//                finish();
                callbackJson();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onHeadler(Message msg) {

    }
}

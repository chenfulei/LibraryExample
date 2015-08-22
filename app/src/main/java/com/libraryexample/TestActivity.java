package com.libraryexample;

import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.library.FLJson;
import com.library.callback.AjaxCallback;
import com.library.callback.AjaxStatus;
import com.library.ui.FLActivity;
import com.library.ui.FLBindView;
import com.library.utils.Debug;
import com.libraryexample.bean.ReportBean;

import org.json.JSONObject;

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

    @Override
    public void setRootView() {
        setContentView(R.layout.act_test);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        Debug.LogE("this is test!");
    }

    @Override
    public void initData() {
        super.initData();
    }

    public void callbackJson(){
        ajaxMain.ajax("http://bapi.baby-kingdom.com/index.php?mod=misc&op=reportcustom&ver=2.0.0&app=android" ,String.class , new AjaxCallback<String>(){

            public void callback(String url, String object, AjaxStatus status) {
                if (TextUtils.isEmpty(object)){
                    text.setText("没有数据");
                    return;
                }
              Debug.LogE(object);

              try {
                  JSONObject jsonObject = new JSONObject(object);
                  ReportBean bean = FLJson.get(jsonObject , ReportBean.class);

                  String str = "";
                  for (ReportBean.Data data : bean.getData()){
                      str += data.getStr() +"\n";
                  }

                  text.setText(str);
              }catch (Exception e){
                  e.printStackTrace();
              }

            }
        });

    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
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

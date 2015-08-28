package com.libraryexample.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.library.ui.FLBindView;
import com.library.ui.FLFragment;
import com.libraryexample.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

/**
 * Created by chen_fulei on 2015/7/21.
 */
public class FragmentOne extends FLFragment {

    @FLBindView(id = R.id.text)
    private TextView textView;

    @FLBindView(id = R.id.btn_test , click = true)
    private Button btn_test;

    @FLBindView(id = R.id.image)
    private ImageView image;

    public static FragmentOne newInstance() {

        return new FragmentOne();
    }

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.act_fragment, container, false);

        return view;
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initWidget(View parentView) {
        super.initWidget(parentView);
        btn_test.setVisibility(View.VISIBLE);
        textView.setText("this is FragmentOne!");
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            textView.setText((String)msg.obj);
        }
    };

    @Override
    protected void widgetClick(View v) {
        switch (v.getId()){
            case R.id.btn_test:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpResponse response = getData("http://bapi.baby-kingdom.com/index.php?mod=misc&op=smiley&ver=2.0.0&app=android");
                            if(response.getStatusLine().getStatusCode() == 200){
                                String str = EntityUtils.toString(response.getEntity());
                                handler.sendMessage(handler.obtainMessage(0 , str));
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }).start();


                break;

            default:
                break;
        }
    }

    private HttpResponse getData(String url ) throws Exception{
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000 );
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT , 45 * 1000);

        HttpGet get = new HttpGet(url);

        return client.execute(get);
    }

}

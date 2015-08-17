package com.libraryexample;

import android.content.Intent;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.library.ui.FLActivity;
import com.library.ui.FLBindView;
import com.library.ui.FLFragment;
import com.libraryexample.fragment.FragmentFour;
import com.libraryexample.fragment.FragmentOne;
import com.libraryexample.fragment.FragmentThree;
import com.libraryexample.fragment.FragmentTwo;


public class MainActivity extends FLActivity {

    @FLBindView(id = R.id.btn_one, click = true)
    private Button btn_one;

    @FLBindView(id = R.id.btn_two, click = true)
    private Button btn_two;

    @FLBindView(id = R.id.btn_three, click = true)
    private Button btn_three;

    @FLBindView(id = R.id.btn_four, click = true)
    private Button btn_four;

    private FLFragment mFragment;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initWidget() {
        super.initWidget();


        mFragment = FragmentOne.newInstance();
        changeFragment(R.id.main_fragment, mFragment);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.btn_one:
                mFragment = FragmentOne.newInstance();
                changeFragment(R.id.main_fragment, mFragment);
                break;

            case R.id.btn_two:
                changeFragment(R.id.main_fragment, FragmentTwo.newInstance());
                break;

            case R.id.btn_three:
//                changeFragment(R.id.main_fragment, FragmentThree.newInstance());
                Intent intent = new Intent(activity , TestActivity.class);
                showActivity(activity , intent);

                break;

            case R.id.btn_four:
                changeFragment(R.id.main_fragment, FragmentFour.newInstance());
                break;

            default:
                break;
        }
    }

    @Override
    protected void onHeadler(Message msg) {

    }
}

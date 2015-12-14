package com.libraryexample.fragment;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.library.ui.FLBindView;
import com.library.ui.FLFragment;
import com.libraryexample.R;

/**
 * Created by chen_fulei on 2015/7/21.
 */
public class FragmentTwo extends FLFragment {

    @FLBindView(id = R.id.text)
    private TextView textView;

    public static FragmentTwo newInstance() {
        return new FragmentTwo();
    }

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.act_fragment, container, false);

        return view;
    }

    @Override
    protected void initData() {
        super.initData();

        textView.setText("this is FragmentTwo!");
    }

    @Override
    protected void onHeadler(Message msg) {

    }
}

package com.example.baselibrary.demo;

import android.os.Bundle;
import android.view.View;

import com.example.baselibrary.R;
import com.example.baselibrary.base.BaseFragment;

/**
 * description:
 * Date: 2017/2/13 11:10
 * User: Administrator
 */
public class ModelFragment extends BaseFragment {

    //##########################  custom variables start ##########################################


    //##########################   custom variables end  ##########################################

    //###################### Override custom metohds start ########################################

    @Override
    protected int getLayoutResource() {
        return R.layout.baselib_activity_model;
    }

    @Override
    public void initPresenter() {
    }

    @Override
    protected void initView(View view) {
    }

    @Override
    public void initData(Bundle savedInstanceState) {
    }

    //######################  Override custom metohds end  ########################################

    //######################   custom metohds start  ##############################################

    //######################    custom metohds end   ##############################################

    //######################  override methods start ##############################################

    //######################   override methods end  ##############################################
}

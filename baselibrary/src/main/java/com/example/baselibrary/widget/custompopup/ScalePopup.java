package com.example.baselibrary.widget.custompopup;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;

import com.example.baselibrary.R;
import com.example.baselibrary.util.MyToastUtil;
import com.example.baselibrary.widget.basepopup.BasePopupWindow;

/**
 * Created by 大灯泡 on 2016/1/15.
 * git地址：https://github.com/razerdp/BasePopup
 * 普通的popup
 */
public class ScalePopup extends BasePopupWindow implements View.OnClickListener {

    private View popupView;

    public ScalePopup(Activity context) {
        super(context);
        bindEvent();
    }

    @Override
    protected Animation initShowAnimation() {
        return getDefaultScaleAnimation();
    }


    @Override
    public View getClickToDismissView() {
        return popupView.findViewById(R.id.click_to_dismiss);
    }

    @Override
    public View onCreatePopupView() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.baselib_popup_normal, null);
        return popupView;
    }

    @Override
    public View initAnimaView() {
        return popupView.findViewById(R.id.popup_anima);
    }

    private void bindEvent() {
        if (popupView != null) {
            popupView.findViewById(R.id.tx_1).setOnClickListener(this);
            popupView.findViewById(R.id.tx_2).setOnClickListener(this);
            popupView.findViewById(R.id.tx_3).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tx_1) {
            MyToastUtil.showShortToast("click tx_1");
        } else if (i == R.id.tx_2) {
            MyToastUtil.showShortToast("click tx_2");
        } else if (i == R.id.tx_3) {
            MyToastUtil.showShortToast("click tx_3");
        } else {
        }
    }
}

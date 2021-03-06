package com.brilliant.module;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;

import com.brilliant.R;
import com.brilliant.mainlibrary.view.MainPageFragment;
import com.example.baselibrary.appupdate.DownLoadUtils;
import com.example.baselibrary.appupdate.DownloadApk;
import com.example.baselibrary.base.BaseFullScreenNormalActivity;
import com.example.baselibrary.constant.UIFactory;
import com.example.baselibrary.util.MyToastUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class HomeActivity extends BaseFullScreenNormalActivity {

    //##########################  custom variables start ##########################################

    private long mBackPressedTime; // 退出应用相关

    private String download_url = "https://172.16.88.230:8383/apk/ced9ab08-3d7d-4cc7-882d-2d81f72a0976.apk";

    private static final String CURR_INDEX = "currIndex";

    private static int currIndex = 0;

    private RadioGroup group;

    private ArrayList<String> fragmentTags;

    private FragmentManager fragmentManager;


    //##########################   custom variables end  ##########################################

    //###################### Override custom metohds start ########################################

    @Override
    public int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    public void initPresenter() {
    }

    @Override
    public void initView() {
        // app的升级下载和安装封装
        //  dealAppUpdate(download_url);

        fragmentManager = getSupportFragmentManager();
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        fragmentTags = new ArrayList<>(Arrays.asList("HomeFragment", "ImFragment", "InterestFragment", "MemberFragment"));
        currIndex = 0;
        if (savedInstanceState != null) {
            currIndex = savedInstanceState.getInt(CURR_INDEX);
            hideSavedFragment();
        }

        group = (RadioGroup) findViewById(R.id.group);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.foot_bar_home:
                        currIndex = 0;
                        break;
                    case R.id.foot_bar_im:
                        currIndex = 1;
                        break;
                    case R.id.foot_bar_interest:
                        currIndex = 2;
                        break;
                    case R.id.main_footbar_user:
                        currIndex = 3;
                        break;
                    default:
                        break;
                }
                showFragment();
            }
        });
        showFragment();
    }

    //######################  Override custom metohds end  ########################################

    //######################      custom metohds start     ########################################

    /**
     * 退出登录
     */
    private void userExit() {
        long curTime = SystemClock.uptimeMillis();
        if ((curTime - mBackPressedTime) < (2 * 1000)) {
            finish();
        } else {
            mBackPressedTime = curTime;
            MyToastUtil.showShortToast(R.string.app_exit_confirm);
        }
    }

    /**
     * 开启升级
     */
    private void downloadUpdateAPK(String download_url) {
        UIFactory.openDownLoadService(mContext, download_url, "中...");
    }

    /**
     *
     */
    private void showFragment() {
        if (currIndex == 3) {
            // 如果需要登录，在这里处理
            // UIHelper.showLogin(MainActivity.this);
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTags.get(currIndex));
        if (fragment == null) {
            fragment = instantFragment(currIndex);
        }
        for (int i = 0; i < fragmentTags.size(); i++) {
            Fragment f = fragmentManager.findFragmentByTag(fragmentTags.get(i));
            if (f != null && f.isAdded()) {
                fragmentTransaction.hide(f);
            }
        }
        if (fragment.isAdded()) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.add(R.id.fragment_container, fragment, fragmentTags.get(currIndex));
        }
        fragmentTransaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    /**
     * @param currIndex
     * @return
     */
    private Fragment instantFragment(int currIndex) {
        switch (currIndex) {
            case 0:
                return new MainPageFragment();
            case 1:
                return new MainPageFragment();
            case 2:
                return new MainPageFragment();
            case 3:
                return new MainPageFragment();
            default:
                return null;
        }
    }

    /**
     *
     */
    private void hideSavedFragment() {
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTags.get(currIndex));
        if (fragment != null) {
            fragmentManager.beginTransaction().hide(fragment).commit();
        }
    }

    /**
     * app的升级下载和安装封装
     */
    private void dealAppUpdate(String download_url) {
        // 方式1：使用DownloadService下载升级和安装，可以自定义通知栏下载样式
        downloadUpdateAPK(download_url);

        // 方式2：使用Google推荐的下载工具类DownLoadManager，样式比较规范和统一,一共分为4步，(推荐)
        //1.注册下载广播接收器
        DownloadApk.registerBroadcast(this);
        //2.删除已存在的Apk
        DownloadApk.removeFile(this);
        //3.如果手机已经启动下载程序，执行downloadApk。否则跳转到设置界面
        if (DownLoadUtils.getInstance(getApplicationContext()).canDownload()) {
            DownloadApk.downloadApk(getApplicationContext(), download_url, "Hobbees更新", "Hobbees");
        } else {
            DownLoadUtils.getInstance(getApplicationContext()).skipToDownloadManager();
        }
    }

    //######################    custom metohds end   ##############################################

    //######################  override methods start ##############################################

    @Override
    public void onBackPressed() {
        userExit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURR_INDEX, currIndex);
    }

    @Override
    protected void onDestroy() {
        //4.反注册广播接收器
        DownloadApk.unregisterBroadcast(this);
        super.onDestroy();
    }

    //######################   override methods end  ##############################################
}

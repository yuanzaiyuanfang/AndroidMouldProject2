package com.example.baselibrary.base;

/**
 * <页面基础公共功能抽象>
 *
 * @author caoyinfei
 * @version [版本号, 2016/6/6]
 * @see [相关类/方法]
 * @since [V1]
 */
public interface PresentationLayerFunc {

    /**
     * 网络请求加载框
     */
    void showProgressDialog();

    /**
     * 网络请求加载框
     */
    void showProgressDialog( String notice);

    /**
     * 自定义网络请求加载框
     */
    void showCustomProgressDialog(boolean cancelAble, String notice, int orientation, int backgroundColor,
                                  int messageColor);

    /**
     * 隐藏网络请求加载框
     */
    void hideProgressDialog();

}

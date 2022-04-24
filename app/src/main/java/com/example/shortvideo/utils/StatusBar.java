package com.example.shortvideo.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class StatusBar {
    public static void fitSystemBar(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
//        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  -- 能够使得页面延伸到状态栏下,但不会隐藏状态栏
//        View.SYSTEM_UI_FLAG_FULLSCREEN -- 能够使得页面的布局延伸到状态栏下,但是会隐藏状态栏
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN   //不隐藏状态栏
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE           //三大金刚键显示隐藏,都能保证布局在状态栏下,也能保证布局是适应屏幕大小 前两一般搭配使用
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);   //轻量状态栏 默认情况下状态栏颜色以app主题色为主 设置此属性,会将状态栏变为白底黑字

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);   // 允许window绘制状态栏背景
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);  //如果不指定状态栏为绿色
    }

    public static void lightStatusBar(Activity activity, boolean light) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        int visibility = decorView.getSystemUiVisibility();
        if (light) {
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(visibility);
    }
}

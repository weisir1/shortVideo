package com.example.libcommon.util;

import android.util.DisplayMetrics;

import com.example.libcommon.global.AppGlobals;

public class PixUtils {
    public static int dp2px(int dpValue) {
        DisplayMetrics metrics = AppGlobals.getsApplication().getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue + 0.5f);
    }

    public static int getScreenWidth() {
        DisplayMetrics metrics = AppGlobals.getsApplication().getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight() {
        DisplayMetrics metrics = AppGlobals.getsApplication().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }
}

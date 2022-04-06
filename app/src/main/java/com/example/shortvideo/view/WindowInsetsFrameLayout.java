package com.example.shortvideo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WindowInsetsFrameLayout extends FrameLayout {
    public WindowInsetsFrameLayout(@NonNull Context context) {
        super(context);
    }

    public WindowInsetsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(View child) {   //因为sofa界面是在发送之后才被创建的,也就是说分发过程中,sofa界面还不算是其子view,所以仍旧调用不到,所以在添加玩以后需要在调用一次
        super.addView(child);
        requestApplyInsets();
    }

//    分发自适应状态栏事件
    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        WindowInsets windowInsets = super.dispatchApplyWindowInsets(insets);
        if (!insets.isConsumed()){  //如果为被消费 ,将事件分发给后续子view ,因为在父布局中如果当前的页面被消费,那么会打断事件传递,导致后续子view无法接受
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                windowInsets = getChildAt(i).dispatchApplyWindowInsets(insets);
            }
        }
        return windowInsets;
    }
}

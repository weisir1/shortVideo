package com.example.shortvideo.ui.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.libcommon.util.PixUtils;
import com.example.shortvideo.R;

// 此处view 指被behavior应用的view
public class ViewAnchorBehavior extends CoordinatorLayout.Behavior<View> {

    private int anchorId;
    private int extraUsed;

    public ViewAnchorBehavior() {
    }

    public ViewAnchorBehavior(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.view_anchor_behavior, 0, 0);
        anchorId = typedArray.getResourceId(R.styleable.view_anchor_behavior_anchorId, 0);
        typedArray.recycle();
        extraUsed = PixUtils.dp2px(48);
    }

    public ViewAnchorBehavior(int anchorId) {
        this.anchorId = anchorId;
        extraUsed = PixUtils.dp2px(48);
    }

    /**
     * @param parent     根布局
     * @param child      ViewAnchorBehavior被应用的那个布局
     * @param dependency 当前view所依赖的view
     * @return
     */
    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return anchorId == dependency.getId();
    }

//    CoordinatorLayout在测量每一个子view时会调用此方法
//    如果返回true 就不会再测量child 会使用咱们给的测量的值 去摆放view的位置

    //    parent布局摆放位置默认是从0,0位置开始的
    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent,
                                  @NonNull View child,
                                  int parentWidthMeasureSpec,
                                  int widthUsed,
                                  int parentHeightMeasureSpec,
                                  int heightUsed) {
        View anchorView = parent.findViewById(this.anchorId);
        if (anchorView == null) {
            return false;
        }
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
//       分析:  加上当前view(也占空间),heightUsed其实就是当前view的getBottom位置 同时加上自己的marginTop
        int topMargin = layoutParams.topMargin;
        int bottom = anchorView.getBottom();

//        对于此界面来说 为了不让bottomView遮盖上方的view,要默认每次把extraUsed高度算上,也就意味着
//        当前界面实际能使用的高度范围是maxHeight - extraUsed(底部互动界面的高度)
        heightUsed = bottom + topMargin + extraUsed;
//        因为只考虑垂直布局的位置,所以水平使用设为0 代表宽度使用为0
        parent.onMeasureChild(child, parentWidthMeasureSpec, 0, parentHeightMeasureSpec, heightUsed);

        return true;
    }

    // 摆放子view位置
//     return true 将不会摆放此view
    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull View child, int layoutDirection) {
        View anchorView = parent.findViewById(anchorId);
        if (anchorView == null) {
            return false;
        }
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        int topMargin = params.topMargin;
        int bottom = anchorView.getBottom();
        parent.onLayoutChild(child, layoutDirection);
        child.offsetTopAndBottom(bottom + topMargin);
        return true;
    }
}

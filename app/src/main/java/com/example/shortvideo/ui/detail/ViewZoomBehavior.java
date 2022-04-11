package com.example.shortvideo.ui.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.example.libcommon.util.PixUtils;
import com.example.shortvideo.R;
import com.example.shortvideo.view.FullScreenPlayerView;

public class ViewZoomBehavior extends CoordinatorLayout.Behavior<FullScreenPlayerView> {

    private int scrollingId;
    private int minHeight;
    private OverScroller overScroller;
    private ViewDragHelper viewDragHelper;
    private View scrollingView;
    private FullScreenPlayerView refChild;
    private int childOriginalHeight;
    private boolean canFullScreen;

    public ViewZoomBehavior() {
    }

    public ViewZoomBehavior(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.view_zoom_behavior, 0, 0);
        scrollingId = typedArray.getResourceId(R.styleable.view_zoom_behavior_scrolling_id, 0);
        minHeight = typedArray.getDimensionPixelOffset(R.styleable.view_zoom_behavior_min_height, PixUtils.dp2px(200));
        typedArray.recycle();

        overScroller = new OverScroller(context);
    }
//    全局保存childview

//   获取scrollingView
//    计算出该view的底部位置,即他的高度 ,方便后面使用其最终高度来做限制
//    同时判断当前否是需要全屏显示
    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, int layoutDirection) {
        if (viewDragHelper == null) {
            viewDragHelper = ViewDragHelper.create(parent, 1.0f, callback);
            this.scrollingView = parent.findViewById(scrollingId);   //获取recyclerView的id 并加载
            this.refChild = child;
            this.childOriginalHeight = child.getMeasuredHeight();
            canFullScreen = childOriginalHeight > child.getMeasuredWidth();
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
//            告诉ViewDragHelper 什么时候可以拦截此view的滑动事件
            if (canFullScreen
                    && refChild.getBottom() >= minHeight
                    && refChild.getBottom() <= childOriginalHeight) {
                return true;
            }
            return false;
        }

        //        告诉viewDragHelper 滑动多少算拖拽
        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return viewDragHelper.getTouchSlop();
        }

        //      告诉ViewDragHelper本次滑动的view最多能够拖拽的距离
        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (refChild == null || dy == 0) {
                return 0;
            }
//          向上滑动且 当播放视图底部位置不能小于最小高度
            if (dy < 0 && refChild.getBottom() < minHeight
//                  向下滑动 且底部位置不能比全屏高度还大
                    || (dy > 0 && refChild.getBottom() > childOriginalHeight)
//                  下拉 且 评论列表(recyclerView) 不为null 且列表要滑到顶部 播放视图才能进行放大
                    || (dy > 0 && (scrollingView != null && scrollingView.canScrollVertically(-1)))) {
                return 0;
            }

            int maxConsumed = 0;
            if (dy > 0) {  //下滑
//                手指滑动的dy值加播放器底部位置 超出满屏的最大高度 可滑动距离为最大高度到当前播放器底部的距离
                if (refChild.getBottom() + dy > childOriginalHeight) {
                    maxConsumed = childOriginalHeight - refChild.getBottom();
                } else {
//                   没有超过 则正常滑动的距离
                    maxConsumed = dy;
                }
            } else {  //上滑
//                因为是上滑，所以dy为负值  当dy与播放器底部位置的和 小于最小高度时 可滑动距离为播放器底部到最小高度处的距离（注意是负值）
                if (refChild.getBottom() + dy < minHeight) {
                    maxConsumed = minHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            }
//           将计算好的可移动的距离设置给播放器的高度 并回调
            ViewGroup.LayoutParams layoutParams = refChild.getLayoutParams();
            layoutParams.height = layoutParams.height + maxConsumed;
            refChild.setLayoutParams(layoutParams);

            if (zoomCallback != null) {
                zoomCallback.onDragZoom(layoutParams.height);
            }
            return maxConsumed;
        }


        //        手指滑动离开屏幕
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (refChild.getBottom() > minHeight && refChild.getBottom() < childOriginalHeight && yvel != 0) {
                FlingRunnable runnable = new FlingRunnable(refChild);
                runnable.fling((int) xvel, (int) yvel);
            }
        }
    };

    //    如果不是全屏滑动 则不需要拦截事件
    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, @NonNull MotionEvent ev) {
        if (!canFullScreen || viewDragHelper == null) {
            return super.onTouchEvent(parent, child, ev);
        }
        viewDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, @NonNull MotionEvent ev) {
        if (!canFullScreen || viewDragHelper == null) {
            return super.onInterceptTouchEvent(parent, child, ev);
        }
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    private ViewZoomCallback zoomCallback;

    public void setViewZoomCallback(ViewZoomCallback zoomCallback) {
        this.zoomCallback = zoomCallback;
    }

    public interface ViewZoomCallback {
        void onDragZoom(int height);
    }


    private class FlingRunnable implements Runnable {
        private View flingView;

        //要滑动的view
        public FlingRunnable(View flingView) {
            this.flingView = flingView;
        }

        private void fling(int xvel, int yel) {
            overScroller.fling(0, flingView.getBottom(), xvel, yel, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            run();
        }


        @Override
        public void run() {
            ViewGroup.LayoutParams layoutParams = flingView.getLayoutParams();
            int height = layoutParams.height;
            if (overScroller.computeScrollOffset() && height > minHeight && height < childOriginalHeight) {
                int newHeight = Math.min(overScroller.getCurrY(), childOriginalHeight);
                if (newHeight != height) {
                    layoutParams.height = newHeight;
                    flingView.setLayoutParams(layoutParams);
                    if (zoomCallback != null) {
                        zoomCallback.onDragZoom(newHeight);
                    }
                }
                ViewCompat.postOnAnimation(flingView, this);
            } else {
                flingView.removeCallbacks(this);
            }
        }
    }
}

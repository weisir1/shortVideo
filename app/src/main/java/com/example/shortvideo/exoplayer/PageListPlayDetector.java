package com.example.shortvideo.exoplayer;

import android.util.Pair;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/*
 * 实现列表自动播放
 * */
public class PageListPlayDetector {
    //  收集所有实现IPlayTarget接口的视频播放器实例对象
    private List<IPlayTarget> mTargets = new ArrayList<>();
    private RecyclerView recyclerView;
    //    当前正在播放的播放器
    private IPlayTarget playingTarget;
    private Pair<Integer, Integer> rvLocation = null;


    public void addTarget(IPlayTarget target) {
        mTargets.add(target);
    }

    public void removeTarget(IPlayTarget target) {
        mTargets.remove(target);
    }

    public PageListPlayDetector(LifecycleOwner owner, RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        owner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    playingTarget = null;
                    mTargets.clear();
                    recyclerView.removeCallbacks(delayAutoPlay);
                    recyclerView.removeOnScrollListener(scrollListener);
                    owner.getLifecycle().removeObserver(this);
                }
            }
        });
        recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);
        recyclerView.addOnScrollListener(scrollListener);
    }

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            postAutoPlay();
        }
    };

    Runnable delayAutoPlay = () -> {
        autoPlay();
    };
    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                autoPlay();
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dx == 0 && dy == 0) {
                //时序问题。当执行了AdapterDataObserver#onItemRangeInserted  可能还没有被布局到RecyclerView上。
                //所以此时 recyclerView.getChildCount()还是等于0的。
                //等childView 被布局到RecyclerView上之后，会执行onScrolled（）方法
                //并且此时 dx,dy都等于0
                postAutoPlay();
            } else {
//                在滑动过程中,如果正在播放的视频item被划出了屏幕,暂停掉它
                if (playingTarget != null && playingTarget.isPlaying() && !isTargetInBounds(playingTarget)) {
                    playingTarget.inActive();
                }
            }
        }
    };

    private void postAutoPlay() {
        recyclerView.post(delayAutoPlay);
    }

    private void autoPlay() {
        if (mTargets.size() <= 0 || recyclerView.getChildCount() <= 0) {
            return;
        }
        if (playingTarget != null && playingTarget.isPlaying() && isTargetInBounds(playingTarget)) {
            return;
        }
        IPlayTarget activeTarget = null;   //正在播放的播放器
        for (IPlayTarget target : mTargets) {
            //            遍历所有的target,如果有视图位置在recyclerview的中间部分,将其设置为播放状态
            boolean inBounds = isTargetInBounds(target);
            if (inBounds) {
                activeTarget = target;
                break;
            }
        }
        if (activeTarget != null) {
            if (playingTarget != null) {
                playingTarget.inActive();
            }
            playingTarget = activeTarget;
            playingTarget.onActive();
        }
    }

    //    检查IPlayTarget播放器所显示的界面至少还有一半大小在屏幕内
    private boolean isTargetInBounds(IPlayTarget target) {
        ViewGroup owner = target.getOwner();
        ensureRecyclerViewLocation();
//        isShown所有祖先视图和当前视图都是visible 返回true
        if (!owner.isShown() || !owner.isAttachedToWindow()) {
            return false;
        }
        int[] location = new int[2];
//        获取此视图在屏幕上的位置坐标
        owner.getLocationOnScreen(location);
        int center = location[1] + owner.getHeight() / 2;
        //承载视频播放画面的ViewGroup它需要至少一半的大小 在RecyclerView上下范围内
        return center >= rvLocation.first && center <= rvLocation.second;
    }

    //    记录recyclerView在屏幕中的中间位置
    private Pair<Integer, Integer> ensureRecyclerViewLocation() {
        if (rvLocation == null) {
            int[] location = new int[2];
            recyclerView.getLocationOnScreen(location);
            int top = location[1];
            int bottom = top + recyclerView.getHeight();

            rvLocation = new Pair<>(top, bottom);
        }
        return rvLocation;
    }

    public void onPause() {
        if (playingTarget != null) {
            playingTarget.inActive();
        }
    }

    public void onResume() {
        if (playingTarget != null) {
            playingTarget.onActive();
        }
    }
}

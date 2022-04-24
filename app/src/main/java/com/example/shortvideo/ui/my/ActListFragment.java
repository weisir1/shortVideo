package com.example.shortvideo.ui.my;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shortvideo.AbsListActivity;
import com.example.shortvideo.exoplayer.PageListPlayDetector;
import com.example.shortvideo.exoplayer.PageListPlayManager;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.ui.AbsListFragment;
import com.example.shortvideo.ui.home.FeedAdapter;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

public class ActListFragment extends AbsListFragment<Feed, ActViewModel> {
    private static final String CATEGORY = "user_behavior_list";
    int behavior;
    private boolean showPause = true;

    public static ActListFragment newInstance(int behavior) {

        Bundle args = new Bundle();
        args.putInt(AbsListActivity.BEHAVIOR_TYPE, behavior);
        ActListFragment fragment = new ActListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterCreateView() {
        behavior = getArguments().getInt(AbsListActivity.BEHAVIOR_TYPE);
        mViewModel.setBehavior(behavior);
    }

    @Override
    public PagedListAdapter getAdapter() {
        return new FeedAdapter(getContext(), CATEGORY) {

            @Override
            public void onViewAttachedToWindow2(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    detector.addTarget(holder.getListPlayerView());
                }
            }
            @Override
            public void onViewDetachedFromWindow2(ViewHolder holder) {
                if (detector != null) {
                    detector.removeTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onStartFeedDetailActivity(Feed item) {
                showPause = false;
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<Feed> currentList = adapter.getCurrentList();
        finishRefresh(currentList != null && currentList.size() > 0);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (showPause) {
            detector.onPause();
        }
    }

    @Override
    public void onResume() {
        showPause = true;
        detector.onPause();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        PageListPlayManager.release(CATEGORY);
        super.onDestroyView();


    }
}

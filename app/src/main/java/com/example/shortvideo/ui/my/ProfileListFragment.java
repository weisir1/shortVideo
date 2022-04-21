package com.example.shortvideo.ui.my;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shortvideo.exoplayer.PageListPlayDetector;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.ui.AbsListFragment;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

public class ProfileListFragment extends AbsListFragment<T, ProfileViewModel> {

    private String tabType;
    private boolean shouldPause = true;

    public static ProfileListFragment newInstance(String tabType) {
        Bundle args = new Bundle();
        ProfileListFragment fragment = new ProfileListFragment();
        args.putString(ProfileActivity.KEY_TAB_TYPE, tabType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterCreateView() {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PageListPlayDetector detector = new PageListPlayDetector(this, recyclerView);
        mViewModel.setProfileType(tabType);
        smartRefreshLayout.setEnableRefresh(false);

    }

    @Override
    public PagedListAdapter getAdapter() {
        tabType = getArguments().getString(ProfileActivity.KEY_TAB_TYPE);

        return new ProfileListAdapter(getContext(),tabType){
            @Override
            public void onViewDetachedFromWindow2(ViewHolder holder) {
                if (holder.isVideoItem()){
                    detector.removeTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewAttachedToWindow2(ViewHolder holder) {
                if (holder.isVideoItem()){
                    detector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onStartFeedDetailActivity(Feed item) {
                shouldPause = false;
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shouldPause){
            detector.onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        detector.onResume();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

    }
}

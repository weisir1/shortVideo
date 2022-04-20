package com.example.shortvideo.ui.my;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shortvideo.model.Feed;
import com.example.shortvideo.ui.AbsListFragment;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

public class ProfileListFragment extends AbsListFragment<Feed,ProfileViewModel> {
    public static ProfileListFragment newInstance(String tabType) {
        Bundle args = new Bundle();
        ProfileListFragment fragment = new ProfileListFragment();
        args.putString(ProfileActivity.KEY_TAB_TYPE,tabType);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    protected void afterCreateView() {

    }

    @Override
    public PagedListAdapter<Feed, RecyclerView.ViewHolder> getAdapter() {
        return null;
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

    }
}

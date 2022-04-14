package com.example.shortvideo.ui.home;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.example.libnavannotation.FragmentDestination;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.ui.AbsListFragment;
import com.example.shortvideo.ui.MutablePageKeyedDataSource;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {

    private String feedType;
    private boolean shouldPause = true;

    public static HomeFragment newInstance(String feedType) {

        Bundle args = new Bundle();
        args.putString("feedType", feedType);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterCreateView() {
        mViewModel.getCacheLiveData().observe(this, new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                adapter.submitList(feeds);
            }
        });
        mViewModel.setFeedType(feedType);
    }

    @Override
    public PagedListAdapter getAdapter() {
        feedType = getArguments() == null ? "" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType) {
            @Override
            public void onViewAttachedToWindow2(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    detector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow2(ViewHolder holder) {
                detector.removeTarget(holder.getListPlayerView());
            }

            @Override
            public void onStartFeedDetailActivity(Feed item) {
//                实现详情页无缝续播, 需要在getAdapter点击item事件发生以后 回调此方法 ,判断点击item为视频 不允许暂停
                boolean isVideo = item.itemType == Feed.TYPE_VIDEO;
                shouldPause = !isVideo;
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
//                这个方法是在我们每提交一次 pageList对象到adapter就会出发一次
//                每调用一次 adapter.submitlist
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        recyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    //上拉刷新
    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

        PagedList<Feed> currentList = adapter.getCurrentList();
        if (currentList == null || currentList.size() <= 0) return;

        Feed feed = currentList.get(adapter.getItemCount() - 1);  //获取最后一个id
        mViewModel.loadAfter(feed.id, new ItemKeyedDataSource.LoadCallback<Feed>() {
            @Override
            public void onResult(@NonNull List data) {
                PagedList.Config config = adapter.getCurrentList().getConfig();
                if (data != null && data.size() > 0) {
//                    因为当pagelist某一次加载时返回emptyList后,之后将不会在自动加载数据,所以在这里创建一个dataSource来替换
//                    内容为原始数据集+手动刷新后响应数据集的集合
                    MutablePageKeyedDataSource muteableSource = new MutablePageKeyedDataSource();
                    muteableSource.data.addAll(adapter.getCurrentList());
                    muteableSource.data.addAll(data);
//                   当datasource与pagelist绑定时,会回调loadInitial()
                    PagedList pagedList = muteableSource.buildNewPagedList(config);
                    submitList(pagedList);
                    return;
                }
            }
        });

    }


    //    下拉刷新
    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        //在下拉刷新没有重绘view,其原因 dataSource对象不是最新的对象,在调用一次invalidate()之后mInvalid设为true 最终到
//          if (mDataSource.isInvalid()) {
//            detach();} 内将mDetached设置为true 将会不断创建DataSource的pagelist进入死循环
//        解决方式: 将将成员变量dataSource改为自定义类,并在createDataSource()中在每次调用创建此类实例
        mViewModel.getDataSource().invalidate();
//        finishRefresh(false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
//            暂停自动播放
            detector.onPause();
        } else {
//            开启自动播放
            detector.onResume();
        }
    }

    @Override
    public void onPause() {
//        跳转详情页时 不能暂停播放(要实现无缝续播),其他类型的界面切换 不考虑
        if (shouldPause) {
            detector.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        shouldPause = true;
//        sofaFragment中tabFragment引用了此fragment 所以只有当前parentFragment和Fragment都可见,才能恢复视频播放
        if (getParentFragment() != null) {
            if (getParentFragment().isVisible() && isVisible()) {
                detector.onResume();
            }
        } else { //在tabFragment中,homefragment放在viewpager中, 重新可见时显示 ??? 不理解
            if (isVisible()) {
                detector.onResume();
            }
        }
    }
}
package com.example.shortvideo.ui.find;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.example.libcommon.extention.AbsPagedListAdapter;
import com.example.libcommon.util.PixUtils;
import com.example.libcommon.view.EmptyView;
import com.example.shortvideo.R;
import com.example.shortvideo.databinding.ActivityTagFeedListBinding;
import com.example.shortvideo.databinding.LayoutTagFeedListHeaderBinding;
import com.example.shortvideo.exoplayer.PageListPlayDetector;
import com.example.shortvideo.exoplayer.PageListPlayManager;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.model.TagList;
import com.example.shortvideo.ui.home.FeedAdapter;
import com.example.shortvideo.utils.StatusBar;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

public class TagFeedListActivity extends AppCompatActivity implements View.OnClickListener, OnLoadMoreListener, OnRefreshListener {
    public static final String KEY_TAG_LIST = "tag_list";
    private ActivityTagFeedListBinding binding;
    private RecyclerView recyclerView;
    public static final String KEY_FEED_TYPE = "tag_feed_list";
    private SmartRefreshLayout refreshLayout;
    private TagList tagList;
    private PageListPlayDetector detector;
    private boolean shouldPause = true;
    private AbsPagedListAdapter adapter;
    private int totalScrollY;
    private TagFeedListViewModel tagFeedListViewModel;
    private EmptyView emptyView;

    public static void startActivity(Context context, TagList tagList) {
        Intent intent = new Intent(context, TagFeedListActivity.class);
        intent.putExtra(KEY_TAG_LIST, tagList);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_feed_list);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tag_feed_list);
        recyclerView = binding.refreshLayout.recyclerView;
        emptyView = binding.refreshLayout.emptyView;
        refreshLayout = binding.refreshLayout.refreshLayout;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = (AbsPagedListAdapter) getAdapter();
        recyclerView.setAdapter(adapter);

        binding.actionBack.setOnClickListener(this);

        tagList = (TagList) getIntent().getSerializableExtra(KEY_TAG_LIST);
        binding.setTagList(tagList);
        binding.setOwner(this);
        detector = new PageListPlayDetector(this, recyclerView);

        tagFeedListViewModel = ViewModelProviders.of(this).get(TagFeedListViewModel.class);
        tagFeedListViewModel.setFeedType(tagList.title);
        tagFeedListViewModel.getLiveData().observe(this, feeds -> {
            submitList(feeds);
        });
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadMoreListener(this);
        addHeaderView();
    }

    private void submitList(PagedList<Feed> feeds) {
        if (feeds.size() > 0) {
            adapter.submitList(feeds);
        }
//        如果没有数据 显示空布局 如果正在显示刷新动画 关闭它
        finishRefresh(feeds.size() > 0);
    }

    private void finishRefresh(boolean hasData) {
        PagedList currentList = adapter.getCurrentList();
        hasData = currentList != null && currentList.size() > 0 || hasData;
        if (hasData) {
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }

        RefreshState state = refreshLayout.getState();
        if (state.isOpening && state.isHeader) {
            refreshLayout.finishRefresh();
        } else if (state.isOpening && state.isFooter) {
            refreshLayout.finishLoadMore();
        }
    }

    private void addHeaderView() {
        LayoutTagFeedListHeaderBinding headerBinding = LayoutTagFeedListHeaderBinding.inflate(LayoutInflater.from(this),
                null, false);
        headerBinding.setTagList(tagList);
        adapter.addHeaderView(headerBinding.getRoot());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalScrollY += dy;
                boolean overHeight = totalScrollY > PixUtils.dp2px(48);
                binding.tagLogo.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                binding.tagTitle.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                binding.topBarFollow.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                binding.actionBack.setImageResource(overHeight ? R.drawable.icon_back_black : R.drawable.icon_back_white);
                binding.topBar.setBackgroundColor(overHeight ? Color.WHITE : Color.TRANSPARENT);
                binding.topLine.setVisibility(overHeight ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    public PagedListAdapter getAdapter() {
        return new FeedAdapter(this, KEY_FEED_TYPE) {
            @Override
            public void onViewAttachedToWindow2(FeedAdapter.ViewHolder holder) {
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

    @Override
    protected void onPause() {
        super.onPause();
        if (shouldPause) {
            detector.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldPause) {
            detector.onResume();
        }
    }

    @Override
    protected void onDestroy() {
//        释放此页面持有的视频播放器
        PageListPlayManager.release(KEY_FEED_TYPE);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
//全权委托给paging框架
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        tagFeedListViewModel.getDataSource().invalidate();
    }
}
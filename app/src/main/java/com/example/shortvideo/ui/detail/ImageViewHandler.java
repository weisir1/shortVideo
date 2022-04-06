package com.example.shortvideo.ui.detail;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shortvideo.R;
import com.example.shortvideo.databinding.ActivityFeedDetailTypeImageBinding;
import com.example.shortvideo.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.example.shortvideo.databinding.LayoutFeedDetailTypeImageHeaderBinding;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.view.SVImageView;

public class ImageViewHandler extends ViewHandler {

    protected ActivityFeedDetailTypeImageBinding binding;
    protected LayoutFeedDetailTypeImageHeaderBinding headerBinding;

    public ImageViewHandler(FragmentActivity activity) {
        super(activity);
        binding = DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_image);
        interactionBinding = binding.interactionLayouts;
        recyclerView = binding.recyclerView;
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        binding.setFeed(feed);
//        将头布局添加到recyclerView中
        headerBinding = LayoutFeedDetailTypeImageHeaderBinding.inflate(LayoutInflater.from(activity), recyclerView, false);
        headerBinding.setFeed(feed);
        SVImageView headerImage = headerBinding.headerImage;
        headerImage.bingData(feed.width, feed.height, feed.width > feed.height ? 0 : 16, feed.cover);
        listAdapter.addHeaderView(headerBinding.getRoot());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                判断headerLayout划出屏幕的top值是否已经小于标题栏的高度(当headerView完全滑出,getTop为-headerView的高度值)
                boolean visible = headerBinding.getRoot().getTop() <= -binding.titleLayout.getMeasuredHeight();
//                如果headerView被滑出屏幕,将headerinfo信息显示在标题栏上,否则gone掉
                binding.authorInfo.getRoot().setVisibility(visible? View.VISIBLE:View.GONE);
                binding.title.setVisibility(visible?View.GONE:View.VISIBLE);
            }
        });
    }
}

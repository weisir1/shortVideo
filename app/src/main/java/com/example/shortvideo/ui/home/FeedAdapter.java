package com.example.shortvideo.ui.home;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libcommon.extention.AbsPagedListAdapter;
import com.example.shortvideo.BR;
import com.example.shortvideo.R;
import com.example.shortvideo.databinding.LayoutFeedTypeImageBinding;
import com.example.shortvideo.databinding.LayoutFeedTypeVideoBinding;
import com.example.shortvideo.exoplayer.LiveDataBus;
import com.example.shortvideo.exoplayer.PageListPlayDetector;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.ui.detail.FeedDetailActivity;
import com.example.shortvideo.view.ListPlayerView;

import jp.wasabeef.glide.transformations.internal.Utils;

public class FeedAdapter extends AbsPagedListAdapter<Feed, FeedAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private Context context;
    private String category;
    private ListPlayerView listPlayerView;

    protected FeedAdapter(Context context, String category) {
        //用于对数据差分异做比对时候的回调    新旧数据进行对比,如果有需要修改或删除的item 对其进行修改和删除操作
        super(new DiffUtil.ItemCallback<Feed>() {

            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.context = context;
        this.category = category;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType2(int position) {
        Feed feed = getItem(position);
        if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
            return R.layout.layout_feed_type_image;
        } else if (feed.itemType == Feed.TYPE_VIDEO) {
            return R.layout.layout_feed_type_video;
        }
        return 0;
    }

    @NonNull
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder2(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, viewType, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder2(@NonNull FeedAdapter.ViewHolder holder, int position) {
        Feed item = getItem(position);
        holder.bindData(item);
        holder.itemView.setOnClickListener(v -> {
//            category在详情页做无缝续播时会用到
            onStartFeedDetailActivity(item);
            FeedDetailActivity.startFeedDetailActivity(context, getItem(position), category);
            if (feedObserver == null) {   //添加普通事件注册
                feedObserver = new FeedObserver();

                LiveDataBus.get().with(InteractionPresenter.DATA_FROM_INTERACTION)
                        .observe((LifecycleOwner) context, feedObserver);
                feedObserver.setFeed(item);   //设置旧数据
            }
        });
    }

    public void onStartFeedDetailActivity(Feed item) {

    }

    private FeedObserver feedObserver;

//    详情页数据更新后 回调到此处 进行首页的界面更新
    private class FeedObserver implements Observer<Feed> {

        private Feed feed;

        @Override
        public void onChanged(Feed newOne) {
//            当前详情页后,在详情页面点赞,评论,收藏等,事件会发送到此处
//            首先得是同一条数据, 然后更新修改后的数据,通知界面刷新
            if (feed.id != newOne.id) {
                return;
            }
            feed.author = newOne.author;
            feed.ugc = newOne.ugc;
            feed.notifyChange();
        }

        public void setFeed(Feed feed) {

            this.feed = feed;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            this.binding = binding;
        }

        public void bindData(Feed item) {
            binding.setVariable(BR.feed, item);
            binding.setVariable(BR.lifeCycleOwner, context);
            if (binding instanceof LayoutFeedTypeImageBinding) {
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) binding;
//                imageBinding.setFeed(item);
                imageBinding.feedImage.bingData(item.width, item.height, 16, item.cover);
                imageBinding.interactionBinding.setFeed(item);

//                imageBinding.setLifecycleOwner((LifecycleOwner) context);

            } else if (binding instanceof LayoutFeedTypeVideoBinding){
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) binding;
//                videoBinding.setFeed(item);
                videoBinding.listPlayerView.bindData(category, item.width, item.height, item.cover
                        , item.url);
//                videoBinding.setLifecycleOwner((LifecycleOwner) context);
                listPlayerView = videoBinding.listPlayerView;
            }
        }

        public boolean isVideoItem() {
            return binding instanceof LayoutFeedTypeVideoBinding;
        }

        public ListPlayerView getListPlayerView() {
            return listPlayerView;
        }
    }
}

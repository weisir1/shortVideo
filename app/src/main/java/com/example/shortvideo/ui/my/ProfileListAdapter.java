package com.example.shortvideo.ui.my;

import android.content.Context;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.example.shortvideo.R;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.ui.MutableItemKeyedDataSource;
import com.example.shortvideo.ui.home.FeedAdapter;
import com.example.shortvideo.ui.home.InteractionPresenter;
import com.example.shortvideo.utils.TimeUtils;

import org.w3c.dom.Text;

public class ProfileListAdapter extends FeedAdapter {
    private Context context;
    private String category;

    protected ProfileListAdapter(Context context, String category) {
        super(context, category);
        this.context = context;
        this.category = category;
    }

    @Override
    public int getItemViewType2(int position) {
//        个人页面总共有三个布局,因为前两个页面feedAdapter中有,所以只需要在复写第三个即可
        if (TextUtils.equals(category, ProfileActivity.TAB_TYPE_COMMENT)) {
            return R.layout.layout_feed_type_comment;
        }
        return super.getItemViewType2(position);
    }

    @Override
    public void onBindViewHolder2(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder2(holder, position);

        View diss = holder.itemView.findViewById(R.id.diss);
        View feedDelete = holder.itemView.findViewById(R.id.feed_delete);
        TextView createTime = holder.itemView.findViewById(R.id.create_time);

        Feed feed = getItem(position);
        createTime.setVisibility(View.VISIBLE);
        createTime.setText(TimeUtils.calculate(feed.createTime));

        boolean isCommentTab = TextUtils.equals(category, ProfileActivity.TAB_TYPE_COMMENT);
        feedDelete.setVisibility(View.VISIBLE);
        if (isCommentTab) {
            diss.setVisibility(View.GONE);
        }
        feedDelete.setOnClickListener(v -> {
            if (isCommentTab) {  //如果评论页面deleteFeedComment
                InteractionPresenter.deleteFeedComment(context, feed.itemId, feed.topComment.commentId)
                        .observe((LifecycleOwner) context, aBoolean -> {
                            refreshList(feed);
                        });
            } else { //否则为帖子页面
                InteractionPresenter.deleteFeed(context, feed.itemId)
                        .observe((LifecycleOwner) context, aBoolean -> {
//                           删除成功之后,同样要刷新列表
                            refreshList(feed);
                        });
            }
        });
    }

    private void refreshList(Feed delete) {
        PagedList<Feed> currentList = getCurrentList();
        MutableItemKeyedDataSource<Long, Feed> dataSource = new MutableItemKeyedDataSource<Long, Feed>((ItemKeyedDataSource) currentList.getDataSource()) {
            @NonNull
            @Override
            public Long getKey(@NonNull Feed item) {
                return item.id;
            }
        };
        for (Feed feed : currentList) {
            if (feed!=delete){
//                过滤旧集合中要被删除的帖子
                dataSource.data.add(feed);
            }
        }
        PagedList<Feed> pageList = dataSource.buildNewPagedList(currentList.getConfig());
        submitList(pageList);
    }
}

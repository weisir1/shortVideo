package com.example.shortvideo.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.camera.core.Preview;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libcommon.extention.AbsPagedListAdapter;
import com.example.libcommon.util.PixUtils;
import com.example.shortvideo.BR;
import com.example.shortvideo.databinding.LayoutFeedCommentListItemBinding;
import com.example.shortvideo.model.Comment;
import com.example.shortvideo.ui.MutableItemKeyedDataSource;
import com.example.shortvideo.ui.login.UserManager;
import com.example.shortvideo.ui.pulish.PreviewActivity;

public class FeedCommentAdapter extends AbsPagedListAdapter<Comment, FeedCommentAdapter.ViewHolder> {

    private Context context;
    private final LayoutInflater inflater;

    protected FeedCommentAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.context = context;
        inflater = LayoutInflater.from(context);

    }
    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutFeedCommentListItemBinding binding = LayoutFeedCommentListItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        Comment item = getItem(position);
        holder.bindData(item);
        holder.binding.commentDelete.setOnClickListener(v -> {
            MutableItemKeyedDataSource<Integer, Comment> source = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) getCurrentList().getDataSource()) {
                @NonNull
                @Override
                public Integer getKey(@NonNull Comment item) {
                    return item.id;
                }
            };
            PagedList<Comment> currentList = getCurrentList();
            for (Comment comment : currentList) {
                if (comment != getItem(position)) {
                    source.data.add(comment);
                }
            }

            PagedList<Comment> pagedList = source.buildNewPagedList(getCurrentList().getConfig());
            submitList(pagedList);
        });
        holder.binding.commentCover.setOnClickListener(v -> {
//            查看评论图片或视频
           boolean isVideo =  item.commentType==Comment.COMMENT_TYPE_VIDEO;
            PreviewActivity.startActivityForResult((Activity) context,isVideo?item.videoUrl:item.imageUrl,isVideo,null);
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LayoutFeedCommentListItemBinding binding;

        public ViewHolder(@NonNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            this.binding = binding;
        }

        public void bindData(Comment item) {
            binding.setVariable(BR.comments,item);
            binding.setVariable(BR.owner,context);
//            binding.setOwner(BR.lifeCycleOwner,);

            if (item.author ==null){
                return;
            }
//            如果评论列表当前评论是作者写的,会显示有红色作者标签
            binding.labelAuthor.setVisibility(UserManager.get().getUserId() == item.author.userId ? View.VISIBLE : View.GONE);
            binding.commentDelete.setVisibility(UserManager.get().getUserId() == item.author.userId ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(item.imageUrl)) {
//                显示视频或图片封面图
                binding.commentExt.setVisibility(View.VISIBLE);
                binding.commentCover.setVisibility(View.VISIBLE);
                binding.commentCover.bingData(item.width, item.height, 0, PixUtils.dp2px(200), PixUtils.dp2px(200), item.imageUrl);
                if (!TextUtils.isEmpty(item.videoUrl)) {
//                    如果视频,显示播放按钮
                    binding.videoIcon.setVisibility(View.VISIBLE);
                } else {
                    binding.videoIcon.setVisibility(View.GONE);
                }
            } else {
                binding.commentExt.setVisibility(View.GONE);
                binding.commentCover.setVisibility(View.GONE);
                binding.videoIcon.setVisibility(View.GONE);
            }
        }
    }
}

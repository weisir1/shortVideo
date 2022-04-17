package com.example.shortvideo.ui.find;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libcommon.extention.AbsPagedListAdapter;
import com.example.shortvideo.databinding.LayoutTagListItemBinding;
import com.example.shortvideo.model.TagList;
import com.example.shortvideo.ui.home.InteractionPresenter;

public class TagListAdapter extends AbsPagedListAdapter<TagList, TagListAdapter.ViewHolder> {
    private Context context;
    private final LayoutInflater inflater;

    protected TagListAdapter(Context context) {
        super(new DiffUtil.ItemCallback<TagList>() {
            @Override
            public boolean areItemsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.tagId == newItem.tagId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.equals(newItem);
            }
        });
        setOnItemClickListener((item, position) -> {
            TagFeedListActivity.startActivity(context, item);
        });
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutTagListItemBinding itemBinding = LayoutTagListItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(itemBinding.getRoot(), itemBinding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        holder.bindData(getItem(position));
        holder.itemBinding.actionFollw.setOnClickListener(v -> {
            InteractionPresenter.toggleTagLike((LifecycleOwner) context, getItem(position));
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private com.example.shortvideo.databinding.LayoutTagListItemBinding itemBinding;

        public ViewHolder(@NonNull View itemView, LayoutTagListItemBinding itemBinding) {
            super(itemView);
            this.itemBinding = itemBinding;
        }

        public void bindData(TagList item) {
            itemBinding.setTagList(item);
        }
    }
}

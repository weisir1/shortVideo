package com.example.shortvideo.ui.my;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libcommon.util.PixUtils;
import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.libnetwork.JsonCallback;
import com.example.shortvideo.R;
import com.example.shortvideo.model.TagList;
import com.example.shortvideo.ui.login.UserManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class TagBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private TagsAdapter tagsAdapter;
    private List<TagList> tagLists = new ArrayList<>();
    private OnTagItemSelectedListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_tag_bottom_sheet_dialog, null, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tagsAdapter = new TagsAdapter();
        recyclerView.setAdapter(tagsAdapter);

        dialog.setContentView(view);
        ViewGroup parent = (ViewGroup) view.getParent();
//        BottomSheetDialog之所以能实现点击展开和收缩效果 主要通过BottomSheetBehavior实现的
        BottomSheetBehavior<ViewGroup> behavior = BottomSheetBehavior.from(parent);
//        设置对话框默认高度为屏幕1/3
        behavior.setPeekHeight(PixUtils.getScreenHeight() / 3);
//       false 表示随着手指下滑到最小高度后将不能在收缩
//       true 表示随着手指下滑 dialog会不断收缩直到0
        behavior.setHideable(false);

        ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
//        设置对话框最大值为屏幕2/3
        layoutParams.height = PixUtils.getScreenHeight() / 3 * 2;
        parent.setLayoutParams(layoutParams);

        queryTagList();
        return dialog;
    }

    private void queryTagList() {
        ApiService.get("/tag/queryTagList")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("pageCount", 100)
                .addParam("tagId", 0)
                .execute(new JsonCallback<List<TagList>>() {
                    @Override
                    public void onSuccess(ApiResponse<List<TagList>> response) {
                        if (response.body != null) {
                            List<TagList> body = response.body;
                            tagLists.clear();
                            tagLists.addAll(body);
                            ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                                tagsAdapter.notifyDataSetChanged();
                            });
                        }
                    }

                    @Override
                    public void onError(ApiResponse<List<TagList>> response) {
                        ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                            Toast.makeText(getContext(), response.message, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    class TagsAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextSize(13);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_000));
            textView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PixUtils.dp2px(45)));
            RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(textView) {
            };
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            TagList tagList = tagLists.get(position);
            textView.setText(tagList.title);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTagItemSelected(tagList);
                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return tagLists.size();
        }
    }

    public void setOnTagItemSelectedListener(OnTagItemSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnTagItemSelectedListener {
        void onTagItemSelected(TagList item);
    }
}

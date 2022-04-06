package com.example.shortvideo.ui.detail;

import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libcommon.global.AppGlobals;
import com.example.libcommon.view.EmptyView;
import com.example.shortvideo.R;
import com.example.shortvideo.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.example.shortvideo.databinding.LayoutFeedInteractionBinding;
import com.example.shortvideo.model.Comment;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.model.User;
import com.example.shortvideo.ui.MutableItemKeyedDataSource;
import com.example.shortvideo.ui.login.UserManager;

public abstract class ViewHandler {
    protected FragmentActivity activity;
    protected Feed feed;
    protected RecyclerView recyclerView;
    protected LayoutFeedDetailBottomInteractionBinding interactionBinding;
    protected FeedCommentAdapter listAdapter;
    private FeedDetailViewModel viewModel;
    private CommentDialog commentDialog;

    public ViewHandler(FragmentActivity activity) {
        this.activity = activity;
        viewModel = new ViewModelProvider(activity).get(FeedDetailViewModel.class);
    }

    @CallSuper   //注解表示如果子类要重写此方法,必须要要调用super.xxx这个方法 否则变异不通过
    public void bindInitData(Feed feed) {
        interactionBinding.setOwner(activity);
        this.feed = feed;
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.VERTICAL, false));
        recyclerView.setItemAnimator(null);
        listAdapter = new FeedCommentAdapter(activity);
        recyclerView.setAdapter(listAdapter);
        viewModel.setItemId(feed.itemId);
        viewModel.getLiveData().observe(activity, comments -> {
            listAdapter.submitList(comments);
            handleEmpty(comments.size() > 0);
        });

//        当点击输入文本框时 创建对话框并弹出
        interactionBinding.inputView.setOnClickListener(v -> {
            if (!UserManager.get().isLogin()) {
                LiveData<User> data = UserManager.get().login(AppGlobals.getsApplication());
                data.observe(activity, new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        if (user != null) {
                            clickInputView();
                        }
                        data.removeObserver(this);
                    }

                });
                return;
            }
            clickInputView();
        });
    }

    private void clickInputView() {
        if (commentDialog == null) {
            commentDialog = CommentDialog.newInstance(feed.itemId);
        }
        commentDialog.setCommentAddListener(comment -> {
            //因为dataSource不能增删, 若要将评论添加至顶,重新换一个dataSource 将旧的传入
            MutableItemKeyedDataSource<Integer, Comment> source = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) viewModel.getDataSource()) {
                @NonNull
                @Override
                public Integer getKey(@NonNull Comment item) {
                    return item.id;
                }
            };
//                要将新增的数据添加到列表首位置
            source.data.add(comment);
//                其次将原始列表添加

            source.data.addAll(listAdapter.getCurrentList());
            PagedList<Comment> pageList = source.buildNewPagedList(listAdapter.getCurrentList().getConfig());
            listAdapter.submitList(pageList);
        });
        commentDialog.show(activity.getSupportFragmentManager(), "comment_dialog");
    }

    EmptyView emptyView;

    protected void handleEmpty(boolean hasData) {
        if (hasData) {
            if (emptyView != null) {
                listAdapter.removeHeaderView(emptyView);
            }
        } else {
            if (emptyView == null) {
                emptyView = new EmptyView(activity);
                emptyView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                emptyView.setTile(activity.getString(R.string.feed_comment_empty));
                listAdapter.addHeaderView(emptyView);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

//        注意在从dialog中跳转带回参的activity时, onActivityResult()方法会回调到dialog所在的activity dialog本身onActivityResult()并不会被回调
//        所以需要手动调用
        if (commentDialog != null && commentDialog.isAdded()) {
            commentDialog.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onBackPressed() {

    }
}

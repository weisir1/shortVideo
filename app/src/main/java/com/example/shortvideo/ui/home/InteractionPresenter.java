package com.example.shortvideo.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONObject;
import com.example.libcommon.global.AppGlobals;
import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.libnetwork.JsonCallback;
import com.example.shortvideo.exoplayer.LiveDataBus;
import com.example.shortvideo.model.Comment;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.model.User;
import com.example.shortvideo.ui.ShareDialog;
import com.example.shortvideo.ui.login.UserManager;

public class InteractionPresenter {
    public static final String DATA_FROM_INTERACTION = "data_from_interaction";
    private static final String URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike";
    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";
    private static final String URL_SHARE = "/ugc/increaseShareCount";
    private static final String URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike";

    //    点赞事件
    public static void toggleFeedLike(LifecycleOwner owner, Feed feed) {
        if (!UserManager.get().isLogin()) {  //当点击点赞或其他按钮时,如果未登录状态会先跳转到登陆界面进行登陆
            LiveData<User> loginDataSource = UserManager.get().login(AppGlobals.getsApplication());
            loginDataSource.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        ttoggleFeedLikeInteral(feed);
                    }
                    loginDataSource.removeObserver(this);
                }
            });
            return;
        }
        ttoggleFeedLikeInteral(feed);
    }

    public static void ttoggleFeedLikeInteral(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIKE)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId)
                .execute(new JsonCallback<com.alibaba.fastjson.JSONObject>() {
                             @Override
                             public void onSuccess(ApiResponse<com.alibaba.fastjson.JSONObject> response) {
                                 if (response.body != null) {
                                     try {
                                         boolean hasLiked = response.body.getBooleanValue("hasLiked");
                                         feed.getUgc().setHasLiked(hasLiked);
                                         LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                                 .postValue(feed);

                                     } catch (Exception e) {
                                         e.printStackTrace();
                                     }
                                 }
                             }

                             @Override
                             public void onError(ApiResponse<JSONObject> response) {
                                 showToast(response.message);
                             }
                         }

                );
    }

    //踩事件
    public static void toggleFeedDiss(LifecycleOwner owner, Feed feed) {
        if (!UserManager.get().isLogin()) {  //当点击点赞或其他按钮时,如果未登录状态会先跳转到登陆界面进行登陆
            LiveData<User> loginDataSource = UserManager.get().login(AppGlobals.getsApplication());
            loginDataSource.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        toggleFeedDiss(feed);
                    }
                    loginDataSource.removeObserver(this);
                }
            });
            return;
        }
        toggleFeedDiss(feed);
    }

    private static void toggleFeedDiss(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_DISS)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId)
                .execute(new JsonCallback<com.alibaba.fastjson.JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<com.alibaba.fastjson.JSONObject> response) {
                        if (response.body != null) {
                            try {
                                boolean hasdiss = response.body.getBooleanValue("hasLiked");
                                feed.getUgc().setHasdiss(hasdiss);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    //   分享面板
    public static void openShare(Context context, Feed feed) {
        String feeds_text = feed.feeds_text;
        if (!TextUtils.isEmpty(feed.url)) {
            feeds_text = feed.url;
        } else {
            feeds_text = feed.cover;
        }
        ShareDialog shareDialog = new ShareDialog(context);
        shareDialog.setShareCount(feeds_text);
        shareDialog.setShareItemClickListener(v -> {
            ApiService.get(URL_SHARE)
                    .addParam("itemId", feed.itemId)
                    .execute(new JsonCallback<com.alibaba.fastjson.JSONObject>() {   //当dialog的监听触发 并且成功跳转到对应app后 才会回调此点击
                        @Override
                        public void onSuccess(ApiResponse<com.alibaba.fastjson.JSONObject> response) {
                            if (response.body != null) {
                                int count = response.body.getIntValue("count");
                                feed.getUgc().setShareCount(count);
                            }
                        }

                        @Override
                        public void onError(ApiResponse<com.alibaba.fastjson.JSONObject> response) {
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show();
                        }

                    });
        });
        shareDialog.show();
    }

    // 贴子评论点赞
    public static void toggleCommentLike(LifecycleOwner owner, Comment comment) {
        if (!UserManager.get().isLogin()) {
            UserManager.get().login(AppGlobals.getsApplication()).observe(owner, user -> {
                if (user != null) {
                    toggleCommentLikeInternal(comment);
                }
            });
        } else {
            toggleCommentLikeInternal(comment);
        }

    }

    public static void toggleCommentLikeInternal(Comment comment) {
        ApiService.get(URL_TOGGLE_COMMENT_LIKE)
                .addParam("commentId", comment.commentId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBooleanValue("hasLiked");
                            comment.ugc.setHasLiked(hasLiked);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    //    收藏事件
    public static void toggleFeedFavorite(LifecycleOwner owner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            UserManager.get().login(AppGlobals.getsApplication()).observe(owner, user -> {
                if (user != null) {
                    toggleFeedFavorite(feed);
                }
            });
        } else {
            toggleFeedFavorite(feed);
        }
    }

    public static void toggleFeedFavorite(Feed feed) {
        ApiService.get("/ugc/toggleFavorite")
                .addParam("itemId", feed.itemId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFavorite = response.body.getBooleanValue("hasFavorite");
                            feed.getUgc().setHasFavorite(hasFavorite);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);

                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    //    关注事件
    public static void toggleFollowUser(LifecycleOwner owner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            UserManager.get().login(AppGlobals.getsApplication()).observe(owner, user1 -> {
                if (user1 != null) {
                    toggleFollowUser(feed);
                }
            });
        } else {
            toggleFollowUser(feed);
        }
    }

    private static void toggleFollowUser(Feed feed) {
        ApiService.get("/ugc/toggleUserFollow")
                .addParam("followUserId", UserManager.get().getUserId())
                .addParam("userId", feed.author.userId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBooleanValue("hasLiked");
                            feed.author.setHasFollow(hasLiked);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);

                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    //    删除事件
    public static LiveData<Boolean> deleteFeedComment(Context context, long itemId, long commentId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        new AlertDialog.Builder(context)
                .setNegativeButton("删除", (dialog, which) -> {
                    dialog.dismiss();
                    deleteFeedComment(liveData, itemId, commentId);
                }).setPositiveButton("取消", (dialog, which) -> {
            dialog.dismiss();


        }).setMessage("确认要删除这条评论吗?").create().show();
        return liveData;
    }

    public static void deleteFeedComment(LiveData liveData, long itemId, long commentId) {
        ApiService.get("/comment/deleteComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("commentId", commentId)
                .addParam("itemId", itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean result = response.body.getBooleanValue("result");
                            ((MutableLiveData) liveData).postValue(result);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private static void showToast(String message) {
        ArchTaskExecutor.getMainThreadExecutor().execute(() -> Toast.makeText(AppGlobals.getsApplication(), message, Toast.LENGTH_SHORT).show());
    }
}

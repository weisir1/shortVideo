package com.example.shortvideo.ui.detail;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shortvideo.R;
import com.example.shortvideo.databinding.LayoutFeedDetailTypeVideoBinding;
import com.example.shortvideo.databinding.LayoutFeedDetailTypeVideoHeaderBinding;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.view.FullScreenPlayerView;

public class VideoViewHandler extends ViewHandler {

    private LayoutFeedDetailTypeVideoBinding videoBinding;
    private String category;
    private boolean backPressed;
    private final CoordinatorLayout coordinator;
    private final FullScreenPlayerView playerView;

    public VideoViewHandler(FragmentActivity activity) {
        super(activity);
        videoBinding = DataBindingUtil.setContentView(activity, R.layout.layout_feed_detail_type_video);
        interactionBinding = videoBinding.videoBottomInteractionVideo;
        recyclerView = videoBinding.videoRecyclerView;
        playerView = videoBinding.playerView;
        coordinator = videoBinding.coordinator;
        View authorInfoView = videoBinding.videoAuthorInfo.getRoot();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) authorInfoView.getLayoutParams();
        layoutParams.setBehavior(new ViewAnchorBehavior(R.id.player_view));

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) playerView.getLayoutParams();
        ViewZoomBehavior behavior = (ViewZoomBehavior) params.getBehavior();
        params.setBehavior(behavior);
        behavior.setViewZoomCallback(height -> {
            int bottom = playerView.getBottom();
            boolean moveUp = height < bottom;
//            上滑过程中,认为上滑距离超过底部导航高度 时为非全屏显示, 否则 即使上滑 仍为全屏显示
            boolean fullscreen = moveUp ? height >= coordinator.getBottom() - interactionBinding.getRoot().getHeight() :
                    height >= coordinator.getBottom();
            setViewApprearance(fullscreen);
        });
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        videoBinding.setFeed(feed);
        category = activity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        videoBinding.playerView.bindData(category, feed.width, feed.height, feed.cover, feed.url);
        videoBinding.playerView.post(() -> {
//            判断视频播放器底部是否与父view底部对其, 对其为全屏
            boolean fullscreen = videoBinding.playerView.getBottom() >= videoBinding.coordinator.getBottom();
            setViewApprearance(fullscreen);
        });
        LayoutFeedDetailTypeVideoHeaderBinding head = LayoutFeedDetailTypeVideoHeaderBinding.inflate(LayoutInflater.from(activity), recyclerView, false);
        head.setFeed(feed);
        listAdapter.addHeaderView(head.getRoot());
    }

    private void setViewApprearance(boolean fullscreen) {
        videoBinding.setFullscreens(fullscreen);
        videoBinding.videoFullscreenAuthorInfo.getRoot().setVisibility(fullscreen ? View.VISIBLE : View.GONE);

//            全屏情况下 控制条的位置需要上挪 否则会被bottominteraction覆盖
        int inputHeight = interactionBinding.getRoot().getMeasuredHeight();
        int ctrViewHeight = videoBinding.playerView.getPlayController().getMeasuredHeight();

        int bottom = videoBinding.playerView.getPlayController().getBottom();

        videoBinding.playerView.getPlayController().setY(fullscreen ? bottom - inputHeight - ctrViewHeight : bottom - ctrViewHeight);
        interactionBinding.inputView.setBackgroundResource(fullscreen?R.drawable.bg_edit_view2:R.drawable.bg_edit_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressed = false;
        videoBinding.playerView.onActive();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!backPressed) {
            videoBinding.playerView.inActive();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;  //用于表示是否通过点击返回键触发了onPause()方法

        //按了返回键后需要 恢复 播放控制器的位置。否则回到列表页时 可能会不正确的显示
        videoBinding.playerView.getPlayController().setTranslationY(0);
    }
}

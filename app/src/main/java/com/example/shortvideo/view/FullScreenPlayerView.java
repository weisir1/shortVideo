package com.example.shortvideo.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.libcommon.util.PixUtils;
import com.example.shortvideo.R;
import com.example.shortvideo.exoplayer.PageListPlay;
import com.example.shortvideo.exoplayer.PageListPlayManager;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class FullScreenPlayerView extends ListPlayerView {

    private final PlayerView exoPlayerView;

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        exoPlayerView = (PlayerView) LayoutInflater.from(context).inflate(R.layout.layout_exo_player, null, false);
    }

    @Override
    protected void setSize(int widthPx, int heightPx) {
        if (widthPx > heightPx) {
            super.setSize(widthPx, heightPx);
// !!!!!!!!!!!!!!!!!!! 宽大于高 记得return
            return;
        }
//        playerView布局大小
        int maxWidth = PixUtils.getScreenWidth();
        int maxHeight = PixUtils.getScreenHeight();
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = maxWidth;
        params.height = maxHeight;
        setLayoutParams(params);

/*        FrameLayout.LayoutParams coverLayoutParams = (LayoutParams) cover.getLayoutParams();
        coverLayoutParams.width = (int) (widthPx / (heightPx * 1.0f / maxHeight));
        coverLayoutParams.height = maxHeight;
        coverLayoutParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverLayoutParams);*/
    }

    //    外界设置参数时,视频内容要跟着变化
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
//        设置封面图的等比缩放
        if (heightPx > widthPx) {//必须为全屏
            int layoutWidth = params.width;
            int layoutHeight = params.height;
            ViewGroup.LayoutParams coverLayoutParams = cover.getLayoutParams();
//            给当前封面设置手势缩放后应该的大小
            coverLayoutParams.width = (int) (widthPx / (heightPx * 1.0f / layoutHeight));
            coverLayoutParams.height = layoutHeight;
            cover.setLayoutParams(coverLayoutParams);

//            设置视频播放的等比缩放
            if (exoPlayerView != null) {
                ViewGroup.LayoutParams layoutParams = exoPlayerView.getLayoutParams();
                if (layoutParams != null) {
//                    根据封面的大小同步缩放
                    float scalex = coverLayoutParams.width * 1.0f / layoutParams.width;
                    float scaley = coverLayoutParams.height * 1.0f / layoutParams.height;

                    exoPlayerView.setScaleX(scalex);
                    exoPlayerView.setScaleY(scaley);
                }
            }
        }
        super.setLayoutParams(params);
    }

    @Override
    public void onActive() {
//        category是由上个activity传递的参数, 所以在PageListPlayManager中已经存在实例 也就代表着保存了上个界面的playerView的实例对象playerView
        PageListPlay pageListPlay = PageListPlayManager.get(category);
//        这个为当前新页面创建的playerView onActive中将接替旧界面的playerView工作  在inActive中又将工作还给旧PlayerView
        PlayerView playerView = exoPlayerView; //pageListPlay.playerView;  替换为详情页的
        PlayerControlView controlView = pageListPlay.controlView;
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playerView == null) {
            return;
        }

//       重新将exoPlayer绑定到此处创建的playerView上
        pageListPlay.switchPlayerView(playerView, true);
//        更新到当前页面对应playerVIew
        ViewParent parent = playerView.getParent();
        if (parent != this) {
            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
                ((ListPlayerView) parent).inActive();
            }
            ViewGroup.LayoutParams params = cover.getLayoutParams();
//            在video布局中添加视频播放器,位置第二层
            addView(playerView, 1, params);
        }
//        同样的方式删除添加控制条
        ViewParent ctlParent = controlView.getParent();
        if (ctlParent != this) {
            if (ctlParent != null) {
                ((ViewGroup) ctlParent).removeView(controlView);
            }
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            addView(controlView, params);
        }

        if (TextUtils.equals(pageListPlay.playUrl, videoUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY);
        } else {
            MediaSource mediaSource = PageListPlayManager.createMediaSource(videoUrl);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            pageListPlay.playUrl = videoUrl;
        }
        controlView.show();
        controlView.setVisibilityListener(this);  //控制条显示隐藏状态监听 伴随屏幕的暂停按钮的变化
        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void inActive() {
        super.inActive();
//        在退出详情页时 解绑当前payerView关联 ,重新绑定会主页面
        PageListPlay pageListPlay = PageListPlayManager.get(category);
        pageListPlay.switchPlayerView(exoPlayerView, false);
    }
}

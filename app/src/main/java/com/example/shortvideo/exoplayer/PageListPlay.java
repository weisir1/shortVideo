package com.example.shortvideo.exoplayer;

import android.app.Application;
import android.view.LayoutInflater;

import com.example.libcommon.global.AppGlobals;
import com.example.shortvideo.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class PageListPlay {

    public SimpleExoPlayer exoPlayer;
    public PlayerView playerView;
    public PlayerControlView controlView;
    public String playUrl;

    public PageListPlay() {
        Application application = AppGlobals.getsApplication();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(application,
//                视频每一帧的画面如何渲染
                new DefaultRenderersFactory(application),
//                默认的音视频轨道加载
                new DefaultTrackSelector(),
//                默认的视频的缓存进度条
                new DefaultLoadControl());

        playerView = (PlayerView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player, null, false);
        controlView = (PlayerControlView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_controler_view, null, false);
//      将视频播放器  和进度条与控制类关联
        playerView.setPlayer(exoPlayer);
        controlView.setPlayer(exoPlayer);
    }

    public void release() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop(true);
            exoPlayer.release();
            exoPlayer = null;
        }

        if (playerView != null) {
            playerView.setPlayer(null);
            playerView = null;
        }
        if (controlView != null) {
            controlView.setPlayer(null);

            controlView.setVisibilityListener(null);
            controlView = null;
        }
    }

    public void switchPlayerView(PlayerView newPlayerView, boolean attach) {

//        断开当前playerview 将传入的新playerview绑定exoPlayer 用于详情页无缝连接
        playerView.setPlayer(attach ? null : exoPlayer);
        newPlayerView.setPlayer(attach ? exoPlayer : null);
    }
}

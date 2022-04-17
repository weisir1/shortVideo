package com.example.shortvideo.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.libcommon.util.PixUtils;
import com.example.shortvideo.R;
import com.example.shortvideo.exoplayer.IPlayTarget;
import com.example.shortvideo.exoplayer.PageListPlay;
import com.example.shortvideo.exoplayer.PageListPlayManager;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class ListPlayerView extends FrameLayout implements IPlayTarget, Player.EventListener, PlayerControlView.VisibilityListener {
    private View bufferView;
    public SVImageView cover, blur;
    private ImageView playBtn;
    public String category;
    public String videoUrl;
    protected int widthPx;
    protected int heightPx;

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);
        bufferView = findViewById(R.id.buffer_view);
        cover = findViewById(R.id.cover);
        blur = findViewById(R.id.blur_background);
        playBtn = findViewById(R.id.paly_btn);

        playBtn.setOnClickListener(v -> {
            if (isPlaying()) {
                inActive();
            } else {
                onActive();
            }
        });
    }


    public void bindData(String category, int widthPx, int heightPx, String coverUrl, String videoUrl) {
        this.category = category;
        this.videoUrl = videoUrl;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
        this.heightPx = heightPx;

        cover.setImageUrl(cover, coverUrl, false);
        if (widthPx < heightPx) {
//            全屏 则显示封面图和背景色(黑色)
            SVImageView.setBlurImageUrl(blur, coverUrl, 10);
            blur.setVisibility(VISIBLE);
        } else {
            blur.setVisibility(INVISIBLE);
        }
        setSize(widthPx, heightPx);
    }

    @Override
    public ViewGroup getOwner() {
        return this;
    }

    @Override
    public void onActive() {
//        通过每个fragment的标识,(比如首页列表tab_all,沙发tab的tab_video,标签帖子聚合的tag_feed) 字段，
        PageListPlay pageListPlay = PageListPlayManager.get(category);
        PlayerView playerView = pageListPlay.playerView;
        PlayerControlView controlView = pageListPlay.controlView;
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playerView == null) {
            return;
        }
        /*        因为playerView只有在初始化pageListPlay时候才会playerView.setPlayer(exoPlayer)和controlView.setPlayer(exoPlayer);
                   因为pageListPlay存储在hashmap中,下一次通过PageListPlayManager.get(category)获取时,如果map中有该对象,则不会创建
                   而直接使用map中的对象
                   因为在切换详情页面的时候,会有不同的页面复用exo,为了view间的切换顺畅而使用
         */
        pageListPlay.switchPlayerView(playerView, true);
//        更新到当前页面对应playerVIew
        ViewParent parent = playerView.getParent();
//        playerview对象可能在其他地方显示 当父view不是当前容器, 调用旧容器删除player 添加到当前容器下
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

//        如果是同一个视频资源地址 不需要重新创建mediaSource
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

    //    播放状态发生变化
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        PageListPlay pageListPlay = PageListPlayManager.get(category);
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady) {
            cover.setVisibility(GONE);
            bufferView.setVisibility(GONE);
        } else if (playbackState == Player.STATE_BUFFERING) {
            bufferView.setVisibility(VISIBLE);
        }
        isPlaying = playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady;
        //控制条设置有显示时间,当控制条状态更改后,会回调onVisibilityChange() 同步playBtn的显示状态
        playBtn.setImageResource(isPlaying ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    @Override
    public void inActive() {
//        暂停视频播放,显示按钮和控制条
        PageListPlay pageListPlay = PageListPlayManager.get(category);
        if (pageListPlay.exoPlayer == null || pageListPlay.controlView == null) {
            return;
        }

        pageListPlay.exoPlayer.setPlayWhenReady(false);
        pageListPlay.controlView.setVisibilityListener(null);
        pageListPlay.exoPlayer.removeListener(this);
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//    点击屏幕 显示控制条
        PageListPlay pageListPlay = PageListPlayManager.get(category);
        pageListPlay.controlView.show();
        return true;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    private boolean isPlaying;


    protected void setSize(int widthPx, int heightPx) {
        int maxWidth = PixUtils.getScreenWidth();
        int maxHeight = PixUtils.getScreenHeight();

        int layoutWidth = maxWidth;
        int layoutHeight = 0;

        int coverWidth;
        int coverHeight;
        if (widthPx >= heightPx) {  //如果width大 视频大小为width宽 高为width增大时的差值比例
            coverWidth = maxWidth;
            layoutHeight = coverHeight = (int) (heightPx / (widthPx * 1.0 / maxWidth));
        } else {
            layoutWidth = coverHeight = maxHeight;
            coverWidth = (int) (widthPx / (heightPx * 1.0 / maxHeight));
        }
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = layoutWidth;
        params.height = layoutHeight;
        setLayoutParams(params);
        ViewGroup.LayoutParams params1 = blur.getLayoutParams();
        params1.width = layoutWidth;
        params1.height = layoutHeight;
        blur.setLayoutParams(params1);

        FrameLayout.LayoutParams layoutParams = (LayoutParams) cover.getLayoutParams();
        layoutParams.width = coverWidth;
        layoutParams.height = coverHeight;
        cover.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams playBtnLayoutParams = (LayoutParams) playBtn.getLayoutParams();
        playBtnLayoutParams.gravity = Gravity.CENTER;
        playBtn.setLayoutParams(playBtnLayoutParams);
    }

    //视图与窗口分离时调用 不在具有用于绘制的表面
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isPlaying = false;
        bufferView.setVisibility(GONE);
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public void onVisibilityChange(int visibility) {
        playBtn.setVisibility(visibility);
        playBtn.setImageResource(isPlaying ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    public View getPlayController() {
        PageListPlay listPlay = PageListPlayManager.get(category);
        return listPlay.controlView;
    }
}

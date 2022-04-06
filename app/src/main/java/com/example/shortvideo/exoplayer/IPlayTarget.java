package com.example.shortvideo.exoplayer;

import android.view.ViewGroup;

public interface IPlayTarget {
    ViewGroup getOwner();

    //    视频可播放
    void onActive();

    //    非活跃状态
    void inActive();

    boolean isPlaying();
}

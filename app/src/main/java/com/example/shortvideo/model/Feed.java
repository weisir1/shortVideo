package com.example.shortvideo.model;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.io.Serializable;
import java.util.Objects;
import java.util.Observable;

public class Feed extends BaseObservable implements Serializable {

    public static final int TYPE_IMAGE_TEXT = 1;//图文
    public static final int TYPE_VIDEO = 2;//视频
    /**
     * id : 364
     * itemId : 6739143063064549000
     * itemType : 2
     * createTime : 1569079017
     * duration : 299.435
     * feeds_text : 当中国地图出来那一幕，我眼泪都出来了！
     * 太震撼了！
     * authorId : 3223400206308231
     * activityIcon : null
     * activityText : null
     * width : 640
     * height : 368
     * url : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/6739143063064549643.mp4
     * cover : https://p3-dy.byteimg.com/img/mosaic-legacy/2d676000e36289f35f70c~640x368_q80.webp
     */

    public int id;
    public long itemId;
    public int itemType;
    public long createTime;
    public double duration;
    public String feeds_text;
    public long authorId;
    public String activityIcon;
    public String activityText;
    public int width;
    public int height;
    public String url;
    public String cover;

    public User author;
    public Comment topComment;

    @Bindable
    public Ugc getUgc() {
        if (ugc == null) ugc = new Ugc();
        return ugc;
    }

    public Ugc ugc;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;

        return id == feed.id &&
                itemId == feed.itemId &&
                itemType == feed.itemType &&
                createTime == feed.createTime &&
                Double.compare(feed.duration, duration) == 0 &&
                authorId == feed.authorId &&
                width == feed.width &&
                height == feed.height &&
                TextUtils.equals(activityIcon, feed.activityIcon) &&
                feeds_text.equals(feed.feeds_text) &&
                TextUtils.equals(activityText, feed.activityText) &&
                TextUtils.equals(url, feed.url) &&
                TextUtils.equals(cover, feed.cover) &&
                author != null &&
                author.equals(feed.author) &&
                topComment != null &&
                topComment.equals(feed.topComment) &&
                ugc != null &&
                ugc.equals(feed.ugc);
    }


}

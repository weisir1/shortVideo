package com.example.shortvideo.model;

import android.text.TextUtils;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.shortvideo.BR;
import java.io.Serializable;
import java.util.Objects;

public class User extends BaseObservable implements Serializable {

    /**
     * id : 962
     * userId : 3223400206308231
     * name : 二师弟请随我来
     * avatar : https://p3-dy.byteimg.com/img/p1056/8c50025c85244140910a513345ae7358~200x200.webp
     * description :
     * likeCount : 0
     * topCommentCount : 0
     * followCount : 0
     * followerCount : 0
     * qqOpenId : null
     * expires_time : 0
     * score : 0
     * historyCount : 0
     * commentCount : 0
     * favoriteCount : 0
     * feedCount : 0
     * hasFollow : false
     */

    public int id;
    public long userId;
    public String name;
    public String avatar;
    public String description;
    public int likeCount;
    public int topCommentCount;
    public int followCount;
    public int followerCount;
    public String qqOpenId;
    public long expires_time;
    public int score;
    public int historyCount;
    public int commentCount;
    public int favoriteCount;
    public int feedCount;
    public boolean hasFollow;

    @Bindable
    public boolean isHasFollow() {
        return hasFollow;
    }

    public void setHasFollow(boolean hasFollow) {
        this.hasFollow = hasFollow;
        notifyPropertyChanged(BR._all);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                TextUtils.equals(name, user.name) &&
                TextUtils.equals(avatar, user.avatar) &&
                TextUtils.equals(description, user.description) &&
                userId == user.userId &&
                likeCount == user.likeCount &&
                topCommentCount == user.topCommentCount &&
                followCount == user.followCount &&
                followerCount == user.followerCount &&
                expires_time == user.expires_time &&
                score == user.score &&
                historyCount == user.historyCount &&
                commentCount == user.commentCount &&
                favoriteCount == user.favoriteCount &&
                feedCount == user.feedCount &&
                hasFollow == user.hasFollow &&
                TextUtils.equals(qqOpenId, user.qqOpenId);

    }

}

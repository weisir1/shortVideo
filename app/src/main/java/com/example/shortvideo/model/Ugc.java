package com.example.shortvideo.model;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.shortvideo.BR;

import java.io.Serializable;
import java.util.Objects;

public class Ugc extends BaseObservable implements Serializable {
    /**
     * likeCount : 153
     * shareCount : 0
     * commentCount : 4454
     * hasFavorite : false
     * hasLiked : true
     * hasdiss:false
     */

    public int likeCount;


    public int shareCount;

    @Bindable
    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
        notifyPropertyChanged(BR._all);
    }

    public int commentCount;
    public boolean hasFavorite;

    @Bindable
    public boolean isHasdiss() {
        return hasdiss;
    }

    public void setHasdiss(boolean hasdiss) {
        if (hasdiss == this.hasdiss) {
            return;
        }
        if (hasdiss) {
            setHasLiked(false);
        }
        this.hasdiss = hasdiss;
    }

    public boolean hasdiss;

    @Bindable
    public boolean isHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(boolean hasLiked) {
        if (this.hasLiked == hasLiked) {
            return;
        }
        if (hasLiked) {

            likeCount = likeCount + 1;
            setHasdiss(false);
        } else {
            likeCount = likeCount - 1;
        }
        this.hasLiked = hasLiked;
        notifyPropertyChanged(BR._all);   //在资源文件中创建一个布局 凡是variable标签内都会自动生成对象,通过BR来通知到他们
    }


    public boolean hasLiked;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ugc ugc = (Ugc) o;
        return likeCount == ugc.likeCount &&
                shareCount == ugc.shareCount &&
                commentCount == ugc.commentCount &&
                hasFavorite == ugc.hasFavorite &&
                hasdiss == ugc.hasdiss &&
                hasLiked == ugc.hasLiked;
    }

    @Bindable
    public boolean isHasFavorite() {
        return hasFavorite;
    }

    public void setHasFavorite(boolean hasFavorite) {
        this.hasFavorite = hasFavorite;
        notifyPropertyChanged(BR._all);
    }
}

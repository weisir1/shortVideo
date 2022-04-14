package com.example.shortvideo.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.libcommon.util.PixUtils;
import com.example.libcommon.view.ViewHelper;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class SVImageView extends AppCompatImageView {
    public SVImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewHelper.setViewOutLine(this, attrs, defStyleAttr, 0);
    }

    public SVImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    //第二个参数: 只有在布局文件中将value两个参数都写上才会显示   false 表示只要有一个参数就行
    @BindingAdapter(value = {"image_url", "isCircle"}, requireAll = true)
    public static void setImageUrl(SVImageView view, String imageUrl, boolean isCircle) {
        RequestBuilder<Drawable> builder = Glide.with(view).load(imageUrl);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (isCircle) {
            builder.transform(new CircleCrop());
        }
        if (layoutParams != null && layoutParams.width > 0 && layoutParams.height > 0) {
            //防止图片尺寸很大 而需要的尺寸很小造成的资源浪费 ?
            builder.override(layoutParams.width, layoutParams.height);
        }
        builder.into(view);
    }

    public void bingData(int widthPx, int heightPx, int marginLeft, String imgUrl) {
        bingData(widthPx, heightPx, marginLeft, PixUtils.getScreenWidth(), PixUtils.getScreenHeight(), imgUrl);
    }

    public void setImageUrl(String imageUrl) {
        setImageUrl(this, imageUrl, false);
    }

    public void bingData(int widthPx, int heightPx, int marginLeft, int maxWidth, int maxHeight, String imgUrl) {
        if (widthPx <= 0 || heightPx <= 0) {
            Glide.with(this).load(imgUrl).into(new SimpleTarget<Drawable>() {
                // 图片加载完成后回调
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    int height = resource.getIntrinsicHeight();
                    int width = resource.getIntrinsicWidth();
                    setSize(width, height, marginLeft, maxWidth, maxHeight);
                    setImageDrawable(resource);
                }
            });
            return;
        }
        setSize(widthPx, heightPx, marginLeft, maxWidth, maxHeight);
        setImageUrl(this, imgUrl, false);
    }

    private void setSize(int width, int height, int marginLeft, int maxWidth, int maxHeight) {
        int finalWidth, finalHeight;
        if (width > height) {  //如果宽度大于高度, 图片宽度要沾满全屏
            finalWidth = maxWidth;
            finalHeight = (int) (height / (width * 1.0f / finalWidth)); //高度根据宽度放大的比例进行自适应放大
        } else {
            finalHeight = maxHeight;
            finalWidth = (int) (width / (height * 1.0f / finalHeight));
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = finalWidth;
        params.height = finalHeight;
        if (params instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) params).leftMargin = height > width ? PixUtils.dp2px(marginLeft) : 0;
        } else if (params instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) params).leftMargin = height > width ? PixUtils.dp2px(marginLeft) : 0;
        }
        setLayoutParams(params);
    }

    public void setBlurImageUrl(String coverUrl, int radius) {
        Glide.with(this).load(coverUrl).override(50)
                .transform(new BlurTransformation())
                .dontAnimate()
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        setBackground(resource);
                    }
                });
    }
}

package com.example.shortvideo.ui.detail;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.shortvideo.R;
import com.example.shortvideo.model.Feed;

public class FeedDetailActivity extends AppCompatActivity {
    private static final String KEY_FEED = "key_feed";
    public static final String KEY_CATEGORY = "key_category";
    private ViewHandler viewHandler;

    public static void startFeedDetailActivity(Context context, Feed item, String category) {
        Intent intent = new Intent(context, FeedDetailActivity.class);
        intent.putExtra(KEY_FEED, item);
        intent.putExtra(KEY_CATEGORY, category);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Feed feed = (Feed) getIntent().getSerializableExtra(KEY_FEED);
        if (feed == null) {
            finish();
            return;
        }
//        根据不同的itemType设置对应的布局
        if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
            viewHandler = new ImageViewHandler(this);
        } else {
            viewHandler = new VideoViewHandler(this);
        }
        viewHandler.bindInitData(feed);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (viewHandler!=null){
            viewHandler.onActivityResult(requestCode,resultCode,data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewHandler!=null){
            viewHandler.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewHandler!=null){
            viewHandler.onResume();
        }
    }

//    点击返回键时,不希望直接返回, 为了能无缝续播,将事件传给videoHandler 自行实现
    @Override
    public void onBackPressed() {
        if (viewHandler!=null){
            viewHandler.onBackPressed();
        }
        super.onBackPressed();
    }
}
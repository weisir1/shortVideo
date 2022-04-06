package com.example.shortvideo.ui.pulish;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.example.shortvideo.R;
import com.example.shortvideo.databinding.ActivityLayoutPreviewBinding;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityLayoutPreviewBinding binding;
    public static final String KEY_PREVIEW_URL = "preview_url";
    public static final String KEY_PREVIEW_VIDEO = "preview_video";
    public static final String KEY_PREVIEW_BTNTEXT = "preview_btntext";
    public static final int REUQ_CODE = 1000;
    private SimpleExoPlayer player;

    public static void startActivityForResult(Activity activity, String previewUrl, boolean isVideo, String btnText) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(KEY_PREVIEW_URL, previewUrl);
        intent.putExtra(KEY_PREVIEW_VIDEO, isVideo);
        intent.putExtra(KEY_PREVIEW_BTNTEXT, btnText);
        activity.startActivityForResult(intent, REUQ_CODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_layout_preview);
        Intent intent = getIntent();
        String previewUrl = intent.getStringExtra(KEY_PREVIEW_URL);
        boolean isVideo = intent.getBooleanExtra(KEY_PREVIEW_VIDEO, false);
        String btnText = intent.getStringExtra(KEY_PREVIEW_BTNTEXT);
        if (TextUtils.isEmpty(btnText)) {
            binding.actionOk.setVisibility(View.GONE);
        } else {
            binding.actionOk.setVisibility(View.VISIBLE);
            binding.actionOk.setText(btnText);
            binding.actionOk.setOnClickListener(this);
        }
        binding.actionClose.setOnClickListener(this);
        if (isVideo) {
            previewVideo(previewUrl);
        } else {
            previewImage(previewUrl);
        }
    }

    private void previewImage(String previewUrl) {
        binding.photoView.setVisibility(View.VISIBLE);
        Glide.with(this).load(previewUrl).into(binding.photoView);
    }
    private void previewVideo(String previewUrl) {
        binding.playerView.setVisibility(View.VISIBLE);
        player = ExoPlayerFactory.newSimpleInstance(this,new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());


        Uri uri = null;
        File file = new File(previewUrl);
//        判断是否为本地文件路径
        if (file.exists()) {
            DataSpec dataSpec = new DataSpec(Uri.fromFile(file));
//            创建文件数据源
            FileDataSource fileDataSource = new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
                uri = fileDataSource.getUri();
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }
        } else {
            uri = Uri.parse(previewUrl);
        }
        ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this, Util.getUserAgent(this, getPackageName())), new DefaultExtractorsFactory());
        ProgressiveMediaSource mediaSource = factory.createMediaSource(uri);
//        player绑定数据源
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);

        binding.playerView.setPlayer(player);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_close:
                finish();
                break;
            case R.id.action_ok:
//                预览后对结果满意,将文件路径等一些传回   回调onActivityResult
                setResult(RESULT_OK, new Intent());
                finish();
                break;
        }
    }
}

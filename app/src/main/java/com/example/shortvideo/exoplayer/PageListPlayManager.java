package com.example.shortvideo.exoplayer;

import android.app.Application;
import android.net.Uri;

import com.example.libcommon.global.AppGlobals;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.util.HashMap;

public class PageListPlayManager {
    private static HashMap<String,PageListPlay> pageListPlayHashMap = new HashMap<>();
    static {
        Application application = AppGlobals.getsApplication();
//        可以将提供的视频url地址解析并播放  即视频文件的下载
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(application, application.getPackageName()));
//        指定最大缓存大小200M   视频文件的缓存
        SimpleCache cache = new SimpleCache(application.getCacheDir(), new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 200));

//        视频文件的写入
        CacheDataSinkFactory cacheDataSinkFactory = new CacheDataSinkFactory(cache, Long.MAX_VALUE);
//        最后一个参数表示,如果在请求本地缓存文件时候,其他线程正在写入,则会等待操作结束后进行读取
        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                cache,
                dataSourceFactory,
                new FileDataSourceFactory(),
                cacheDataSinkFactory,
                CacheDataSource.FLAG_BLOCK_ON_CACHE,
                null);

        mediaDataSourceFactory = new ProgressiveMediaSource.Factory(cacheDataSourceFactory);
    }

    private static ProgressiveMediaSource.Factory mediaDataSourceFactory;
    /*
     *   在创建source的时候,需要通过url创建MediaSource对象,通过mediaDataSourceFactory创建,此时会根据mediaDataSourceFactory传入的
     *   cacheDataSourceFactory查询本地文件cache中是否有资源,有则直接使用,否则通过dataSourceFactory去下载url对应的网络视频,下载到
     *   配置的cacheDir中
     * */
    public static MediaSource createMediaSource(String url){
        return mediaDataSourceFactory.createMediaSource(Uri.parse(url));
    }

    public static PageListPlay get(String pageName){
        PageListPlay pageListPlay = pageListPlayHashMap.get(pageName);
        if (pageListPlay ==null){
            pageListPlay = new PageListPlay();
            pageListPlayHashMap.put(pageName,pageListPlay);
        }
        return pageListPlay;
    }
    public static void release(String pageName){
        PageListPlay pageListPlay = pageListPlayHashMap.get(pageName);
        if (pageListPlay!=null){
            pageListPlay.release();
        }
    }
}

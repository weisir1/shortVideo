package com.example.libcommon.util;

import android.graphics.Bitmap;
import android.media.MediaDataSource;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    /*
     * 获取视频封面
     * */
    public static LiveData<String> generateVideoCover(final String filePath) {
        MutableLiveData<String> liveData = new MutableLiveData<>();
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//           扫描目标路径媒体
            retriever.setDataSource(filePath);
//           获取默认的第一个关键帧，
            Bitmap frame = retriever.getFrameAtTime();
            if (frame != null) {
//                获取的压缩图片写进文件中,并通知给livedata观察者图片的路径
                byte[] bytes = compressBitmap(frame, 200);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                try {
                    file.createNewFile();
                    FileOutputStream output = new FileOutputStream(file);
                    output.write(bytes);
                    output.flush();
                    output.close();
                    output = null;
                    liveData.postValue(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                liveData.postValue(null);
            }
        });
        return liveData;
    }

    private static byte[] compressBitmap(Bitmap frame, int limit) {
        if (frame!=null && limit>0)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            frame.compress(Bitmap.CompressFormat.JPEG,options,baos);
//            如果压缩后图片还是比较大 降低5后重新压缩 直到满意为止
            while(baos.toByteArray().length > limit*1024){
                baos.reset();
                options-=5;
                frame.compress(Bitmap.CompressFormat.JPEG,options,baos);
            }
           byte[] bytes = baos.toByteArray();
            if (baos!=null){
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                baos = null;
            }
            return bytes;
        }
        return null;
    }
}

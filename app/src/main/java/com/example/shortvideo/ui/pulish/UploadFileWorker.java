package com.example.shortvideo.ui.pulish;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.libcommon.util.FileUploadManager;

public class UploadFileWorker extends Worker {
    public UploadFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

//    doWork方法运行在子线程中
    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String filePath = inputData.getString("file");
        String fileURl = FileUploadManager.upload(filePath);
//        如果fileURl返回为空,表示上传失败了 否则成功
        if (TextUtils.isEmpty(fileURl)){
            return  Result.failure();
        }else{
            Data outputData = new Data.Builder().putString("fileUrl", fileURl)
                    .build();
            return  Result.success(outputData);
        }
    }
}

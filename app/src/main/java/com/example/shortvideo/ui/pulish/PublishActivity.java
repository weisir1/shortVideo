package com.example.shortvideo.ui.pulish;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.libcommon.dialog.LoadingDialog;
import com.example.libcommon.util.FileUtils;
import com.example.libnavannotation.ActivityDestination;
import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.libnetwork.JsonCallback;
import com.example.shortvideo.R;
import com.example.shortvideo.databinding.PublishFragmentBinding;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.model.TagList;
import com.example.shortvideo.ui.login.UserManager;
import com.example.shortvideo.ui.my.TagBottomSheetDialogFragment;
import com.example.shortvideo.utils.StatusBar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity implements View.OnClickListener {

    private PublishFragmentBinding binding;
    private int width, height;
    private String filePath;
    private boolean isVideo;
    private String coverPath;
    private UUID coverUUID;
    private UUID fileUploadUUID;
    private TagList tagList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.publish_fragment);
        binding.actionClose.setOnClickListener(this);
        binding.actionPublish.setOnClickListener(this);
        binding.actionDeleteFile.setOnClickListener(this);
        binding.actionAddTag.setOnClickListener(this);
        binding.actionAddFile.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_close:
                showExitDialog();
                break;
            case R.id.action_publish:
                publish();
                break;
            case R.id.action_delete_file:
                binding.actionAddFile.setVisibility(View.VISIBLE);
                binding.fileContainer.setVisibility(View.GONE);
                binding.cover.setImageDrawable(null);
                width = 0;
                height = 0;
                filePath = null;
                isVideo = false;
                break;
            case R.id.action_add_tag:
//                创建对话框
                TagBottomSheetDialogFragment fragment = new TagBottomSheetDialogFragment();
                fragment.setOnTagItemSelectedListener(item -> {
                    tagList = item;
                    binding.actionAddTag.setText(item.title);
                });
                fragment.show(getSupportFragmentManager(), "tag_dialog");
                break;
            case R.id.action_add_file:
                CaptureActivity.startActivityForResult(this);
                break;
        }
    }

    @SuppressLint("EnqueueWork")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void publish() {
        showLoadingDialog();
        List<OneTimeWorkRequest> workRequests = new ArrayList<>();
        if (!TextUtils.isEmpty(filePath)) {
            if (isVideo) {
                FileUtils.generateVideoCover(filePath).observe(this, coverPath -> {
                    OneTimeWorkRequest request = getOneTimeWorkeRequest(coverPath);
                    coverUUID = request.getId();
                    workRequests.add(request);
                    enqueue(workRequests);
                });
            }
            OneTimeWorkRequest request = getOneTimeWorkeRequest(filePath);
            fileUploadUUID = request.getId();
            workRequests.add(request);
//            如果不是视频类型的同样执行一次
            if (!isVideo) {
                enqueue(workRequests);
            }
        } else {
//            本次发布没有上传文件 直接发布
            publishFeed();

        }
    }

//    将视频或封面图上传到ali,并得到存储后的远程地址
    private void enqueue(List<OneTimeWorkRequest> workRequests) {
        WorkContinuation workContinuation = WorkManager.getInstance(PublishActivity.this).beginWith(workRequests);
//                    加入队列后,两个request将交由wokermanager队列来调度了
        workContinuation.enqueue();
//       任务执行后,回调给观察者执行情况
        workContinuation.getWorkInfosLiveData().observe(PublishActivity.this, workInfos -> {
//      block running enqueued failed susscess finish 状态变化都会回调到此方法
            int completedCount = 0;
            for (WorkInfo workInfo : workInfos) {
                WorkInfo.State state = workInfo.getState();
                Data outputData = workInfo.getOutputData();
                UUID uuId = workInfo.getId();
                if (state == WorkInfo.State.FAILED) {
                    if (uuId.equals(coverUUID)) {
                        showToast(getString(R.string.file_upload_failed));
                    } else if (uuId.equals(fileUploadUUID)) {
                        showToast(getString(R.string.file_upload_original_message));
                    }
                } else if (state == WorkInfo.State.SUCCEEDED) {
                    String fileUrl = outputData.getString("fileUrl");
                    if (uuId.equals(coverUUID)) {
                        coverPath = fileUrl;
                    } else if (uuId.equals(fileUploadUUID)) {
                        filePath = fileUrl;
                    }
//                    每完成一个任务 自增
                    completedCount++;
                }
                if (completedCount >= workInfos.size()) {
                    publishFeed();
                }
            }
        });
    }

    private void publishFeed() {
        ApiService.post("/feeds/publish")
                .addParam("coverUrl", coverPath)
                .addParam("fileUrl", filePath)
                .addParam("fileWidth", width)
                .addParam("fileHeight", height)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("tagId", tagList == null ? 0 : tagList.tagId)
                .addParam("tagTitle", tagList == null ? "" : tagList.title)
                .addParam("feedText", binding.inputView.getText().toString())
                .addParam("feedType", isVideo ? Feed.TYPE_VIDEO : Feed.TYPE_IMAGE_TEXT)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        showToast(getString(R.string.feed_publisj_success));
                        PublishActivity.this.finish();
                        dismissLoading();
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        dismissLoading();
                        showToast(response.message);
                    }
                });
    }

    private LoadingDialog loadingDialog = null;

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.setLoadingText(getString(R.string.feed_publish_ing));
        }
        loadingDialog.show();

    }

    private void dismissLoading() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
        } else {
            runOnUiThread(() -> {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }
            });
        }
    }

    private void showToast(String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            runOnUiThread(() -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)

    private OneTimeWorkRequest getOneTimeWorkeRequest(String coverPath) {
        Data inputData = new Data.Builder()
                .putString("file", coverPath)
                .build();

//        @SuppressLint("RestrictedApi")  Constraints constraints = new Constraints();
////                    设备存储空间充足的时候才能执行 >15%
//        constraints.setRequiresStorageNotLow(true);
////                    必须在指定的网络条件下才能执行 wifi
//        constraints.setRequiredNetworkType(NetworkType.UNMETERED);
////                    设备的充电量充足才能执行 >15%
//        constraints.setRequiresBatteryNotLow(true);
////                    只有在充电的情况下,才能允许执行
//        constraints.setRequiresCharging(true);
////                    只有在设备空闲的情况下才能执行, 比如息屏,CPU利用率不高
//        constraints.setRequiresDeviceIdle(true);
////                    workerManager利用contentObserver监控传递进来的这个uri对应的内容是否发生变化, 当且仅当它发生变化了
////                    我们的任务才会被触发
//        constraints.setContentUriTriggers(null);
////                   设置从content变化到被执行中间的延迟时间,如果在这期间,content发生了变化,延迟时间就会被重新计算
//        constraints.setTriggerContentUpdateDelay(0);
////                    设置从content变化到被执行中间的最大延迟时间
//        constraints.setTriggerMaxContentDelay(0);

//                    该request的唯一id

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UploadFileWorker.class)
                .setInputData(inputData)
//                .setConstraints(constraints)
//                           设置一个拦截器,在任务执行之前可以进行一次拦截, 去修改入参的数据然后返回新的数据交由worker使用
//                .setInputMerger(null)
////                            当一个任务被调度失败后,所要采取的策略,可以通过BackoffPolicy来执行具体的策略 任务失败后 每隔10s执行一次 随后成指数级增长
//                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
////                           设置被调度任务的执行时间
//                .setInitialDelay(10, TimeUnit.SECONDS)
////                            设置该任务尝试执行的最大次数
//                .setInitialRunAttemptCount(2)
////                            设置这个任务开始执行的时间
////                            System.currentTimeMillis()
//                .setPeriodStartTime(0, TimeUnit.SECONDS)
////                            设置该任务被调度的时间
//                .setScheduleRequestedAt(0, TimeUnit.SECONDS)
////                            当一个任务执行状态变成finish时,有没有其他观察者来消费这个结果,那么workerManager会在内存中
////                            保留一段时间的该任务结果。超过这个时间，这个结果就会被存储到数据库中,下次想要查询结果时,会触发workermnager的数据库查询
////                            操作,可以通过uuid来查询任务的状态
//                .keepResultsForAtLeast(10, TimeUnit.SECONDS)
                .build();
        return workRequest;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CaptureActivity.REQ_CODE && data != null) {
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);
            showFileThumbnail();
        }
    }

    private void showFileThumbnail() {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        binding.actionAddFile.setVisibility(View.GONE);
        binding.fileContainer.setVisibility(View.VISIBLE);
        binding.cover.setImageUrl(filePath);
        binding.videoIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);
//        点击封面预览
        binding.cover.setOnClickListener(v -> {
            PreviewActivity.startActivityForResult(PublishActivity.this, filePath, isVideo, null);
        });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.publish_exit_message))
                .setNegativeButton(getString(R.string.publish_exit_action_cancel), null)
                .setPositiveButton(getString(R.string.publish_exit_action_ok), (dialog, which) -> {
                    dialog.dismiss();
                    PublishActivity.this.finish();
                }).create().show();
    }
}
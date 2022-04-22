package com.example.shortvideo.ui.pulish;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.example.shortvideo.R;
import com.example.shortvideo.databinding.ActivityLayoutCaptureBinding;
import com.example.shortvideo.view.RecordView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

/*
 * 此类相当于重写了调用系统相机的过程,客户端调用相机时,会调用到自定义的相机这里,然后从跳转到预览界面来看拍摄的效果,根据结果决定是否满意,
 * 无论是否满意 回到此activity,在通过resultactivity返回给客户端
 * */
public class CaptureActivity extends AppCompatActivity {

    private ActivityLayoutCaptureBinding binding;
    private static final String[] PERMISSION = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private static final int PERMISSION_CODE = 1000;
    //    记录没有授予的权限
    private ArrayList<String> deniedPermission = new ArrayList<>();
    //    后置摄像头
    private CameraX.LensFacing lensFacing = CameraX.LensFacing.BACK;
    private int rotation = Surface.ROTATION_0;
    private Size resolution = new Size(1280, 720);
    private Rational rational = new Rational(9, 16);
    private Preview preview;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private boolean takingPicture;
    private String outputFilePath;
    public static final int REQ_CODE = 10001;

    public static final String RESULT_FILE_PATH = "file_path";
    public static final String RESULT_FILE_WIDTH = "file_width";
    public static final String RESULT_FILE_HEIGHT = "file_height";
    public static final String RESULT_FILE_TYPE = "file_type";

    //    详情页点击评论框右侧的拍摄图标 跳转, 在onActivityForResult中返回资源的相关属性
    public static void startActivityForResult(@NotNull Activity activity) {
        activity.startActivityForResult(new Intent(activity, CaptureActivity.class), REQ_CODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_layout_capture);
        ActivityCompat.requestPermissions(this, PERMISSION, PERMISSION_CODE);
        binding.recordView.setOnRecordListener(new RecordView.OnRecordListener() {
            @Override
            public void onClick() {
//判断是拍照还是视频
                takingPicture = true;
//                开始拍照  参数1拍照结果保存路径,参数2为拍照完成回调

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    //                    方法在子线程中回调
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        Log.i("WeiSir", "imageError: " + useCaseError.toString()+" ,message: "+message);
                        showErrorToast(message);
                    }
                });
            }


            @SuppressLint({"MissingPermission", "RestrictedApi"})
            @Override
            public void onLongClick() {//等下
                takingPicture = false;  //长按为视频
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".mp4");
                videoCapture.startRecording(file, new VideoCapture.OnVideoSavedListener() {
                    @Override
                    public void onVideoSaved(File file) {
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(VideoCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                        Log.i("WeiSir", "videoError: " + useCaseError.toString()+" ,message: "+message);
                        showErrorToast(message);
                    }
                });
            }

            /*            当长按时间超过最大时间或者手指抬起时 结束录制*/
            @SuppressLint({"MissingPermission", "RestrictedApi"})
            @Override
            public void onFinish() {
                videoCapture.stopRecording();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PreviewActivity.REUQ_CODE && resultCode == RESULT_OK) {
//            将视频或图片返回给评论界面
            Intent intent = new Intent();
            intent.putExtra(RESULT_FILE_PATH, outputFilePath);
            intent.putExtra(RESULT_FILE_HEIGHT, resolution.getWidth());
            intent.putExtra(RESULT_FILE_WIDTH, resolution.getHeight());
            intent.putExtra(RESULT_FILE_TYPE, !takingPicture);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @SuppressLint("RestrictedApi")
    private void showErrorToast(String useCaseError) {
        ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
            Toast.makeText(CaptureActivity.this, useCaseError.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    private void onFileSaved(File file) {
        outputFilePath = file.getAbsolutePath();
        String mimeType = takingPicture ? "image/jpeg" : "video/mp4";
//        拍摄的文件在下载目录下,使用此方法扫描目标文件(必须有准确的后缀)后,相册中会显示目标文件
        MediaScannerConnection.scanFile(this, new String[]{outputFilePath}, new String[]{mimeType}, null);

//        点击拍摄或录制结束 调转到预览界面
        PreviewActivity.startActivityForResult(this, outputFilePath, !takingPicture, "完成");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            deniedPermission.clear();
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
//                授权结果
                int result = grantResults[i];
//                将未授权权限记录
                if (result != PackageManager.PERMISSION_GRANTED) {
                    deniedPermission.add(permission);
                }
            }
            if (deniedPermission.isEmpty()) {
//                权限全部授予,初始化相机
                bindCameraX();  //请求权限 也就是刚开始时候就会先解绑在bind
            } else {
                //未授权列表不为空 代表有未授权权限
                new AlertDialog.Builder(this)
                        .setMessage("必须的权限没有得到,功能将无法使用\n请重新授权")
                        .setNegativeButton("不了,谢谢", (dialog, which) -> {
                            dialog.dismiss();
                            CaptureActivity.this.finish();
                        })
                        .setPositiveButton("好的", (dialog, which) -> {
//                            点击确认重新申请未授权的权限
                            String[] denied = new String[deniedPermission.size()];
                            ActivityCompat.requestPermissions(CaptureActivity.this, deniedPermission.toArray(denied), PERMISSION_CODE);
                        }).create().show();
            }

        }
    }

    @SuppressLint("RestrictedApi")
    private void bindCameraX() {
        //        解除掉之前与cameraX相关联的useCase
        CameraX.unbindAll();
//        查询当前是否存在可用的摄像头设备
        boolean hasAvailableCameraId = false;
        try {
            hasAvailableCameraId = CameraX.hasCameraWithLensFacing(lensFacing);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        if (!hasAvailableCameraId) {
            showErrorToast("无可用设备,请检查设备的相机是否被占用");
            finish();
            return;
        }

        String cameraIdForLensFacing = null;
        try {
            cameraIdForLensFacing = CameraX.getCameraWithLensFacing(lensFacing);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(cameraIdForLensFacing)) {
            showErrorToast("无可用设备cameraId,请检查设备相机是否被占用");
            finish();
            return;
        }

        PreviewConfig config = new PreviewConfig.Builder()
                .setLensFacing(lensFacing)
                .setTargetRotation(rotation)
                .setTargetResolution(resolution)
                .setTargetAspectRatio(rational)
                .build();
//        预览图配置
        preview = new Preview(config);
//      拍照配置
        imageCapture = new ImageCapture(new ImageCaptureConfig.Builder()
                .setTargetAspectRatio(rational)
                .setTargetResolution(resolution)
                .setTargetRotation(rotation)
                .setLensFacing(lensFacing).build()
        );
//        录制配置
        videoCapture = new VideoCapture(new VideoCaptureConfig.Builder()
                .setTargetRotation(rotation)
                .setLensFacing(lensFacing)
                .setTargetAspectRatio(rational)
                .setTargetResolution(resolution)
                .setVideoFrameRate(25)
                .setBitRate(3 * 1024 * 1024)
                .build());

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {

            private TextureView textureView;

            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                textureView = binding.textureView;
//                每次需要重新添加textureview才会进行渲染
                ViewGroup parent = (ViewGroup) textureView.getParent();
                parent.removeView(textureView);
                parent.addView(textureView, 0);

                textureView.setSurfaceTexture(output.getSurfaceTexture());
            }
        });
//        重新绑定
        CameraX.bindToLifecycle(this, preview, imageCapture, videoCapture);
    }

    @Override
    protected void onDestroy() {
        CameraX.unbindAll();
        super.onDestroy();
    }
}

package com.example.shortvideo.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.fragment.app.DialogFragment;

import com.alibaba.fastjson.JSONObject;
import com.example.libcommon.dialog.LoadingDialog;
import com.example.libcommon.global.AppGlobals;
import com.example.libcommon.util.FileUploadManager;
import com.example.libcommon.util.FileUtils;
import com.example.libcommon.util.PixUtils;
import com.example.libcommon.view.ViewHelper;
import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.libnetwork.JsonCallback;
import com.example.shortvideo.R;
import com.example.shortvideo.databinding.LayoutCommentDialogBinding;
import com.example.shortvideo.model.Comment;
import com.example.shortvideo.ui.login.UserManager;
import com.example.shortvideo.ui.pulish.CaptureActivity;

import org.w3c.dom.Text;

import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.TransformerFactory;

import jp.wasabeef.glide.transformations.internal.Utils;

public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {

    private LayoutCommentDialogBinding binding;
    private long itemId;
    private static final String KEY_ITEM_ID = "key_item_id";
    private commentAddListener listener;
    private int height;
    private int width;
    private boolean type;
    private String path;
    private LoadingDialog dialog;
    private String fileUrl;
    private String coverUrl;

    //    传递帖子id 用于标记为哪条帖子添加评论(详情页)
    public static CommentDialog newInstance(long itemId) {

        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog fragment = new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            //设置 window 的背景色为透明色.
            //如果通过 window 设置宽高时，想要设置宽为屏宽，就必须调用下面这行代码。
            window.setBackgroundDrawable(new ColorDrawable(0xff000000));
            WindowManager.LayoutParams attributes = window.getAttributes();
            //在这里我们可以设置 DialogFragment 弹窗的位置
            attributes.gravity = Gravity.BOTTOM;
            //我们可以在这里指定 window的宽高
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
            // 设置窗体的高度
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
            ;
//            window.getDecorView().setPadding(0, 0, 0, 0);
            //设置 DialogFragment 的进出动画
//            attributes.windowAnimations = R.style.DialogAnimation;
            window.setAttributes(attributes);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutCommentDialogBinding.inflate(inflater, container, false);
        binding.commentVideo.setOnClickListener(this);
        binding.commentDelete.setOnClickListener(this);
        binding.commentSend.setOnClickListener(this);
   /*     Window window = getDialog().getWindow();
        window.setWindowAnimations(0);
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);*/
        ViewHelper.setViewOutLine(binding.getRoot(), PixUtils.dp2px(10), ViewHelper.RADIUS_TOP);
        itemId = getArguments().getLong(KEY_ITEM_ID);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showSoftInputMethod(true);
    }

    public void showSoftInputMethod(boolean isShow) {
//        布局加载完成 但还未绘制完成 软键盘不会显示  显示软键盘的前提 必须可见 并且已获得焦点,否则不起作用
//        获取焦点
        getDialog().getWindow().getDecorView().postDelayed(
                () -> {
                    binding.inputView.setFocusable(isShow);
                    binding.inputView.setFocusableInTouchMode(isShow);
                    binding.inputView.requestFocus(); //这行必须有 否则不会弹出软键盘   软键盘的绘制尽可能的放到了后边,否则不显示
                    binding.inputView.setShowSoftInputOnFocus(true);
                    InputMethodManager manager = (InputMethodManager) AppGlobals.getsApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.showSoftInput(binding.inputView, 0);
                }, 50);  //通过延迟 来与父view的焦点错开 稍微慢一下 否则两者起冲突
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_send:
                publishComment();
                break;
            case R.id.comment_video:
                CaptureActivity.startActivityForResult(getActivity());
                break;
            case R.id.comment_delete:
                path = null;
                type = false;
                width = 0;
                height = 0;
                binding.commentCover.setImageDrawable(null);
                binding.commentExtLayout.setVisibility(View.GONE);
//                恢复可点击效果
                binding.commentVideo.setEnabled(true);
                binding.commentVideo.setAlpha(255);
                break;
        }
    }

    //    这里的结果回调是由viewHandler调用的 dialog带结果的跳转, 结果最终要回调到dialog所附属的activity中
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CODE && resultCode == Activity.RESULT_OK) {
            path = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);

            type = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            if (!TextUtils.isEmpty(path)) {
                binding.commentExtLayout.setVisibility(View.VISIBLE);
                binding.commentCover.setImageUrl(binding.commentCover, path, false);
                if (type) {
//                    如果是视频 显示小三角
                    binding.commentIconVideo.setVisibility(View.VISIBLE);
                }
            }
//            视频拍摄按钮不可点击
            binding.commentVideo.setEnabled(false);
            binding.commentVideo.setAlpha(50);
        }
    }

    private void publishComment() {
        if (TextUtils.isEmpty(binding.inputView.getText().toString())) {
            showToast("请输入内容");
            return;
        }
//      如果是视频,首先生成一张视频封面图,并且压缩后放入coverPath中, 将视频和封面图一同上传至云端服务器
        if (type && !TextUtils.isEmpty(path)) {
            FileUtils.generateVideoCover(path).observe(this, coverPath -> {
                uploadFile(coverPath, path);
            });
        } else if (!TextUtils.isEmpty(path)) {  //如果上传的文件不是视频类型
            uploadFile(null, path);
        } else {
            publish();
        }

    }

    private void showLoadingDialog() {
        if (dialog == null) {
            dialog = new LoadingDialog(getContext());
            dialog.setLoadingText(getString(R.string.upload_text));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }
        dialog.show();
    }

    private void dismissLoadingDialog() {
        if (dialog != null) {  //取消loadingdialog显示
            dialog.dismiss();
        }
    }

    //    可能会有多个文件需要上传
    private void uploadFile(String coverPath, String filePath) {
        showLoadingDialog();
        //AtomicInteger, CountDownLatch, CyclicBarrier
//        设为1 是因为已经有一个视频文件
        AtomicInteger count = new AtomicInteger(1);
        if (!TextUtils.isEmpty(coverPath)) {
            count.set(2);
            ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
                int remain = count.decrementAndGet();
                coverUrl = FileUploadManager.upload(coverPath);
                if (remain <= 0) {
                    if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(coverUrl)) {
                        publish();
                    } else {
                        dismissLoadingDialog();
                        showToast(getString(R.string.file_upload_failed));
                    }
                }
            });
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
//            使用阿里云提供的文件上传类 将视频文件上传

//            文件上传一次 count-1
            int remain = count.decrementAndGet();
//            当文件上传成功后,会返回一个fileUrl
//            并且 fileUrl不能为null 因为其代表着拍摄的视频或图片,如果此路径为空,根本的资源都没有 谈不上什么上传了
            fileUrl = FileUploadManager.upload(filePath);
            if (remain <= 0) {   //所有文件都上传成功
//                fileUrl为null意味着原始视频不存在,没有上传的意义,其次 如果coverpath有值,但是上传返回的url为null表示上传失败,否则代表上传成功
                if (!TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(coverUrl)) {
                    publish();
                } else {
                    dismissLoadingDialog();
                    showToast(getString(R.string.file_upload_failed));
                }
            }
        });
    }

    private void showToast(String s) {
        //showToast几个可能会出现在异步线程调用
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(AppGlobals.getsApplication(), s, Toast.LENGTH_SHORT).show();
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(() -> Toast.makeText(AppGlobals.getsApplication(), s, Toast.LENGTH_SHORT).show());
        }
    }

    //  向服务器发送评论信息
    private void publish() {
        String commentText = binding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", itemId)
                .addParam("commentText", commentText)
                .addParam("image_url", type ? coverUrl : fileUrl)
                .addParam("video_url", fileUrl)
                .addParam("width", width)
                .addParam("height", height)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        showToast("评论失败");
                        dismissAllowingStateLoss();
                    }
                });
    }

    private void onCommentSuccess(Comment body) {
        showToast("评论发送成功");
        if (listener != null) {
// 当添加评论的网络请求成功回调后,通过此监听来回调到界面,通知界面要添加的评论(此评论为根据用户输入框输入后提交到服务器后返回的)
            listener.onAddComment(body);
            dismiss();
        }
    }

    public interface commentAddListener {
        void onAddComment(Comment comment);
    }

    public void setCommentAddListener(commentAddListener listener) {
        this.listener = listener;
    }
}

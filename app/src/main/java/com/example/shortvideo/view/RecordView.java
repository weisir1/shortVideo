package com.example.shortvideo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.libcommon.util.PixUtils;
import com.example.shortvideo.R;

public class RecordView extends View implements View.OnClickListener, View.OnLongClickListener {

    private static int PROGRESS_INTERVAL = 100;
    private int radius;
    private int progressWidth;
    private int progressColor;
    private int fillColor;
    private int duration;
    private int progressMaxValue;
    private Paint filePaint;
    private Paint progressPaint;
    private boolean isRecording;
    private float progressValue;
    private long startRecordTime;
    private OnRecordListener listener;

    public RecordView(Context context) {
        this(context, null);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView, defStyleAttr, defStyleRes);
        radius = typedArray.getDimensionPixelOffset(R.styleable.RecordView_radius, 0);
        progressWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordView_progress_width, PixUtils.dp2px(3));
        progressColor = typedArray.getColor(R.styleable.RecordView_progress_color, Color.RED);
        fillColor = typedArray.getColor(R.styleable.RecordView_fill_color, Color.WHITE);
        duration = typedArray.getInteger(R.styleable.RecordView_duration, 10);
        setMaxDuration(duration);
        typedArray.recycle();

        filePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        filePaint.setColor(fillColor);
        filePaint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                progressValue++;
                postInvalidate();
//                如果小于进度条最大值 ,不断刷新
                if (progressValue <= progressMaxValue) {
                    sendEmptyMessageDelayed(0, PROGRESS_INTERVAL);
                } else { //超过最大值, 结束录制
                    finishRecord();
                }
            }


        };


        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
//                    点击按下状态 圆环会跟着开始绘制
                    case MotionEvent.ACTION_DOWN:
                        isRecording = true;
                        startRecordTime = System.currentTimeMillis();
                        handler.sendEmptyMessage(0);
                        break;

                    case MotionEvent.ACTION_UP:
                        long now = System.currentTimeMillis();
//                        点击时间超过长按时间时,认为是长按时间 , 抬起事件,结束录制
                        if (now - startRecordTime > ViewConfiguration.getLongPressTimeout()){
                            finishRecord();
                        }
//                        回收消息,以复用 参数为null 表示删除所有的回调函数和message
                        handler.removeCallbacksAndMessages(null);
//                        松手后初始化数据
                        isRecording = false;
                        startRecordTime = 0;
                        progressValue = 0;
                        postInvalidate();
                        break;
                }
                return false;
            }
        });
        setOnClickListener(this);
        setOnLongClickListener(this);
    }
//    结束录制
    private void finishRecord() {
        if (listener!=null){
            listener.onFinish();
        }
    }
    private void setMaxDuration(int maxDuration) {
//        maxDuration为总时间,即在多久完成动画,  PROGRESS_INTERVAL为每隔多少ms更新一次  progressMaxValue每次更新最大值
        this.progressMaxValue = maxDuration * 1000 / PROGRESS_INTERVAL;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

//        视频是否处于播放状态
        if (isRecording) {
            canvas.drawCircle(width / 2, height / 2, width / 2, filePaint);
            int left = 0;
            int top = 0;
            int right = width;
            int bottom = height;
//此处的progressValue应该是手指按下时值不断变化
            float sweepAngle = (progressValue / progressMaxValue) * 360;
            canvas.drawArc(left, top, right, bottom, -90, sweepAngle, false, progressPaint);
        } else {
//            否则只绘制圆圈
            canvas.drawCircle(width / 2, height / 2, radius, filePaint);
        }
    }

    /*
    * 设置录制监听回调
    * */
    public void setOnRecordListener(OnRecordListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        listener.onClick();
    }

    @Override
    public boolean onLongClick(View v) {
        listener.onLongClick();
        return true;
    }

    public interface OnRecordListener{
        void onClick();
        void onLongClick();
        void onFinish();
    }
}

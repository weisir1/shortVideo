package com.example.libcommon.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.libcommon.R;

import org.w3c.dom.Text;

public class LoadingDialog extends AlertDialog {

    private TextView loadingText;

    public LoadingDialog(Context context) {
        super(context);
    }

    public LoadingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setLoadingText(String loadingText) {
//       在commentDialog中show方法在此方法之后调用 所以view还未初始化
        if (this.loadingText != null) {
            this.loadingText.setText(loadingText);
        }
    }

    @Override
    public void show() {
        super.show();
//        一定要写到show()后面,否则不会带来理想效果
        setContentView(R.layout.layout_loading_view);
        loadingText = findViewById(R.id.loading_text);

        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.gravity = Gravity.CENTER;
        attributes.dimAmount = 0.35f;  //窗口透明程度
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));  //如果不设置背景透明,会有边框
        window.setAttributes(attributes);
    }
}

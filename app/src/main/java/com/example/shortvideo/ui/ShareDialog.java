package com.example.shortvideo.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.libcommon.util.PixUtils;
import com.example.libcommon.view.RoundFrameLayout;
import com.example.libcommon.view.ViewHelper;
import com.example.shortvideo.R;

import java.util.ArrayList;
import java.util.List;

public class ShareDialog extends AlertDialog {

    private List<ResolveInfo> shareItems = new ArrayList<>();
    private RoundFrameLayout frameLayout;
    private ShareAdapter shareAdapter;
    private String shareCount;
    private View.OnClickListener listener;

    public ShareDialog(@NonNull Context context) {
        super(context);
    }

    protected ShareDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        frameLayout = new RoundFrameLayout(getContext());
        frameLayout.setViewOutline(PixUtils.dp2px(20), ViewHelper.RADIUS_TOP);
        frameLayout.setBackgroundColor(Color.WHITE);
        GridView gridView = new GridView(getContext());
        gridView.setNumColumns(4);
        gridView.setHorizontalSpacing(PixUtils.dp2px(4));
        gridView.setVerticalSpacing(PixUtils.dp2px(20));
//        RecyclerView recyclerView = new RecyclerView(getContext());
//        GridLayoutManager layout = new GridLayoutManager(getContext(), 4);
//        recyclerView.setLayoutManager(layout);
        shareAdapter = new ShareAdapter();
        gridView.setAdapter(shareAdapter);

//        recyclerView.setAdapter(shareAdapter);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = PixUtils.dp2px(20);
        params.bottomMargin = params.topMargin = params.leftMargin = params.rightMargin = margin;
        params.gravity = Gravity.CENTER;
        frameLayout.addView(gridView, params);
        int height = getWindow().getWindowManager().getDefaultDisplay().getHeight() / 3;
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        setContentView(frameLayout, layoutParams);

        if (getWindow() != null) {
            getWindow().setGravity(Gravity.BOTTOM);
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //设置透明背景 否则会有缝隙
        }
        queryShareItems();
    }

    // 查找当前设备的微信和qq包名对象
    private void queryShareItems() {
        Intent intent = new Intent();
        /*
         * Android系统为我们提供了分享功能，只需要向startActivity传递一个Action为ACTION_SEND的Intent,系统会自动弹出一个应用程序列表，
         * 可以理解为从当前Activity跨越进程，发送数据到另一个Activity， 我们只需要指定数据以及类型，接受方会自动识别，并启动相应的Activity
         * */
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
//        查询其他应用程序中是否声明有ACTION_SEND意图
        List<ResolveInfo> resolveInfos = getContext().getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos
        ) {
            String packageName = resolveInfo.activityInfo.packageName;
//            if (TextUtils.equals(packageName, "com.tencent.mm") || TextUtils.equals(packageName, "com.tencent.mobileqq")) {
            shareItems.add(resolveInfo);
//            }
        }
        shareAdapter.notifyDataSetChanged();
    }


    public void setShareItemClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public void setShareCount(String shareCount) {
        this.shareCount = shareCount;
    }


    private class ShareAdapter extends BaseAdapter {

        private final PackageManager packageManager;
        private View inflate = null;

        public ShareAdapter() {
            packageManager = getContext().getPackageManager();
        }

        @Override
        public int getCount() {
            return shareItems == null ? 0 : shareItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            inflate = LayoutInflater.from(getContext()).inflate(R.layout.layout_share_item, parent, false);
            ResolveInfo resolveInfo = shareItems.get(position);
            ImageView imageView = inflate.findViewById(R.id.share_icon);
            TextView shareText = inflate.findViewById(R.id.share_text);
            Drawable drawable = resolveInfo.loadIcon(packageManager);
            imageView.setImageDrawable(drawable);
            shareText.setText(resolveInfo.loadLabel(packageManager));

            inflate.setOnClickListener(view -> {
                String pcn = resolveInfo.activityInfo.packageName;
                String name = resolveInfo.activityInfo.name;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setComponent(new ComponentName(pcn, name));
                intent.putExtra(Intent.EXTRA_TEXT, shareCount);
//startActivity(Intent.createChooser(shareIntent, "对话框标题"));   也可以通过内置的分享dialog框 优点:如果没有匹配到对应程序,
//会提示,同时可以指定dialog标题
                getContext().startActivity(intent);
                if (listener != null) {
                    listener.onClick(view);
                }
                dismiss();
            });
            return inflate;
        }
    }
}


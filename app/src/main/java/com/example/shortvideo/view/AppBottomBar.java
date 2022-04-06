package com.example.shortvideo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.shortvideo.R;
import com.example.shortvideo.model.BottomBar;
import com.example.shortvideo.model.Destination;
import com.example.shortvideo.utils.AppConfig;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import java.util.List;

public class AppBottomBar extends BottomNavigationView {
    private static int[] sIcons = new int[]{R.drawable.icon_tab_home, R.drawable.icon_tab_sofa, R.drawable.icon_tab_publish, R.drawable.icon_tab_find, R.drawable.icon_tab_mine};

    @SuppressLint("RestrictedApi")
    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        BottomBar bottomBar = AppConfig.getsBottomBar();
        List<BottomBar.Tabs> tabs = bottomBar.getTabs();
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};
        //第一个为按钮选中颜色 第二个为按钮常规颜色
        int[] colors = new int[]{Color.parseColor("#FD6C96"), Color.parseColor(bottomBar.getInActiveColor())};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        setItemIconTintList(colorStateList);
        setItemTextColor(colorStateList);
        //有四种模式,用于决定底部导航是否显示文本 这里使用全部显示
        //LABEL_VISIBILITY_LABELED:设置按钮的文本为一直显示模式
        //LABEL_VISIBILITY_AUTO:当按钮个数小于三个时一直显示，或者当按钮个数大于3个且小于5个时，被选中的那个按钮文本才会显示
        //LABEL_VISIBILITY_SELECTED：只有被选中的那个按钮的文本才会显示
        //LABEL_VISIBILITY_UNLABELED:所有的按钮文本都不显示
        setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        //设置默认选中按钮
        setSelectedItemId(bottomBar.getSelectTab());

        for (int i = 0; i < tabs.size(); i++) {

            BottomBar.Tabs tab = tabs.get(i);
            if (!tab.isEnable()) {
                continue;
            }
            //将页面id绑定到bottomBar按钮上
            int id = getId(tab.getPageUrl());
            if (id < 0) {
                continue;
            }
            //向menu中添加tab,因为添加过程中需要对按钮进行排序,就会将原来按钮移除掉然后对其排序才能将按钮加入对应的位置中
            MenuItem item = getMenu().add(0, id, tab.getIndex(), tab.getTitle());
            item.setIcon(sIcons[tab.getIndex()]);
        }
//        所以如果想设置tab大小,需要等所有按钮都添加到底部导航栏后才可以
        for (int i = 0; i < tabs.size(); i++) {
            BottomBar.Tabs tab = tabs.get(i);
            int iconSize = dp2px(tab.getSize());
            //bottomNavigation没有专门暴露相关api去设置按钮大小
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) getChildAt(0);
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(tab.getIndex());
            itemView.setIconSize(iconSize);
            if (TextUtils.isEmpty(tab.getTitle())) {   //只有中间的按钮没有title(+图标)
                itemView.setIconTintList(ColorStateList.valueOf(Color.parseColor(tab.getTintColor())));
                //点击按钮时不要上下浮动效果
                itemView.setShifting(false);
            }
        }
    }

    private int dp2px(int size) {

        float value = Resources.getSystem().getDisplayMetrics().density * size + 0.5f;

        return (int) value;
    }

    private int getId(String pageUrl) {
        //在destnation.json中返回不同的页面,通过json解析到map中,key为pageRrl,最终获取id
        Destination destination = AppConfig.getsDestConfig().get(pageUrl);
        if (destination == null) return -1;

        return destination.getId();
    }
}

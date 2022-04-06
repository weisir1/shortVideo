package com.example.shortvideo.utils;

import android.content.ComponentName;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;

import com.example.libcommon.global.AppGlobals;
import com.example.shortvideo.FixFragmentNavigator;
import com.example.shortvideo.model.Destination;

import java.util.HashMap;

public class NavGraphBuilder {
    public static void build(NavController controller, FragmentActivity activity, int containerId) {
        //provider中含有一个hashmap用于存储navigator类型对象和其对应的类名字
        NavigatorProvider provider = controller.getNavigatorProvider();
        //最后通过getNavigator()获取
//        FragmentNavigator fragmentNavigator = provider.getNavigator(FragmentNavigator.class);
        //使用自定义的的navigator  因为默认的navigator在切换页面时使用的replace,导致fragment生命周期的不断重启
        FixFragmentNavigator fragmentNavigator = new FixFragmentNavigator(activity, activity.getSupportFragmentManager(), containerId);
       //添加到provider中,这样除了navigation框架内置的四个导航器外,自己内置了一个导航器
        provider.addNavigator(fragmentNavigator);


        ActivityNavigator activityNavigator = provider.getNavigator(ActivityNavigator.class);
        //navGraph是类似资源文件的fragment集
        NavGraph navGraph = new NavGraph(new NavGraphNavigator(provider));
        //获取带注解的fragment或activity属性 最后加入navGraph中
        HashMap<String, Destination> destConfig = AppConfig.getsDestConfig();
        for (Destination value : destConfig.values()) {
            if (value.isIsFragment()) {
                FragmentNavigator.Destination destination = fragmentNavigator.createDestination();
                destination.setClassName(value.getClazName());
                destination.setId(value.getId());
                destination.addDeepLink(value.getPageUrl());
                navGraph.addDestination(destination);
            } else {
                ActivityNavigator.Destination destination = activityNavigator.createDestination();
                destination.setId(value.getId());
                destination.addDeepLink(value.getPageUrl());
                destination.setComponentName(new ComponentName(AppGlobals.getsApplication().getPackageName(), value.getClazName()));
                navGraph.addDestination(destination);
            }
            if (value.isAsStarter()) {
                navGraph.setStartDestination(value.getId());
            }
        }
        controller.setGraph(navGraph);

    }
}

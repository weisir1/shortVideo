package com.example.shortvideo.ui.find;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.libnavannotation.FragmentDestination;
import com.example.shortvideo.R;
import com.example.shortvideo.model.SofaTab;
import com.example.shortvideo.ui.sofa.SofaFragment;
import com.example.shortvideo.utils.AppConfig;

@FragmentDestination(pageUrl = "main/tabs/find", asStarter = false)
public class FindFragment extends SofaFragment {
    @Override
    protected Fragment getTabFragment(int position) {
//        获取对应位置的列表类型 1为已关注列表 2为推荐列表
        TagListFragment fragment = TagListFragment.newInstance(getTabConfig().getTabs().get(position).getTag());
        return fragment;
    }

//    当子fragment被添加到当前fragment中时回调此方法
    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        String tagType = childFragment.getArguments().getString(TagListFragment.KEY_TAG_TYPE);
//        绑定的子fragment是TagListFragment 即关注tab的Fragment "onlyFollow"
        if (TextUtils.equals(tagType,"onlyFollow")){
//            给添加的子Fragment绑定的livedata注册一个更改监听
            ViewModelProviders.of(childFragment).get(TagListViewModel.class)
                    .getSwitchTabLiveData().observe(this,o -> {
                        viewPager.setCurrentItem(1);
            });
        }
    }

    @Override
    protected SofaTab getTabConfig() {
        return AppConfig.getFindTagConfig();
    }
}
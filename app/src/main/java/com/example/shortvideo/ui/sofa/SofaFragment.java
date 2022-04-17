package com.example.shortvideo.ui.sofa;

import androidx.lifecycle.ViewModelProvider;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.libnavannotation.FragmentDestination;
import com.example.shortvideo.R;
import com.example.shortvideo.databinding.SofaFragmentBinding;
import com.example.shortvideo.model.SofaTab;
import com.example.shortvideo.ui.home.HomeFragment;
import com.example.shortvideo.utils.AppConfig;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FragmentDestination(pageUrl = "main/tabs/sofa", asStarter = false)
public class SofaFragment extends Fragment {
    private SofaFragmentBinding binding;
    private TabLayout tabLayout;
    protected ViewPager2 viewPager;
    private SofaTab tabConfig;
    private List<SofaTab.Tabs> tabs;
    private Map<Integer, Fragment> fragmentMap = new HashMap<>();
    private TabLayoutMediator mediator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SofaFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        tabLayout = binding.tabLayout;
        viewPager = binding.viewPager;
//        根据getTabConfig()加载assets中的json文件tab显示到tablayout上

//        在findFragment中也是通过此方法 但是调用的为findFragment复写的
        tabConfig = getTabConfig();
        tabs = new ArrayList<>();
        for (SofaTab.Tabs tab :    //只将可点击的ta加入列表
                tabConfig.getTabs()) {
            if (tab.isEnable()) {
                tabs.add(tab);
            }
        }
//        关闭预加载
        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        viewPager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), this.getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Fragment fragment = fragmentMap.get(position);
                if (fragment == null) {
                    fragment = getTabFragment(position);
                }
                return fragment;
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }
        });
// 若要绑定,则不能在attach之前进行,因为此类的会将当前所有的tab移除后重新创建绑定fragment的tab,在新建tab的同时会调用onConfigureTab,此方法正处在移除后
//        新建tab的过程中,再次之前的原tab将不复存在
        mediator = new TabLayoutMediator(tabLayout, viewPager, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setCustomView(makeTabView(position));
            }
        });
        mediator.attach();
//      页面选中回调
        viewPager.registerOnPageChangeCallback(pageChangeCallback);
//      tab与viewpager绑定需要时间,应该等到绑定成功后在进行选择
        viewPager.post(() -> viewPager.setCurrentItem(tabConfig.getSelect()));
    }

    ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            int tabCount = tabLayout.getTabCount();
            for (int i = 0; i < tabCount; i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                TextView customView = (TextView) tab.getCustomView();
                if (tab.getPosition() == position) {
                    customView.setTextSize(tabConfig.getActiveSize());
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    customView.setTextSize(tabConfig.getNormalSize());
                    customView.setTypeface(Typeface.DEFAULT);
                }
            }
        }

    };

    private View makeTabView(int position) {
        TextView tabView = new TextView(getContext());
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};
        int[] colors = new int[]{Color.parseColor(tabConfig.getActiveColor()), Color.parseColor(tabConfig.getNormalColor())};
        ColorStateList colorStateList = new ColorStateList(states, colors);   //返回状态向颜色的映射
        tabView.setTextColor(colorStateList);
        tabView.setGravity(Gravity.CENTER);
        tabView.setText(tabs.get(position).getTitle());
        tabView.setTextSize(tabConfig.getNormalSize());
        return tabView;
    }

    protected Fragment getTabFragment(int position) {
        return HomeFragment.newInstance(tabs.get(position).getTag());
    }

    protected  SofaTab getTabConfig() {
        return AppConfig.getSofaTabConfig();
    }


//     ?悬疑
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment.isAdded() && fragment.isVisible()){
                fragment.onHiddenChanged(hidden);
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        mediator.detach();
        viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        super.onDestroy();
    }
}
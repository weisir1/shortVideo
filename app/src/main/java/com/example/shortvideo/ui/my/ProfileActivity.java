package com.example.shortvideo.ui.my;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.shortvideo.R;
import com.example.shortvideo.databinding.ActivityProfileBinding;
import com.example.shortvideo.model.User;
import com.example.shortvideo.ui.login.UserManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    public static final String TAB_TYPE_ALL = "tab_all";
    public static final String TAB_TYPE_FEED = "tab_feed";
    public static final String TAB_TYPE_COMMENT = "tab_comment";
    public static final String KEY_TAB_TYPE = "key_tab_type";

    public static void startProfileActivity(Context context, String tabType) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(KEY_TAB_TYPE, tabType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        User user = UserManager.get().getUser();
        binding.setUser(user);
        binding.actionBack.setOnClickListener(v -> {
            finish();
        });
        String[] tabs = getResources().getStringArray(R.array.profile_tabs);
        ViewPager2 viewPager = binding.viewPager;
        TabLayout tabLayout = binding.tabLayout;

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return ProfileListFragment.newInstance(getTabTypeByPosistion(position));
            }

            private String getTabTypeByPosistion(int position) {
                switch (position) {
                    case 0:
                        return TAB_TYPE_ALL;
                    case 1:
                        return TAB_TYPE_FEED;
                    case 2:
                        return TAB_TYPE_COMMENT;
                }
                return TAB_TYPE_ALL;
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        //        autoRefresh当我们调用viewpager的adapter#notifychanged方法的时候,要不要主动的吧tabLayout选项卡移除掉 重新配置
        new TabLayoutMediator(tabLayout, viewPager, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
//                实现tab与viewpager的联动
                tab.setText(tabs[position]);
            }
        }).attach();
        int initTabPosition = getInitTabPosition();
        if (initTabPosition != 0) {
//            等待viewpager页面加载完成后切换页面
            viewPager.post(() -> {
                viewPager.setCurrentItem(initTabPosition);
            });
        }

        binding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                ppBarLayout.getTotalScrollRange()表示bar的最大可滑动距离 一旦超过将会折叠
                boolean expand = Math.abs(verticalOffset) < appBarLayout.getTotalScrollRange();
                binding.setExpand(expand);
            }
        });
    }

//    根据点击跳转的类型 进行tab索引
    private int getInitTabPosition() {
        String initTab = getIntent().getStringExtra(KEY_TAB_TYPE);
        switch (initTab) {
            case TAB_TYPE_ALL:
                return 0;
            case TAB_TYPE_FEED:
                return 1;
            case TAB_TYPE_COMMENT:
                return 2;
            default:
                return 0;
        }
    }
}
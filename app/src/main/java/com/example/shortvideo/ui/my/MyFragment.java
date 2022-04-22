package com.example.shortvideo.ui.my;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.libnavannotation.FragmentDestination;
import com.example.shortvideo.R;
import com.example.shortvideo.databinding.FragmentDashboardBinding;
import com.example.shortvideo.model.User;
import com.example.shortvideo.ui.login.UserManager;
import com.example.shortvideo.utils.StatusBar;

@FragmentDestination(pageUrl = "main/tabs/my", asStarter = false, needLogin = true)
public class MyFragment extends Fragment implements View.OnClickListener {

    private MyViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = UserManager.get().getUser();
        binding.setUser(user);
//        更新用户登陆信息
        UserManager.get().refresh().observe(getViewLifecycleOwner(), user1 -> {
            binding.setUser(user1);
        });
        binding.actionLayout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.fragment_my_logout))
                    .setPositiveButton(getString(R.string.fragment_my_logout_ok), (dialog, which) -> {
                        dialog.dismiss();
                        UserManager.get().logout();
                        getActivity().onBackPressed();
                    })
                    .setNegativeButton(getString(R.string.fragment_my_logout_cancel), null).create().show();
        });
        binding.goDetail.setOnClickListener(this);
        binding.userFeed.setOnClickListener(this);
        binding.userComment.setOnClickListener(this);
        binding.userFavorite.setOnClickListener(this);
        binding.userHistory.setOnClickListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBar.lightStatusBar(getActivity(), false);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
//        当myfragment变得可见时状态栏的颜色就会变为黑体白字 不可见时变为白体黑字
        StatusBar.lightStatusBar(getActivity(), hidden);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_detail:
                ProfileActivity.startProfileActivity(getContext(),ProfileActivity.TAB_TYPE_ALL);
                break;
            case R.id.user_feed:
                ProfileActivity.startProfileActivity(getContext(),ProfileActivity.TAB_TYPE_FEED);
                break;
            case R.id.user_comment:
                ProfileActivity.startProfileActivity(getContext(),ProfileActivity.TAB_TYPE_COMMENT);
                break;
            case R.id.user_favorite:
                ProfileActivity.startProfileActivity(getContext(),ProfileActivity.TAB_TYPE_ALL);
                break;
            case R.id.user_history:
                ProfileActivity.startProfileActivity(getContext(),ProfileActivity.TAB_TYPE_ALL);
                break;
        }
    }
}
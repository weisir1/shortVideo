package com.example.shortvideo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.textclassifier.TextLanguage;

import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.libnetwork.GetRequest;
import com.example.libnetwork.JsonCallback;
import com.example.libnetwork.JsonConvert;
import com.example.libnetwork.Request;
import com.example.shortvideo.model.Destination;
import com.example.shortvideo.model.User;
import com.example.shortvideo.ui.login.UserManager;
import com.example.shortvideo.utils.AppConfig;
import com.example.shortvideo.utils.NavGraphBuilder;
import com.example.shortvideo.utils.StatusBar;
import com.example.shortvideo.view.AppBottomBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private AppBottomBar navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);    //启动activity后加载页面之前要改回,否则会始终有张图片
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = NavHostFragment.findNavController(fragment);

        NavGraphBuilder.build(navController, this, fragment.getId());
        navView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        HashMap<String, Destination> hashMap = AppConfig.getsDestConfig();
        Iterator<Map.Entry<String, Destination>> iterator = hashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Destination> next = iterator.next();
            Destination value = next.getValue();
            // 当所有条件匹配 ---点击的当前页面需要登陆且当前状态未登录,匹配成功 则只有当登陆成功以后才能索引到当前这个正常页面,否则跳转到loginActivity去登陆
            if (value != null && !UserManager.get().isLogin() && value.isNeedLogin() && value.getId() == item.getItemId()) {
//                跳转到登陆界面，登陆成功后回调此接口，进行页面跳转
                UserManager.get().login(MainActivity.this).observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        if (user != null) {
                            UserManager.get().getUserLiveData().removeObservers(MainActivity.this);
                            navView.setSelectedItemId(item.getItemId());
                        }
                    }
                });
                return false;
            }
        }

        //因为按钮item的id已经与页面绑定,所以可以直接使用item的id进行导航跳转
        navController.navigate(item.getItemId());
        //返回true代表这个按钮是被选中的,会有上下浮动效果,反之没有 又因为底部导航按钮只有中间是一个图标,没有title,所以中间的自然没有选中效果
        return !TextUtils.isEmpty(item.getTitle());
    }

    @Override
    public void onBackPressed() {
//  当前正在显示的destinationId
        int currentDestinationId = navController.getCurrentDestination().getId();

//        获取导航首页的destinationId
        int homeDesId = navController.getGraph().getStartDestination();

//        如果当前显示的页面不在首页,返回按钮直接切回到首页
        if (currentDestinationId != homeDesId) {
            navView.setSelectedItemId(homeDesId);
            return;
        }

// 否则直接finish, 如果回调super.onBackPressed()会逐次回退栈内的fragment,
        finish();
    }
}
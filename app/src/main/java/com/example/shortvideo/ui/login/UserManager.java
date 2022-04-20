package com.example.shortvideo.ui.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.libcommon.global.AppGlobals;
import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.libnetwork.JsonCallback;
import com.example.libnetwork.cache.CacheManager;
import com.example.shortvideo.model.User;

public class UserManager {
    private static final String KEY_CACHE_USER = "cache_user";
    private static UserManager mUserManager = new UserManager();
    //    因为登陆有可能发生在主页面或个人页面,使用MutableLiveData可以在登陆保存之后迅速回调给登陆方 MutableLiveData因为实现了观察者模式且与生命周期绑定
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private User user;

    public static UserManager get() {
        return mUserManager;
    }

    //    设置单例模式
    private UserManager() {
        User cache = (User) CacheManager.getCache(KEY_CACHE_USER);
        if (cache != null && cache.expires_time < System.currentTimeMillis()) {
            this.user = cache;
        }
    }

    public void logout() {
        CacheManager.delete(KEY_CACHE_USER, user);
        userLiveData.postValue(null);  //  ??? 少一行 死机了 监听者发生了什么
        user = null;
    }

    public void save(User user) {
        if (user != null) {
            this.user = user;
            CacheManager.save(KEY_CACHE_USER, user);
            if (userLiveData.hasObservers()) {
                userLiveData.postValue(user);
            }
        } else {
            if (userLiveData.hasObservers()) {
                userLiveData.postValue(null);
            }
        }
    }

    public MutableLiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<User> login(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
//        因为standard模式的activity默认会进入启动他的任务栈,但是非activity的context并没有任务栈,所以需要为他创建一个新的任务栈
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return userLiveData;
    }

    public boolean isLogin() {
        return user == null ? false : user.expires_time > System.currentTimeMillis();
    }

    public User getUser() {
        return isLogin() ? user : null;
    }

    public long getUserId() {
        return isLogin() ? user.userId : 0;
    }

    public LiveData<User> refresh() {
        if (!isLogin()) {
            return login(AppGlobals.getsApplication());
        }
        MutableLiveData<User> liveData = new MutableLiveData<>();

        ApiService.get("/user/query")
                .addParam("userId", getUserId())
                .execute(new JsonCallback<User>() {
                             @Override
                             public void onSuccess(ApiResponse<User> response) {
                                 save(response.body);
                                 liveData.postValue(getUser());
                             }

                             @SuppressLint("RestrictedApi")
                             @Override
                             public void onError(ApiResponse<User> response) {
                                 ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                                     Toast.makeText(AppGlobals.getsApplication(), response.message, Toast.LENGTH_SHORT).show();
                                 });
                                 liveData.postValue(null);
                             }
                         }

                );
        return liveData;
    }
}

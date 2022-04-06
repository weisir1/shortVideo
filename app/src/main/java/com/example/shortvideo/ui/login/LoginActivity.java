package com.example.shortvideo.ui.login;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.libnetwork.JsonCallback;
import com.example.shortvideo.R;
import com.example.shortvideo.model.User;
import com.google.android.material.button.MaterialButton;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView action_close;
    private MaterialButton action_login;
    public Tencent tencent;
    private MutableLiveData<Boolean> loginMessage = new MutableLiveData<>(false);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_login);
        initView();
//        注册登陆监听 ,当登陆成功后 通知界面finish当前登陆界面
        loginMessage.observe(this, aBoolean -> {
            if (aBoolean) {
                finish();
            }
        });
    }

    private void initView() {
        action_close = (ImageView) findViewById(R.id.action_close);
        action_login = (MaterialButton) findViewById(R.id.action_login);
        action_login.setOnClickListener(this);
        action_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_login:
                login();
                break;
            case R.id.action_close:
                finish();
                break;
        }
    }

    private void login() {
        if (tencent == null) {
            tencent = Tencent.createInstance("102001200", getApplicationContext());
        }
//        设置用户授权
        Tencent.setIsPermissionGranted(true, Build.MODEL);
        tencent.login(this, "all", iUiListener);
    }

    IUiListener iUiListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            JSONObject response = (JSONObject) o;
            try {
                String openid = response.getString("openid");
                String access_token = response.getString("access_token");
                String expires_in = response.getString("expires_in");
                String expires_time = response.getString("expires_time");
                tencent.setAccessToken(access_token, expires_in);
                tencent.setOpenId(openid);
                QQToken qqToken = tencent.getQQToken();
//                登陆成功后 获取用户信息
                getUserInfo(qqToken, openid, expires_time);
            } catch (JSONException e) {
                Log.i("WeiSir", "getToken erorr: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError uiError) {
            Log.i("WeiSir", "onError: " + uiError.errorMessage);
            Toast.makeText(LoginActivity.this, "登陆失败" + uiError.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Log.i("WeiSir", "onCancel: ");
            Toast.makeText(LoginActivity.this, "登陆取消", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onWarning(int i) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        必须要在登陆后重新result方法,否则接口不会进行回调
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, iUiListener);
            /*if (resultCode == Constants.REQUEST_LOGIN){
                Tencent.handleResultData(data,iUiListener);
            }*/
        }
    }

    private void getUserInfo(QQToken qqToken, String openid, String expires_time) {
        UserInfo userInfo = new UserInfo(getApplicationContext(), qqToken);
        userInfo.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object o) {
                JSONObject response = (JSONObject) o;
                try {
                    String nickname = response.getString("nickname");
                    String figureurl_2 = response.getString("figureurl_2");
                    save(nickname, figureurl_2, expires_time, openid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {
                Toast.makeText(LoginActivity.this, "登陆失败" + uiError.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "登陆取消", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWarning(int i) {

            }
        });
    }

    private void save(String nickname, String avatar, String expires_time, String openid) {
        ApiService.get("/user/insert")
                .addParam("name", nickname)
                .addParam("avatar", avatar)
                .addParam("qqOpenId", openid)
                .addParam("expires_time", expires_time)
                .execute(new JsonCallback<User>() {
                    @Override
                    public void onSuccess(ApiResponse<User> response) {
                        if (response.body != null) {
                            UserManager.get().save(response.body);
                            loginMessage.postValue(true);
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onError(ApiResponse<User> response) {
                        Toast.makeText(LoginActivity.this, "登陆失败,msg:" + response.message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

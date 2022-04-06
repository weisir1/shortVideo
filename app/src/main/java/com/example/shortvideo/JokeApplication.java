package com.example.shortvideo;

import android.app.Application;

import com.example.libnetwork.ApiService;

public class JokeApplication  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiService.init("http://47.94.38.41:8080/serverdemo",null);
    }
}

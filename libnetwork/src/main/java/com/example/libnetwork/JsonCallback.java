package com.example.libnetwork;

public abstract class JsonCallback<T> {
    public  void onSuccess(ApiResponse<T> response) {};

    public  void onError(ApiResponse<T> response) {};
    //如果发送请求 命中缓存,会回调次方法获取本地缓存内容
    public  void onCacheSuccess(ApiResponse<T> response){} ;
}

package com.example.libnetwork;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.example.libnetwork.cache.CacheManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

// Cloneable 是用来克隆对象的 当既想要获取缓存又要网络时 需要两个不同的策略变量的request
// 就可以用clone()方法了 不需要写长穿的builder代码了 修改策略变量 ,
public abstract class Request<T, R extends Request> implements Cloneable {
    public String mUrl;
    public HashMap<String, String> header = new HashMap<>();
    public HashMap<String, Object> params = new HashMap<>();

    //只访问缓存 不发情网络请求
    public static final int CACHE_ONLY = 1;
    //先访问缓存 同时发起网络请求 成功后缓存到本地
    public static final int CACHE_FIRST = 2;
    //仅仅是访问服务器,不存储
    public static final int NET_ONLY = 3;
    //先访问网络 成功后缓存到本地
    public static final int NET_CACHE = 4;
    private String cacheKey;
    private Type type;
    private Class claz;
    private int cacheStrategy = NET_ONLY;

    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_ONLY, NET_CACHE})
    public @interface CacheStrategy {

    }

    public Request(String url) {
        mUrl = url;
    }

    public R addHeader(String key, String value) {
        header.put(key, value);
        return (R) this;
    }

    public R addParam(String key, Object value) {
        if (value == null) {
            return (R) this;
        }

        //在传入的value中需要进行约束 即只能是8中基本数据类型
        //在每一个基本数据类型封装类中都有一个type,通过反射获取
        try {
            if (value.getClass() == String.class) {
                params.put(key, value);
            } else {
                Field field = value.getClass().getField("TYPE");   //获取value的对象中type信息 判断是否是8种数据类型
                Class claz = (Class) field.get(null);
                if (claz.isPrimitive()) {   //判断是否为8种数据类型
                    params.put(key, value);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) this;
    }

    public R cacheKey(String key) {
        this.cacheKey = key;
        return (R) this;
    }

    @SuppressLint("RestrictedApi")
    public void execute(JsonCallback<T> callback) {
        //首先尝试获取缓存
        if (cacheStrategy != NET_ONLY) {
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ApiResponse<T> response = readCache();
                    if (callback != null && response.body != null) {
                        callback.onCacheSuccess(response);
                    }
                }
            });
        }
        if (cacheStrategy != CACHE_ONLY) {
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ApiResponse<T> response = new ApiResponse<>();
                    response.message = e.getMessage();
                    callback.onError(response);
                }


                //网络层的成功回调
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    ApiResponse<T> apiResponse = parseResponse(response, callback);  //业务层判断成功
                    if (apiResponse.success) {
                        callback.onSuccess(apiResponse);
                    } else {
                        callback.onError(apiResponse);
                    }
                }
            });
        }
    }

    public ApiResponse<T> readCache() {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> result = new ApiResponse<>();
        result.status = 304;
        result.message = "缓存获取成功";
        result.body = (T) cache;
        result.success = true;
        return result;
    }

    private ApiResponse<T> parseResponse(Response response, JsonCallback<T> callback) {
        String message = null;
        int status = response.code();
        boolean isSuccess = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Convert convert = ApiService.sConvert;
        try {
            String content = response.body().string();
            if (isSuccess) {
                if (callback != null) {
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];   //获取客户端实际callback泛型类型,并将其转换
                    result.body = (T) convert.convert(content, argument);
                } else if (type != null) {
                    result.body = (T) convert.convert(content, type);
                } else if (claz != null) {
                    result.body = (T) convert.convert(content, claz);
                } else {
                    Log.e("WeiSir", "parseResponse:无法解析 ");
                }
            } else {
                message = content;
            }
        } catch (Exception e) {
            message = e.getMessage();
            isSuccess = false;
            status = 0;
        }
        result.success = isSuccess;
        result.status = status;
        result.message = message;

        if (cacheStrategy != NET_ONLY && result.success && result.body != null && result.body instanceof Serializable) {
            saveCache(result.body);
        }
        return result;
    }

    private void saveCache(T body) {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        CacheManager.save(key, body);
    }

    private String generateCacheKey() {
        cacheKey = UrlCreator.createUrlFromParams(mUrl, params);
        return cacheKey;
    }

    public R responseType(Type type) {
        this.type = type;
        return (R) this;
    }

    public R responseType(Class claz) {
        this.claz = claz;
        return (R) this;
    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        okhttp3.Request request = generateRequest(builder);
        Call call = ApiService.okHttpClient.newCall(request);
        return call;
    }

    public R cacheStrategy(@CacheStrategy int cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
        return (R) this;
    }

    public abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);


    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    public ApiResponse<T> execute() {
        ApiResponse<T> result = null;
        try {
            if (cacheStrategy == CACHE_ONLY) {
                return readCache();
            }
            Response response = getCall().execute();
            result = parseResponse(response, null);
            return result;
        } catch (IOException e) {
            result =new ApiResponse<>();
            result.message = e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

    @NonNull
    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request<T, R>) super.clone();
    }
}

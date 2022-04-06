package com.example.libnetwork;

import java.util.Map;

import okhttp3.FormBody;

public class PostRequest<T> extends Request<T,PostRequest> {
    public PostRequest(String url) {
        super(url);
    }

    @Override
    public okhttp3.Request generateRequest(okhttp3.Request.Builder builder) {
        FormBody.Builder bodyBuild = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            bodyBuild.add(entry.getKey(),String.valueOf(entry.getValue()));
        }
        okhttp3.Request request = builder.url(mUrl).post(bodyBuild.build()).build();
        return request;
    }
}

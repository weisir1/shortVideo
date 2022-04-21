package com.example.shortvideo.ui.my;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.shortvideo.AbsViewModel;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ProfileViewModel extends AbsViewModel<Feed> {
    private String profileType;

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    private class DataSource extends ItemKeyedDataSource<Long, Feed> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Feed> callback) {
            loadData(params.requestedInitialKey, callback);
        }

        private void loadData(Long key, LoadInitialCallback<Feed> callback) {
            ApiResponse<List<Feed>> response = ApiService.get("/feed/queryProfileFeeds")
                    .addParam("inId", key)
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("pageCount", 10)
                    .addParam("profileType", profileType)
                    .responseType(new TypeReference<ArrayList<Feed>>() {
                    }.getType())
                    .execute();
            List<Feed> result = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(result);
            if (key>0){
//                告知ui层本次分页是否有更多数据成功返回,同时方便ui层关闭动画
                ((MutableLiveData)getBoundaryPageData()).postValue(result.size() > 0);
            }
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Feed> callback) {

        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Feed> callback) {

        }

        @NonNull
        @Override
        public Long getKey(@NonNull Feed item) {
            return null;
        }
    }
}

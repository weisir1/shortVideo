package com.example.shortvideo.ui.my;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PageKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.shortvideo.AbsViewModel;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActViewModel extends AbsViewModel<Feed> {
    private int behavior;

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setBehavior(int behavior) {
        this.behavior = behavior;
    }

    private class DataSource extends ItemKeyedDataSource<Integer, Feed> {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
//            loadData(params.requestedInitialKey, callback);
            loadData(0, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        private void loadData(int feedId, @NonNull LoadCallback<Feed> callback) {
            ApiResponse<List<Feed>> response = ApiService.get("/feeds/queryUserBehaviorList")
                    .addParam("behavior", behavior)
                    .addParam("feedId", feedId)
                    .addParam("pageCount", 10)
                    .addParam("userId", UserManager.get().getUserId())
                    .responseType(new TypeReference<ArrayList<Feed>>() {
                    }.getType())
                    .execute();
            List<Feed> result = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(result);
            if (feedId > 0) {
                ((MutableLiveData) getBoundaryPageData()).postValue(result.size() > 0);
            }
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    }

}

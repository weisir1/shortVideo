package com.example.shortvideo.ui;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MutablePageKeyedDataSource<Key, Value> extends PageKeyedDataSource {
    public List<Value> data = new ArrayList<>();

    public PagedList<Value> buildNewPagedList(PagedList.Config config){
        PagedList<Value> pagedList = new PagedList.Builder<Key, Value>(this, config)
                .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
                .build();
        return  pagedList;
    }
    @Override
    public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback callback) {
//        只有在与pagelist绑定时 会执行一次,返回的为相应的数据   到loadBefore()时返回空集合,表示分页功能就失效
        callback.onResult(data,null,null);
    }

    @Override
    public void loadBefore(@NonNull LoadParams params, @NonNull LoadCallback callback) {
        callback.onResult(Collections.emptyList(), null);
    }

    @Override
    public void loadAfter(@NonNull LoadParams params, @NonNull LoadCallback callback) {
        callback.onResult(Collections.emptyList(), null);
    }
}

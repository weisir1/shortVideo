package com.example.shortvideo.ui;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MutableItemKeyedDataSource<Key, Value> extends ItemKeyedDataSource<Key, Value> {
    private ItemKeyedDataSource dataSource;

    public List<Value> data = new ArrayList<>();

    public PagedList<Value> buildNewPagedList(PagedList.Config config) {
        PagedList<Value> pagedList = new PagedList.Builder<Key, Value>(this, config)
                .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
                .build();
        return pagedList;
    }

    public MutableItemKeyedDataSource(ItemKeyedDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Key> params, @NonNull LoadInitialCallback<Value> callback) {
        callback.onResult(data);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Value> callback) {
        if (dataSource!=null){
//            一旦使用新的DataSource替换原始对象后, 就得DataSource将不会在调用,
//            我们需要继续使用就得分页功能， 可以用代理方式来手动调用旧的分页功能
            dataSource.loadAfter(params, callback);
        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Value> callback) {
        callback.onResult(Collections.emptyList());
    }

    @NonNull
    @Override
    public abstract Key getKey(@NonNull Value item);
}

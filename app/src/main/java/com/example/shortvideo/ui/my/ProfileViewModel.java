package com.example.shortvideo.ui.my;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.example.shortvideo.AbsViewModel;
import com.example.shortvideo.model.Feed;

public class ProfileViewModel extends AbsViewModel<Feed> {
    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }
    private class DataSource extends ItemKeyedDataSource<Long,Feed>{

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Feed> callback) {

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

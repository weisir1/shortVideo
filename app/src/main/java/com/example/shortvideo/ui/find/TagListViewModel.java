package com.example.shortvideo.ui.find;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.shortvideo.AbsViewModel;
import com.example.shortvideo.model.TagList;
import com.example.shortvideo.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TagListViewModel extends AbsViewModel<TagList> {
    private String tagType;
    private int offset;
    private AtomicBoolean loadAfater = new AtomicBoolean();
    private MutableLiveData switchTabLiveData = new MutableLiveData();
    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }
    public MutableLiveData getSwitchTabLiveData(){
        return switchTabLiveData;
    }

    private class DataSource extends ItemKeyedDataSource<Long, TagList> {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<TagList> callback) {
            loadData(0L, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Long getKey(@NonNull TagList item) {
            return item.tagId;
        }


        private void loadData(Long requestKey, LoadCallback<TagList> callback) {
            if (requestKey > 0){
                loadAfater.set(true);
            }
            ApiResponse<List<TagList>> response = ApiService.get("/tag/queryTagList")
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("tagId", requestKey)
                    .addParam("tagType", tagType)
                    .addParam("pageCount", 10)
                    .addParam("offset", offset)
                    .responseType(new TypeReference<ArrayList<TagList>>(){}.getType())
                    .execute();

            List<TagList> result = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(result);
            if (requestKey > 0) {
//                分页结束后设为false
                loadAfater.set(false);
//                如果值大于0代表为分页加载 累加数量
                offset += result.size();
//                分页中还要将本次数据传递出去,方便ui层结束上拉加载,一些动画,一些提示信息等 相当于通知消息的作用
                ((MutableLiveData)getBoundaryPageData()).postValue(result.size() > 0);
            } else {
//                否则记录当前页大小
                offset = result.size();
            }
        }

    }
    public void loadData(long tagId, ItemKeyedDataSource.LoadCallback callback){
        if (tagId <= 0|| loadAfater.get()){
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
            ((TagListViewModel.DataSource)getDataSource()).loadData(tagId,callback);
        });
    }
}

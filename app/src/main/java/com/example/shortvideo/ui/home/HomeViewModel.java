package com.example.shortvideo.ui.home;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.alibaba.fastjson.TypeReference;
import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.libnetwork.JsonCallback;
import com.example.libnetwork.Request;
import com.example.shortvideo.AbsViewModel;
import com.example.shortvideo.model.Feed;
import com.example.shortvideo.ui.MutablePageKeyedDataSource;
import com.example.shortvideo.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AbsViewModel<Feed> {
    private volatile boolean witchCache = true;  //缓存标记 默认使用缓存

    //    防止paging与手动上拉操作重复导致重复发送请求
    private AtomicBoolean loadAfter = new AtomicBoolean(false);
    private String feedType;

    public MutableLiveData<PagedList<Feed>> getCacheLiveData() {
        return cacheLiveData;
    }

    private MutableLiveData<PagedList<Feed>> cacheLiveData = new MutableLiveData<>();

    @Override
    public DataSource createDataSource() {
        return new FeedDateSource();
    }

    public void setFeedType(String feedType){
        this.feedType = feedType;
    }
    /*
    * 数据源抽象类，Paging有三种实现

        (1)PageKeyedDataSource 按页加载，如请求数据时传入page页码。

        (2)ItemKeyedDataSource 按条目加载，即请求数据需要传入其它item的信息，如加载第n+1项的数据需传入第n项的id。

        (3)PositionalDataSource 按位置加载，如加载指定从第n条到n+20条。
    * */
//    根据条目来进行查询, loadAfter会帮你返回当前页最后一条数据,这样在进行网络请求时不需要手动计算最后一条数据的位置,而直接使用现有的进行访问
    class FeedDateSource extends ItemKeyedDataSource<Integer, Feed> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
//            params.requestedInitialKey  下一页的起始key
//            加载初始化数据
            loadData(0, callback);
            witchCache = false;
        }

        //下一页数据
        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            //       记载分页数据的最后一条

            loadData(params.key, callback);
        }

        //       可以向前加载数据
        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            //这里应该直接修改了liveData数据了 ,那么在absListFragment中的observe()方法中应该可以收到回调
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    };

    private void loadData(int key, ItemKeyedDataSource.LoadCallback<Feed> callback) {
//        从这里到livedata.postvalue()为一次请求
        if (key > 0) {
            loadAfter.set(true);
        }
        //由于paging框架在回调上面方法时切换到至线程 所以网络请求方法可以直接用子线程
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", feedType)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("feedId", key)
                .addParam("pageCount", 10)
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType());

        if (witchCache) {    //至于为什么判断witchCache后又会进行网络请求, 原因在于,首先加载页面时会显示默认的本地缓存,以免显示空布局
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    MutablePageKeyedDataSource<Object, Object> source = new MutablePageKeyedDataSource<>();
                    source.data.addAll(response.body);
                    PagedList pagedList = source.buildNewPagedList(config);
                    cacheLiveData.postValue(pagedList);
                }
            });
        }
        try {
            Request netRequest = witchCache ? request.clone() : request;
            netRequest.cacheStrategy(key == 0 ? Request.NET_CACHE : Request.NET_ONLY);
            ApiResponse<List<Feed>> response = netRequest.execute();
            List<Feed> data = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(data);
            if (key > 0) {   //key大于0表示为上拉加载 无论请求是否有数据,发送请求以后都应该将上拉动画关闭
//                    通过liveData发送数据,告诉UI层 是否应该主动关闭上拉加载分页的动画
                getBoundaryPageData().postValue(data.size() > 0);
                loadAfter.set(false);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void loadAfter(int id, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }

        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
            loadData(id, callback);
        });
    }

}
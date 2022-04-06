package com.example.shortvideo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public abstract class AbsViewModel<T> extends ViewModel {

    private MutableLiveData<Boolean> boundaryPageData = new MutableLiveData<>();
    private DataSource dataSource;
    private final LiveData<PagedList<T>> liveData;
    protected PagedList.Config config;

    public AbsViewModel() {
        config = new PagedList.Config.Builder()
                .setPageSize(10)   //每次分页时需要加载的数量
                .setInitialLoadSizeHint(12)  //第一次加载数据时的数量 因为每页是10 多加是为了加载下一页时不会直接进行加载 而是当滑到最后两个时加载
//                .setMaxSize()  //总共加载多少数据
//                 .setEnablePlaceholders()   //将还未加载的数据用占位符替代
//                .setPrefetchDistance()    //距离屏幕底部还有多少item时加载下一页
                .build();

        liveData = new LivePagedListBuilder(factory, config)
                .setInitialLoadKey(0)    //加载初始化数据的时候需要传入的参数
//                .setFetchExecutor()  在加载时执行的异步线程池
                .setBoundaryCallback(callback).build();

    }

    DataSource.Factory factory = new DataSource.Factory() {

        @NonNull
        @Override
        public DataSource create() {
            if (dataSource == null || dataSource.isInvalid()) {
                dataSource = createDataSource();
            }
            return dataSource;
        }
    };


    public LiveData<PagedList<T>> getLiveData() {
        return liveData;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public MutableLiveData<Boolean> getBoundaryPageData() {
        return boundaryPageData;
    }


    //    刷新动画回调
    PagedList.BoundaryCallback callback = new PagedList.BoundaryCallback<T>() {
        @Override
        public void onZeroItemsLoaded() {  //当需要加载数据为0  可以用于判断是否显示空布局
            boundaryPageData.postValue(false);
        }

        //新提交的PagedList中第一条数据被加载到列表上
        @Override
        public void onItemAtFrontLoaded(@NonNull T itemAtFront) {
            boundaryPageData.postValue(true);
        }

        //        数据加载完毕
        @Override
        public void onItemAtEndLoaded(@NonNull T itemAtEnd) {
            super.onItemAtEndLoaded(itemAtEnd);
        }
    };


    public abstract DataSource createDataSource();

}

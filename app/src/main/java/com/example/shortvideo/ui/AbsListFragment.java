package com.example.shortvideo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libcommon.view.EmptyView;
import com.example.shortvideo.AbsViewModel;
import com.example.shortvideo.R;
import com.example.shortvideo.databinding.LayoutRefreshViewBinding;
import com.example.shortvideo.exoplayer.PageListPlayDetector;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbsListFragment<T, M extends AbsViewModel<T>> extends Fragment implements OnRefreshListener, OnLoadMoreListener {

    private LayoutRefreshViewBinding binding;
    public RecyclerView recyclerView;
    private SmartRefreshLayout smartRefreshLayout;
    private EmptyView emptyView;
    public PagedListAdapter<T, RecyclerView.ViewHolder> adapter;

    //    不同的子类通过继承AbsListFragment后将子类的viewModel类型通过泛型第二个参数传递 这样在父类中就可以解析使用了
    protected M mViewModel;
    public PageListPlayDetector detector;

 /*   @Override
    protected void initViewModel() {
        genericViewModel();
    }

    @Override
    protected DataBindingConfig getDataBindingConfig() {
        return null;
    }
*/
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutRefreshViewBinding.inflate(inflater, container, false);
        recyclerView = binding.recyclerView;
        smartRefreshLayout = binding.refreshLayout;
        emptyView = binding.emptyView;

        adapter = getAdapter();
        recyclerView.setAdapter(adapter);

        smartRefreshLayout.setEnableRefresh(true);
        smartRefreshLayout.setEnableLoadMore(true);
        smartRefreshLayout.setOnRefreshListener(this);
        smartRefreshLayout.setOnLoadMoreListener(this);

//      给每一个item一个10dp空隙
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(null);
        genericViewModel();
        afterCreateView();
        return binding.getRoot();
    }

    protected abstract void afterCreateView();


    private void genericViewModel() {
        //获取父类的参数化类型(泛型)
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] arguments = type.getActualTypeArguments();  //获取泛型参数
        if (arguments.length > 1) {
//            第二个泛型参数为AbsViewModel
            Type argument = arguments[1];
            Class modeClaz = ((Class) argument).asSubclass(AbsViewModel.class);
//            从ViewModelProviders中获取子类第二个泛型参数类型的viewModel对象
            mViewModel = (M) ViewModelProviders.of(this).get(modeClaz);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //            设置data数据观察，当数据有变化时回调方法
        mViewModel.getLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<T>>() {
            @Override
            public void onChanged(PagedList<T> pagedList) {  //liveData数据修改后的回调
                submitList(pagedList);
            }
        });
//          在callback中有下拉与上拉边界回调监听，当没有数据或加载第一个数据时,更新getBoundaryPageData(boolean值)
//           若有数据 追加到适配器上,若没有数据 关闭上下拉动画 并且显示空view界面

        detector = new PageListPlayDetector(this,binding.recyclerView);
//            通过监听boundaryPageData的数据变化控制空布局的显隐藏
        mViewModel.getBoundaryPageData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hasData) {
                finishRefresh(hasData);
            }
        });
//        detector = new PageListPlayDetector(this, ((LayoutRefreshViewBinding) getBinding()).recyclerView);

    }

    public void submitList(PagedList<T> pagedList) {
        if (pagedList.size() > 0) {
            //将本次刷新的结果变更到下拉列表中
            adapter.submitList(pagedList);    //追加到适配器上
        }
        finishRefresh(pagedList.size() > 0);

    }

    //        当有数据更新成功后,关闭刷新组件
    public void finishRefresh(boolean hasData) {
        //适配器当前所有值
        PagedList<T> currentList = adapter.getCurrentList();
        //判断当前是否有值
        hasData = hasData || currentList != null && currentList.size() > 0;
        RefreshState state = smartRefreshLayout.getState();  //下拉刷新状态值
        if (state.isFooter && state.isOpening) {   //如果当前状态是上拉 结束上拉操作
            smartRefreshLayout.finishLoadMore();
        } else if (state.isHeader && state.isOpening) {  //如果是下拉,并且正在进行 ,结束下拉刷新
            smartRefreshLayout.finishRefresh();
        }
        if (hasData) {
            emptyView.setVisibility(View.GONE);
        } else
            emptyView.setVisibility(View.VISIBLE);
    }

    // 为了要实现分页加载效果 返回值必须是PagedListAdapter的子类
    public abstract PagedListAdapter<T, RecyclerView.ViewHolder> getAdapter();

}

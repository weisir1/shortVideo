package com.example.shortvideo.ui.find;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.example.shortvideo.R;
import com.example.shortvideo.model.TagList;
import com.example.shortvideo.ui.AbsListFragment;
import com.example.shortvideo.ui.MutableItemKeyedDataSource;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import java.util.List;

//TagListFragment会被创建两次 但是两次的type不相同
public class TagListFragment extends AbsListFragment<TagList, TagListViewModel> {
    public static final String KEY_TAG_TYPE = "tag_type";
    private String tagType;

//    根据不同的type 判断请求为已关注列表还是未关注列表
    public static TagListFragment newInstance(String tagType) {
        Bundle bundle = new Bundle();
        TagListFragment fragment = new TagListFragment();
        bundle.putString(KEY_TAG_TYPE, tagType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        如果是已关注界面 直接显示没有任何关注?
        if (TextUtils.equals(tagType,"onlyFollow")){
            emptyView.setTile(getString(R.string.tag_list_no_follow));
            emptyView.setButton(getString(R.string.tag_list_no_follow_button),v -> {
                mViewModel.getSwitchTabLiveData().setValue(new Object());
            });
        }
        mViewModel.setTagType(tagType);

    }

    @Override
    protected void afterCreateView() {
//        删除最后一个item间隙效果
        recyclerView.removeItemDecorationAt(recyclerView.getItemDecorationCount() -1);
    }

    @Override
    public PagedListAdapter getAdapter() {
        tagType = getArguments().getString(KEY_TAG_TYPE);
        TagListAdapter tagListAdapter = new TagListAdapter(getContext());
        return tagListAdapter;
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<TagList> currentList = getAdapter().getCurrentList();
        long tagId = currentList == null ? 0 : currentList.get(currentList.size() - 1).tagId;
        mViewModel.loadData(tagId, new ItemKeyedDataSource.LoadCallback() {
            @Override
            public void onResult(@NonNull List data) {
                if (data!=null&& data.size() > 0){
                    MutableItemKeyedDataSource<Long, TagList> mutableItemKeyedDataSource = new MutableItemKeyedDataSource<Long, TagList>((ItemKeyedDataSource) mViewModel.getDataSource()) {
                        @NonNull
                        @Override
                        public Long getKey(@NonNull TagList item) {
                            return item.tagId;
                        }
                    };
                    mutableItemKeyedDataSource.data.addAll(currentList);
                    mutableItemKeyedDataSource.data.addAll(data);
                    PagedList<TagList> pageList = mutableItemKeyedDataSource.buildNewPagedList(currentList.getConfig());
                    if (data.size() > 0) {
                        submitList(pageList);
                    }
                }else{
                    finishRefresh(false);
                }

            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }
}

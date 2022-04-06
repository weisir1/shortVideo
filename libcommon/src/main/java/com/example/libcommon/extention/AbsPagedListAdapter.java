package com.example.libcommon.extention;

import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbsPagedListAdapter<T, VH extends RecyclerView.ViewHolder> extends PagedListAdapter<T, VH> {
    private SparseArray<View> header = new SparseArray<>();
    private SparseArray<View> footer = new SparseArray<>();
    private int BASE_ITEM_TYPE_HEADER = 10000;
    private int BASE_ITEM_TYPE_FOOTER = 20000;

    protected AbsPagedListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public void addHeaderView(View view) {
        if (header.indexOfValue(view) < 0) {
            header.put(BASE_ITEM_TYPE_HEADER++, view);
            notifyDataSetChanged();
        }
    }

    public void removeHeaderView(View view) {
        int index = header.indexOfValue(view);
        if (index < 0) {
            return;
        }
        header.removeAt(index);
        notifyDataSetChanged();
    }

    public void addFooterView(View view) {
        if (footer.indexOfValue(view) < 0) {
            footer.put(BASE_ITEM_TYPE_FOOTER++, view);
            notifyDataSetChanged();
        }
    }

    public void removeFooterView(View view) {
        int index = footer.indexOfValue(view);
        if (index < 0) return;
        footer.removeAt(index);
        notifyDataSetChanged();
    }

    /*
     *
     * 因为此处有三个view
     * headerView在列表第一个
     * itemView在中间,有多个
     * footerView为最后一个
     *
     * 明白这一点,下面的count计算就理解了
     * */
    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        int count = itemCount + header.size() + footer.size();
        return count;
    }


    public int getOriginalItemCount() {
        return getItemCount() - header.size() - footer.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
//            返回该position对应的headerView的 viewType
            return header.keyAt(position);
        }
//        因为footerView元素在列表中虽然是最后一个,但是在footer集合中却是第一个,所以要减去headerView和普通itemView才能与footerView在集合中真正的位置对应
        if (isFooterPosition(position)) {
            position = position - getOriginalItemCount() - header.size();
            return footer.keyAt(position);
        }
//        如果既不是headerVIwe也不是footerVIew那就是普通itemVIew
//        同理 剪掉headerView数量
        position = position - header.size();
        return getItemViewType2(position);
    }

    protected int getItemViewType2(int position) {
        return 0;
    }

    private boolean isFooterPosition(int position) {   //大于header的为footer
        return position >= getOriginalItemCount() + header.size();
    }

    private boolean isHeaderPosition(int position) {
//        如果当前的position小于header表示为header类型
        return position < header.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //根据viewtype加载不同的view 如果既不是header也不是footer 普通view将交由子类去实现
        if (header.indexOfKey(viewType) >= 0) {
            View view = header.get(viewType);
            return (VH) new RecyclerView.ViewHolder(view) {
            };
        }
        if (footer.indexOfKey(viewType) >= 0) {
            View view = footer.get(viewType);
            return (VH) new RecyclerView.ViewHolder(view) {
            };
        }
        return onCreateViewHolder2(parent, viewType);
    }

    protected abstract VH onCreateViewHolder2(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
//        拦截头和尾
        if (isFooterPosition(position) || isHeaderPosition(position)) {
            return;
        }
        position = position - header.size();
        onBindViewHolder2(holder, position);
    }

    protected abstract void onBindViewHolder2(VH holder, int position);

    //    在使用pageList时内部只存储普通view的数据,并没有header和footer,所以会导致position不对应,
//    因此写一个代理类对数据更改回调各方法监听,加上header
    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(new AdapterDataObserverProxy(observer));
    }

    private class AdapterDataObserverProxy extends RecyclerView.AdapterDataObserver {


        private RecyclerView.AdapterDataObserver observer;

        public AdapterDataObserverProxy(RecyclerView.AdapterDataObserver observer) {
            this.observer = observer;
        }

        @Override
        public void onChanged() {
            observer.onChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            observer.onItemRangeChanged(positionStart + header.size(), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            observer.onItemRangeChanged(positionStart + header.size(), itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            observer.onItemRangeInserted(positionStart + header.size(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            observer.onItemRangeRemoved(positionStart + header.size(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            observer.onItemRangeMoved(fromPosition + header.size(), toPosition + header.size(), itemCount);
        }
    }
}

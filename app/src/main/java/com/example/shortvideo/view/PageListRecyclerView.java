package com.example.shortvideo.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class PageListRecyclerView extends RecyclerView {
    private Adapter adapter;

    public PageListRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(Adapter adapter){
        this.adapter = adapter;
    }
}

package com.example.shortvideo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.example.shortvideo.view.WindowInsetsFrameLayout;

public class WindowInsetsNavHostFragment extends NavHostFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        WindowInsetsFrameLayout insetsFrameLayout = new WindowInsetsFrameLayout(inflater.getContext());
        insetsFrameLayout.setId(getId());
        return  insetsFrameLayout;
    }
}

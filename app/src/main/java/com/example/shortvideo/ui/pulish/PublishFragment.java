package com.example.shortvideo.ui.pulish;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.libnavannotation.FragmentDestination;
import com.example.shortvideo.R;
@FragmentDestination(pageUrl ="main/tabs/publish",asStarter = false,needLogin = true)
public class PublishFragment extends Fragment {

    private PublishViewModel mViewModel;

    public static PublishFragment newInstance() {
        return new PublishFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.publish_fragment, container, false);
    }
}
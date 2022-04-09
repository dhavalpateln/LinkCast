package com.dhavalpateln.linkcast.ui.discover.ui.suggested;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhavalpateln.linkcast.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuggestedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuggestedFragment extends Fragment {

    public SuggestedFragment() {
        // Required empty public constructor
    }

    public static SuggestedFragment newInstance() {
        SuggestedFragment fragment = new SuggestedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_suggested, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
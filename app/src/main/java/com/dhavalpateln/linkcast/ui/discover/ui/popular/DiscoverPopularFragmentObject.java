package com.dhavalpateln.linkcast.ui.discover.ui.popular;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DiscoverPopularFragmentObject#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DiscoverPopularFragmentObject extends Fragment {

    private PopularViewModel.TYPE type;
    private String TAG = "PopularFragmentObject";

    public DiscoverPopularFragmentObject() {
        // Required empty public constructor
    }

    public static DiscoverPopularFragmentObject newInstance(PopularViewModel.TYPE type) {
        DiscoverPopularFragmentObject fragment = new DiscoverPopularFragmentObject();
        Bundle args = new Bundle();
        args.putSerializable("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = (PopularViewModel.TYPE) getArguments().getSerializable("type");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover_popular_object, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PopularViewModel model = new ViewModelProvider(this).get(PopularViewModel.class);
        model.getData(type).observe(getViewLifecycleOwner(), new Observer<List<MyAnimelistAnimeData>>() {
            @Override
            public void onChanged(List<MyAnimelistAnimeData> myAnimelistAnimeData) {
                Log.d(TAG, "Data changed");
            }
        });
    }
}
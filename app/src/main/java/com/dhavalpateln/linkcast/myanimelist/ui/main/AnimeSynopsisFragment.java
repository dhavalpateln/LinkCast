package com.dhavalpateln.linkcast.myanimelist.ui.main;

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
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnimeSynopsisFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnimeSynopsisFragment extends Fragment {

    private final String TAG = "AnimeSynopsis";
    private TextView synopsisTextView;

    public AnimeSynopsisFragment() {
        // Required empty public constructor
    }

    public static AnimeSynopsisFragment newInstance() {
        AnimeSynopsisFragment fragment = new AnimeSynopsisFragment();
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
        return inflater.inflate(R.layout.fragment_anime_synopsis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        synopsisTextView = view.findViewById(R.id.anime_synopsis_text_view);
        MyAnimelistDataViewModel viewModel = new ViewModelProvider(getActivity()).get(MyAnimelistDataViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<MyAnimelistAnimeData>() {
            @Override
            public void onChanged(MyAnimelistAnimeData myAnimelistAnimeData) {
                if(myAnimelistAnimeData.getId() > 0) {
                    synopsisTextView.setText(myAnimelistAnimeData.getSynopsis());
                    myAnimelistAnimeData.setTitle("something new");
                }
                Log.d("MyAnimeInfoFrag", "Changed");
            }
        });
    }
}
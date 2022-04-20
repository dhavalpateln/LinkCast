package com.dhavalpateln.linkcast.myanimelist.ui.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.MyAnimeListCharacterAdapter;
import com.dhavalpateln.linkcast.database.MyAnimeListDatabase;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistCharacterData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CharacterInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CharacterInfoFragment extends Fragment {

    private List<MyAnimelistCharacterData> dataList;
    private MyAnimeListCharacterAdapter recyclerAdapter;
    private RecyclerView characterRecyclerView;

    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public CharacterInfoFragment() {
        // Required empty public constructor
    }

    public static CharacterInfoFragment newInstance() {
        CharacterInfoFragment fragment = new CharacterInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_character_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        characterRecyclerView = view.findViewById(R.id.mal_characters_recycler_view);

        recyclerAdapter = new MyAnimeListCharacterAdapter(dataList, getContext());
        characterRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        characterRecyclerView.setAdapter(recyclerAdapter);


        MyAnimelistDataViewModel viewModel = new ViewModelProvider(getActivity()).get(MyAnimelistDataViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), myAnimelistAnimeData -> {
            if(myAnimelistAnimeData.getId() > 0) {
                updateCharactersRecyclerView(myAnimelistAnimeData.getCharacters());
                executor.execute(() -> {
                    MyAnimeListDatabase.getInstance().getAllCharacters(myAnimelistAnimeData);
                    uiHandler.post(() -> updateCharactersRecyclerView(myAnimelistAnimeData.getCharacters()));
                });
            }
            Log.d("MyAnimeCharacterFrag", "Changed");
        });
    }

    private void updateCharactersRecyclerView(List<MyAnimelistCharacterData> characterList) {
        for(MyAnimelistCharacterData characterData: characterList) {
            if(!dataList.contains(characterData)) {
                dataList.add(characterData);
            }
        }
        recyclerAdapter.notifyDataSetChanged();
    }

}
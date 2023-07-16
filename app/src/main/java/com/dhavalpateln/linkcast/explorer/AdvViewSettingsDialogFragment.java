package com.dhavalpateln.linkcast.explorer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkDataContract;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.dialogs.LinkDownloadManagerDialog;
import com.dhavalpateln.linkcast.dialogs.MyAnimeListSearchDialog;
import com.dhavalpateln.linkcast.explorer.listeners.SourceChangeListener;
import com.dhavalpateln.linkcast.explorer.listeners.TaskCompleteListener;
import com.dhavalpateln.linkcast.explorer.listeners.VideoSelectedListener;
import com.dhavalpateln.linkcast.explorer.listeners.VideoServerListener;
import com.dhavalpateln.linkcast.explorer.tasks.ExtractMALMetaData;
import com.dhavalpateln.linkcast.explorer.tasks.ExtractVideoServers;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.extractors.AnimeMangaSearch;
import com.dhavalpateln.linkcast.extractors.Providers;
import com.dhavalpateln.linkcast.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AdvViewSettingsDialogFragment extends BottomSheetDialogFragment {

    private static final int CHANGE_SOURCE_REQUEST_CODE = 3;
    private LinkWithAllData linkWithAllData;
    private MaterialAutoCompleteTextView sourceDropdown;
    private TextView selectedSourceTitleTextView;
    private TextView malTitleTextView;
    private TextView wrongSourceTitleTextView;
    private TextView wrongMALTitleTextView;
    private MaterialSwitch notificationSwitch;
    private String[] sourceItems;
    private String TAG = "AdvViewSettings";
    private Map<String, LinkData> cachedSearchResult;
    private SourceChangeListener sourceChangeListener;
    private LinkCastRoomRepository roomRepo;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Executor executor = Executors.newCachedThreadPool();

    public AdvViewSettingsDialogFragment(LinkWithAllData linkWithAllData, SourceChangeListener sourceChangeListener) {
        this.linkWithAllData = linkWithAllData;
        this.sourceItems = linkWithAllData.isAnime() ? Providers.getAnimeProviderNames() : Providers.getMangaProviderNames();
        this.cachedSearchResult = new HashMap<>();
        this.sourceChangeListener = sourceChangeListener;
        this.roomRepo = new LinkCastRoomRepository(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_adv_view_settings, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sourceDropdown = view.findViewById(R.id.sourceDropdownAutocompleteTextView);
        selectedSourceTitleTextView = view.findViewById(R.id.selectedSourceTitleTextView);
        notificationSwitch = view.findViewById(R.id.notification_switch);
        malTitleTextView = view.findViewById(R.id.malTitleTextView);
        wrongSourceTitleTextView = view.findViewById(R.id.wrongSourceTitleTextView);
        wrongMALTitleTextView = view.findViewById(R.id.wrongMalTitleTextView);

        sourceDropdown.setSimpleItems(this.sourceItems);
        sourceDropdown.setText(this.linkWithAllData.getMetaData(LinkDataContract.DATA_SOURCE), false);

        if(this.linkWithAllData.linkData.getTitle() != null) {
            updateSelectedTitle(this.linkWithAllData.linkData.getTitle());
        }

        if(this.linkWithAllData.alMalMetaData != null && this.linkWithAllData.alMalMetaData.getName() != null) {
            malTitleTextView.setText(this.linkWithAllData.alMalMetaData.getName());
        }

        sourceDropdown.setOnItemClickListener((adapterView, view1, i, l) -> {
            Log.d(TAG, "Selected " + sourceItems[i]);
            executor.execute(() -> changeSource(sourceItems[i]));
        });

        notificationSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            this.linkWithAllData.updateLocalData(LinkDataContract.NOTIFICATION, isChecked ? "1" : "0");
            if(this.linkWithAllData.getId() != null)
                this.roomRepo.insert(this.linkWithAllData.linkMetaData);
        });

        wrongSourceTitleTextView.setOnClickListener(v -> {
            Intent intent = AnimeSearchActivity.prepareSearchIntent(
                    getContext(),
                    this.linkWithAllData.getTitle(),
                    this.linkWithAllData.getMetaData(LinkDataContract.DATA_SOURCE)
            );
            startActivityForResult(intent, CHANGE_SOURCE_REQUEST_CODE);
        });

        wrongMALTitleTextView.setOnClickListener(v -> {
            MyAnimeListSearchDialog myAnimeListSearchDialog = new MyAnimeListSearchDialog(this.linkWithAllData.getTitle(), this.linkWithAllData.isAnime(), myAnimelistAnimeData -> {
                this.linkWithAllData.updateData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID, String.valueOf(myAnimelistAnimeData.getId()));
                malTitleTextView.setText(myAnimelistAnimeData.getTitle());
                sourceChangeListener.onSourceChanged();

            });
            myAnimeListSearchDialog.show(getParentFragmentManager(), "MALSearch");
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CHANGE_SOURCE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                LinkWithAllData changedSourceDataLWAD = (LinkWithAllData) data.getSerializableExtra(AnimeSearchActivity.RESULT_ANIMELINKDATA);
                this.linkWithAllData.updateData(changedSourceDataLWAD.linkData);
                selectedSourceTitleTextView.setText(changedSourceDataLWAD.linkData.getTitle());
                sourceChangeListener.onSourceChanged();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedTitle(String title) {
        selectedSourceTitleTextView.setText(title);
    }

    private void changeSource(String sourceName) {
        AnimeMangaSearch searcher = Providers.getInstance().getSearcher(sourceName);
        if(searcher.requiresInit()) searcher.init();
        List<AnimeLinkData> searchResult = searcher.search(this.linkWithAllData.getTitle());
        if(searchResult.size() > 0) {
            AnimeLinkData animeLinkData = searchResult.get(0);
            LinkData linkData = LinkData.from(animeLinkData);
            uiHandler.post(() -> {
                selectedSourceTitleTextView.setText(linkData.getTitle());
                Log.d(TAG, linkData.getUrl());
                this.linkWithAllData.updateData(linkData);
                sourceChangeListener.onSourceChanged();
            });
        }
    }
}

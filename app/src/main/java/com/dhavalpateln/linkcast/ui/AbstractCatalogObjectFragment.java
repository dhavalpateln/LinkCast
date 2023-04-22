package com.dhavalpateln.linkcast.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbstractCatalogObjectFragment extends Fragment {

    protected static final String CATALOG_TYPE_ARG = "type";
    private static final String SORT_ARG = "sort";
    private static final String TAG = "CATALOG_FRAGMENT";
    private View root;

    protected String tabName;
    protected List<LinkWithAllData> dataList;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ImageView sortButton;
    private TextView animeEntriesCountTextView;
    private Sort currentSortOrder = Sort.BY_SCORE;

    public enum Sort {
        BY_NAME,
        BY_SCORE,
        BY_DATE_ADDED_ASC,
        BY_DATE_ADDED_DESC
    }

    public AbstractCatalogObjectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabName = getArguments().getString(CATALOG_TYPE_ARG);
        }
        if(savedInstanceState != null) {
            currentSortOrder = (Sort) savedInstanceState.getSerializable(SORT_ARG);
        }
        else {
            getDefaultSortOrder();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SORT_ARG, currentSortOrder);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(root == null) {
            root = inflater.inflate(R.layout.fragment_catalog_object, container, false);
        }
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(dataList == null) {
            dataList = new ArrayList<>();
        }
        animeEntriesCountTextView = view.findViewById(R.id.anime_entries_text_view);
        sortButton = view.findViewById(R.id.anime_list_sort_button);
        recyclerView = view.findViewById(R.id.catalog_recycler_view);
        recyclerView.setHasFixedSize(true);
        boolean useListAdapter = false;
        if(useListAdapter) {
            adapter = getListAdapter(dataList, getContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        }
        else {
            adapter = getGridAdapter(dataList, getContext());
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }
        recyclerView.setAdapter(adapter);
        sortButton.setOnClickListener(v -> showSortOptions(v));
    }

    public abstract RecyclerView.Adapter getAdapter(List<LinkWithAllData> adapterDataList, Context context);
    public abstract RecyclerView.Adapter getListAdapter(List<LinkWithAllData> adapterDataList, Context context);
    public abstract RecyclerView.Adapter getGridAdapter(List<LinkWithAllData> adapterDataList, Context context);

    private void showSortOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.anime_sort_menu, popupMenu.getMenu());

        switch (currentSortOrder) {
            case BY_NAME:
                popupMenu.getMenu().findItem(R.id.sort_by_alphabet).setChecked(true);
                break;
            case BY_SCORE:
                popupMenu.getMenu().findItem(R.id.sort_by_score).setChecked(true);
                break;
            case BY_DATE_ADDED_ASC:
                popupMenu.getMenu().findItem(R.id.sort_by_date_added_asc).setChecked(true);
                break;
            case BY_DATE_ADDED_DESC:
                popupMenu.getMenu().findItem(R.id.sort_by_date_added_desc).setChecked(true);
                break;
        }

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.sort_by_alphabet:
                    currentSortOrder = Sort.BY_NAME;
                    break;
                case R.id.sort_by_score:
                    currentSortOrder = Sort.BY_SCORE;
                    break;
                case R.id.sort_by_date_added_asc:
                    currentSortOrder = Sort.BY_DATE_ADDED_ASC;
                    break;
                case R.id.sort_by_date_added_desc:
                    currentSortOrder = Sort.BY_DATE_ADDED_DESC;
                    break;
                default:
                    break;
            }
            refreshAdapter();
            return false;
        });

        popupMenu.show();
    }

    private void sortData() {
        switch (currentSortOrder) {
            case BY_NAME:
                Collections.sort(dataList, (animeLinkData1, animeLinkData2) -> animeLinkData1.getTitle().compareToIgnoreCase(animeLinkData2.getTitle()));
                break;
            case BY_SCORE:
                Collections.sort(dataList, (animeLinkData1, animeLinkData2) -> {
                    int score1 = Integer.valueOf(animeLinkData1.getMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
                    int score2 = Integer.valueOf(animeLinkData2.getMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
                    if(score1 > score2) return -1;
                    else if(score2 > score1)    return 1;
                    else {
                        return animeLinkData1.getTitle().compareToIgnoreCase(animeLinkData2.getTitle());
                    }
                });
                break;
            case BY_DATE_ADDED_ASC:
                Collections.sort(dataList, (animeLinkData1, animeLinkData2) -> animeLinkData1.linkData.getId().compareToIgnoreCase(animeLinkData2.linkData.getId()));
                break;
            case BY_DATE_ADDED_DESC:
                Collections.sort(dataList, (animeLinkData1, animeLinkData2) -> animeLinkData2.linkData.getId().compareToIgnoreCase(animeLinkData1.linkData.getId()));
                break;
        }
    }

    protected void refreshAdapter() {
        Log.d("Catalog", "refresh called");
        animeEntriesCountTextView.setText(dataList.size() + " Entries");
        sortData();
        adapter.notifyDataSetChanged();
    }

    private void getDefaultSortOrder() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        try {
            currentSortOrder = Sort.valueOf(prefs.getString(SharedPrefContract.ANIME_LIST_SORT_ORDER, SharedPrefContract.ANIME_LIST_SORT_DEFAULT));
        } catch (Exception e) {
            currentSortOrder = Sort.BY_SCORE;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(SharedPrefContract.ANIME_LIST_SORT_ORDER, currentSortOrder.name());
            editor.commit();
        }
    }

}
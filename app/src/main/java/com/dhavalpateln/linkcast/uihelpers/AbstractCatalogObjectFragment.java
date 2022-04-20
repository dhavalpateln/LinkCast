package com.dhavalpateln.linkcast.uihelpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.SharedPrefContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbstractCatalogObjectFragment extends Fragment {

    protected static final String CATALOG_TYPE_ARG = "type";
    private static final String SORT_ARG = "sort";
    private static final String TAG = "CATALOG_FRAGMENT";

    protected String tabName;
    protected List<AnimeLinkData> dataList;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
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

    public abstract void onRecyclerBindViewHolder(@NonNull RecyclerViewAdapter.RecyclerViewHolder holder, int position, AnimeLinkData data);

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

        private List<AnimeLinkData> animeLinkDataList;
        private Context mcontext;

        public RecyclerViewAdapter(List<AnimeLinkData> recyclerDataArrayList, Context mcontext) {
            this.animeLinkDataList = recyclerDataArrayList;
            this.mcontext = mcontext;
        }

        @NonNull
        @Override
        public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate Layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_recycler_object, parent, false);
            return new RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
            // Set the data to textview and imageview.
            onRecyclerBindViewHolder(holder, position, animeLinkDataList.get(position));
        }

        @Override
        public int getItemCount() {
            // this method returns the size of recyclerview
            return animeLinkDataList.size();
        }

        // View Holder Class to handle Recycler View.
        public class RecyclerViewHolder extends RecyclerView.ViewHolder {

            public TextView titleTextView;
            public TextView subTextView;
            public ImageView animeImageView;
            public Button openButton;
            public Button deleteButton;
            public Button editButton;
            public TextView scoreTextView;
            public ConstraintLayout mainLayout;

            public RecyclerViewHolder(@NonNull View itemView) {
                super(itemView);
                this.mainLayout = (ConstraintLayout) itemView;
                this.titleTextView = itemView.findViewById(R.id.catalog_recycler_object_text_view);
                this.subTextView = itemView.findViewById(R.id.catalog_recycler_object_sub_text_view);
                this.scoreTextView = itemView.findViewById(R.id.anime_score_text_view);
                this.animeImageView = itemView.findViewById(R.id.anime_image_view);
                this.openButton = itemView.findViewById(R.id.open_button_catalog_recycler);
                this.deleteButton = itemView.findViewById(R.id.delete_button_catalog_recycler);
                this.editButton = itemView.findViewById(R.id.edit_button_catalog_recycler);
            }
        }
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
        View root = inflater.inflate(R.layout.fragment_catalog_object, container, false);
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
        adapter = new RecyclerViewAdapter(dataList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        sortButton.setOnClickListener(v -> showSortOptions(v));
    }

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
            sortData();
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
                    int score1 = Integer.valueOf(animeLinkData1.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
                    int score2 = Integer.valueOf(animeLinkData2.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
                    if(score1 > score2) return -1;
                    else if(score2 > score1)    return 1;
                    else {
                        return animeLinkData1.getTitle().compareToIgnoreCase(animeLinkData2.getTitle());
                    }
                });
                break;
            case BY_DATE_ADDED_ASC:
                Collections.sort(dataList, (animeLinkData1, animeLinkData2) -> animeLinkData1.getId().compareToIgnoreCase(animeLinkData2.getId()));
                break;
            case BY_DATE_ADDED_DESC:
                Collections.sort(dataList, (animeLinkData1, animeLinkData2) -> animeLinkData2.getId().compareToIgnoreCase(animeLinkData1.getId()));
                break;
        }
        adapter.notifyDataSetChanged();
    }

    protected void refreshAdapter() {
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
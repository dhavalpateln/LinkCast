package com.dhavalpateln.linkcast.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.AnimeAdvancedView;
import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CatalogObjectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CatalogObjectFragment extends Fragment {

    private static final String CATALOG_TYPE_ARG = "type";
    private static final String SORT_ARG = "sort";
    private static final String TAG = "CATALOG_FRAGMENT";

    private String tabName;
    private ArrayList<AnimeLinkData> data;
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

    public CatalogObjectFragment() {
        // Required empty public constructor
    }

    public static CatalogObjectFragment newInstance(String catalogType) {
        CatalogObjectFragment fragment = new CatalogObjectFragment();
        Bundle args = new Bundle();
        args.putString(CATALOG_TYPE_ARG, catalogType);
        fragment.setArguments(args);
        return fragment;
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

        private ArrayList<AnimeLinkData> episodeDataArrayList;
        private Context mcontext;

        public RecyclerViewAdapter(ArrayList<AnimeLinkData> recyclerDataArrayList, Context mcontext) {
            this.episodeDataArrayList = recyclerDataArrayList;
            this.mcontext = mcontext;
        }

        @NonNull
        @Override
        public RecyclerViewAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate Layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_recycler_object, parent, false);
            return new RecyclerViewAdapter.RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.RecyclerViewHolder holder, int position) {
            // Set the data to textview and imageview.
            AnimeLinkData recyclerData = episodeDataArrayList.get(position);
            String sourceName = recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
            holder.episodeNumTextView.setText(recyclerData.getTitle() + (sourceName.equals("") ? "" : " (" + sourceName + ")"));
            holder.openButton.setOnClickListener(v -> {
                Intent intent = AnimeAdvancedView.prepareIntent(getContext(), recyclerData);
                startActivity(intent);
            });
            holder.deleteButton.setOnClickListener(v -> {
                FirebaseDBHelper.removeAnimeLink(recyclerData.getId());
            });
            holder.editButton.setOnClickListener(v -> {
                // TODO: add more fields to edit
                BookmarkLinkDialog dialog = new BookmarkLinkDialog(recyclerData.getId(), recyclerData.getTitle(), recyclerData.getUrl(), recyclerData.getData());
                dialog.show(getParentFragmentManager(), "bookmarkEdit");
            });
            if(recyclerData.getData().containsKey("imageUrl")) {
                Glide.with(getContext())
                        .load(recyclerData.getData().get("imageUrl"))
                        .centerCrop()
                        .crossFade()
                        //.bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.animeImageView);
                holder.animeImageView.setClipToOutline(true);
            }
            else {
                holder.animeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_stat_name));
            }
            holder.scoreTextView.setText("\u2605" + recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
        }

        @Override
        public int getItemCount() {
            // this method returns the size of recyclerview
            return episodeDataArrayList.size();
        }

        // View Holder Class to handle Recycler View.
        public class RecyclerViewHolder extends RecyclerView.ViewHolder {

            private TextView episodeNumTextView;
            private TextView sourceTextView;
            private ImageView animeImageView;
            private Button openButton;
            private Button deleteButton;
            private Button editButton;
            private TextView scoreTextView;
            private ConstraintLayout mainLayout;

            public RecyclerViewHolder(@NonNull View itemView) {
                super(itemView);
                this.mainLayout = (ConstraintLayout) itemView;
                this.episodeNumTextView = itemView.findViewById(R.id.catalog_recycler_object_text_view);
                this.sourceTextView = itemView.findViewById(R.id.catalog_recycler_object_source_text_view);
                scoreTextView = itemView.findViewById(R.id.anime_score_text_view);
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
        if(data == null) {
            data = new ArrayList<>();
        }
        SharedAnimeLinkDataViewModel viewModel = new ViewModelProvider(getActivity()).get(SharedAnimeLinkDataViewModel.class);
        animeEntriesCountTextView = view.findViewById(R.id.anime_entries_text_view);
        sortButton = view.findViewById(R.id.anime_list_sort_button);
        recyclerView = view.findViewById(R.id.catalog_recycler_view);
        adapter = new RecyclerViewAdapter(data, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        viewModel.getData().observe(getViewLifecycleOwner(), stringAnimeLinkDataMap -> {
            Log.d(TAG, "Data changed");
            data.clear();
            for(Map.Entry<String, AnimeLinkData> entry: stringAnimeLinkDataMap.entrySet()) {
                AnimeLinkData animeLinkData = entry.getValue();
                animeLinkData.setId(entry.getKey());
                if (!animeLinkData.getData().containsKey(AnimeLinkData.DataContract.DATA_STATUS)) {
                    animeLinkData.getData().put(AnimeLinkData.DataContract.DATA_STATUS, "Planned");
                }
                if (!animeLinkData.getData().containsKey(AnimeLinkData.DataContract.DATA_FAVORITE)) {
                    animeLinkData.getData().put(AnimeLinkData.DataContract.DATA_FAVORITE, "false");
                }

                if(tabName.equals(CatalogFragment.Catalogs.ALL) ||
                    tabName.equalsIgnoreCase(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS)) ||
                    (tabName.equals(CatalogFragment.Catalogs.FAVORITE) &&
                            animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_FAVORITE).equals("true"))) {
                    data.add(entry.getValue());
                }

                /*if (tabName.equals("All") ||
                        tabName.equalsIgnoreCase(animeLinkData.getData().get("status")) ||
                        (tabName.equals("FAV") && animeLinkData.getData().get(AnimeLinkData.DataContract.DATA_FAVORITE).equals("true"))) {
                    data.add(entry.getValue());
                }*/

            }
            animeEntriesCountTextView.setText(data.size() + " Entries");
            sortData();
            //adapter.notifyDataSetChanged();
        });

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
                Collections.sort(data, (animeLinkData1, animeLinkData2) -> animeLinkData1.getTitle().compareToIgnoreCase(animeLinkData2.getTitle()));
                break;
            case BY_SCORE:
                Collections.sort(data, (animeLinkData1, animeLinkData2) -> {
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
                Collections.sort(data, (animeLinkData1, animeLinkData2) -> animeLinkData1.getId().compareToIgnoreCase(animeLinkData2.getId()));
                break;
            case BY_DATE_ADDED_DESC:
                Collections.sort(data, (animeLinkData1, animeLinkData2) -> animeLinkData2.getId().compareToIgnoreCase(animeLinkData1.getId()));
                break;
        }
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
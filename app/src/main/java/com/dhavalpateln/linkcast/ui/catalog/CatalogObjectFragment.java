package com.dhavalpateln.linkcast.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.AnimeAdvancedView;
import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CatalogObjectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CatalogObjectFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String TAG = "CATALOG_FRAGMENT";


    // TODO: Rename and change types of parameters
    private String tabName;
    private ArrayList<AnimeLinkData> data;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    public CatalogObjectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment CatalogObjectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CatalogObjectFragment newInstance(String param1) {
        CatalogObjectFragment fragment = new CatalogObjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
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
            //holder.sourceTextView.setText(recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE));
            holder.openButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AnimeWebExplorer.class);
                if(recyclerData.getData() != null) {
                    if(recyclerData.getData().containsKey("mode") && recyclerData.getData().get("mode").equals("advanced")) {
                        intent = new Intent(getContext(), AnimeAdvancedView.class);
                        intent.putExtra("url", recyclerData.getUrl());
                    }
                    intent.putExtra("mapdata", (HashMap<String, String>) recyclerData.getData());
                    for(Map.Entry<String, String> entry: recyclerData.getData().entrySet()) {
                        intent.putExtra("data-" + entry.getKey(), entry.getValue());
                    }
                }
                intent.putExtra("search", recyclerData.getUrl());
                intent.putExtra("source", "saved");
                intent.putExtra("id", recyclerData.getId());
                intent.putExtra("title", recyclerData.getTitle());
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
            if(recyclerData.getTitle().contains("Chuuni") || recyclerData.getTitle().contains("Boku no Hero Academia 3")) {
                Log.d(TAG, "fk");

            }
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
            private ConstraintLayout mainLayout;

            public RecyclerViewHolder(@NonNull View itemView) {
                super(itemView);
                this.mainLayout = (ConstraintLayout) itemView;
                this.episodeNumTextView = itemView.findViewById(R.id.catalog_recycler_object_text_view);
                this.sourceTextView = itemView.findViewById(R.id.catalog_recycler_object_source_text_view);
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
            tabName = getArguments().getString(ARG_PARAM1);
        }
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
        recyclerView = view.findViewById(R.id.catalog_recycler_view);
        adapter = new RecyclerViewAdapter(data, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<Map<String, AnimeLinkData>>() {
            @Override
            public void onChanged(Map<String, AnimeLinkData> stringAnimeLinkDataMap) {
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
                adapter.notifyDataSetChanged();
            }
        });
    }




}
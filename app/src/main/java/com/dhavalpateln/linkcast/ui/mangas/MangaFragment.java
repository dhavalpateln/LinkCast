package com.dhavalpateln.linkcast.ui.mangas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.LinkMaterialCardView;
import com.dhavalpateln.linkcast.MangaAdvancedView;
import com.dhavalpateln.linkcast.MangaWebExplorer;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.ListRecyclerAdapter;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.Link;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.ui.catalog.SharedAnimeLinkDataViewModel;
import com.dhavalpateln.linkcast.ui.feedback.CrashReportActivity;
import com.dhavalpateln.linkcast.utils.UIBuilder;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MangaFragment extends Fragment {

    private List<AnimeLinkData> dataList;
    private ListRecyclerAdapter<AnimeLinkData> recyclerAdapter;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_manga, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dataList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.manga_list_recycler_view);

        recyclerAdapter = new ListRecyclerAdapter<>(dataList, getContext(), (ListRecyclerAdapter.RecyclerInterface<AnimeLinkData>) (holder, position, data) -> {
            holder.titleTextView.setText(data.getTitle());
            Glide.with(getContext())
                    .load(data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL))
                    .centerCrop()
                    .crossFade()
                    //.bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
            holder.imageView.setClipToOutline(true);

            Button openButton = UIBuilder.generateMaterialButton(getContext(), "OPEN");
            openButton.setOnClickListener(v -> {
                Intent intent = MangaAdvancedView.prepareIntent(getContext(), data);
                startActivity(intent);
            });

            Button deleteButton = UIBuilder.generateMaterialButton(getContext(), "DELETE");
            deleteButton.setOnClickListener(v -> {
                FirebaseDBHelper.removeMangaLink(data.getId());
            });

            holder.buttonHolder.addView(openButton);
            holder.buttonHolder.addView(deleteButton);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recyclerAdapter);

        MangaDataViewModel viewModel = new ViewModelProvider(getActivity()).get(MangaDataViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), stringAnimeLinkDataMap -> {
            dataList.clear();
            for(Map.Entry<String, AnimeLinkData> entry: stringAnimeLinkDataMap.entrySet()) {
                dataList.add(entry.getValue());
            }
            recyclerAdapter.notifyDataSetChanged();
        });
    }
}
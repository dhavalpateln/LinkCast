package com.dhavalpateln.linkcast.ui.mangas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.MangaAdvancedView;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.uihelpers.AbstractCatalogObjectFragment;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

public class MangaFragmentObject extends AbstractCatalogObjectFragment {

    public static MangaFragmentObject newInstance(String catalogType) {
        MangaFragmentObject fragment = new MangaFragmentObject();
        Bundle args = new Bundle();
        args.putString(CATALOG_TYPE_ARG, catalogType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onRecyclerBindViewHolder(@NonNull RecyclerViewAdapter.RecyclerViewHolder holder, int position, AnimeLinkData data) {
        holder.titleTextView.setText(data.getTitle());
        Glide.with(getContext())
                .load(data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL))
                .centerCrop()
                .crossFade()
                //.bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.animeImageView);
        holder.animeImageView.setClipToOutline(true);

        holder.openButton.setOnClickListener(v -> {
            Intent intent = MangaAdvancedView.prepareIntent(getContext(), data);
            startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            FirebaseDBHelper.removeMangaLink(data.getId());
        });

        holder.editButton.setVisibility(View.GONE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MangaDataViewModel viewModel = new ViewModelProvider(getActivity()).get(MangaDataViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), stringAnimeLinkDataMap -> {
            dataList.clear();
            for(Map.Entry<String, AnimeLinkData> entry: stringAnimeLinkDataMap.entrySet()) {
                dataList.add(entry.getValue());
            }
            refreshAdapter();
        });
    }
}

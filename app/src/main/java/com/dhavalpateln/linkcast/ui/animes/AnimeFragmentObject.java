package com.dhavalpateln.linkcast.ui.animes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.AnimeAdvancedView;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.uihelpers.AbstractCatalogObjectFragment;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

public class AnimeFragmentObject extends AbstractCatalogObjectFragment {

    private final String TAG = "AnimeFragment";

    public static AnimeFragmentObject newInstance(String catalogType) {
        AnimeFragmentObject fragment = new AnimeFragmentObject();
        Bundle args = new Bundle();
        args.putString(CATALOG_TYPE_ARG, catalogType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onRecyclerBindViewHolder(@NonNull RecyclerViewAdapter.RecyclerViewHolder holder, int position, AnimeLinkData data) {
        String sourceName = data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
        holder.titleTextView.setText(data.getTitle() + (sourceName.equals("") ? "" : " (" + sourceName + ")"));
        holder.openButton.setOnClickListener(v -> {
            Intent intent = AnimeAdvancedView.prepareIntent(getContext(), data);
            startActivity(intent);
        });
        holder.deleteButton.setOnClickListener(v -> {
            FirebaseDBHelper.removeAnimeLink(data.getId());
        });
        holder.editButton.setOnClickListener(v -> {
            // TODO: add more fields to edit
            BookmarkLinkDialog dialog = new BookmarkLinkDialog(data.getId(), data.getTitle(), data.getUrl(), data.getData());
            dialog.show(getParentFragmentManager(), "bookmarkEdit");
        });
        if(data.getData().containsKey("imageUrl")) {
            Glide.with(getContext())
                    .load(data.getData().get("imageUrl"))
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
        holder.scoreTextView.setText("\u2605" + data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedAnimeLinkDataViewModel viewModel = new ViewModelProvider(getActivity()).get(SharedAnimeLinkDataViewModel.class);

        viewModel.getData().observe(getViewLifecycleOwner(), stringAnimeLinkDataMap -> {
            Log.d(TAG, "Data changed");
            dataList.clear();
            for(Map.Entry<String, AnimeLinkData> entry: stringAnimeLinkDataMap.entrySet()) {
                AnimeLinkData animeLinkData = entry.getValue();
                animeLinkData.setId(entry.getKey());
                if (!animeLinkData.getData().containsKey(AnimeLinkData.DataContract.DATA_STATUS)) {
                    animeLinkData.getData().put(AnimeLinkData.DataContract.DATA_STATUS, "Planned");
                }
                if (!animeLinkData.getData().containsKey(AnimeLinkData.DataContract.DATA_FAVORITE)) {
                    animeLinkData.getData().put(AnimeLinkData.DataContract.DATA_FAVORITE, "false");
                }

                if(tabName.equals(AnimeFragment.Catalogs.ALL) ||
                        tabName.equalsIgnoreCase(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS)) ||
                        (tabName.equals(AnimeFragment.Catalogs.FAVORITE) &&
                                animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_FAVORITE).equals("true"))) {
                    dataList.add(entry.getValue());
                }

                /*if (tabName.equals("All") ||
                        tabName.equalsIgnoreCase(animeLinkData.getData().get("status")) ||
                        (tabName.equals("FAV") && animeLinkData.getData().get(AnimeLinkData.DataContract.DATA_FAVORITE).equals("true"))) {
                    data.add(entry.getValue());
                }*/

            }
            refreshAdapter();
        });
    }
}

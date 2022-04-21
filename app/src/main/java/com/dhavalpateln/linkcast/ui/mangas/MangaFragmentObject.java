package com.dhavalpateln.linkcast.ui.mangas;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dhavalpateln.linkcast.AdvancedView;
import com.dhavalpateln.linkcast.adapters.AnimeDataListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeListViewHolder;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.ui.animes.AnimeFragment;
import com.dhavalpateln.linkcast.ui.AbstractCatalogObjectFragment;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

public class MangaFragmentObject extends AbstractCatalogObjectFragment {

    public static MangaFragmentObject newInstance(String catalogType) {
        MangaFragmentObject fragment = new MangaFragmentObject();
        Bundle args = new Bundle();
        args.putString(CATALOG_TYPE_ARG, catalogType);
        fragment.setArguments(args);
        return fragment;
    }

    private class MangaCatalogListAdapter extends AnimeDataListRecyclerAdapter {

        public MangaCatalogListAdapter(List<AnimeLinkData> recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(@NonNull AnimeListViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            AnimeLinkData data = dataList.get(position);
            holder.openButton.setOnClickListener(v -> {
                Intent intent = AdvancedView.prepareIntent(getContext(), data);
                intent.putExtra(AdvancedView.INTENT_MODE_ANIME, false);
                startActivity(intent);
            });

            holder.deleteButton.setOnClickListener(v -> {
                FirebaseDBHelper.removeMangaLink(data.getId());
            });
            holder.editButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MangaDataViewModel viewModel = new ViewModelProvider(getActivity()).get(MangaDataViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), stringAnimeLinkDataMap -> {
            dataList.clear();
            for(Map.Entry<String, AnimeLinkData> entry: stringAnimeLinkDataMap.entrySet()) {
                AnimeLinkData animeLinkData = entry.getValue();
                if(tabName.equals(AnimeFragment.Catalogs.ALL) ||
                        tabName.equalsIgnoreCase(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS)) ||
                        (tabName.equals(AnimeFragment.Catalogs.FAVORITE) &&
                                animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_FAVORITE).equals("true"))) {
                    dataList.add(entry.getValue());
                }
            }
            refreshAdapter();
        });
    }

    @Override
    public RecyclerView.Adapter getAdapter(List<AnimeLinkData> adapterDataList, Context context) {
        return new MangaCatalogListAdapter(adapterDataList, context);
    }
}

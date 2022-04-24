package com.dhavalpateln.linkcast.ui.animes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dhavalpateln.linkcast.AdvancedView;
import com.dhavalpateln.linkcast.MainActivity;
import com.dhavalpateln.linkcast.adapters.AnimeDataListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeListViewHolder;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.ui.AbstractCatalogObjectFragment;
import com.dhavalpateln.linkcast.ui.mangas.MangaDataViewModel;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

public class AnimeFragmentObject extends AbstractCatalogObjectFragment {

    private final String TAG = "AnimeFragment";

    public static AnimeFragmentObject newInstance(String catalogType) {
        AnimeFragmentObject fragment = new AnimeFragmentObject();
        Bundle args = new Bundle();
        args.putString(CATALOG_TYPE_ARG, catalogType);
        fragment.setArguments(args);
        return fragment;
    }

    private class AnimeCatalogListAdapter extends AnimeDataListRecyclerAdapter {

        public AnimeCatalogListAdapter(List<AnimeLinkData> recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(@NonNull AnimeListViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            AnimeLinkData data = dataList.get(position);
            holder.openButton.setOnClickListener(v -> {
                Intent intent = AdvancedView.prepareIntent(getContext(), data);
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
            holder.scoreTextView.setText("\u2605" + data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedAnimeLinkDataViewModel viewModel = new ViewModelProvider(getActivity()).get(SharedAnimeLinkDataViewModel.class);

        new ViewModelProvider(getActivity()).get(MangaDataViewModel.class).getData(); // load manga cache

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


            }
            refreshAdapter();
        });
    }

    @Override
    public RecyclerView.Adapter getAdapter(List<AnimeLinkData> adapterDataList, Context context) {
        return new AnimeCatalogListAdapter(adapterDataList, context);
    }
}

package com.dhavalpateln.linkcast.ui.mangas;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

import com.dhavalpateln.linkcast.adapters.LinkDataGridRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.LinkDataListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.viewholders.LinkDataGridViewHolder;
import com.dhavalpateln.linkcast.adapters.viewholders.LinkDataViewHolder;
import com.dhavalpateln.linkcast.database.LinkDataViewModel;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.dialogs.LinkDataBottomSheet;
import com.dhavalpateln.linkcast.explorer.AdvancedView;
import com.dhavalpateln.linkcast.adapters.AnimeDataListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeListViewHolder;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.dialogs.ConfirmationDialog;
import com.dhavalpateln.linkcast.ui.animes.AnimeFragment;
import com.dhavalpateln.linkcast.ui.AbstractCatalogObjectFragment;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

public class MangaFragmentObject extends AbstractCatalogObjectFragment {

    private SharedPreferences prefs;

    public static MangaFragmentObject newInstance(String catalogType) {
        MangaFragmentObject fragment = new MangaFragmentObject();
        Bundle args = new Bundle();
        args.putString(CATALOG_TYPE_ARG, catalogType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onLinkDataClicked(LinkWithAllData linkData, ImageView animeImage) {
        Intent intent = AdvancedView.prepareIntent(getContext(), linkData);
        ActivityOptions options = ActivityOptions
                .makeSceneTransitionAnimation(getActivity(), animeImage, "animeImage");
        intent.putExtra(AdvancedView.INTENT_MODE_ANIME, false);
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onLinkDataLongClick(LinkWithAllData linkData) {
        LinkDataBottomSheet bottomSheet = new LinkDataBottomSheet(linkData, prefs.getString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, "ask"));
        bottomSheet.show(getActivity().getSupportFragmentManager(), "LDBottomSheet");
    }

    /*private class MangaCatalogListAdapter extends AnimeDataListRecyclerAdapter {

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
                if(prefs.getString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, "ask").equalsIgnoreCase("ask")) {
                    ConfirmationDialog confirmationDialog = new ConfirmationDialog("Are you sure you want to delete this?", () -> {
                        FirebaseDBHelper.removeMangaLink(data.getId());
                    });
                    confirmationDialog.show(getParentFragmentManager(), "Confirm");
                }
                else {
                    FirebaseDBHelper.removeMangaLink(data.getId());
                }
            });
            holder.editButton.setVisibility(View.GONE);
        }
    }
*/
    private class MangaCatalogListAdapter extends LinkDataListRecyclerAdapter {

        public MangaCatalogListAdapter(List<LinkWithAllData> recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(@NonNull LinkDataViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            LinkWithAllData linkWithAllData = dataList.get(position);
            AnimeLinkData data = AnimeLinkData.from(linkWithAllData.linkData);
            holder.mainLayout.setOnClickListener(v -> {
                Intent intent = AdvancedView.prepareIntent(getContext(), linkWithAllData);
                intent.putExtra(AdvancedView.INTENT_MODE_ANIME, false);
                startActivity(intent);
            });

        }
    }

    private class MangaCatalogGridAdapter extends LinkDataGridRecyclerAdapter {

        public MangaCatalogGridAdapter(List<LinkWithAllData> recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(@NonNull LinkDataGridViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            LinkWithAllData linkWithAllData = dataList.get(position);
            AnimeLinkData data = AnimeLinkData.from(linkWithAllData.linkData);
            holder.mainLayout.setOnClickListener(v -> {
                Intent intent = AdvancedView.prepareIntent(getContext(), linkWithAllData);
                intent.putExtra(AdvancedView.INTENT_MODE_ANIME, false);
                startActivity(intent);
            });

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinkDataViewModel viewModel = new ViewModelProvider(getActivity()).get(LinkDataViewModel.class);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        viewModel.getMangaLinks().observe(getViewLifecycleOwner(), linkDataList -> {
            dataList.clear();
            for(LinkWithAllData linkWithAllData: linkDataList) {
                AnimeLinkData animeLinkData = AnimeLinkData.from(linkWithAllData.linkData);
                if(tabName.equals(AnimeFragment.Catalogs.ALL) ||
                        tabName.equalsIgnoreCase(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS)) ||
                        (tabName.equals(AnimeFragment.Catalogs.FAVORITE) &&
                                animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_FAVORITE).equals("true"))) {
                    dataList.add(linkWithAllData);
                }
            }
            refreshAdapter();
        });
    }

    @Override
    public RecyclerView.Adapter getAdapter(List<LinkWithAllData> adapterDataList, Context context) {
        return new MangaCatalogListAdapter(adapterDataList, context);
    }
    @Override
    public RecyclerView.Adapter getListAdapter(List<LinkWithAllData> adapterDataList, Context context) {
        return new MangaCatalogListAdapter(adapterDataList, context);
    }
    @Override
    public RecyclerView.Adapter getGridAdapter(List<LinkWithAllData> adapterDataList, Context context) {
        return new MangaCatalogGridAdapter(adapterDataList, context);
    }
}

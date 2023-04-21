package com.dhavalpateln.linkcast.ui.animes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.dhavalpateln.linkcast.adapters.LinkDataListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.viewholders.LinkDataViewHolder;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.explorer.AdvancedView;
import com.dhavalpateln.linkcast.adapters.AnimeDataListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeListViewHolder;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.dialogs.ConfirmationDialog;
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
    private SharedPreferences prefs;

    public static AnimeFragmentObject newInstance(String catalogType) {
        AnimeFragmentObject fragment = new AnimeFragmentObject();
        Bundle args = new Bundle();
        args.putString(CATALOG_TYPE_ARG, catalogType);
        fragment.setArguments(args);
        return fragment;
    }

    /*private class AnimeCatalogListAdapter extends AnimeDataListRecyclerAdapter {

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
                if(prefs.getString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, "ask").equalsIgnoreCase("ask")) {
                    ConfirmationDialog confirmationDialog = new ConfirmationDialog("Are you sure you want to delete this?", () -> {
                        FirebaseDBHelper.removeAnimeLink(data.getId());
                    });
                    confirmationDialog.show(getParentFragmentManager(), "Confirm");
                }
                else {
                    FirebaseDBHelper.removeAnimeLink(data.getId());
                }
            });
            holder.editButton.setOnClickListener(v -> {
                // TODO: add more fields to edit
                BookmarkLinkDialog dialog = new BookmarkLinkDialog(data);
                dialog.show(getParentFragmentManager(), "bookmarkEdit");
            });
            holder.scoreTextView.setText("\u2605" + data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
        }
    }
*/
    private class AnimeCatalogListAdapter extends LinkDataListRecyclerAdapter {

        public AnimeCatalogListAdapter(List<LinkWithAllData> recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(@NonNull LinkDataViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            LinkWithAllData data = dataList.get(position);
            holder.mainLayout.setOnClickListener(v -> {
                Intent intent = AdvancedView.prepareIntent(getContext(), AnimeLinkData.from(data.linkData));
                startActivity(intent);
            });
            /*holder.deleteButton.setOnClickListener(v -> {
                if(prefs.getString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, "ask").equalsIgnoreCase("ask")) {
                    ConfirmationDialog confirmationDialog = new ConfirmationDialog("Are you sure you want to delete this?", () -> {
                        FirebaseDBHelper.removeAnimeLink(data.getId());
                    });
                    confirmationDialog.show(getParentFragmentManager(), "Confirm");
                }
                else {
                    FirebaseDBHelper.removeAnimeLink(data.getId());
                }
            });
            holder.editButton.setOnClickListener(v -> {
                // TODO: add more fields to edit
                BookmarkLinkDialog dialog = new BookmarkLinkDialog(data);
                dialog.show(getParentFragmentManager(), "bookmarkEdit");
            });*/
            holder.scoreTextView.setText("\u2605" + data.getMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedAnimeLinkDataViewModel viewModel = new ViewModelProvider(getActivity()).get(SharedAnimeLinkDataViewModel.class);

        new ViewModelProvider(getActivity()).get(MangaDataViewModel.class).getData(); // load manga cache
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        viewModel.getLinkData().observe(getViewLifecycleOwner(), linkDataList -> {
            Log.d(TAG, "Data changed");
            dataList.clear();
            for(LinkWithAllData linkWithAllData: linkDataList) {
                AnimeLinkData animeLinkData = AnimeLinkData.from(linkWithAllData.linkData);
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
                    dataList.add(linkWithAllData);
                }
            }
            refreshAdapter();
        });
    }

    @Override
    public RecyclerView.Adapter getAdapter(List<LinkWithAllData> adapterDataList, Context context) {
        return new AnimeCatalogListAdapter(adapterDataList, context);
    }
}

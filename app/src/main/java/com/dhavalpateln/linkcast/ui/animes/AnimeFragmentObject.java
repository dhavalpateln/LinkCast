package com.dhavalpateln.linkcast.ui.animes;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dhavalpateln.linkcast.LauncherActivity;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.LinkDataGridRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.LinkDataListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.viewholders.LinkDataGridViewHolder;
import com.dhavalpateln.linkcast.adapters.viewholders.LinkDataViewHolder;
import com.dhavalpateln.linkcast.database.LinkDataViewModel;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.dialogs.LinkDataBottomSheet;
import com.dhavalpateln.linkcast.explorer.AdvancedView;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.ui.AbstractCatalogObjectFragment;
import com.dhavalpateln.linkcast.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
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

    @Override
    public void onLinkDataClicked(LinkWithAllData linkData, ImageView animeImage) {
        Intent intent = AdvancedView.prepareIntent(getContext(), linkData);
        ActivityOptions options = ActivityOptions
                .makeSceneTransitionAnimation(getActivity(), animeImage, "animeImage");
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onLinkDataLongClick(LinkWithAllData linkData) {
        LinkDataBottomSheet bottomSheet = new LinkDataBottomSheet(linkData, prefs.getString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, "ask"));
        bottomSheet.show(getActivity().getSupportFragmentManager(), "LDBottomSheet");
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
                Intent intent = AdvancedView.prepareIntent(getContext(), data);
                //ActivityOptions options = ActivityOptions
                //        .makeSceneTransitionAnimation(getActivity(), holder.animeImageView, "animeImage");
                startActivity(intent);
            });

            holder.mainLayout.setOnLongClickListener(view -> {
                LinkDataBottomSheet bottomSheet = new LinkDataBottomSheet(data, prefs.getString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, "ask"));
                bottomSheet.show(getActivity().getSupportFragmentManager(), "LDBottomSheet");
                return true;
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

    private class AnimeCatalogGridAdapter extends LinkDataGridRecyclerAdapter {

        public AnimeCatalogGridAdapter(List<LinkWithAllData> recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        private void setAnimation(View viewToAnimate, int position)
        {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fall_down);
            viewToAnimate.startAnimation(animation);
        }

        @Override
        public void onBindViewHolder(@NonNull LinkDataGridViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            //Log.d("List", "Binding listener");
            LinkWithAllData data = dataList.get(position);
            holder.mainLayout.setOnClickListener(v -> {
                Intent intent = AdvancedView.prepareIntent(getContext(), data);

                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(getActivity(), holder.animeImageView, "animeImage");
                /*ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(getActivity());*/
                startActivity(intent, options.toBundle());
            });
            holder.mainLayout.setOnLongClickListener(view -> {
                LinkDataBottomSheet bottomSheet = new LinkDataBottomSheet(data, prefs.getString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, "ask"));
                bottomSheet.show(getActivity().getSupportFragmentManager(), "LDBottomSheet");
                return true;
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

            //setAnimation(holder.mainLayout, position);
        }
    }

    private void sendNewEpisodeNotification(LinkWithAllData linkWithAllData, int episodeNum) {

        Intent intent = new Intent(getContext(), LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String animeImageUrl = linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL);
        Bitmap bitmap;
        try {
            bitmap = Glide.with(getContext()).asBitmap().load(animeImageUrl).skipMemoryCache(true).into(150, 150).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "LinkCastNotification")
                .setSmallIcon(R.drawable.ic_stat_lc_notification_small)
                .setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setContentTitle("New episode")
                .setContentText(linkWithAllData.getTitle() + "Episode - " + episodeNum + " is out")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if(bitmap != null) {
            builder.setLargeIcon(bitmap);
        }
        else {
            builder.setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(),
                    R.mipmap.ic_launcher_notification));
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        int notificationID = linkWithAllData.malMetaData != null ? Integer.parseInt(linkWithAllData.malMetaData.getId()) : Utils.getRandomInt(1, 65535);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(notificationID, builder.build());
    }

    private boolean hasListChanged(List<LinkWithAllData> oldList, List<LinkWithAllData> newList) {
        if(oldList.size() != newList.size())    return true;
        Map<String, LinkWithAllData> linkMap = new HashMap<>();
        for(LinkWithAllData linkData: newList)  linkMap.put(linkData.getId(), linkData);
        for(LinkWithAllData oldLinkData: oldList) {
            LinkWithAllData newLinkData = linkMap.get(oldLinkData.getId());
            if(!oldLinkData.equals(newLinkData)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //SharedAnimeLinkDataViewModel viewModel = new ViewModelProvider(getActivity()).get(SharedAnimeLinkDataViewModel.class);

        //new ViewModelProvider(getActivity()).get(MangaDataViewModel.class).getData(); // load manga cache

        LinkDataViewModel viewModel = new ViewModelProvider(getActivity()).get(LinkDataViewModel.class);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        viewModel.getAnimeLinks().observe(getViewLifecycleOwner(), linkDataList -> {
            Log.d(TAG, "Data changed");
            List<LinkWithAllData> tempDataList = new ArrayList<>();
            for(LinkWithAllData linkWithAllData: linkDataList) {
                AnimeLinkData animeLinkData = AnimeLinkData.from(linkWithAllData.linkData);
                if(animeLinkData.getTitle() == null) {
                    Log.d(TAG, "why");
                }
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
                    tempDataList.add(linkWithAllData);
                }
            }
            if(hasListChanged(dataList, tempDataList)) {
                dataList.clear();
                dataList.addAll(tempDataList);
                refreshAdapter();
            }
        });
    }

    @Override
    public RecyclerView.Adapter getAdapter(List<LinkWithAllData> adapterDataList, Context context) {
        return new AnimeCatalogListAdapter(adapterDataList, context);
    }

    @Override
    public RecyclerView.Adapter getListAdapter(List<LinkWithAllData> adapterDataList, Context context) {
        return new AnimeCatalogListAdapter(adapterDataList, context);
    }

    @Override
    public RecyclerView.Adapter getGridAdapter(List<LinkWithAllData> adapterDataList, Context context) {
        return new AnimeCatalogGridAdapter(adapterDataList, context);
    }
}

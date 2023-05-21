package com.dhavalpateln.linkcast.worker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.WorkerParameters;

import com.bumptech.glide.Glide;
import com.dhavalpateln.linkcast.LauncherActivity;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeMALMetaData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.JikanDatabase;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaData;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.extractors.MangaExtractor;
import com.dhavalpateln.linkcast.extractors.Providers;
import com.dhavalpateln.linkcast.utils.Utils;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class LinkMonitorTask extends LinkCastWorker {

    private final Map<String, MangaExtractor> mangaExtractors;
    private List<LinkWithAllData> links;
    private Map<String, AnimeExtractor> animeExtractors;
    private String CHANNEL_ID = "LinkCastNotification";
    private String TAG = "LinkMonitorWorker";
    private Set<String> monitorStatusSet;

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public LinkMonitorTask(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        this.animeExtractors = Providers.getAnimeExtractors();
        this.mangaExtractors = Providers.getMangaExtractors();
        this.monitorStatusSet = new HashSet<>();
        this.monitorStatusSet.add("planned");
        this.monitorStatusSet.add("watching");
        this.monitorStatusSet.add("reading");
    }

    private void sendNewEpisodeNotification(LinkWithAllData linkWithAllData, int episodeNum) {

        Intent intent = new Intent(getApplicationContext(), LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String animeImageUrl = linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL);
        Bitmap bitmap;
        try {
            bitmap = Glide.with(getApplicationContext()).asBitmap().load(animeImageUrl).skipMemoryCache(true).into(150, 150).get();
        } catch (ExecutionException e) {
            bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.mipmap.ic_launcher_notification);
        } catch (InterruptedException e) {
            bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.mipmap.ic_launcher_notification);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_lc_notification_small)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if(linkWithAllData.linkData.getType().equalsIgnoreCase("anime")) {
            builder.setContentTitle("New episode")
                    .setContentText(linkWithAllData.getTitle() + ", Episode - " + episodeNum + " is out");
        }
        else {
            builder.setContentTitle("New chapter")
                    .setContentText(linkWithAllData.getTitle() + ", Chapter - " + episodeNum + " is out");
        }

        if(bitmap != null) {
            builder.setLargeIcon(bitmap);
        }
        else {
            builder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.mipmap.ic_launcher_notification));
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        int notificationID = linkWithAllData.alMalMetaData != null ? Integer.parseInt(linkWithAllData.alMalMetaData.getId()) : Utils.getRandomInt(1, 65535);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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

    private boolean checkNewEpisode(AnimeLinkData data, LinkWithAllData linkWithAllData) {
        int lastFetchedCount = linkWithAllData.linkMetaData.getLastEpisodeNodesFetchCount();
        Log.d(TAG, "Checking new episode for: " + data.getId());
        Log.d(TAG, "Anime name: " + linkWithAllData.getTitle());
        String source = data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
        if(this.animeExtractors.containsKey(source)) {
            AnimeExtractor extractor = this.animeExtractors.get(source);
            if(extractor.requiresInit())    extractor.init();
            int episodes = extractor.extractData(data).size();
            if(episodes < lastFetchedCount) {
                return false;
            }
            getRoomRepo().updateLastFetchedEpisode(linkWithAllData, episodes);
            if(episodes - lastFetchedCount == 1) {
                sendNewEpisodeNotification(linkWithAllData, episodes);
                return true;
            }
        }
        return false;
    }

    private boolean checkNewChapter(AnimeLinkData data, LinkWithAllData linkWithAllData) {
        int lastFetchedCount = linkWithAllData.linkMetaData.getLastEpisodeNodesFetchCount();
        Log.d(TAG, "Checking new episode for: " + data.getId());
        Log.d(TAG, "Manga name: " + linkWithAllData.getTitle());
        String source = data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
        if(this.mangaExtractors.containsKey(source)) {
            MangaExtractor extractor = this.mangaExtractors.get(source);
            if(extractor.requiresInit())    extractor.init();
            int episodes = extractor.extractData(data).size();
            if(episodes < lastFetchedCount) {
                return false;
            }
            linkWithAllData.linkMetaData.setLastEpisodeNodesFetchCount(episodes);
            getRoomRepo().insert(linkWithAllData.linkMetaData);
            if(episodes - lastFetchedCount == 1) {
                sendNewEpisodeNotification(linkWithAllData, episodes);
                return true;
            }
        }
        return false;
    }

    private boolean shouldCheckNewEpisode(AnimeLinkData animeLinkData, LinkWithAllData linkWithAllData) {
        if(linkWithAllData.alMalMetaData != null) {
            return !linkWithAllData.alMalMetaData.getStatus().toLowerCase().contains("finished") ||
                    (monitorStatusSet.contains(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS).toLowerCase())
                            && Utils.isNumeric(linkWithAllData.alMalMetaData.getTotalEpisodes())
                            && linkWithAllData.linkMetaData.getLastEpisodeNodesFetchCount() < Integer.parseInt(linkWithAllData.alMalMetaData.getTotalEpisodes()));
        }
        else if(monitorStatusSet.contains(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS).toLowerCase())) {
            return true;
        }
        return false;
    }

    private boolean shouldUpdateMalMetaData(LinkWithAllData linkWithAllData) {
        if(linkWithAllData.linkData.getMalId() != null) {
            return linkWithAllData.alMalMetaData == null || !linkWithAllData.alMalMetaData.getStatus().toLowerCase().contains("finished");
        }
        return false;
    }

    @Override
    public void run() {

        this.links = getRoomRepo().getLinkWithData();

        int exceptionsOccured = 0;
        List<String> exceptionList = new ArrayList<>();
        int newEpiChecked = 0;
        int newEpisNoti = 0;
        int malUpdated = 0;

        for(LinkWithAllData linkWithAllData: this.links) {
            LinkData linkData = linkWithAllData.linkData;
            try {
                AnimeLinkData animeLinkData = AnimeLinkData.from(linkData);
                // Only anime supported ATM
                //if(!animeLinkData.isAnime()) continue;


                // init local link meta data
                if(linkWithAllData.linkMetaData == null) {
                    linkWithAllData.linkMetaData = new LinkMetaData();
                    linkWithAllData.linkMetaData.setId(linkWithAllData.linkData.getId());
                    linkWithAllData.linkMetaData.setLastEpisodeNodesFetchCount(-2);
                    linkWithAllData.linkMetaData.setMiscData(new HashMap<>());
                    getRoomRepo().insert(linkWithAllData.linkMetaData);
                }

                // update mal meta data if required
                if(shouldUpdateMalMetaData(linkWithAllData)) {
                    AnimeMALMetaData animeMALMetaData = JikanDatabase.getInstance().getMalMetaData(linkData.getMalId(), animeLinkData.isAnime());
                    linkWithAllData.alMalMetaData = AlMalMetaData.from(animeMALMetaData);
                    getRoomRepo().insert(linkWithAllData.alMalMetaData);
                    malUpdated++;
                }

                if(shouldCheckNewEpisode(animeLinkData, linkWithAllData)) {
                    newEpiChecked++;

                    boolean hasNewEpisode;
                    if(linkWithAllData.linkData.getType().equalsIgnoreCase("anime")) {
                        hasNewEpisode = checkNewEpisode(animeLinkData, linkWithAllData);
                    }
                    else {
                        hasNewEpisode = checkNewChapter(animeLinkData, linkWithAllData);
                    }
                    if(hasNewEpisode)   newEpisNoti++;
                }
            } catch (Exception e) {
                exceptionsOccured++;
                exceptionList.add(e.toString());
                e.printStackTrace();
                Log.d("WORKER", "Exception while updating " + linkData.getId());
            }

        }

        DatabaseReference ref = FirebaseDBHelper.getUserWorkerRunHistory().child(Utils.getCurrentTime());
        ref.child("newepi").setValue(newEpiChecked);
        ref.child("ex").setValue(exceptionsOccured);
        ref.child("exlist").setValue(exceptionList);
        ref.child("newnoti").setValue(newEpisNoti);
        ref.child("mal").setValue(malUpdated);

        getCallback().onComplete();
    }

    @Override
    public int getVersion() {
        return 0;
    }
}

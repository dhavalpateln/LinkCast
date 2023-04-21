package com.dhavalpateln.linkcast.worker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.WorkerParameters;

import com.dhavalpateln.linkcast.LauncherActivity;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeMALMetaData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.JikanDatabase;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.database.room.maldata.MALMetaData;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.extractors.Providers;
import com.dhavalpateln.linkcast.utils.Utils;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LinkMonitorTask extends LinkCastWorker {

    private List<LinkWithAllData> links;
    private Map<String, AnimeExtractor> extractors;
    private String CHANNEL_ID = "LinkCastNotification";
    private String TAG = "LinkMonitorWorker";
    private Set<String> monitorStatusSet;

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public LinkMonitorTask(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        this.extractors = Providers.getAnimeExtractors();
        this.monitorStatusSet = new HashSet<>();
        this.monitorStatusSet.add("planned");
        this.monitorStatusSet.add("watching");
    }

    private void sendNewEpisodeNotification(LinkWithAllData linkWithAllData, int episodeNum) {

        Intent intent = new Intent(getApplicationContext(), LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_lc_notification_small)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_launcher_notification))
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setContentTitle("New episode")
                .setContentText(linkWithAllData.getTitle() + "Episode - " + episodeNum + " is out")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        int notificationID = linkWithAllData.malMetaData != null ? Integer.parseInt(linkWithAllData.malMetaData.getId()) : Utils.getRandomInt(1, 65535);
        notificationManager.notify(notificationID, builder.build());
    }

    private boolean checkNewEpisode(AnimeLinkData data, LinkWithAllData linkWithAllData) {
        int lastFetchedCount = linkWithAllData.linkMetaData.getLastEpisodeNodesFetchCount();
        Log.d(TAG, "Checking new episode for: " + data.getId());
        Log.d(TAG, "Anime name: " + linkWithAllData.getTitle());
        String source = data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
        if(this.extractors.containsKey(source)) {
            AnimeExtractor extractor = this.extractors.get(source);
            if(extractor.requiresInit())    extractor.init();
            int episodes = extractor.extractData(data).size();
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
        if(linkWithAllData.malMetaData != null) {
            return !linkWithAllData.malMetaData.getStatus().equalsIgnoreCase("finished airing") ||
                    (monitorStatusSet.contains(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS).toLowerCase())
                            && Utils.isNumeric(linkWithAllData.malMetaData.getTotalEpisodes())
                            && linkWithAllData.linkMetaData.getLastEpisodeNodesFetchCount() < Integer.parseInt(linkWithAllData.malMetaData.getTotalEpisodes()));
        }
        else if(monitorStatusSet.contains(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS).toLowerCase())) {
            return true;
        }
        return false;
    }

    private boolean shouldUpdateMalMetaData(LinkWithAllData linkWithAllData) {
        if(linkWithAllData.linkData.getMalId() != null) {
            return linkWithAllData.malMetaData == null || !linkWithAllData.malMetaData.getStatus().equalsIgnoreCase("finished airing");
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
                if(!animeLinkData.isAnime()) continue;


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
                    linkWithAllData.malMetaData = MALMetaData.from(animeMALMetaData);
                    getRoomRepo().insert(linkWithAllData.malMetaData);
                    malUpdated++;
                }

                if(shouldCheckNewEpisode(animeLinkData, linkWithAllData)) {
                    newEpiChecked++;
                    boolean hasNewEpisode = checkNewEpisode(animeLinkData, linkWithAllData);
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

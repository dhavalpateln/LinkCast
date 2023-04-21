package com.dhavalpateln.linkcast.worker.tasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.dhavalpateln.linkcast.LauncherActivity;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeMALMetaData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.explorer.tasks.ExtractMALMetaData;
import com.dhavalpateln.linkcast.explorer.tasks.MALMetaDataListener;
import com.dhavalpateln.linkcast.explorer.tasks.TaskCompleteListener;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.extractors.Providers;
import com.dhavalpateln.linkcast.utils.Utils;
import com.dhavalpateln.linkcast.worker.LinkCastWorkerCallback;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkUpdaterTask implements Runnable, MALMetaDataListener, TaskCompleteListener {

    private List<AnimeLinkData> links;
    private LinkCastWorkerCallback callback;
    private Map<String, AnimeExtractor> extractors;
    private String CHANNEL_ID = "LinkCastNotification";
    private Context context;
    private String TAG = "LinkUpdaterWorker";

    public LinkUpdaterTask(Context context, List<AnimeLinkData> links, LinkCastWorkerCallback callback) {
        this.context = context;
        this.callback = callback;
        this.links = links;
        this.extractors = Providers.getAnimeExtractors();
    }

    private void validateData(AnimeLinkData data) {
        int dataVer = Integer.valueOf(data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_VERSION));
        if(dataVer < 1) {
            if(data.getMalMetaData() == null) {
                new ExtractMALMetaData(data, this, this, false).run();
            }
        }
        data.updateData(AnimeLinkData.DataContract.DATA_VERSION, "1", true, data.isAnime());
    }

    private void sendNewEpisodeNotification(AnimeLinkData data) {

        Intent intent = new Intent(this.context, LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_lc_notification_small)
                .setLargeIcon(BitmapFactory.decodeResource(this.context.getResources(),
                        R.mipmap.ic_launcher_notification))
                .setColor(ContextCompat.getColor(this.context, R.color.colorPrimary))
                .setContentTitle("New episode")
                .setContentText(data.getMalMetaData().getName() + "Episode - " + data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_LAST_FETCHED_EPISODES) + " is out")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.context);

        notificationManager.notify(Integer.parseInt(data.getMalMetaData().getId()), builder.build());
    }

    private void checkNewEpisode(AnimeLinkData data) {
        int lastFetchedCount = Integer.parseInt(data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_LAST_FETCHED_EPISODES));
        if(Utils.isNumeric(data.getMalMetaData().getTotalEpisodes())) {
            int totalAnimeEpisodes = Integer.parseInt(data.getMalMetaData().getTotalEpisodes());
            if(totalAnimeEpisodes <= lastFetchedCount) {
                return;
            }
        }
        Log.d(TAG, "Checking new episode for: " + data.getId());
        Log.d(TAG, "Anime name: " + data.getMalMetaData().getName());
        String source = data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
        if(this.extractors.containsKey(source)) {
            int episodes = this.extractors.get(source).extractData(data).size();
            data.updateData(AnimeLinkData.DataContract.DATA_LAST_FETCHED_EPISODES, String.valueOf(episodes), true, data.isAnime());
            if(episodes - lastFetchedCount == 1) {
                sendNewEpisodeNotification(data);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {
        Map<String, Object> metricCount = new HashMap<>();
        int exceptionsOccured = 0;
        List<String> exceptionList = new ArrayList<>();
        int newEpiChecked = 0;

        for(AnimeLinkData linkData: this.links) {
            try {
                validateData(linkData);
                if (linkData.getMalMetaData() != null) {
                    if(!linkData.getMalMetaData().getStatus().equalsIgnoreCase("finished airing")) {
                        newEpiChecked++;
                        checkNewEpisode(linkData);
                    }
                }
            } catch (Exception e) {
                exceptionsOccured++;
                exceptionList.add(e.toString());
                Log.d("WORKER", "Exception while updating " + linkData.getId());
            }

        }

        DatabaseReference ref = FirebaseDBHelper.getUserWorkerRunHistory().child(Utils.getCurrentTime());
        ref.child("newepi").setValue(newEpiChecked);
        ref.child("ex").setValue(exceptionsOccured);
        ref.child("exlist").setValue(exceptionList);

        this.callback.onComplete();
    }

    @Override
    public void onMALMetaData(AnimeMALMetaData metaData) {
        Log.d("WORKER", "Updated MAL meta data for " + metaData.getId());
    }


    @Override
    public void onTaskCompleted(String taskName) {}
}

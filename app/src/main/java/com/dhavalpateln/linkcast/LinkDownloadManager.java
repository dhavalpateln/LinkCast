package com.dhavalpateln.linkcast;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.Context.DOWNLOAD_SERVICE;
import static java.security.AccessController.getContext;

public class LinkDownloadManager {

    private DownloadManager.Request request;
    private LinkDownloadListener listener;
    private long downloadID;

    public interface LinkDownloadListener {
        void onDownloadComplete();
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                listener.onDownloadComplete();
            }
        }
    };



    public LinkDownloadManager(String source, String fileName, LinkDownloadListener listener) {



        this.listener = listener;

        fileName = fileName.substring(0,1).toUpperCase() + fileName.substring(1);

        request = new DownloadManager.Request(Uri.parse(source))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "LinkCast/" + fileName)// Uri of the destination file
                .setTitle(fileName)// Title of the Download Notification
                .setDescription("Downloading " + fileName)// Description of the Download Notificatio
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
    }

    public void startDownload(Context context) {
        DownloadManager downloadManager= (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.



    }
}

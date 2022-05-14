package com.dhavalpateln.linkcast.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.DOWNLOAD_SERVICE;

public class LinkDownloadManagerDialog extends LinkCastDialog {

    private DownloadManager.Request request;
    private LinkDownloadListener listener;
    private String fileName;
    private String source;
    private String referer;
    private long downloadID;
    private EditText fileNameEditText;

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

    public LinkDownloadManagerDialog(String source, String fileName, LinkDownloadListener listener) {
        this.source = source;
        this.fileName = fileName.substring(0,1).toUpperCase() + fileName.substring(1);
        this.listener = listener;
        this.referer = null;
    }

    public LinkDownloadManagerDialog(String source, String fileName, String referer, LinkDownloadListener listener) {
        this.source = source;
        this.fileName = fileName.substring(0,1).toUpperCase() + fileName.substring(1);
        this.listener = listener;
        this.referer = referer;
    }

    @Override
    public int getContentLayout() {
        return R.layout.link_download_manager_dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = getContentView();
        fileNameEditText = view.findViewById(R.id.link_download_manager_fileName_EditText);
        fileNameEditText.setText(this.fileName);

        view.findViewById(R.id.link_download_manager_download_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request = new DownloadManager.Request(Uri.parse(source))
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                        //.setDestinationUri(Uri.parse("/Network storage/RASPBERRYPI/pidisk1/" + fileName))
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "LinkCast/" + fileName)// Uri of the destination file
                        .setTitle(fileName)// Title of the Download Notification
                        .setDescription("Downloading " + fileName)// Description of the Download Notification
                        .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                        .setAllowedOverRoaming(true);// Set if download is allowed on roaming network

                if(referer != null) {
                    request.addRequestHeader("Referer", referer);
                }


                DownloadManager downloadManager= (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);

                downloadID = downloadManager.enqueue(request);
                LinkDownloadManagerDialog.this.getDialog().cancel();
            }
        });
        view.findViewById(R.id.link_download_manager_cancel_button).setOnClickListener(v -> LinkDownloadManagerDialog.this.getDialog().cancel());
        return dialog;
    }
}

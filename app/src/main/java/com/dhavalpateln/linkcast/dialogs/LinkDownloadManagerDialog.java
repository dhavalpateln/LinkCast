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

import static android.content.Context.DOWNLOAD_SERVICE;

public class LinkDownloadManagerDialog extends DialogFragment {

    private static final String LOCAL_DOWNLOAD = "Local";

    private DownloadManager.Request request;
    private LinkDownloadListener listener;
    private String fileName;
    private String source;
    private long downloadID;
    private EditText fileNameEditText;
    private Spinner downloadTypeSpinner;
    ArrayAdapter<String> spinnerAdapter;

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
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.link_download_manager_dialog, null);
        fileNameEditText = view.findViewById(R.id.link_download_manager_fileName_EditText);
        downloadTypeSpinner = view.findViewById(R.id.link_download_manager_spinner);
        fileNameEditText.setText(this.fileName);
        spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item);
        downloadTypeSpinner.setAdapter(spinnerAdapter);
        spinnerAdapter.setNotifyOnChange(true);
        spinnerAdapter.add(LOCAL_DOWNLOAD);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        view.findViewById(R.id.link_download_manager_download_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDownloadType = (String) downloadTypeSpinner.getSelectedItem();
                if(selectedDownloadType == LOCAL_DOWNLOAD) {
                    request = new DownloadManager.Request(Uri.parse(source))
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                            //.setDestinationUri(Uri.parse("/Network storage/RASPBERRYPI/pidisk1/" + fileName))
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "LinkCast/" + fileName)// Uri of the destination file
                            .setTitle(fileName)// Title of the Download Notification
                            .setDescription("Downloading " + fileName)// Description of the Download Notificatio
                            .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                            .setAllowedOverRoaming(true);// Set if download is allowed on roaming network

                    DownloadManager downloadManager= (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);
                    downloadID = downloadManager.enqueue(request);
                }
                LinkDownloadManagerDialog.this.getDialog().cancel();
            }
        });
        view.findViewById(R.id.link_download_manager_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkDownloadManagerDialog.this.getDialog().cancel();
            }
        });
        return builder.create();
    }
}

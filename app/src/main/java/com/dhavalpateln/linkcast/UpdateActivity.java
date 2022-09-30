package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.dhavalpateln.linkcast.dialogs.LinkDownloadManagerDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class UpdateActivity extends AppCompatActivity {

    public final String TAG = "UPDATE_ACTIVITY";
    private TextView versionTextView;
    private TextView updateTextView;
    private long downloadID;
    private ProgressDialog progressDialog;

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast

            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                progressDialog.cancel();
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor c = manager.query(query);
                if(c.moveToFirst()) {
                    do {
                        @SuppressLint("Range") String name = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        File mFile = new File(Uri.parse(name).getPath());
                        String path = mFile.getAbsolutePath();
                        Log.d("DOWNLOAD LISTENER", "file name: " + path);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                        builder.setTitle("Update ready to install");
                        builder.setPositiveButton("Install", (dialogInterface, i) -> {
                            Intent apkInstallIntent = new Intent(Intent.ACTION_VIEW);
                            apkInstallIntent.setData(Uri.parse(name));
                            startActivity(apkInstallIntent);
                            dialogInterface.cancel();
                        });
                        File toInstall = new File(path);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", toInstall);
                            Intent apkInstallIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            apkInstallIntent.setData(apkUri);
                            apkInstallIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(apkInstallIntent);
                        }

                    } while (c.moveToNext());
                }
                c.close();

            }
        }
    };

    private BroadcastReceiver onNotificationClicked = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(onNotificationClicked, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        versionTextView = findViewById(R.id.versionTextView);
        updateTextView = findViewById(R.id.updatesTextView);

        FirebaseDBHelper.getValue(FirebaseDBHelper.getAppVersionRef(), dataSnapshot -> {
            String version = dataSnapshot.getValue().toString();
            versionTextView.setText(version);
            FirebaseDBHelper.getValue(FirebaseDBHelper.getUpdatesRef(version), updateSnapshot -> {
                String html = "<ul>";
                for(DataSnapshot updateChild: updateSnapshot.getChildren()) {
                    String updateStr = updateChild.getValue().toString();
                    html += "<li>" + updateStr + "</li>";
                }
                html += "</ul>";
                updateTextView.setText(Html.fromHtml(html));
            });
        });

    }

    public void download(View view) {
        DatabaseReference linkRef = FirebaseDBHelper.getAppAPKLinkRef();
        linkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String source = dataSnapshot.getValue().toString();
                String fileName = "LinkCast." + versionTextView.getText().toString() + ".apk";
                Log.i(TAG, "Link obtained : " + source);

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(source))
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                        //.setDestinationUri(Uri.parse("/Network storage/RASPBERRYPI/pidisk1/" + fileName))
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "LinkCast/" + fileName)// Uri of the destination file
                        .setTitle(fileName)// Title of the Download Notification
                        .setDescription("Downloading " + fileName)// Description of the Download Notification
                        .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                        .setAllowedOverRoaming(true);// Set if download is allowed on roaming network

                DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                downloadID = downloadManager.enqueue(request);

                progressDialog = new ProgressDialog(UpdateActivity.this);
                progressDialog.setMessage("Downloading...");
                progressDialog.setCancelable(false);
                progressDialog.show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void copyLink(View view) {
        DatabaseReference linkRef = FirebaseDBHelper.getAppAPKLinkRef();
        linkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String link = dataSnapshot.getValue().toString();
                Log.i(TAG, "Link obtained : " + link);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("apk link", link);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Copied", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}

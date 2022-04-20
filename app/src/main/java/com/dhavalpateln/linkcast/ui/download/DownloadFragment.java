package com.dhavalpateln.linkcast.ui.download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dhavalpateln.linkcast.LinkMaterialCardView;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.ListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeListViewHolder;
import com.dhavalpateln.linkcast.utils.UIBuilder;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadFragment extends Fragment {

    private String TAG = "DownloadFragment";

    private Map<String, MaterialCardView> viewMap;
    private List<File> downloadFileList;
    private ListRecyclerAdapter<File> listRecyclerAdapter;
    private RecyclerView recyclerView;
    private Button openFolderButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_download, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.download_recycler_view);
        openFolderButton = view.findViewById(R.id.open_download_folder_button);
        downloadFileList = new ArrayList<>();

        openFolderButton.setOnClickListener(v -> openDownloadFolder());

        listRecyclerAdapter = new DownloadListAdapter(downloadFileList, getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(listRecyclerAdapter);

        refreshDownloads();
    }

    private class DownloadListAdapter extends ListRecyclerAdapter<File> {

        public DownloadListAdapter(List<File> recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(@NonNull AnimeListViewHolder holder, int position) {

            File data = downloadFileList.get(position);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250);
            layoutParams.setMargins(4, 4, 4, 4);
            holder.mainLayout.setLayoutParams(layoutParams);
            holder.animeImageView.setVisibility(View.GONE);
            holder.subTextView.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            holder.titleTextView.setText(data.getName());

            holder.openButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(Uri.parse(data.getAbsolutePath()), "video/*");
                startActivity(intent);
            });

            holder.deleteButton.setOnClickListener(v -> {
                boolean fileDeleted = data.delete();
                if(fileDeleted) {
                    downloadFileList.remove(data);
                    listRecyclerAdapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(getContext(), "Unable to delete", Toast.LENGTH_LONG).show();
                }
            });

            holder.editButton.setVisibility(View.GONE);
        }
    }

    private void openDownloads() {
        if (isSamsung()) {
            Intent intent = getActivity().getPackageManager()
                    .getLaunchIntentForPackage("com.sec.android.app.myfiles");
            intent.setAction("samsung.myfiles.intent.action.LAUNCH_MY_FILES");
            intent.putExtra("samsung.myfiles.intent.extra.START_PATH",
                    getDownloadsFile().getPath());
            startActivity(intent);
        }
        else startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
    }

    private boolean isSamsung() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer != null) return manufacturer.toLowerCase().equals("samsung");
        return false;
    }

    private File getDownloadsFile() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    private void openDownloadFolder() {
        File downloadDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/LinkCast");
        openDownloads();
        //if(downloadDirectory != null) {
        //    Intent intent = new Intent(Intent.ACTION_VIEW);
        //    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //    intent.setDataAndType(Uri.fromFile(downloadDirectory), "*/*");
        //    startActivity(intent);
        //}
    }

    private void refreshDownloads() {
        File downloadDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/LinkCast");
        if (downloadDirectory != null) {
            downloadFileList.clear();
            for (File file : downloadDirectory.listFiles()) {
                if (!file.getName().endsWith(".mp4")) {
                    continue;
                }
                downloadFileList.add(file);
            }
            listRecyclerAdapter.notifyDataSetChanged();
        }
    }
}
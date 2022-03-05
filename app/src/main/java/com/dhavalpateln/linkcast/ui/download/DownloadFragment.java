package com.dhavalpateln.linkcast.ui.download;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhavalpateln.linkcast.LinkMaterialCardView;
import com.dhavalpateln.linkcast.R;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DownloadFragment extends Fragment {

    private Map<String, MaterialCardView> viewMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_download, container, false);

        viewMap = new HashMap<>();

        final LinearLayout linearLayout = root.findViewById(R.id.download_linear_layout);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/LinkCast");
        if(file != null) {
            for (File fileElement : file.listFiles()) {
                String fileName = fileElement.getName();
                String filePath = fileElement.getAbsolutePath();
                if (!fileName.endsWith(".mp4")) {
                    continue;
                }
                LinkMaterialCardView card = new LinkMaterialCardView(getContext(), filePath, fileName, filePath);
                card.addButton(getContext(), "OPEN", new LinkMaterialCardView.MaterialCardViewButtonClickListener() {
                    @Override
                    public void onClick(String id, String title, String url, Map<String, String> data) {
                        //Uri uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", new File(url));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        //intent.setData(Uri.parse(url));
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(Uri.parse(url), "video/*");
                        startActivity(intent);
                    }
                });
                card.addButton(getContext(), "DELETE", new LinkMaterialCardView.MaterialCardViewButtonClickListener() {
                    @Override
                    public void onClick(String id, String title, String url, Map<String, String> data) {
                        new File(url).delete();
                        //Uri uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", new File(url));
                        linearLayout.removeView(viewMap.get(id));
                    }
                });
                linearLayout.addView(card.getCard(), 0);
                viewMap.put(filePath, card.getCard());
            }
        }
        return root;
    }

}
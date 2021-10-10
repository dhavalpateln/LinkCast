package com.dhavalpateln.linkcast.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dhavalpateln.linkcast.LinkMaterialCardView;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.Link;
import com.dhavalpateln.linkcast.dialogs.LinkDownloadManagerDialog;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private Map<String, LinkMaterialCardView> viewMap;
    private DatabaseReference linkRef;
    private ChildEventListener linkChildEventListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);

        viewMap = new HashMap<>();

        final LinearLayout linearLayout = root.findViewById(R.id.homeLinearLayout);

        linkRef = FirebaseDBHelper.getUserLinkRef();
        linkChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Link link = dataSnapshot.getValue(Link.class);
                LinkMaterialCardView card = new LinkMaterialCardView(getContext(), dataSnapshot.getKey(), link.getTitle(), link.getUrl());
                card.addButton(getContext(), "OPEN", new LinkMaterialCardView.MaterialCardViewButtonClickListener() {
                    @Override
                    public void onClick(String id, String title, String url, Map<String, String> data) {
                        /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);*/
                        Intent intent = new Intent(getContext(), ExoPlayerActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("title", title);
                        if(data != null) {
                            if(data.containsKey("Referer")) {
                                intent.putExtra("Referer", data.get("Referer"));
                            }
                        }
                        startActivity(intent);
                    }
                });
                card.addButton(getContext(), "DELETE", new LinkMaterialCardView.MaterialCardViewButtonClickListener() {
                    @Override
                    public void onClick(String id, String title, String url, Map<String, String> data) {
                        FirebaseDBHelper.removeLink(id);
                    }
                });
                card.addButton(getContext(), "DOWNLOAD", new LinkMaterialCardView.MaterialCardViewButtonClickListener() {
                    @Override
                    public void onClick(String id, String title, String url, Map<String, String> data) {

                        LinkDownloadManagerDialog linkDownloadManagerDialog = new LinkDownloadManagerDialog(url, title + ".mp4", new LinkDownloadManagerDialog.LinkDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                Toast.makeText(getContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                            }
                        });
                        linkDownloadManagerDialog.show(getParentFragmentManager(), "Download");

                    }
                });

                linearLayout.addView(card.getCard(), 1);
                viewMap.put(dataSnapshot.getKey(), card);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                viewMap.get(dataSnapshot.getKey()).updateData((Map<String, String>) dataSnapshot.child("data").getValue());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                linearLayout.removeView(viewMap.get(dataSnapshot.getKey()).getCard());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        linkRef.addChildEventListener(linkChildEventListener);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        linkRef.removeEventListener(linkChildEventListener);
    }
}
package com.dhavalpateln.linkcast.ui.animes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.LinkMaterialCardView;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.Link;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.listeners.CrashListener;
import com.dhavalpateln.linkcast.ui.feedback.CrashReportActivity;
import com.dhavalpateln.linkcast.ui.feedback.FeedbackFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class AnimeFragment extends Fragment {

    private Map<String, LinkMaterialCardView> viewMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_anime, container, false);
        viewMap = new HashMap<>();
        final LinearLayout linearLayout = root.findViewById(R.id.anime_links_linear_layout);
        DatabaseReference linkRef = FirebaseDBHelper.getUserAnimeWebExplorerLinkRef();
        linkRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    Link link = dataSnapshot.getValue(Link.class);
                    //MaterialCardView card = createCard(dataSnapshot.getKey(), link.getTitle(), link.getUrl());
                    LinkMaterialCardView card = new LinkMaterialCardView(getContext(), dataSnapshot.getKey(), link.getTitle(), link.getUrl());
                    card.addButton(getContext(), "OPEN", new LinkMaterialCardView.MaterialCardViewButtonClickListener() {
                        @Override
                        public void onClick(String id, String title, String url) {
                            Intent intent = new Intent(getContext(), AnimeWebExplorer.class);
                            intent.putExtra("search", url);
                            intent.putExtra("source", "saved");
                            intent.putExtra("id", id);
                            intent.putExtra("title", title);
                            startActivity(intent);
                        }
                    });
                    card.addButton(getContext(), "DELETE", new LinkMaterialCardView.MaterialCardViewButtonClickListener() {
                        @Override
                        public void onClick(String id, String title, String url) {
                            FirebaseDBHelper.removeAnimeLink(id);
                        }
                    });
                    card.addButton(getContext(), "EDIT", new LinkMaterialCardView.MaterialCardViewButtonClickListener() {
                        @Override
                        public void onClick(String id, String title, String url) {
                            BookmarkLinkDialog dialog = new BookmarkLinkDialog(id, title, url);
                            dialog.show(getParentFragmentManager(), "bookmarkEdit");
                        }
                    });

                    linearLayout.addView(card.getCard(), 1);
                    viewMap.put(dataSnapshot.getKey(), card);
                }
                catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    Intent crashIntent = new Intent(getContext(), CrashReportActivity.class);
                    crashIntent.putExtra("subject", "Crash");
                    crashIntent.putExtra("message", sw.toString());
                    startActivity(crashIntent);

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                viewMap.get(dataSnapshot.getKey()).cardTitle(dataSnapshot.child("title").getValue().toString());
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
        });
        return root;
    }

}
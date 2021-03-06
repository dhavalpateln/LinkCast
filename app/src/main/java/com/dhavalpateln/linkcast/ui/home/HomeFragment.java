package com.dhavalpateln.linkcast.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.LinkMaterialCardView;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.Link;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private Map<String, MaterialCardView> viewMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);

        viewMap = new HashMap<>();

        final LinearLayout linearLayout = root.findViewById(R.id.homeLinearLayout);

        DatabaseReference linkRef = FirebaseDBHelper.getUserLinkRef();
        linkRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Link link = dataSnapshot.getValue(Link.class);
                LinkMaterialCardView card = new LinkMaterialCardView(getContext(), dataSnapshot.getKey(), link.getTitle(), link.getUrl());
                card.addButton(getContext(), "OPEN", new LinkMaterialCardView.MaterialCardViewButtonClickListener() {
                    @Override
                    public void onClick(String id, String title, String url) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    }
                });
                card.addButton(getContext(), "DELETE", new LinkMaterialCardView.MaterialCardViewButtonClickListener() {
                    @Override
                    public void onClick(String id, String title, String url) {
                        FirebaseDBHelper.removeLink(id);
                    }
                });

                linearLayout.addView(card.getCard(), 1);
                viewMap.put(dataSnapshot.getKey(), card.getCard());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                linearLayout.removeView(viewMap.get(dataSnapshot.getKey()));
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
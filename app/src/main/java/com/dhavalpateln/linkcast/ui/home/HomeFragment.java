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

    private HomeViewModel homeViewModel;
    private Map<String, MaterialCardView> viewMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);

        viewMap = new HashMap<>();

        final LinearLayout linearLayout = root.findViewById(R.id.homeLinearLayout);

        DatabaseReference linkRef = FirebaseDBHelper.getUserLinkRef();
        linkRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Link link = dataSnapshot.getValue(Link.class);
                MaterialCardView card = createCard(dataSnapshot.getKey(), link.getTitle(), link.getUrl());
                linearLayout.addView(card, 1);
                viewMap.put(dataSnapshot.getKey(), card);
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

    private MaterialCardView createCard(final String id, String title, final String url) {
        MaterialCardView materialCardView = new MaterialCardView(getContext());
        //materialCardView.setCheckable(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10,10,10,10);
        materialCardView.setLayoutParams(layoutParams);
        materialCardView.setCardBackgroundColor(Color.parseColor("#E6E6E6"));
        materialCardView.setStrokeWidth(1);
        materialCardView.setStrokeColor(Color.BLACK);
        materialCardView.setCardElevation(10);

        LinearLayout linearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParams.setMargins(5,5,5,5);
        linearLayout.setLayoutParams(linearLayoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView titleTextView = new TextView(getContext());
        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleTextView.setLayoutParams(textViewLayoutParams);
        titleTextView.setText(title);
        titleTextView.setTextSize(20);

        LinearLayout buttonLinearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams buttonLinearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLinearLayoutParams.setMargins(5,5,5,5);
        buttonLinearLayout.setLayoutParams(buttonLinearLayoutParams);
        buttonLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        MaterialButton openButton = new MaterialButton(getContext(), null, R.attr.borderlessButtonStyle);
        LinearLayout.LayoutParams openButtonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        openButtonLayoutParams.setMargins(0,0,10,0);
        openButton.setLayoutParams(openButtonLayoutParams);
        openButton.setText("OPEN");
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        MaterialButton deleteButton = new MaterialButton(getContext(), null, R.attr.borderlessButtonStyle);
        LinearLayout.LayoutParams deleteButtonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //deleteButtonLayoutParams.setMargins(0,0,10,0);
        deleteButton.setLayoutParams(deleteButtonLayoutParams);
        deleteButton.setText("DELETE");
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDBHelper.removeLink(id);
            }
        });

        buttonLinearLayout.addView(openButton);
        buttonLinearLayout.addView(deleteButton);

        linearLayout.addView(titleTextView);
        linearLayout.addView(buttonLinearLayout);

        materialCardView.addView(linearLayout);

        return materialCardView;

    }


}
package com.dhavalpateln.linkcast.ui.faq;

import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.dhavalpateln.linkcast.R;

public class FAQFragment extends Fragment {

    private LinearLayout toExapnd;
    private CardView cardView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_faq, container, false);
        /*toExapnd = root.findViewById(R.id.toexpand);
        cardView = root.findViewById(R.id.card);
        root.findViewById(R.id.card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(cardView);
                if(toExapnd.getVisibility() == View.VISIBLE) {
                    toExapnd.setVisibility(View.GONE);
                }
                else {
                    toExapnd.setVisibility(View.VISIBLE);
                }
            }
        });*/
        // TODO: Implement FAQ
        return root;
    }


}
package com.dhavalpateln.linkcast;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.Link;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Map;

public class LinkMaterialCardView {

    public interface MaterialCardViewButtonClickListener {
        void onClick(String id, String title, String url, Map<String, String> data);
    }

    private MaterialCardView materialCardView;
    private LinearLayout buttonLinearLayout;
    private TextView titleTextView;
    private LinearLayout linearLayout;

    private String id;
    private String title;
    private String url;
    private Map<String, String> data;
    private Link link;

    public LinkMaterialCardView(Context context, String id, String title, String url, Map<String, String> data) {

        this.id = id;
        this.title = title;
        this.url = url;
        this.data = data;

        materialCardView = new MaterialCardView(context);
        //materialCardView.setCheckable(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(30,10,30,10);
        materialCardView.setLayoutParams(layoutParams);
        materialCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.materialCardBackgroud));
        materialCardView.setPadding(20, 50, 20, 50);
        materialCardView.setStrokeWidth(1);
        materialCardView.setStrokeColor(context.getResources().getColor(R.color.materialCardTextColor));
        materialCardView.setCardElevation(10);

        linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParams.setMargins(5,5,5,5);
        linearLayout.setLayoutParams(linearLayoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        titleTextView = new TextView(context);
        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(30,30,0,0);
        titleTextView.setLayoutParams(textViewLayoutParams);
        titleTextView.setText(title);
        titleTextView.setTextSize(20);

        buttonLinearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams buttonLinearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLinearLayoutParams.setMargins(5,5,5,5);
        buttonLinearLayout.setLayoutParams(buttonLinearLayoutParams);
        buttonLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        linearLayout.addView(titleTextView);
        linearLayout.addView(buttonLinearLayout);

        materialCardView.addView(linearLayout);
    }

    public LinkMaterialCardView(Context context, String id, String title, String url) {
        this(context, id, title, url, null);
    }

    public LinkMaterialCardView(Context context, String id, Link link) {
        this(context, id, link.getTitle(), link.getUrl(), link.getData());
    }

    public void addButton(Context context, String text, final MaterialCardViewButtonClickListener listener) {
        MaterialButton openButton = new MaterialButton(context, null, R.attr.borderlessButtonStyle);
        LinearLayout.LayoutParams openButtonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        openButtonLayoutParams.setMargins(0,0,10,0);
        openButton.setLayoutParams(openButtonLayoutParams);
        openButton.setText(text);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(id, title, url, data);
            }
        });
        buttonLinearLayout.addView(openButton);
    }

    public void cardTitle(String title) {
        this.title = title;
        titleTextView.setText(title);
    }

    public MaterialCardView getCard() {
        return materialCardView;
    }
}

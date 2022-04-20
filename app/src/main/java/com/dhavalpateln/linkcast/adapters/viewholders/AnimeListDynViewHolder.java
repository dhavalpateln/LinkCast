package com.dhavalpateln.linkcast.adapters.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.utils.UIBuilder;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AnimeListDynViewHolder extends RecyclerView.ViewHolder {

    public TextView subTextTextView;
    public TextView titleTextView;
    public ImageView imageView;
    public LinearLayout buttonHolder;
    public ConstraintLayout mainLayout;
    private Map<String, Button> buttonMap;

    public AnimeListDynViewHolder(@NonNull View itemView, String[] buttons, Context context) {
        super(itemView);
        this.mainLayout = (ConstraintLayout) itemView;
        this.subTextTextView = itemView.findViewById(R.id.list_object_subtext_text_view);
        this.titleTextView = itemView.findViewById(R.id.list_object_title_text_view);
        this.buttonHolder = itemView.findViewById(R.id.list_object_buttons_holder_linearlayout);
        this.imageView = itemView.findViewById(R.id.list_object_image_view);
        buttonMap = new HashMap<>();
        for (String buttonName : buttons) {
            Button button = UIBuilder.generateMaterialButton(context, buttonName);
            buttonMap.put(buttonName, button);
            this.buttonHolder.addView(button);
        }
    }
}
package com.dhavalpateln.linkcast.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.dhavalpateln.linkcast.R;
import com.google.android.material.button.MaterialButton;

public class UIBuilder {
    public static Button generateMaterialButton(Context context, String text) {
        Button button = (Button) LayoutInflater.from(context).inflate(R.layout.list_recycler_button, null);
        button.setText(text);
        return button;
    }
}

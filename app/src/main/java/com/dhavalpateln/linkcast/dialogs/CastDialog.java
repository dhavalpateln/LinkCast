package com.dhavalpateln.linkcast.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.dhavalpateln.linkcast.R;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class CastDialog extends LinkCastDialog {

    private String title;
    private String url;
    private Map<String, CastDialog.OnClickListener> map;
    private Map<String, String> data;

    /*public CastDialog(String title, String url, Map<String, CastDialog.OnClickListener> map) {
        this.title = title;
        this.url = url;
        this.map = map;
    }*/

    public CastDialog(String title, String url, Map<String, CastDialog.OnClickListener> map, Map<String, String> data) {
        this.title = title;
        this.url = url;
        this.map = map;
        this.data = data;
    }

    public void close() {
        CastDialog.this.getDialog().cancel();
    }

    public interface OnClickListener {
        void onClick(CastDialog castDialog, String title, String url, Map<String, String> data);
    }


    @Override
    public int getContentLayout() {
        return R.layout.cast_dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = getContentView();
        LinearLayout layout = view.findViewById(R.id.castDialogLinearLayout);
        for(String key: map.keySet()) {
            MaterialButton button = new MaterialButton(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(10, 0, 10, 0);
            button.setLayoutParams(layoutParams);
            button.setPadding(10, 10, 10, 10);
            button.setText(key);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    map.get(key).onClick(CastDialog.this, title, url, data);
                }
            });
            layout.addView(button);
        }
        return dialog;
    }
}

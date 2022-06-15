package com.dhavalpateln.linkcast.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdvancedSourceSelector extends LinkCastDialog {

    private List<VideoURLData> sources;
    private OnClickListener listener;

    public AdvancedSourceSelector(List<VideoURLData> sources, AdvancedSourceSelector.OnClickListener adClickListener) {
        this.sources = sources;
        this.listener = adClickListener;
    }

    public void close() {
        AdvancedSourceSelector.this.getDialog().cancel();
    }

    public interface OnClickListener {
        void onClick(AdvancedSourceSelector dialog, VideoURLData videoURLData);
    }


    @Override
    public int getContentLayout() {
        return R.layout.advanced_source_selector_dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = getContentView();
        LinearLayout layout = view.findViewById(R.id.advancedSourceSelectorDialogLinearLayout);

        for(VideoURLData source: sources) {
            if(source.equals("dummy")) {
                continue;
            }
            MaterialButton button = new MaterialButton(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(10, 0, 10, 0);
            button.setLayoutParams(layoutParams);
            button.setPadding(10, 10, 10, 10);
            button.setText(source.getTitle());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(AdvancedSourceSelector.this, source);
                }
            });
            layout.addView(button);
        }
        return dialog;
    }
}

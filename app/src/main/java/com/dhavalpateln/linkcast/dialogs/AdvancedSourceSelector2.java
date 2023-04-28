package com.dhavalpateln.linkcast.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdvancedSourceSelector2 extends BottomSheetDialogFragment {

    private List<VideoURLData> sources;
    private OnClickListener listener;

    public AdvancedSourceSelector2(List<VideoURLData> sources, AdvancedSourceSelector2.OnClickListener adClickListener) {
        this.sources = sources;
        this.listener = adClickListener;
    }

    public void close() {
        AdvancedSourceSelector2.this.getDialog().cancel();
    }

    public interface OnClickListener {
        void onClick(AdvancedSourceSelector2 dialog, VideoURLData videoURLData);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = inflater.inflate(R.layout.advanced_source_selector_dialog, container, false);
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
                    listener.onClick(AdvancedSourceSelector2.this, source);
                }
            });
            layout.addView(button);
        }
        return view;
    }
}

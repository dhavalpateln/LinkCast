package com.dhavalpateln.linkcast.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.dhavalpateln.linkcast.R;
import com.google.android.material.button.MaterialButton;

import java.util.Map;

public class AdvancedSourceSelector extends DialogFragment {

    private Map<String, String> sources;
    private OnClickListener listener;

    public AdvancedSourceSelector(Map<String, String> sources, AdvancedSourceSelector.OnClickListener adClickListener) {
        this.sources = sources;
        this.listener = adClickListener;
    }

    public void close() {
        AdvancedSourceSelector.this.getDialog().cancel();
    }

    public interface OnClickListener {
        void onClick(AdvancedSourceSelector dialog, String source, String url);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.advanced_source_selector_dialog, null);
        LinearLayout layout = view.findViewById(R.id.advancedSourceSelectorDialogLinearLayout);
        String order = sources.get("order");
        for(String source: order.split(",")) {
            if(source.equals("dummy")) {
                continue;
            }
            String url = sources.get(source);
            MaterialButton button = new MaterialButton(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(10, 0, 10, 0);
            button.setLayoutParams(layoutParams);
            button.setPadding(10, 10, 10, 10);
            button.setText(source);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(AdvancedSourceSelector.this, source, url);
                }
            });
            layout.addView(button);
        }
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);

        return builder.create();
    }
}

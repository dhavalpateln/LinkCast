package com.dhavalpateln.linkcast.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

public abstract class LinkCastDialog extends DialogFragment {

    public interface OnClickListener {
        void onClick(Dialog dialog, View view);
    }

    private View contentView;
    private Button positiveButton;
    private Button negativeButton;
    private Button neutralButton;
    private TextView titleTextView;

    /*@LayoutRes
    private int layoutResId = 0;

    public LinkCastDialog(@LayoutRes int layoutResId) {
        this.layoutResId = layoutResId;
    }*/

    @LayoutRes
    public abstract int getContentLayout();

    protected View getContentView() {
        return this.contentView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        ConstraintLayout dialogView = (ConstraintLayout) inflater.inflate(R.layout.dialog_linkcast, null);

        LinearLayout contentContainerView = dialogView.findViewById(R.id.linkcast_dialog_content_view);
        positiveButton = dialogView.findViewById(R.id.linkcast_dialog_positive_button);
        negativeButton = dialogView.findViewById(R.id.linkcast_dialog_negative_button);
        neutralButton = dialogView.findViewById(R.id.linkcast_dialog_neutral_button);
        titleTextView = dialogView.findViewById(R.id.linkcast_dialog_title_text_view);

        contentView = inflater.inflate(getContentLayout(), null);
        contentContainerView.addView(contentView);

        builder.setView(dialogView);
        return builder.create();
    }

    public void setPositiveButton(String text, OnClickListener listener) {
        positiveButton.setVisibility(View.VISIBLE);
        positiveButton.setText(text);
        positiveButton.setOnClickListener(view -> listener.onClick(getDialog(), view));
    }

    public void setNegativeButton(String text, OnClickListener listener) {
        negativeButton.setVisibility(View.VISIBLE);
        negativeButton.setText(text);
        negativeButton.setOnClickListener(view -> listener.onClick(getDialog(), view));
    }

    public void setNeutralButton(String text, OnClickListener listener) {
        neutralButton.setVisibility(View.VISIBLE);
        neutralButton.setText(text);
        neutralButton.setOnClickListener(view -> listener.onClick(getDialog(), view));
    }

    public void setTitle(String text) {
        titleTextView.setVisibility(View.VISIBLE);
        titleTextView.setText(text);
    }
}

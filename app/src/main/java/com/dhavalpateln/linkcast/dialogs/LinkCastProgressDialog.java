package com.dhavalpateln.linkcast.dialogs;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhavalpateln.linkcast.R;

public class LinkCastProgressDialog extends LinkCastDialog {

    private String dialogMessage;

    @Override
    public int getContentLayout() {
        return R.layout.dialog_progressbar;
    }

    public LinkCastProgressDialog() {
        this("Please wait...");
    }

    public LinkCastProgressDialog(String message) {
        this.dialogMessage = message;
        this.setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = getContentView();
        TextView messageTV = view.findViewById(R.id.progress_bar_msg);
        messageTV.setText(this.dialogMessage);
        return dialog;
    }
}

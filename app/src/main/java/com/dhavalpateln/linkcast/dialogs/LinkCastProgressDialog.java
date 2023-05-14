package com.dhavalpateln.linkcast.dialogs;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.d("progress_dialog", "ignore illegal state");
        }

    }
}

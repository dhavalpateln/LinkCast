package com.dhavalpateln.linkcast.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ConfirmationDialog extends LinkCastDialog {

    private String message;
    private ConfirmationListener listener;

    public interface ConfirmationListener {
        void onConfirm();
    }

    public ConfirmationDialog(String message, ConfirmationListener listener) {
        this.message = message;
        this.listener = listener;
    }

    @Override
    public int getContentLayout() {
        return R.layout.dialog_confirmation;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        View view = getContentView();
        TextView msgTextView = view.findViewById(R.id.dialog_confirmation_message);
        msgTextView.setText(this.message);

        setPositiveButton("Confirm", (d, v) -> {
            this.listener.onConfirm();
            d.dismiss();
        });
        setNegativeButton("Cancel", (d, v) -> {
            d.dismiss();
        });
        return dialog;
    }
}

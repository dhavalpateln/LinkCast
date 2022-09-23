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
    private ConfirmationListener confirmationListener;
    private DenyListener denyListener;
    private String mPositiveMessage = "Confirm";
    private String mNegativeMessage = "Cancel";

    public interface ConfirmationListener {
        void onConfirm();
    }

    public interface DenyListener {
        void onDenied();
    }

    public ConfirmationDialog(String message, ConfirmationListener confirmationListener) {
        this.message = message;
        this.confirmationListener = confirmationListener;
    }

    public ConfirmationDialog(String message, String confirmMessage, String denyMessage) {
        this.message = message;
        this.mPositiveMessage = confirmMessage;
        this.mNegativeMessage = denyMessage;
    }

    public void setConfirmationListener(ConfirmationListener listener) {
        this.confirmationListener = listener;
    }

    public void setDenyListener(DenyListener listener) {
        this.denyListener = listener;
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

        setPositiveButton(mPositiveMessage, (d, v) -> {
            if(this.confirmationListener != null) {
                this.confirmationListener.onConfirm();
            }
            d.dismiss();
        });
        setNegativeButton(mNegativeMessage, (d, v) -> {
            if(this.denyListener != null) {
                this.denyListener.onDenied();
            }
            d.dismiss();
        });
        return dialog;
    }
}

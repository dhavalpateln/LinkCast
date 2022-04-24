package com.dhavalpateln.linkcast.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.dhavalpateln.linkcast.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class EpisodeNoteDialog extends LinkCastDialog {

    public interface NoteChangeListener {
        void onNoteUpdated(String note);
        void onNoteRemoved();
    }

    private NoteChangeListener listener;
    private String initNote;

    public EpisodeNoteDialog(String initNote, NoteChangeListener listener) {
        if(initNote == null)    this.initNote = "";
        else this.initNote = initNote;
        this.listener = listener;
    }

    @Override
    public int getContentLayout() {
        return R.layout.dialog_episode_note;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog alertDialog = super.onCreateDialog(savedInstanceState);
        EditText editText = getContentView().findViewById(R.id.episode_note_edit_text_view);
        editText.setText(initNote);
        super.setPositiveButton("Ok", (dialog, view) -> {
            String note = editText.getText().toString().trim();
            if(note.equals("")) listener.onNoteRemoved();
            else    listener.onNoteUpdated(note);
            dialog.dismiss();
        });

        super.setNeutralButton("Clear", (dialog, view) -> editText.setText(""));
        return alertDialog;
    }
}

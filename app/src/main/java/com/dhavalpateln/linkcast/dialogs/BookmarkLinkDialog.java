package com.dhavalpateln.linkcast.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;

public class BookmarkLinkDialog extends DialogFragment {
    private SearchDialog.SearchButtonClickListener searchButtonClickListener;
    private EditText bookmarkEditText;
    private String bookmarkLinkTitle;
    private String id;

    public BookmarkLinkDialog(String id, String bookmarkLinkTitle, String url) {
        super();
        this.id = id;
        this.bookmarkLinkTitle = bookmarkLinkTitle;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.bookmark_edit_dialog, null);
        bookmarkEditText = view.findViewById(R.id.bookmark_edit_text);
        bookmarkEditText.setText(bookmarkLinkTitle);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        view.findViewById(R.id.bookmark_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDBHelper.getUserAnimeWebExplorerLinkRef().child(id).child("title")
                        .setValue(bookmarkEditText.getText().toString());
                BookmarkLinkDialog.this.getDialog().cancel();
            }
        });
        view.findViewById(R.id.bookmark_edit_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookmarkLinkDialog.this.getDialog().cancel();
            }
        });
        return builder.create();
    }

}

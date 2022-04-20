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
import com.dhavalpateln.linkcast.ui.animes.AnimeFragment;

import java.util.Map;

public class BookmarkLinkDialog extends DialogFragment {
    private SearchDialog.SearchButtonClickListener searchButtonClickListener;
    private EditText bookmarkEditText;
    private Spinner animeStatusSpinner;
    private String bookmarkLinkTitle;
    private String id;
    private Map<String, String> data;

    public BookmarkLinkDialog(String id, String bookmarkLinkTitle, String url, Map<String, String> data) {
        super();
        this.id = id;
        this.bookmarkLinkTitle = bookmarkLinkTitle;
        this.data = data;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.bookmark_edit_dialog, null);
        bookmarkEditText = view.findViewById(R.id.bookmark_edit_text);
        animeStatusSpinner = view.findViewById(R.id.bookmark_edit_spinner);
        bookmarkEditText.setText(bookmarkLinkTitle);

        ArrayAdapter<String> spinnerContent = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item);
        for(String status: AnimeFragment.Catalogs.BASIC_TYPES) {
            if(!status.toLowerCase().equals("all")) {
                spinnerContent.add(status);
            }
        }
        animeStatusSpinner.setAdapter(spinnerContent);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        view.findViewById(R.id.bookmark_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(id).child("title")
                        .setValue(bookmarkEditText.getText().toString());
                FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(id).child("data").child("status")
                        .setValue(animeStatusSpinner.getSelectedItem().toString());
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

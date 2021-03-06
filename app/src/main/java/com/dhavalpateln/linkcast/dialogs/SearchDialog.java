package com.dhavalpateln.linkcast.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

public class SearchDialog extends DialogFragment {

    private SearchButtonClickListener searchButtonClickListener;
    private EditText searchEditText;
    private Spinner spinner;
    private String searchTerm;

    public SearchDialog() {
        super();
        searchTerm = "";
    }
    public SearchDialog(String searchTerm) {
        super();
        this.searchTerm = searchTerm;
    }

    public interface SearchButtonClickListener {
        void onSearchButtonClicked(String searchString, String source);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.search_dialog, null);
        searchEditText = view.findViewById(R.id.anime_serach_edit_text);
        searchEditText.setText(searchTerm);

        spinner = view.findViewById(R.id.source_spinner_view);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.source_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        view.findViewById(R.id.search_search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchButtonClickListener != null) {
                    searchButtonClickListener.onSearchButtonClicked(searchEditText.getText().toString(), spinner.getSelectedItem().toString());
                }
                SearchDialog.this.getDialog().cancel();
            }
        });
        view.findViewById(R.id.search_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchDialog.this.getDialog().cancel();
            }
        });
        return builder.create();
    }

    public void setSearchListener(SearchButtonClickListener searchButtonClickListener) {
        this.searchButtonClickListener = searchButtonClickListener;
    }
}

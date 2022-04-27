package com.dhavalpateln.linkcast.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.ui.animes.AnimeFragment;
import com.dhavalpateln.linkcast.ui.mangas.MangaFragment;

import java.util.Map;

public class BookmarkLinkDialog extends LinkCastDialog {
    private TextView bookmarkEditText;
    private Spinner animeStatusSpinner;
    private Spinner animeScoreSpinner;
    private AnimeLinkData animeLinkData;
    private boolean isAnime;

    public BookmarkLinkDialog(AnimeLinkData animeLinkData) {
        super();
        this.animeLinkData = animeLinkData;
        this.isAnime = animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_LINK_TYPE).equalsIgnoreCase("anime");
    }

    @Override
    public int getContentLayout() {
        return R.layout.dialog_bookmark_edit;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = getContentView();
        bookmarkEditText = view.findViewById(R.id.bookmark_title_text_view);
        animeStatusSpinner = view.findViewById(R.id.bookmark_status_spinner);
        animeScoreSpinner = view.findViewById(R.id.bookmark_score_spinner);
        bookmarkEditText.setText(animeLinkData.getTitle());

        ArrayAdapter<String> spinnerContent = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item);
        String[] statusTypes;
        if(isAnime) {
            statusTypes = AnimeFragment.Catalogs.BASIC_TYPES;
        }
        else {
            statusTypes = MangaFragment.Catalog.BASIC_TYPES;
        }

        for(String status: statusTypes) {
            spinnerContent.add(status);
        }
        animeStatusSpinner.setAdapter(spinnerContent);

        ArrayAdapter<String> scoreContent = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        animeScoreSpinner.setAdapter(scoreContent);

        setPositiveButton("Update", (dialog1, view1) -> {
            animeLinkData.updateData(AnimeLinkData.DataContract.DATA_STATUS, animeStatusSpinner.getSelectedItem().toString(), true, isAnime);
            animeLinkData.updateData(AnimeLinkData.DataContract.DATA_USER_SCORE, animeScoreSpinner.getSelectedItem().toString(), true, isAnime);
            dialog1.dismiss();
        });

        setNegativeButton("Cancel", (d,v) -> d.dismiss());
        return dialog;
    }

}

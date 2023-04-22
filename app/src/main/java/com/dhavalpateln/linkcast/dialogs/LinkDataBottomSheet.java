package com.dhavalpateln.linkcast.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.ui.animes.AnimeFragment;
import com.dhavalpateln.linkcast.ui.mangas.MangaFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class LinkDataBottomSheet extends BottomSheetDialogFragment {

    private TextView bookmarkEditText;
    private Spinner animeStatusSpinner;
    private Spinner animeScoreSpinner;
    private AnimeLinkData animeLinkData;
    private MaterialAutoCompleteTextView materialAutoCompleteTextView;
    private boolean isAnime;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.link_data_bottom_sheet, container, false);

        /*BottomSheetBehavior behavior = BottomSheetBehavior.from(view);
        behavior.setDraggable(true);
        behavior.setPeekHeight(500);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);*/

        bookmarkEditText = view.findViewById(R.id.link_data_bs_title_text_view);
        animeStatusSpinner = view.findViewById(R.id.link_data_status_spinner);
        animeScoreSpinner = view.findViewById(R.id.link_data_score_spinner);
        materialAutoCompleteTextView = view.findViewById(R.id.source_selector_autocomplete);
        bookmarkEditText.setText("TEST");

        ArrayAdapter<String> spinnerContent = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item);
        String[] statusTypes;
        if(true) {
            statusTypes = AnimeFragment.Catalogs.BASIC_TYPES;
        }
        else {
            statusTypes = MangaFragment.Catalog.BASIC_TYPES;
        }

        int initialStatusSelected = 0;
        for(int i = 0; i < statusTypes.length; i++) {
            spinnerContent.add(statusTypes[i]);
            //if(statusTypes[i].equals(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS)))  initialStatusSelected = i;
        }
        materialAutoCompleteTextView.setAdapter(spinnerContent);
        materialAutoCompleteTextView.setListSelection(0);
        materialAutoCompleteTextView.setText("Watching", false);

        animeStatusSpinner.setAdapter(spinnerContent);
        //animeStatusSpinner.setSelection(initialStatusSelected);


        ArrayAdapter<String> scoreContent = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        animeScoreSpinner.setAdapter(scoreContent);
        //animeScoreSpinner.setSelection(Integer.valueOf(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE)));

        return view;
    }
}

package com.dhavalpateln.linkcast.dialogs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.ui.animes.AnimeFragment;
import com.dhavalpateln.linkcast.ui.mangas.MangaFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class LinkDataBottomSheet extends BottomSheetDialogFragment {

    private TextView animeTitleTextView;
    private LinkWithAllData link;
    private MaterialAutoCompleteTextView statusSelector;
    private MaterialAutoCompleteTextView scoreSelector;
    private ImageView animeImageView;
    private Button updateButton;
    private Button deleteButton;
    private String deletePrefs;

    public LinkDataBottomSheet(LinkWithAllData link, String deletePrefs) {
        this.link = link;
        this.deletePrefs = deletePrefs;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.link_data_bottom_sheet, container, false);

        animeTitleTextView = view.findViewById(R.id.link_data_bs_title_text_view);
        statusSelector = view.findViewById(R.id.status_selector_autocomplete);
        scoreSelector = view.findViewById(R.id.score_selector_autocomplete);
        animeImageView = view.findViewById(R.id.link_data_image_view);
        updateButton = view.findViewById(R.id.updateButton);
        deleteButton = view.findViewById(R.id.deleteButton);

        this.animeTitleTextView.setText(this.link.getTitle());

        Glide.with(getContext())
                .load(this.link.getMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL))
                .centerCrop()
                .transition(new DrawableTransitionOptions().crossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(animeImageView);
        animeImageView.setClipToOutline(true);

        ArrayAdapter<String> spinnerContent = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item);
        String[] statusTypes;
        if(this.link.linkData.getType().equalsIgnoreCase("anime")) {
            statusTypes = AnimeFragment.Catalogs.BASIC_TYPES;
        }
        else {
            statusTypes = MangaFragment.Catalog.BASIC_TYPES;
        }

        int initialStatusSelected = 0;
        for(int i = 0; i < statusTypes.length; i++) {
            spinnerContent.add(statusTypes[i]);
            if(statusTypes[i].equalsIgnoreCase(this.link.getMetaData(AnimeLinkData.DataContract.DATA_STATUS)))  initialStatusSelected = i;
        }
        statusSelector.setAdapter(spinnerContent);
        statusSelector.setListSelection(initialStatusSelected);
        statusSelector.setText(statusTypes[initialStatusSelected], false);

        ArrayAdapter<String> scoreContent = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        scoreSelector.setAdapter(scoreContent);
        scoreSelector.setText(this.link.getMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE), false);

        updateButton.setOnClickListener(v -> {
            this.link.linkData.getData().put(AnimeLinkData.DataContract.DATA_STATUS, statusSelector.getText().toString());
            this.link.linkData.getData().put(AnimeLinkData.DataContract.DATA_USER_SCORE, scoreSelector.getText().toString());
            this.link.updateFirebase();
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            if(deletePrefs.equalsIgnoreCase("ask")) {
                ConfirmationDialog confirmationDialog = new ConfirmationDialog("Are you sure you want to delete this?", () -> {
                    delete();
                });
                confirmationDialog.show(getParentFragmentManager(), "Confirm");
            }
            else {
                delete();
            }
            dismiss();
        });

        return view;
    }

    private void delete() {
        if(this.link.isAnime()) FirebaseDBHelper.removeAnimeLink(this.link.getId());
        else FirebaseDBHelper.removeMangaLink(this.link.getId());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}

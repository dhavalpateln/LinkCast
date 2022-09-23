package com.dhavalpateln.linkcast.ui.settings;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.ui.AbstractCatalogObjectFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private LinearLayout episodeTrackerView;
    private TextView episodeTrackerValue;
    private String[] episodeTrackingOptions;

    private LinearLayout listSortOrderView;
    private TextView listSortOrderValue;
    private String[] sortOrderOptions;

    private LinearLayout bookmarkDeleteView;
    private TextView bookmarkDeleteValue;
    private String[] bookmarkDeleteOptions;

    private LinearLayout resumeOptionView;
    private TextView resumeOptionValue;
    private String[] resumeOptions;


    private SharedPreferences prefs;

    public static class EpisodeTracking {
        public static int LAST_EPISODE = 0;
        public static int MAX_EPISODE = 1;
    }

    public static class RESUME {
        public static int ALWAYS = 0;
        public static int ASK = 1;
        public static int ASK_FOR_COMPLETED = 2;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        episodeTrackerView = root.findViewById(R.id.settings_episode_tracker);
        episodeTrackerValue = root.findViewById(R.id.settings_episode_tracker_value);

        listSortOrderView = root.findViewById(R.id.settings_anime_sort_order);
        listSortOrderValue = root.findViewById(R.id.settings_anime_sort_order_value);

        bookmarkDeleteView = root.findViewById(R.id.settings_bookmark_delete);
        bookmarkDeleteValue = root.findViewById(R.id.settings_bookmark_delete_value);

        resumeOptionView = root.findViewById(R.id.settings_resume_options);
        resumeOptionValue = root.findViewById(R.id.settings_resume_value);

        episodeTrackingOptions = new String[] {"Last episode watched", "Max episode watched"};
        sortOrderOptions = new String[] {
                AbstractCatalogObjectFragment.Sort.BY_NAME.name(),
                AbstractCatalogObjectFragment.Sort.BY_SCORE.name(),
                AbstractCatalogObjectFragment.Sort.BY_DATE_ADDED_ASC.name(),
                AbstractCatalogObjectFragment.Sort.BY_DATE_ADDED_DESC.name()
        };
        bookmarkDeleteOptions = new String[] {
                "Ask",
                "Do not ask"
        };
        resumeOptions = new String[] {
                "Always",
                "Ask",
                "Ask for completed"
        };

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        episodeTrackerValue.setText(episodeTrackingOptions[prefs.getInt(SharedPrefContract.EPISODE_TRACKING, SharedPrefContract.EPISODE_TRACKING_DEFAULT)]);
        listSortOrderValue.setText(prefs.getString(SharedPrefContract.ANIME_LIST_SORT_ORDER, SharedPrefContract.ANIME_LIST_SORT_DEFAULT));
        bookmarkDeleteValue.setText(prefs.getString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, "Ask"));
        resumeOptionValue.setText(resumeOptions[prefs.getInt(SharedPrefContract.RESUME_BEHAVIOUR, SharedPrefContract.RESUME_BEHAVIOUR_DEFAULT)]);

        episodeTrackerView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Choose option");
            builder.setItems(episodeTrackingOptions, (dialog, which) -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(SharedPrefContract.EPISODE_TRACKING, which);
                editor.commit();
                episodeTrackerValue.setText(episodeTrackingOptions[which]);
            });
            builder.show();
        });

        listSortOrderView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Choose option");
            builder.setItems(sortOrderOptions, (dialog, which) -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(SharedPrefContract.ANIME_LIST_SORT_ORDER, sortOrderOptions[which]);
                editor.commit();
                listSortOrderValue.setText(sortOrderOptions[which]);
            });
            builder.show();
        });

        bookmarkDeleteView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Choose option");
            builder.setItems(bookmarkDeleteOptions, (dialog, which) -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, bookmarkDeleteOptions[which]);
                editor.commit();
                bookmarkDeleteValue.setText(bookmarkDeleteOptions[which]);
                //Toast.makeText(getContext(), "Change reflected on app restart", Toast.LENGTH_LONG).show();
            });
            builder.show();
        });

        resumeOptionView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Choose option");
            builder.setItems(resumeOptions, (dialog, which) -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(SharedPrefContract.RESUME_BEHAVIOUR, which);
                editor.commit();
                resumeOptionValue.setText(resumeOptions[which]);
                //Toast.makeText(getContext(), "Change reflected on app restart", Toast.LENGTH_LONG).show();
            });
            builder.show();
        });
    }
}
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




    private SharedPreferences prefs;

    public static class EpisodeTracking {
        public static int LAST_EPISODE = 0;
        public static int MAX_EPISODE = 1;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        episodeTrackerView = root.findViewById(R.id.settings_episode_tracker);
        episodeTrackerValue = root.findViewById(R.id.settings_episode_tracker_value);
        listSortOrderView = root.findViewById(R.id.settings_anime_sort_order);
        listSortOrderValue = root.findViewById(R.id.settings_anime_sort_order_value);

        episodeTrackingOptions = new String[] {"Last episode watched", "Max episode watched"};
        sortOrderOptions = new String[] {
                AbstractCatalogObjectFragment.Sort.BY_NAME.name(),
                AbstractCatalogObjectFragment.Sort.BY_SCORE.name(),
                AbstractCatalogObjectFragment.Sort.BY_DATE_ADDED_ASC.name(),
                AbstractCatalogObjectFragment.Sort.BY_DATE_ADDED_DESC.name()
        };

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        episodeTrackerValue.setText(episodeTrackingOptions[prefs.getInt(SharedPrefContract.EPISODE_TRACKING, SharedPrefContract.EPISODE_TRACKING_DEFAULT)]);
        listSortOrderValue.setText(prefs.getString(SharedPrefContract.ANIME_LIST_SORT_ORDER, SharedPrefContract.ANIME_LIST_SORT_DEFAULT));

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
                //Toast.makeText(getContext(), "Change reflected on app restart", Toast.LENGTH_LONG).show();
            });
            builder.show();
        });
    }
}
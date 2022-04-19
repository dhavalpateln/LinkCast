package com.dhavalpateln.linkcast.ui.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.ui.catalog.CatalogFragment;

public class SettingsFragment extends Fragment {

    private LinearLayout episodeTrackerView;
    private TextView episodeTrackerValue;
    private String[] episodeTrackingOptions;


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

        episodeTrackingOptions = new String[] {"Last episode watched", "Max episode watched"};

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        episodeTrackerValue.setText(episodeTrackingOptions[prefs.getInt(SharedPrefContract.EPISODE_TRACKING, SharedPrefContract.EPISODE_TRACKING_DEFAULT)]);

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
    }
}
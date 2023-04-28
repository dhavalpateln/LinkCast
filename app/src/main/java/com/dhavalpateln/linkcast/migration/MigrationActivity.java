package com.dhavalpateln.linkcast.migration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dhavalpateln.linkcast.MainActivity;
import com.dhavalpateln.linkcast.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MigrationActivity extends AppCompatActivity implements MigrationListener {

    public static final int VERSION = 0;
    public static final String PREF_MIGRATION_VERSION_KEY = "migration_version";

    private ProgressBar progressBar;
    private TextView progressTextView;

    private SharedPreferences prefs;

    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migration);

        this.progressBar = findViewById(R.id.migration_progress_bar);
        this.progressTextView = findViewById(R.id.migration_progress_text_view);

        this.prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        runNextMigration();
    }

    private MigrationTask getMigration(int version) {
        switch (version) {
            case 1: return new Migration_0_1(getApplicationContext(), this);
            default: return null;
        }
    }

    private void runNextMigration() {
        int currentMigration = this.prefs.getInt(PREF_MIGRATION_VERSION_KEY, 0);

        //migration complete
        if(currentMigration == VERSION) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        MigrationTask task = getMigration(currentMigration + 1);
        executor.execute(() -> task.execute());
    }

    @Override
    public void onMigrationProgressChange(int progress) {
        this.progressBar.setProgress(progress);
        this.progressTextView.setText(progress + "%");
    }

    @Override
    public void onMigrationSuccess(int version) {
        int currentMigration = this.prefs.getInt(PREF_MIGRATION_VERSION_KEY, 0);
        SharedPreferences.Editor editor = this.prefs.edit();
        editor.putInt(PREF_MIGRATION_VERSION_KEY, currentMigration + 1);
        editor.commit();
        runNextMigration();
    }

    @Override
    public void onMigrationFail() {

    }
}
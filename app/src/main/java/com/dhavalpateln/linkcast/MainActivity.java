package com.dhavalpateln.linkcast;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomDatabase;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.migration.MigrationActivity;
import com.dhavalpateln.linkcast.utils.Utils;
import com.dhavalpateln.linkcast.worker.LinkCastWorker;
import com.dhavalpateln.linkcast.worker.LinkMonitorTask;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.preference.PreferenceManager;
import android.transition.Explode;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import android.view.Menu;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private TextView displayNameTextView;
    private TextView emailTextView;
    private ImageView profileImageView;
    private String CHANNEL_ID = "LinkCastNotification";
    private SharedPreferences prefs;

    private void checkAndRequestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(
                this, permission) ==
                PackageManager.PERMISSION_GRANTED) {
        }
        else {
            // You can directly ask for the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
        }
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void enqueuePeriodicWork(String name, PeriodicWorkRequest workRequest, int version) {
        String prefKey = "worker" + name + "ver";
        int queuedWorkerVer = prefs.getInt(prefKey, -1);
        ExistingPeriodicWorkPolicy policy = ExistingPeriodicWorkPolicy.KEEP;
        if(version != queuedWorkerVer) {
            policy = ExistingPeriodicWorkPolicy.REPLACE;
        }
        WorkManager
                .getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork(name, policy, workRequest);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(prefKey, version);
        editor.commit();
    }

    private void enqueueWorkers() {
        Constraints linkMonitorConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest uploadWorkRequest =
                new PeriodicWorkRequest.Builder(LinkMonitorTask.class, 1, TimeUnit.HOURS)
                        .setConstraints(linkMonitorConstraints)
                        .build();

        enqueuePeriodicWork("LinkUpdater", uploadWorkRequest, 2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        createNotificationChannel();
        checkAndRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 100);
        checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 101);

        FirebaseDBHelper.getAppMetricsLastAccessedLinkRef().setValue(Utils.getCurrentTime());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_anime_catalog, R.id.nav_manga_catalog, R.id.nav_discover, /*R.id.nav_anime_catalog,*/ R.id.nav_status,
                R.id.nav_downloads, R.id.nav_settings, R.id.nav_feedback, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        profileImageView = navigationView.getHeaderView(0).findViewById(R.id.profileImageView);
        displayNameTextView = navigationView.getHeaderView(0).findViewById(R.id.displayNameTextView);
        emailTextView = navigationView.getHeaderView(0).findViewById(R.id.emailTextView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            Uri imageUri = user.getPhotoUrl();
            if(imageUri != null) {
                int imageResolution = 256;
                String path = imageUri.getPath();
                path = "https://lh5.googleusercontent.com"
                        + path;
                path = path.replace("s96-c", "s" + imageResolution + "-c");

                displayNameTextView.setText(user.getDisplayName());
                emailTextView.setText(user.getEmail());
                Glide.with(getApplicationContext())
                        .load(path)
                        //.centerCrop()
                        .circleCrop()
                        .transition(new DrawableTransitionOptions().crossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImageView);
                /*Glide.with(getApplicationContext())
                        .load("")
                        .placeholder(R.drawable.navback)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(navbg);*/

            }
            enqueueWorkers();
        }
        //new LinkCastRoomRepository(getApplicationContext()).clearLinkData();
        //initRoomDB();
        // Inside your activity (if you did not enable transitions in your theme)
        //getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        // Set an exit transition
        //getWindow().setExitTransition(new Explode());
        //updateUserMetaData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_open_web:

                FirebaseDBHelper.getValue(FirebaseDBHelper.getAppWebLinkRef(), new ValueCallback() {
                    @Override
                    public void onValueObtained(DataSnapshot dataSnapshot) {
                        String url = (String) dataSnapshot.getValue();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                });
                return true;
            /*case R.id.action_remote_code:
                Intent intent = new Intent(MainActivity.this, RemoteCodeActivity.class);
                startActivity(intent);
                return true;*/
            case R.id.action_sign_out:
                AuthUI.getInstance()
                        .signOut(getApplicationContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                cleanupOnSignOut();
                                Intent intent = new Intent(MainActivity.this, LauncherActivity.class);
                                startActivity(intent);
                                MainActivity.this.finish();
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void cleanupOnSignOut() {
        LinkCastRoomRepository roomRepo = new LinkCastRoomRepository(getApplicationContext());
        roomRepo.clearLinkData();
        SharedPreferences.Editor editor = this.prefs.edit();
        editor.putInt(MigrationActivity.PREF_MIGRATION_VERSION_KEY, 0);
        editor.commit();
    }
}

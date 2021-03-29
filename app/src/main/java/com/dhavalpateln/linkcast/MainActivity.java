package com.dhavalpateln.linkcast;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.SearchDialog;
import com.dhavalpateln.linkcast.ui.download.DownloadFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.MenuItem;
import android.view.View;

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
import com.google.firebase.database.DatabaseReference;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private TextView displayNameTextView;
    private TextView emailTextView;
    private ImageView profileImageView;
    private SearchDialog searchDialog;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);

        checkAndRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 100);
        checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 101);

        searchDialog = new SearchDialog();
        searchDialog.setSearchListener(new SearchDialog.SearchButtonClickListener() {
            @Override
            public void onSearchButtonClicked(String searchString, String source) {
                Intent animeSearchIntent = new Intent(getApplicationContext(), AnimeWebExplorer.class);
                animeSearchIntent.putExtra("search", searchString);
                animeSearchIntent.putExtra("source", source);
                startActivity(animeSearchIntent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.show(getSupportFragmentManager(), "Search");
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_anime_links,
                /*R.id.nav_tools, R.id.nav_share,*/ R.id.nav_feedback, R.id.nav_faq)
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
                        .centerCrop()
                        .crossFade()
                        .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImageView);
                /*Glide.with(getApplicationContext())
                        .load("")
                        .placeholder(R.drawable.navback)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(navbg);*/

            }
        }
        //updateUserMetaData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

            case R.id.action_sign_out:
                AuthUI.getInstance()
                        .signOut(getApplicationContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
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

    public void fourAnimeListener(View view) {
        Intent intent = new Intent(this, AnimeWebExplorer.class);
        startActivity(intent);
        this.finish();
    }

    public void updateUserMetaData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDBHelper.getUserDataRef();
        ref.child("displayName").setValue(user.getDisplayName());
        ref.child("email").setValue(user.getEmail());
        ref.child("phone").setValue(user.getPhoneNumber());
        ref.child("photoURI").setValue(user.getPhotoUrl().toString());
    }


}

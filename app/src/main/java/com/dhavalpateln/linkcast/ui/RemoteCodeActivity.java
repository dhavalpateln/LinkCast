package com.dhavalpateln.linkcast.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.google.firebase.database.DataSnapshot;

import java.util.Random;

public class RemoteCodeActivity extends AppCompatActivity {

    TextView remoteCodeTextView;

    public String generateRandomCode() {
        Random random = new Random();
        return Integer.toString(10000 + random.nextInt(90000));
    }

    public void generateRemoteCode() {
        FirebaseDBHelper.getValue(FirebaseDBHelper.getUserRemoteDownloadCode(), new ValueCallback() {
            @Override
            public void onValueObtained(DataSnapshot dataSnapshot) {
                String code = (String) dataSnapshot.getValue();
                if(code != null) {
                    FirebaseDBHelper.getRemoteDownloadQueue().child(code).setValue(null);
                }
                code = generateRandomCode();
                final String finalCode = code;
                FirebaseDBHelper.getValue(FirebaseDBHelper.getRemoteDownloadQueue().child(code), new ValueCallback() {
                    @Override
                    public void onValueObtained(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            generateRemoteCode();
                        }
                        else {
                            FirebaseDBHelper.getUserRemoteDownloadCode().setValue(finalCode);
                            FirebaseDBHelper.getRemoteDownloadQueue().child(finalCode).push().setValue("NULL");
                            remoteCodeTextView.setText(finalCode);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_code);

        remoteCodeTextView = findViewById(R.id.remote_code_text_view);

        FirebaseDBHelper.getValue(FirebaseDBHelper.getUserRemoteDownloadCode(), new ValueCallback() {
            @Override
            public void onValueObtained(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    generateRemoteCode();
                }
                else {
                    remoteCodeTextView.setText((String) dataSnapshot.getValue());
                }
            }
        });

    }

    public void generateCodeButtonListener(View view) {
        generateRemoteCode();
    }
}
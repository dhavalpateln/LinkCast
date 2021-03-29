package com.dhavalpateln.linkcast.ui.feedback;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CrashReportActivity extends AppCompatActivity {

    EditText subjectEditText;
    EditText messageEditText;

    public String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_report);



        subjectEditText = findViewById(R.id.crash_subject_edit_text);
        messageEditText = findViewById(R.id.crash_message_edit_text);

        subjectEditText.setText(getIntent().getStringExtra("subject"));
        messageEditText.setText(getIntent().getStringExtra("message"));

        findViewById(R.id.send_feedback_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(subjectEditText.getText().toString().equals("") && messageEditText.getText().toString().equals("")) {
                    return;
                }



                final Map<String, Object> update = new HashMap<>();
                update.put("subject", subjectEditText.getText().toString());
                update.put("message", messageEditText.getText().toString());

                String currentDate = getCurrentDate();
                final DatabaseReference userFeedbackRef = FirebaseDBHelper.getUserDataRef().child("feedback").child(currentDate);

                FirebaseDBHelper.getValue(userFeedbackRef, new ValueCallback() {
                    @Override
                    public void onValueObtained(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null) {
                            userFeedbackRef.updateChildren(update);
                            FirebaseDBHelper.getFeedbackRef().push().updateChildren(update);
                            subjectEditText.setText("");
                            messageEditText.setText("");
                            Toast.makeText(getApplicationContext(), "Crash Submitted", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Only 1 Crash report allowed per minute to avoid spam", Toast.LENGTH_LONG).show();
                        }
                    }
                });




            }
        });
    }
}
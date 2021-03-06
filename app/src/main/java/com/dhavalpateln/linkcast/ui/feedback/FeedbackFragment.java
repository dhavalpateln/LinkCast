package com.dhavalpateln.linkcast.ui.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FeedbackFragment extends Fragment {

    EditText subjectEditText;
    EditText messageEditText;

    public String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_feedback, container, false);

        subjectEditText = root.findViewById(R.id.feedback_subject_edit_text);
        messageEditText = root.findViewById(R.id.feedback_message_edit_text);

        root.findViewById(R.id.send_feedback_button).setOnClickListener(new View.OnClickListener() {
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
                            Toast.makeText(getContext(), "Feedback Received", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getContext(), "Only 1 Feedback allowed per hour to avoid spam", Toast.LENGTH_LONG).show();
                        }
                    }
                });




            }
        });
        return root;
    }
}
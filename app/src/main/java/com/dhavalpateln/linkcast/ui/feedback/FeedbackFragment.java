package com.dhavalpateln.linkcast.ui.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FeedbackFragment extends Fragment {

    private Spinner subjectSpinner;
    private EditText messageEditText;

    public String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_feedback, container, false);

        subjectSpinner = root.findViewById(R.id.feedback_subject_spinner);
        messageEditText = root.findViewById(R.id.feedback_message_edit_text);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.feedback_subjects, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(adapter);

        root.findViewById(R.id.send_feedback_button).setOnClickListener(v -> {
            if(messageEditText.getText().toString().equals("")) {
                return;
            }



            final Map<String, Object> update = new HashMap<>();
            update.put("subject", subjectSpinner.getSelectedItem().toString());
            update.put("message", messageEditText.getText().toString());

            String currentDate = getCurrentDate();
            final DatabaseReference userFeedbackRef = FirebaseDBHelper.getUserDataRef().child("feedback").child(currentDate);

            FirebaseDBHelper.getValue(userFeedbackRef, dataSnapshot -> {
                if(dataSnapshot.getValue() == null) {
                    userFeedbackRef.updateChildren(update);
                    FirebaseDBHelper.getFeedbackRef().push().updateChildren(update);
                    messageEditText.setText("");
                    Toast.makeText(getContext(), "Feedback Received", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getContext(), "Only 1 Feedback allowed per hour to avoid spam", Toast.LENGTH_LONG).show();
                }
            });




        });
        return root;
    }
}
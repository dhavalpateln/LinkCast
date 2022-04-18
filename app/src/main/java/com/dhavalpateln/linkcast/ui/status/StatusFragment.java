package com.dhavalpateln.linkcast.ui.status;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.animescrappers.GogoAnimeExtractor;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatusFragment extends Fragment {

    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_status, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout statusContainer = view.findViewById(R.id.status_container_linear_layout);

        gogoanimeTest(statusContainer);
    }

    private void gogoanimeTest(LinearLayout container) {
        LinearLayout divider = generateHeaderView(ProvidersData.GOGOANIME.NAME);
        ConstraintLayout gogoplayStatus = generateStatusView("GogoPlay");
        ConstraintLayout sbStatus = generateStatusView("StreamSB");
        container.addView(divider);
        container.addView(gogoplayStatus);
        container.addView(sbStatus);

        executor.execute(() -> {
        });
    }

    private LinearLayout generateHeaderView(String header) {
        LinearLayout linearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.fragment_status_header, null, false);
        TextView headerTextView = linearLayout.findViewById(R.id.status_header_text_view);
        headerTextView.setText(header);
        return linearLayout;
    }

    private ConstraintLayout generateStatusView(String title) {
        ConstraintLayout constraintLayout = (ConstraintLayout) getActivity().getLayoutInflater().inflate(R.layout.fragment_status_object, null, false);
        TextView titleTextView = constraintLayout.findViewById(R.id.status_title_text_view);
        titleTextView.setText(title);
        return constraintLayout;
    }

}
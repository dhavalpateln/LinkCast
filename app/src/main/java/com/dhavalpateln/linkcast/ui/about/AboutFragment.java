package com.dhavalpateln.linkcast.ui.about;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.data.AppInfo;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class AboutFragment extends Fragment {

    private TextView aboutTextView;
    private Runnable textChangerRunnable;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Handler handler = new Handler();
    private int currentMessage = 0;
    private String[] messages;

    private ImageView androidButton;
    private ImageView windowsButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        /*messages = new String[] {
                "<font color='#0A8BC5'>One</font> stop shop <font color='#0A8BC5'>for</font> <font color='#0A8BC5'>all</font> the brings joy",
                "<font color='#0A8BC5'>Smart</font> - Helping you decide your next memorable show",
                "<font color='#0A8BC5'>Free</font> - Not all good things are :)",
                "<font color='#0A8BC5'>No ads</font> - Use feedback to report if you see one",
                "<font color='#0A8BC5'>Incredibly fast</font> - Official sites may make thousands of requests while Linkcast just enough requests(3-5) to get the job done"
        };*/
        messages = new String[] {
                "<font color='#0A8BC5'>One</font> stop shop <font color='#0A8BC5'>for</font> <font color='#0A8BC5'>all</font> that brings joy",
                //"<font color='#0A8BC5'>Incredibly fast</font>",
                //"<font color='#0A8BC5'>Efficient</font>",
                //"<font color='#0A8BC5'>Smart</font>",
                //"<font color='#0A8BC5'>Free</font>",
                //"<font color='#0A8BC5'>No ads</font>"
        };
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        aboutTextView = view.findViewById(R.id.about_one_text);
        androidButton = view.findViewById(R.id.about_share_android_image_view);
        windowsButton = view.findViewById(R.id.about_share_windows_image_view);

        androidButton.setOnClickListener(v -> copyLink("apklink", AppInfo.getInstance().getApkURL()));
        windowsButton.setOnClickListener(v -> copyLink("apklink", AppInfo.getInstance().getWinURL()));
        //windowsButton.setOnClickListener(v -> copyLink("winlink", AppInfo.getInstance().getWinURL()));

        aboutTextView.setText(Html.fromHtml(messages[currentMessage]));

        textChangerRunnable = new TextChanger();
        handler.postDelayed(textChangerRunnable, 12000);
    }

    private void copyLink(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Link Copied", Toast.LENGTH_LONG).show();
    }

    private class TextChanger implements Runnable {

        @Override
        public void run() {
            currentMessage = (currentMessage + 1) % messages.length;
            uiHandler.post(() -> {
                aboutTextView.setText(Html.fromHtml(messages[currentMessage]));
            });
            handler.postDelayed(this, 9000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(textChangerRunnable);
    }
}
package com.dhavalpateln.linkcast.myanimelist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.MyAnimelistGridRecyclerAdapter;
import com.dhavalpateln.linkcast.database.JikanDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MyAnimeListSearchActivity extends AppCompatActivity {

    public static final String INTENT_SEARCH_PARAMS = "searchparams";
    private RecyclerView recyclerView;
    private MyAnimelistGridRecyclerAdapter recyclerAdapter;
    private List<MyAnimelistAnimeData> dataList;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private ProgressDialog dialog;
    private int page = 1;
    private Button nextButton;
    private Button prevButton;
    private TextView currentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_anime_list_search);

        dataList = new ArrayList<>();

        nextButton = findViewById(R.id.mal_search_next_button);
        prevButton = findViewById(R.id.mal_search_prev_button);
        currentTextView = findViewById(R.id.mal_search_current_text_view);

        recyclerView = findViewById(R.id.adv_search_result_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(MyAnimeListSearchActivity.this, 2));
        recyclerAdapter = new MyAnimelistGridRecyclerAdapter(dataList, getApplicationContext());
        recyclerView.setAdapter(recyclerAdapter);

        updateData();

        nextButton.setOnClickListener(v -> next());
        prevButton.setOnClickListener(v -> prev());

    }

    private void updateData() {
        AdvSearchParams searchParams = (AdvSearchParams) getIntent().getSerializableExtra(INTENT_SEARCH_PARAMS);
        Executors.newSingleThreadExecutor().execute(() -> {
            uiHandler.post(() -> {
                dialog = new ProgressDialog(MyAnimeListSearchActivity.this);
                dialog.setMessage("Please Wait...");
                dialog.setCancelable(false);
                dialog.show();
            });
            List<MyAnimelistAnimeData> result = JikanDatabase.getInstance().getSearchResult(searchParams, page);
            uiHandler.post(() -> {
                dataList.clear();
                dataList.addAll(result);
                dialog.dismiss();
                recyclerAdapter.notifyDataSetChanged();
                currentTextView.setText(((page - 1) * 25 + 1) + " - " + (page * 25));
            });
        });
    }

    private void next() {
        if(dataList.size() == 0)    return;
        page++;
        updateData();
    }

    private void prev() {
        if(page == 1)    return;
        page--;
        updateData();
    }

    public static Intent prepareIntent(Context context, AdvSearchParams searchParams) {
        Intent intent = new Intent(context, MyAnimeListSearchActivity.class);
        intent.putExtra(INTENT_SEARCH_PARAMS, searchParams);
        return intent;
    }
}
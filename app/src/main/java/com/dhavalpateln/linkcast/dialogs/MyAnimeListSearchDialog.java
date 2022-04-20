package com.dhavalpateln.linkcast.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.AnimeDataListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.ListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeListViewHolder;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistSearch;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyAnimeListSearchDialog extends DialogFragment {

    private String searchTerm;
    private EditText searchEditText;
    private List<MyAnimelistAnimeData> dataList;
    private MyAnimelistAnimeData myAnimelistAnimeData;
    private RecyclerView recyclerView;
    private DialogAnimeListAdapter recyclerAdapter;
    private SourceSelectedListener sourceSelectedListener;

    public MyAnimeListSearchDialog(SourceSelectedListener sourceSelectedListener) {
        this("", sourceSelectedListener);
    }
    public MyAnimeListSearchDialog(String searchTerm, SourceSelectedListener sourceSelectedListener) {
        super();
        this.searchTerm = searchTerm;
        dataList = new ArrayList<>();
        this.sourceSelectedListener = sourceSelectedListener;
    }

    public interface SourceSelectedListener {
        void onSourceSelected(MyAnimelistAnimeData myAnimelistAnimeData);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_myanimelist_search, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);

        recyclerAdapter = new DialogAnimeListAdapter(dataList, getContext());

        recyclerView = view.findViewById(R.id.mal_search_dialog_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recyclerAdapter);

        searchEditText = view.findViewById(R.id.mal_search_edit_text);
        searchEditText.setText(searchTerm);

        view.findViewById(R.id.myanimelist_search_search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MALSearchTask().execute();
            }
        });

        view.findViewById(R.id.myanimelist_search_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAnimeListSearchDialog.this.getDialog().cancel();
            }
        });

        new MALSearchTask().execute();

        return builder.create();
    }

    private class DialogAnimeListAdapter extends ListRecyclerAdapter<MyAnimelistAnimeData> {

        public DialogAnimeListAdapter(List<MyAnimelistAnimeData> recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(@NonNull AnimeListViewHolder holder, int position) {

            MyAnimelistAnimeData data = dataList.get(position);

            holder.titleTextView.setText(data.getTitle());

            Glide.with(getContext())
                    .load(data.getImages().get(0))
                    .centerCrop()
                    .crossFade()
                    //.bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.animeImageView);

            holder.mainLayout.setOnClickListener(v -> {
                sourceSelectedListener.onSourceSelected(data);
                MyAnimeListSearchDialog.this.getDialog().cancel();
            });
        }
    }

    private class MALSearchTask extends AsyncTask<Void, Void, List<MyAnimelistAnimeData>> {

        @Override
        protected void onPostExecute(List<MyAnimelistAnimeData> myAnimelistAnimeData) {
            super.onPostExecute(myAnimelistAnimeData);
            dataList.clear();
            dataList.addAll(myAnimelistAnimeData);
            recyclerAdapter.notifyDataSetChanged();
        }

        @Override
        protected List<MyAnimelistAnimeData> doInBackground(Void... voids) {
            return MyAnimelistSearch.anime(searchEditText.getText().toString());
        }
    }
}

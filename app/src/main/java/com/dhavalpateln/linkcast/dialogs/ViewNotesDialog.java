package com.dhavalpateln.linkcast.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.viewholders.NotesViewHolder;
import com.dhavalpateln.linkcast.utils.EpisodeNode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ViewNotesDialog extends LinkCastDialog {

    private List<EpisodeNode> noteEpisodeNodes;

    @Override
    public int getContentLayout() {
        return R.layout.dialog_view_notes;
    }

    public ViewNotesDialog(List<EpisodeNode> episodeList) {
        noteEpisodeNodes = new ArrayList<>();
        for(EpisodeNode episodeNode: episodeList) {
            if(episodeNode.getNote() != null) {
                noteEpisodeNodes.add(episodeNode);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = getContentView();
        RecyclerView recyclerView = view.findViewById(R.id.view_notes_recycler_view);
        NotesRecyclerAdapter recyclerAdapter = new NotesRecyclerAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recyclerAdapter);

        if(noteEpisodeNodes.size() == 0) {
            view.findViewById(R.id.dialog_view_notes_instruction).setVisibility(View.VISIBLE);
        }

        return dialog;
    }

    private class NotesRecyclerAdapter extends RecyclerView.Adapter<NotesViewHolder> {

        @NonNull
        @Override
        public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_note_oblect, parent, false);
            return new NotesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
            holder.episodeNumTextView.setText("Episode - " + noteEpisodeNodes.get(position).getEpisodeNumString());
            holder.noteTextView.setText(noteEpisodeNodes.get(position).getNote());
        }

        @Override
        public int getItemCount() {
            return noteEpisodeNodes.size();
        }
    }

}

package com.dhavalpateln.linkcast.adapters.viewholders;

import android.view.View;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotesViewHolder extends RecyclerView.ViewHolder {

    public TextView episodeNumTextView;
    public TextView noteTextView;

    public NotesViewHolder(@NonNull View itemView) {
        super(itemView);
        episodeNumTextView = itemView.findViewById(R.id.episode_num_note_text_view);
        noteTextView = itemView.findViewById(R.id.episode_note_text_view);
    }
}

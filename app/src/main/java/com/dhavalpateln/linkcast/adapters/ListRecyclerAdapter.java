package com.dhavalpateln.linkcast.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ListRecyclerAdapter<T> extends RecyclerView.Adapter<ListRecyclerAdapter.ListRecyclerViewHolder> {

    public interface RecyclerInterface<T> {
        void onBindView(ListRecyclerAdapter.ListRecyclerViewHolder holder, int position, T data);
    }

    private List<T> dataArrayList;
    private Context mcontext;
    private RecyclerInterface recyclerInterface;

    public ListRecyclerAdapter(List<T> recyclerDataArrayList, Context mcontext, RecyclerInterface recyclerInterface) {
        this.dataArrayList = recyclerDataArrayList;
        this.mcontext = mcontext;
        this.recyclerInterface = recyclerInterface;
    }

    @NonNull
    @Override
    public ListRecyclerAdapter.ListRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_recycler_object, parent, false);
        return new ListRecyclerAdapter.ListRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListRecyclerAdapter.ListRecyclerViewHolder holder, int position) {
        // Set the data to textview and imageview.
        this.recyclerInterface.onBindView(holder, position, dataArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        // this method returns the size of recyclerview
        return dataArrayList.size();
    }

    // View Holder Class to handle Recycler View.
    public class ListRecyclerViewHolder extends RecyclerView.ViewHolder {

        public TextView subTextTextView;
        public TextView titleTextView;
        public ImageView imageView;
        public LinearLayout buttonHolder;
        public ConstraintLayout mainLayout;

        public ListRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mainLayout = (ConstraintLayout) itemView;
            this.subTextTextView = itemView.findViewById(R.id.list_object_subtext_text_view);
            this.titleTextView = itemView.findViewById(R.id.list_object_title_text_view);
            this.buttonHolder = itemView.findViewById(R.id.list_object_buttons_holder_linearlayout);
            this.imageView = itemView.findViewById(R.id.list_object_image_view);
        }
    }
}
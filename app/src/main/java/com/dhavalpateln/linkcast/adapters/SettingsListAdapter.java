package com.dhavalpateln.linkcast.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.viewholders.SettingsViewHolder;
import com.dhavalpateln.linkcast.interfaces.SettingsObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsListAdapter extends RecyclerView.Adapter<SettingsViewHolder>{

    protected List<SettingsObject> dataArrayList;
    protected Context mContext;

    public SettingsListAdapter(List<SettingsObject> dataArrayList, Context mContext) {
        this.dataArrayList = dataArrayList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_object, parent, false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        holder.titleTextView.setText(dataArrayList.get(position).getTitle());
        holder.valueTextView.setText(dataArrayList.get(position).getValue());
        holder.mainLayout.setOnClickListener(v -> dataArrayList.get(position).showDialog(mContext, result -> {
            dataArrayList.get(position).setValue(result);
            holder.valueTextView.setText(result);
        }));
    }

    @Override
    public int getItemCount() {
        return dataArrayList.size();
    }
}

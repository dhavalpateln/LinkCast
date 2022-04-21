package com.dhavalpateln.linkcast.adapters.viewholders;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsViewHolder extends RecyclerView.ViewHolder {
    public TextView titleTextView;
    public TextView valueTextView;
    public LinearLayout mainLayout;

    public SettingsViewHolder(@NonNull View itemView) {
        super(itemView);
        this.mainLayout = (LinearLayout) itemView;
        this.titleTextView = itemView.findViewById(R.id.settings_object_title_text_view);
        this.valueTextView = itemView.findViewById(R.id.settings_object_value_text_view);
    }
}
package com.dhavalpateln.linkcast.exoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ui.DefaultTimeBar;

@SuppressLint("ViewConstructor")
public class ExtendedTimeBar extends DefaultTimeBar {
    private boolean enabled;
    private boolean forceDisabled;

    public ExtendedTimeBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        super.setEnabled(!this.forceDisabled && this.enabled);
    }

    public void setForceDisabled(boolean forceDisabled) {
        this.forceDisabled = forceDisabled;
        setEnabled(enabled);
    }
}
package com.dhavalpateln.linkcast.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class RatioedImageView extends androidx.appcompat.widget.AppCompatImageView {
    public RatioedImageView(Context context) {
        super(context);
    }

    public RatioedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RatioedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, (int) ((4.0/3.0) * widthMeasureSpec));
    }
}

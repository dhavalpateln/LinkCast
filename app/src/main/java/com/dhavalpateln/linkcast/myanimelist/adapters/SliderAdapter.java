package com.dhavalpateln.linkcast.myanimelist.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.manga.MangaReaderActivity;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterViewHolder> {

    // list for storing urls of images.
    private final List<String> mSliderItems;
    private Context context;
    private ImageClickListener imageClickListener;

    public interface ImageClickListener {
        void onClick(List<String> images, int position);
    }

    // Constructor
    public SliderAdapter(Context context, ArrayList<String> sliderDataArrayList, ImageClickListener imageClickListener) {
        this.mSliderItems = sliderDataArrayList;
        this.context = context;
        this.imageClickListener = imageClickListener;
    }

    // We are inflating the slider_layout
    // inside on Create View Holder method.
    @Override
    public SliderAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_anime_info, null);
        return new SliderAdapterViewHolder(inflate);
    }

    // Inside on bind view holder we will
    // set data to item of Slider View.
    @Override
    public void onBindViewHolder(SliderAdapterViewHolder viewHolder, final int position) {

        final String sliderItem = mSliderItems.get(position);

        // Glide is use to load image
        // from url in your imageview.
        Glide.with(context)
                .load(sliderItem)
                //.fitCenter()
                .centerCrop()
                .into(viewHolder.imageViewBackground);

        viewHolder.imageViewBackground.setOnClickListener(v -> {
            imageClickListener.onClick(mSliderItems, position);
        });
    }

    // this method will return
    // the count of our list.
    @Override
    public int getCount() {
        return mSliderItems.size();
    }

    static class SliderAdapterViewHolder extends SliderViewAdapter.ViewHolder {
        // Adapter class for initializing
        // the views of our slider view.
        View itemView;
        ImageView imageViewBackground;

        public SliderAdapterViewHolder(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.anime_info_slider_image_view);
            this.itemView = itemView;
        }
    }

}

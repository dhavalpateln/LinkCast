package com.dhavalpateln.linkcast.myanimelist.ui.main;

import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class MyAnimelistDataViewModel extends ViewModel {

    private MutableLiveData<MyAnimelistAnimeData> data;

    public MyAnimelistDataViewModel() { this.data = new MutableLiveData<>(); }

    public void setData(MyAnimelistAnimeData data) {
        this.data.setValue(data);
    }

    public LiveData<MyAnimelistAnimeData> getData() {
        if(data == null) {
            data = new MutableLiveData<>();
            data.setValue(new MyAnimelistAnimeData(0));
        }
        return data;
    }

}
package com.brian19109.weatherapi.database;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.brian19109.weatherapi.model.WeatherPlace;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class PlaceViewModel extends AndroidViewModel {

    private final ProjectRepository mRepository;

    public PlaceViewModel(Application application) {
        super(application);
        mRepository = new ProjectRepository(application);
    }

    public LiveData<List<WeatherPlace>> getAllPlaces(String projectName) throws ExecutionException, InterruptedException {
        return mRepository.getAllPlacesByProjectName(projectName);
    }
}
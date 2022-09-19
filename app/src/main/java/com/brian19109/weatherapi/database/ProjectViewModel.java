package com.brian19109.weatherapi.database;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.brian19109.weatherapi.model.WeatherPlace;
import com.brian19109.weatherapi.model.Project;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProjectViewModel extends AndroidViewModel {

    private ProjectRepository mRepository;

    private final LiveData<List<Project>> mAllProjects;

    public ProjectViewModel (Application application) {
        super(application);
        mRepository = new ProjectRepository(application);
        mAllProjects = mRepository.getAllProjects();
    }

    public LiveData<List<Project>> getAllProjects() { return mAllProjects; }

    public LiveData<List<Project>> getAllProjectsByPartialName(String partialName) throws ExecutionException, InterruptedException { return mRepository.getAllProjectsByPartialName(partialName); }

    public void insertProject(Project project) { mRepository.insertProject(project); }

    public void deleteProject(String projectName) { mRepository.deleteProject(projectName); }

    public void insertPlace(WeatherPlace place) { mRepository.insertPlace(place); }
}
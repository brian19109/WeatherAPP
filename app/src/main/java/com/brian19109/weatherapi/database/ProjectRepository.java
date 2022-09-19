package com.brian19109.weatherapi.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.brian19109.weatherapi.model.WeatherPlace;
import com.brian19109.weatherapi.model.Project;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProjectRepository {
    private final LiveData<List<Project>> mAllProjects;
    private ProjectDao mProjectDao;
    private WeatherPlaceDao mWeatherPlaceDao;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public ProjectRepository(Application application) {
        ProjectRoomDatabase db = ProjectRoomDatabase.getDatabase(application);
        mProjectDao = db.projectDao();
        mWeatherPlaceDao = db.weatherPlaceDao();
        mAllProjects = mProjectDao.getAllProjects();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<Project>> getAllProjects() {
        return mAllProjects;
    }

    public LiveData<Project> getProjectByName(String projectName) throws ExecutionException, InterruptedException {
        return ProjectRoomDatabase.databaseWriteExecutor.submit(() -> mProjectDao.getByProjectName(projectName)).get();
    }

    public LiveData<List<Project>> getAllProjectsByPartialName(String partialName) throws ExecutionException, InterruptedException {
        return ProjectRoomDatabase.databaseWriteExecutor.submit(() -> mProjectDao.getAllProjectsByPartialName(partialName)).get();
    }

    public LiveData<List<WeatherPlace>> getAllPlacesByProjectName(String projectName) throws ExecutionException, InterruptedException {
        return ProjectRoomDatabase.databaseWriteExecutor.submit(() -> mWeatherPlaceDao.getByProjectName(projectName)).get();
    }

    public LiveData<List<WeatherPlace>> getAllPlaces() throws ExecutionException, InterruptedException {
        return ProjectRoomDatabase.databaseWriteExecutor.submit(() -> mWeatherPlaceDao.getAllPlaces()).get();
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insertProject(Project project) {
        ProjectRoomDatabase.databaseWriteExecutor.execute(() -> mProjectDao.insertProject(project));
    }

    public void insertPlace(WeatherPlace place) {
        ProjectRoomDatabase.databaseWriteExecutor.execute(() -> mWeatherPlaceDao.insertPlace(place));
    }

    public void deleteProject(String projectName) {
        ProjectRoomDatabase.databaseWriteExecutor.execute(() -> mProjectDao.deleteByProjectName(projectName));
        ProjectRoomDatabase.databaseWriteExecutor.execute(() -> mWeatherPlaceDao.deleteByProjectName(projectName));
    }
}
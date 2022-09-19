package com.brian19109.weatherapi.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.brian19109.weatherapi.model.WeatherPlace;

import java.util.List;

@Dao
public interface WeatherPlaceDao {
    @Query("SELECT * FROM place_table WHERE project_name = :projectName")
    LiveData<List<WeatherPlace>> getByProjectName(String projectName);

    @Query("SELECT * FROM place_table")
    LiveData<List<WeatherPlace>> getAllPlaces();

    // Insert one place
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPlace(WeatherPlace place);

    // Delete places by project name
    @Query("DELETE FROM place_table WHERE project_name = :projectName")
    void deleteByProjectName(String projectName);
}

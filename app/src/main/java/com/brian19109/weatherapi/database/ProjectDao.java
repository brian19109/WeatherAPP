package com.brian19109.weatherapi.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.brian19109.weatherapi.model.Project;

import java.util.List;

@Dao
public interface ProjectDao {
    @Query("SELECT * FROM project_table")
    LiveData<List<Project>> getAllProjects();

    @Query("SELECT * FROM project_table WHERE project_name = :projectName")
    LiveData<Project> getByProjectName(String projectName);

    @Query("SELECT * FROM project_table WHERE project_name LIKE '%' || :partialName || '%'")
    LiveData<List<Project>> getAllProjectsByPartialName(String partialName);

    // Insert one project
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProject(Project project);

    // Delete one item by name
    @Query("DELETE FROM project_table WHERE project_name = :projectName")
    void deleteByProjectName(String projectName);
}

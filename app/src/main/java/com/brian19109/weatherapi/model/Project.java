package com.brian19109.weatherapi.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "project_table")
public class Project {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "project_name")
    public String mProjectName;

    @ColumnInfo(name = "departure_date_time")
    public String mDepartureDateTime;

    @ColumnInfo(name = "origin_address")
    public String mOriginAddress;

    public Project(@NonNull String projectName, @NonNull String departureDateTime, @NonNull String originAddress) {
        mProjectName = projectName;
        mDepartureDateTime = departureDateTime;
        mOriginAddress = originAddress;
    }

    public String getProjectName() {
        return mProjectName;
    }

    public void setProjectName(String projectName) {
        this.mProjectName = projectName;
    }

    public String getDepartureDateTime() {
        return mDepartureDateTime;
    }

    public void setDepartureDateTime(String departureDateTime) {
        this.mDepartureDateTime = departureDateTime;
    }

    public String getOriginAddress() {
        return mOriginAddress;
    }

    public void setOriginAddress(String originAddress) {
        this.mOriginAddress = originAddress;
    }
}

package com.brian19109.weatherapi.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.brian19109.weatherapi.util.Constants;

import java.time.Duration;

@Entity(tableName = "place_table")
public class WeatherPlace {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "place_uid")
    public long mPlaceUid;

    @ColumnInfo(name = "project_name")
    public String mProjectName;

    @ColumnInfo(name = "travel_mode")
    public Constants.TravelModes mTravelMode;

    @ColumnInfo(name = "address")
    public String mAddress;

    @ColumnInfo(name = "arrival_time")
    public String mArrivalTime;

    @ColumnInfo(name = "stay_duration")
    public Duration mStayDuration;

    public WeatherPlace(@NonNull String projectName, Constants.TravelModes travelMode, @NonNull String address, @NonNull Duration stayDuration) {
        mProjectName = projectName;
        mTravelMode = travelMode;
        mAddress = address;
        mStayDuration = stayDuration;
    }

    public String getArrivalTime() {
        return mArrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.mArrivalTime = arrivalTime;
    }

    public String getProjectName() {
        return mProjectName;
    }

    public void setProjectName(String projectName) {
        this.mProjectName = projectName;
    }

    public long getPlaceUid() {
        return mPlaceUid;
    }

    public void setPlaceUid(long placeUid) {
        this.mPlaceUid = placeUid;
    }

    public Constants.TravelModes getTravelMode() {
        return mTravelMode;
    }

    public void setTravelMode(Constants.TravelModes travelMode) {
        this.mTravelMode = travelMode;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public Duration getStayDuration() {
        return mStayDuration;
    }

    public void setStayDuration(Duration stayDuration) {
        this.mStayDuration = stayDuration;
    }
}

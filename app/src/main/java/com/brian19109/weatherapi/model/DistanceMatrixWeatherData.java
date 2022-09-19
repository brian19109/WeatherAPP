package com.brian19109.weatherapi.model;

import com.google.android.gms.maps.model.LatLng;

public class DistanceMatrixWeatherData {
    String mOriginAddress;
    String mDestinationAddress;
    String mDepartureTime;
    String mDurationTime;
    String mArrivalTime;
    String mWx;
    String mWxIconValue;
    String mPoP6h;
    String mApparentTemperature;
    String mTemperature;
    LatLng mOrigin;
    LatLng mDestination;

    public DistanceMatrixWeatherData() {
    }

    public String getOriginAddress() {
        return mOriginAddress;
    }

    public void setOriginAddress(String mOriginAddress) {
        this.mOriginAddress = mOriginAddress;
    }

    public String getDestinationAddress() {
        return mDestinationAddress;
    }

    public void setDestinationAddress(String mDestinationAddress) {
        this.mDestinationAddress = mDestinationAddress;
    }

    public String getDepartureTime() {
        return mDepartureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.mDepartureTime = departureTime;
    }

    public String getDurationTime() {
        return mDurationTime;
    }

    public void setDurationTime(String mDurationTime) {
        this.mDurationTime = mDurationTime;
    }

    public String getArrivalTime() {
        return mArrivalTime;
    }

    public void setArrivalTime(String mArrivalTime) {
        this.mArrivalTime = mArrivalTime;
    }

    public String getWx() {
        return mWx;
    }

    public void setWx(String mWx) {
        this.mWx = mWx;
    }

    public String getWxIconValue() {
        return mWxIconValue;
    }

    public void setWxIconValue(String mWxIconValue) {
        this.mWxIconValue = mWxIconValue;
    }

    public String getPoP6h() {
        return mPoP6h;
    }

    public void setPoP6h(String mPoP6h) {
        this.mPoP6h = mPoP6h;
    }

    public String getApparentTemperature() {
        return mApparentTemperature;
    }

    public void setApparentTemperature(String mApparentTemperature) {
        this.mApparentTemperature = mApparentTemperature;
    }

    public String getTemperature() {
        return mTemperature;
    }

    public void setTemperature(String mTemperature) {
        this.mTemperature = mTemperature;
    }

    public LatLng getOrigin() {
        return mOrigin;
    }

    public void setOrigin(LatLng mOrigin) {
        this.mOrigin = mOrigin;
    }

    public LatLng getDestination() {
        return mDestination;
    }

    public void setDestination(LatLng mDestination) {
        this.mDestination = mDestination;
    }
}

package com.brian19109.weatherapi;

import android.location.Location;

public interface GetLastLocationCallback {
    void onGetLastLocationSuccess(Location location);
}

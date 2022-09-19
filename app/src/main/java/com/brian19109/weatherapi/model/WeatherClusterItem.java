package com.brian19109.weatherapi.model;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

// marker cluster使用
// 參考 https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering?hl=zh-tw

public class WeatherClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final BitmapDescriptor bitmap;

    public WeatherClusterItem(double lat, double lng, String title, String snippet, BitmapDescriptor bitmap) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
        this.bitmap = bitmap;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public BitmapDescriptor getBitmap() {
        return bitmap;
    }
}



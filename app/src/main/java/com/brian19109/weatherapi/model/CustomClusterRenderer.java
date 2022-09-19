package com.brian19109.weatherapi.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class CustomClusterRenderer extends DefaultClusterRenderer<WeatherClusterItem> {

    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);
    }

    // 把 marker 的相關內容都設定好
    @Override
    protected void onBeforeClusterItemRendered(@NonNull WeatherClusterItem item, @NonNull MarkerOptions markerOptions) {
        markerOptions.icon(item.getBitmap());
        markerOptions.title(item.getTitle());
        markerOptions.snippet(item.getSnippet());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}

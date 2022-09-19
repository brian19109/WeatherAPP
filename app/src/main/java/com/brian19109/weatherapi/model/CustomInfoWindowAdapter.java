package com.brian19109.weatherapi.model;

import static com.brian19109.weatherapi.util.Constants.temperatureMeasureChar;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brian19109.weatherapi.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final Activity mContext;

    public CustomInfoWindowAdapter(Activity context) {
        mContext = context;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        View view = mContext.getLayoutInflater().inflate(R.layout.custom_info_window, null);

        TextView tvCurrentPosition = view.findViewById(R.id.tv_current_position);
        TextView tvCurrentTime = view.findViewById(R.id.tv_current_time);
        TextView tvDurationTime = view.findViewById(R.id.tv_duration_time);
        TextView tvArrivalTime = view.findViewById(R.id.tv_arrival_time);
        TextView tvPrecipitation = view.findViewById(R.id.tv_precipitation);
        TextView tvWeather = view.findViewById(R.id.tv_weather_condition_text);
        TextView tvAT = view.findViewById(R.id.tv_apparent_temperature);
        TextView tvT = view.findViewById(R.id.tv_temperature);
        TextView tvATTitle = view.findViewById(R.id.tv_apparent_temperature_title);

        ImageView ivWeatherConditionIcon = view.findViewById(R.id.iv_weather_condition_icon);

        String[] result = marker.getSnippet().split("\n");

        String currentPosition = "目標位置：" + marker.getTitle();

        tvCurrentPosition.setText(currentPosition);
        tvCurrentTime.setText(result[0]);
        tvDurationTime.setText(result[1]);
        tvArrivalTime.setText(result[2]);
        tvWeather.setText(result[3]);

        if (!result[5].contains("null")) {
            tvPrecipitation.setVisibility(View.VISIBLE);
            tvPrecipitation.setText(result[5]);
        } else {
            tvPrecipitation.setVisibility(View.GONE);
        }

        if (!result[6].contains("null")) {
            tvATTitle.setVisibility(View.VISIBLE);
            tvAT.setVisibility(View.VISIBLE);
            tvAT.setText(new StringBuilder().append(result[6]).append(temperatureMeasureChar).append("C").toString());
        } else {
            tvATTitle.setVisibility(View.GONE);
            tvAT.setVisibility(View.GONE);
        }
        tvT.setText(new StringBuilder().append(result[7]).append(temperatureMeasureChar).append("C").toString());

        int id = mContext.getResources().getIdentifier("ic_" + result[4], "drawable", "com.brian19109.weatherapi");
        ivWeatherConditionIcon.setImageResource(id);

        return view;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }
}

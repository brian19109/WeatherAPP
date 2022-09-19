package com.brian19109.weatherapi.database;

import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class PlaceTypeConverter {

    @TypeConverter
    public static ArrayList<Long> fromString(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<Long> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }

    @TypeConverter
    public static Duration fromSeconds(Long value) {
        return value == null ? null : Duration.ofSeconds(value);
    }

    @TypeConverter
    public static Long durationToSeconds(Duration duration) {
        return duration == null ? null : duration.getSeconds();
    }
}

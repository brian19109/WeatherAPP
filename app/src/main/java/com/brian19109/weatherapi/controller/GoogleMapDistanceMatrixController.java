package com.brian19109.weatherapi.controller;

import com.brian19109.weatherapi.util.Constants;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class GoogleMapDistanceMatrixController {
    public GoogleMapDistanceMatrixController() {
    }

    public JSONObject getGoogleMaDistanceMatrixData(Constants.TravelModes travelMode, LatLng origin, LatLng destination) {
        JSONObject jsonObjectGoogleMaDistanceMatrix = null;
        String originEncodedString = getPlaceLatLngEncodedString(origin);
        String destinationEncodedString = getPlaceLatLngEncodedString(destination);
        String avoid = (travelMode == Constants.TravelModes.MOTORCYCLING ? "&avoid=highways" : "");

        if (originEncodedString != null && destinationEncodedString != null) {
            String URL = Constants.GOOGLE_MAP_DISTANCE_MATRIX_HOST + "?origins=" + originEncodedString + "&destinations=" + destinationEncodedString + "&mode=" + travelMode.toString() + "&language=zh-TW" + avoid + "&key=" + Constants.GOOGLE_MAP_API_AUTH_KEY;

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                    .build();

            //對此URL做request
            Request request = new Request.Builder()
                    .url(URL)
                    .method("GET", null)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                jsonObjectGoogleMaDistanceMatrix = new JSONObject(Objects.requireNonNull(response.body()).string());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        }

        return jsonObjectGoogleMaDistanceMatrix;
    }

    private String getPlaceLatLngEncodedString(LatLng place) {
        String placeLatLngEncodedString = null;

        if (place != null) {
            try {
                placeLatLngEncodedString = URLEncoder.encode(place.latitude + "," + place.longitude, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return placeLatLngEncodedString;
    }
}

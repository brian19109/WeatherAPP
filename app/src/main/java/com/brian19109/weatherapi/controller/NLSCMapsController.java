package com.brian19109.weatherapi.controller;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

// API 說明文件：https://data.gov.tw/dataset/152915
public class NLSCMapsController {

    public NLSCMapsController() {}

    public Document getCityAndDistrictByLatLng(LatLng target) {
        Document xmlDocumentCityAndDistrictByLatLng = null;
        String URL = "https://api.nlsc.gov.tw/other/TownVillagePointQuery1/" + target.longitude + "/" + target.latitude;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();

        //對此URL做request
        Request request = new Request.Builder()
                .url(URL)
                .addHeader("User-Agent:", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36")
                .method("GET", null)
                .build();

        try {
            Response response = client.newCall(request).execute();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            xmlDocumentCityAndDistrictByLatLng = builder.parse(new InputSource(new StringReader(response.body().string())));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return xmlDocumentCityAndDistrictByLatLng;
    }
}

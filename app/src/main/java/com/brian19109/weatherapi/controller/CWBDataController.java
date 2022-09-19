package com.brian19109.weatherapi.controller;

import com.brian19109.weatherapi.util.Constants;
import com.brian19109.weatherapi.util.Constants.CWBDataCityID;
import com.brian19109.weatherapi.util.Constants.CWBDataInterval;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class CWBDataController {
    private final OkHttpClient mClient = new OkHttpClient().newBuilder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build();

    public CWBDataController() {
    }

    // 重點部分
    // 方法大致解說
    // 氣象局api串接格式說明
    public JSONObject getCWBData(CWBDataInterval interval, CWBDataCityID countryID) {
        JSONObject jsonObjectCWB = null;
        String countryIDForDataInterval = null;

        // :=%3A chractor code
        // CWB 的 OpenAPI 使用 /v1/rest/datastore/F-D0047-093 這隻資料
        // 而取樣的資料是有規律的，各縣市的兩天天氣預報尾數以4遞增
        // ex:F-D0047-001=宜蘭縣、F-D0047-005=桃園市......
        switch (interval) {
            case ONE_WEEK:
                countryIDForDataInterval = String.format(Locale.ENGLISH, "%03d", Integer.parseInt(countryID.toString()) + 2);
                break;
            case THREE_DAYS:
            default:
                countryIDForDataInterval = countryID.toString();
                break;
        }
        String URL = Constants.CWB_OPEN_DATA_HOST + "?Authorization=" + Constants.CWB_API_AUTH_KEY + "&format=JSON&locationId=F-D0047-" + countryIDForDataInterval + "&elementName=Wx,PoP6h,AT,T";

        //對此URL做request
        Request request = new Request.Builder()
                .url(URL)
                .addHeader("User-Agent:", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36")
                .build();

        try {
            //此response為上方request之結果，接著進行JSON的解析，此處建議使用json parser相關工具對照著看會比較看得懂解析的步驟
            //json解析工具 http://jsoneditoronline.org/
            Response response = mClient.newCall(request).execute();
            jsonObjectCWB = new JSONObject(Objects.requireNonNull(response.body()).string());
            Objects.requireNonNull(response.body()).close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return jsonObjectCWB;
    }
}

package com.brian19109.weatherapi.util;

import androidx.annotation.NonNull;

public class Constants {
    // 氣象局 API key
    public static final String CWB_API_AUTH_KEY = "CWB-DC98BC30-EA33-4843-8569-8DDC20B6E646";
    public static final String CWB_OPEN_DATA_HOST = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-093";

    public static final String GOOGLE_MAP_DISTANCE_MATRIX_HOST = "https://maps.googleapis.com/maps/api/distancematrix/json";
    public static final String GOOGLE_GEOCODING_HOST = "https://maps.googleapis.com/maps/api/geocode/json";
    public static final String GOOGLE_MAP_API_AUTH_KEY = "AIzaSyD6dOCXPRHQl2V99RT_v3crPnM6EshRZ7Q";
    public static final String GOOGLE_GEOCODING_API_AUTH_KEY = "AIzaSyD6dOCXPRHQl2V99RT_v3crPnM6EshRZ7Q";
    public static final String GOOGLE_PLACES_SDK_AUTH_KEY = "AIzaSyD6dOCXPRHQl2V99RT_v3crPnM6EshRZ7Q";

    public static final String STAY_DURATION_FINAL_DESTINATION = "停留：最後位置";

    public static final String CITY = "city";
    public static final String DISTRICT = "district";

    public static final char temperatureMeasureChar = 0x00B0;

    public enum TravelModes {
        DRIVING("driving"),
        MOTORCYCLING("motorcycling"),
        BICYCLING("bicycling"),
        WALKING("walking");

        private final String mTravelMode;

        TravelModes(String travelMode) {
            mTravelMode = travelMode;
        }

        @NonNull
        @Override
        public String toString() {
            return mTravelMode;
        }
    }

    public enum CWBDataInterval {
        THREE_DAYS("3 days"),
        ONE_WEEK("1 week");

        private final String mInterval;

        CWBDataInterval(String interval) {
            mInterval = interval;
        }

        @NonNull
        @Override
        public String toString() {
            return mInterval;
        }
    }

    public enum CWBDataCityID {
        Yilan_County("宜蘭縣", "001"),
        Taoyuan_City("桃園市", "005"),
        Hsinchu_County("新竹縣", "009"),
        Miaoli_County("苗栗縣", "013"),
        Changhua_County("彰化縣","017"),
        Nantou_County("南投縣", "021"),
        Yunlin_County("雲林縣", "025"),
        Chiayi_County("嘉義縣", "029"),
        Pingtung_County("屏東縣", "033"),
        Taitung_County("臺東縣", "037"),
        Hualien_County("花蓮縣", "041"),
        Penghu_County("澎湖縣", "045"),
        Keelung_City(" 基隆市", "049"),
        Hsinchu_City("新竹市", "053"),
        Chiayi_City("嘉義市", "057"),
        Taipei_City("臺北市", "061"),
        Kaohsiung_City("高雄市", "065"),
        New_Taipei_City("新北市", "069"),
        Taichung_City("臺中市", "073"),
        Tainan_City("臺南市", "077"),
        Lienchiang_County("連江縣", "081"),
        Kinmen_County("金門縣", "085");

        private final String mCityName;
        private final String mCityID;

        CWBDataCityID(String cityName, String cityID) {
            mCityName = cityName;
            mCityID = cityID;
        }

        @NonNull
        @Override
        public String toString() {
            return mCityID;
        }

        public String getCityName() {
            return mCityName;
        }
    }
}

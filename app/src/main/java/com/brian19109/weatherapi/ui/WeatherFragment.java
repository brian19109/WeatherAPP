package com.brian19109.weatherapi.ui;

import static com.brian19109.weatherapi.util.Constants.temperatureMeasureChar;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.brian19109.weatherapi.R;
import com.brian19109.weatherapi.controller.CWBDataController;
import com.brian19109.weatherapi.util.Constants;
import com.brian19109.weatherapi.util.Util;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherFragment extends Fragment {
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(1);
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final String mCurrentCity;
    private final String mCurrentDistrict;
    private final ArrayList<HashMap<String, String>> mTargetLocationCWBDataThreeHours = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> mTargetLocationCWBDataOneWeek = new ArrayList<>();
    private final ArrayList<ImageView> mWeatherConditionIconsThreeDays = new ArrayList<>();
    private final ArrayList<TextView> mWeatherConditionPrecipitationThreeDays = new ArrayList<>();
    private final ArrayList<TextView> mWeatherConditionDayOneWeek = new ArrayList<>();
    private final ArrayList<TextView> mWeatherConditionDateOneWeek = new ArrayList<>();
    private final ArrayList<TextView> mWeatherConditionTemperatureThreeHrs = new ArrayList<>();
    private final ArrayList<TextView> mWeatherConditionTimeThreeHrs = new ArrayList<>();
    private final ArrayList<ImageView> mWeatherConditionIconsOneWeek = new ArrayList<>();
    private final HashMap<String, ArrayList<String>> mTaiwanCitiesDistricts = new HashMap<>();
    private final Fragment mLoadingFragment;
    private ImageView mWeatherConditionIconNow;
    private TextView mWeatherConditionTextNow;
    private TextView mPrecipitationTextNow;
    private TextView mTemperatureNow;
    private TextView mApparentTemperatureNow;
    private ConstraintLayout mFragmentWeather;
    private LineChart mLineChartTemperature;
    private LineData mLineData;
    private boolean mIsSpinnerCityUserClicked = false;
    private boolean mIsSpinnerDistrictUserClicked = false;
    private String mPreviousSelectedCity;

    public WeatherFragment(String currentCity, String currentDistrict) {
        mCurrentCity = currentCity;
        mCurrentDistrict = currentDistrict;
        mLoadingFragment = new LoadingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() != null) {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.mainFrameLayout, mLoadingFragment);
            fragmentTransaction.show(mLoadingFragment);
            fragmentTransaction.hide(this);
            fragmentTransaction.commit();

            mExecutor.execute(() -> {
                if (mCurrentCity != null && mCurrentDistrict != null &&
                        !TextUtils.isEmpty(mCurrentCity) && !TextUtils.isEmpty(mCurrentDistrict)) {
                    getCWBWeatherData(mCurrentCity, mCurrentDistrict, true);
                    initCityAndDistrictSpinners();
                }

                mMainThreadHandler.post(WeatherFragment.this::updateWeatherData);
            });
        }
    }

    private void getCWBWeatherData(String targetCity, String targetDistrict, boolean isInitCitySpinnerNeeded) {
        clearData();

        for (Constants.CWBDataCityID countryID : Constants.CWBDataCityID.values()) {

            JSONObject cwbDataThreeDays = new CWBDataController().getCWBData(Constants.CWBDataInterval.THREE_DAYS, countryID);
            JSONObject cwbDataOneWeek = new CWBDataController().getCWBData(Constants.CWBDataInterval.ONE_WEEK, countryID);

            JSONArray cwbWeatherDataThreeDaysForDistricts = Util.getWeatherData(cwbDataThreeDays);
            JSONArray cwbWeatherDataOneWeekForDistricts = Util.getWeatherData(cwbDataOneWeek);

            // 從氣象局 API 取得資料，初始化臺灣縣市行政區下拉選單的資料。
            if (isInitCitySpinnerNeeded) {
                ArrayList<String> districtsArrayList = new ArrayList<>();

                if (cwbWeatherDataThreeDaysForDistricts != null) {
                    for (int i = 0; i < cwbWeatherDataThreeDaysForDistricts.length(); i++) {
                        String district = null;

                        try {
                            district = cwbWeatherDataThreeDaysForDistricts.getJSONObject(i) != null ? cwbWeatherDataThreeDaysForDistricts.getJSONObject(i).getString("locationName") : null;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (district != null) {
                            districtsArrayList.add(district.trim());
                        }
                    }
                }

                mTaiwanCitiesDistricts.put(countryID.getCityName().trim(), districtsArrayList);
            }

            // 根據傳入縣市行政區的參數讀取氣象資料
            if (countryID.getCityName().trim().equals(targetCity.trim())) {
                if (cwbWeatherDataThreeDaysForDistricts != null) {
                    for (int i = 0; i < cwbWeatherDataThreeDaysForDistricts.length(); i++) {
                        String district = null;

                        try {
                            district = cwbWeatherDataThreeDaysForDistricts.getJSONObject(i) != null ? cwbWeatherDataThreeDaysForDistricts.getJSONObject(i).getString("locationName") : null;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // 拿傳入鄉鎮市區行政區參數的氣象資料
                        if (district != null && district.trim().equals(targetDistrict.trim())) {
                            String targetLocationWxThreeHours;                   // 氣象文字
                            String targetLocationWxIconThreeHours;               // 氣象圖示
                            String targetLocationPrecipitationThreeHours;        // 降雨機率
                            String targetLocationTemperatureThreeHours;          // 溫度
                            String targetLocationApparentTemperatureThreeHours;  // 體感溫度
                            String targetLocationStartTimeThreeHours;            // 起始時間
                            String targetLocationEndTimeThreeHours;              // 結束時間

                            ArrayList<Entry> temperatureThreeHours = new ArrayList<>();

                            for (int j = 0; j < 24; j++) {
                                // 拿到六筆逐3小時天氣預報資料就結束 j 迴圈
                                if (mTargetLocationCWBDataThreeHours.size() == 6) {
                                    break;
                                }

                                try {
                                    targetLocationWxThreeHours = cwbWeatherDataThreeDaysForDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(j).getJSONArray("elementValue").getJSONObject(0).getString("value");
                                    targetLocationWxIconThreeHours = cwbWeatherDataThreeDaysForDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(j).getJSONArray("elementValue").getJSONObject(1).getString("value");
                                    targetLocationPrecipitationThreeHours = cwbWeatherDataThreeDaysForDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(3).getJSONArray("time").getJSONObject(j / 2).getJSONArray("elementValue").getJSONObject(0).getString("value");
                                    targetLocationTemperatureThreeHours = cwbWeatherDataThreeDaysForDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(2).getJSONArray("time").getJSONObject(j).getJSONArray("elementValue").getJSONObject(0).getString("value");
                                    targetLocationApparentTemperatureThreeHours = cwbWeatherDataThreeDaysForDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time").getJSONObject(j).getJSONArray("elementValue").getJSONObject(0).getString("value");
                                    targetLocationStartTimeThreeHours = cwbWeatherDataThreeDaysForDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(j).getString("startTime");
                                    targetLocationEndTimeThreeHours = cwbWeatherDataThreeDaysForDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(j).getString("endTime");

                                    Date currentTime = Calendar.getInstance().getTime();
                                    Date startTimeThreeHours = Calendar.getInstance().getTime();
                                    Date endTimeThreeHours = Calendar.getInstance().getTime();
                                    startTimeThreeHours.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).parse(targetLocationStartTimeThreeHours).getTime());
                                    endTimeThreeHours.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).parse(targetLocationEndTimeThreeHours).getTime());

                                    if ((currentTime.after(startTimeThreeHours) && currentTime.before(endTimeThreeHours)) || (currentTime.before(startTimeThreeHours) && currentTime.before(endTimeThreeHours))) {
                                        HashMap<String, String> targetLocationCWBDataThreeHours = new HashMap<>();

                                        targetLocationCWBDataThreeHours.put("wx", targetLocationWxThreeHours);
                                        targetLocationCWBDataThreeHours.put("wxIcon", targetLocationWxIconThreeHours);
                                        targetLocationCWBDataThreeHours.put("precipitation", targetLocationPrecipitationThreeHours);
                                        targetLocationCWBDataThreeHours.put("T", targetLocationTemperatureThreeHours);
                                        targetLocationCWBDataThreeHours.put("AT", targetLocationApparentTemperatureThreeHours);
                                        targetLocationCWBDataThreeHours.put("time", (startTimeThreeHours.getHours() == 0 && j != 0) ? "明日" : startTimeThreeHours.getHours() + "時");

                                        temperatureThreeHours.add(new Entry(mTargetLocationCWBDataThreeHours.size(), Float.parseFloat(targetLocationTemperatureThreeHours)));

                                        mTargetLocationCWBDataThreeHours.add(targetLocationCWBDataThreeHours);
                                    }
                                } catch (JSONException | ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            // 設定三小時溫度變化折線圖資料
                            LineDataSet lineChartDataSetTemperatureThreeHours = new LineDataSet(temperatureThreeHours, "DataSet Temperature Three Hours");

                            ArrayList<ILineDataSet> lineChartDataSetsTemperatureThreeHours = new ArrayList<>();
                            lineChartDataSetsTemperatureThreeHours.add(lineChartDataSetTemperatureThreeHours); // add the data sets

                            // create a data object with the data sets
                            mLineData = new LineData(lineChartDataSetsTemperatureThreeHours);

                            lineChartDataSetTemperatureThreeHours.setDrawIcons(false);

                            // 灰色的線和點
                            lineChartDataSetTemperatureThreeHours.setColor(Color.GRAY);
                            lineChartDataSetTemperatureThreeHours.setCircleColor(Color.GRAY);

                            // 設定折線圖線的粗細以及點的大小
                            lineChartDataSetTemperatureThreeHours.setLineWidth(2f);
                            lineChartDataSetTemperatureThreeHours.setCircleRadius(4f);

                            // 設定折線圖的點為實心點
                            lineChartDataSetTemperatureThreeHours.setDrawCircleHole(false);

                            lineChartDataSetTemperatureThreeHours.setDrawValues(false);

                            String targetLocationWxIconOneWeek;     // 氣象文字
                            String targetLocationStartTimeOneWeek;  // 起始時間
                            String targetLocationEndTimeOneWeek;    // 結束時間
                            Date previousDate = Calendar.getInstance().getTime();
                            previousDate.setDate(previousDate.getDate() - 1);

                            for (int j = 0; j < 14; j++) {
                                if (mTargetLocationCWBDataOneWeek.size() == 7) {
                                    break;
                                }

                                try {
                                    targetLocationWxIconOneWeek = cwbWeatherDataOneWeekForDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time").getJSONObject(j).getJSONArray("elementValue").getJSONObject(1).getString("value");
                                    targetLocationStartTimeOneWeek = cwbWeatherDataOneWeekForDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time").getJSONObject(j).getString("startTime");
                                    targetLocationEndTimeOneWeek = cwbWeatherDataOneWeekForDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time").getJSONObject(j).getString("endTime");

                                    Date startTimeOneWeek = Calendar.getInstance().getTime();
                                    Date endTimeOneWeek = Calendar.getInstance().getTime();
                                    startTimeOneWeek.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).parse(targetLocationStartTimeOneWeek).getTime());
                                    endTimeOneWeek.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).parse(targetLocationEndTimeOneWeek).getTime());

                                    if (startTimeOneWeek.getDate() != previousDate.getDate()) {
                                        HashMap<String, String> currentLocationCWBDataOneWeek = new HashMap<>();
                                        currentLocationCWBDataOneWeek.put("wxIcon", targetLocationWxIconOneWeek);

                                        previousDate.setTime(startTimeOneWeek.getTime());
                                        switch (mTargetLocationCWBDataOneWeek.size()) {
                                            case 0:
                                                currentLocationCWBDataOneWeek.put("dayOfWeek", "今");
                                                break;
                                            case 1:
                                                currentLocationCWBDataOneWeek.put("dayOfWeek", "明");
                                                break;
                                            default:
                                                currentLocationCWBDataOneWeek.put("dayOfWeek", Util.getDayOfWeekInChinese(startTimeOneWeek.getDay()));
                                                break;
                                        }
                                        currentLocationCWBDataOneWeek.put("dayOfMonth", startTimeOneWeek.getDate() + "日");
                                        mTargetLocationCWBDataOneWeek.add(currentLocationCWBDataOneWeek);
                                    }
                                } catch (JSONException | ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            // 找到傳入縣市行政區的氣象資料就結束 for 迴圈
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        mFragmentWeather = view.findViewById(R.id.fragment_weather);

        mWeatherConditionTextNow = view.findViewById(R.id.tv_current_location_weather_condition_text_now);
        mWeatherConditionIconNow = view.findViewById(R.id.iv_current_location_weather_condition_icon_now);
        mPrecipitationTextNow = view.findViewById(R.id.tv_current_location_precipitation_text_now);
        mTemperatureNow = view.findViewById(R.id.tv_current_location_temperature_now);
        mApparentTemperatureNow = view.findViewById(R.id.tv_current_location_apparent_temperature_now);

        for (int i = 1; i <= 6; i++) {
            ImageView ivWeatherIcon = view.findViewById(getActivity().getResources().getIdentifier("iv_weather_condition_three_days_data_icon_0" + i, "id", getActivity().getPackageName()));
            TextView tvPrecipitation = view.findViewById(getActivity().getResources().getIdentifier("tv_weather_condition_three_days_data_precipitation_0" + i, "id", getActivity().getPackageName()));
            TextView tvTemperature = view.findViewById(getActivity().getResources().getIdentifier("tv_temperature_three_days_data_temperature_0" + i, "id", getActivity().getPackageName()));
            TextView tvTime = view.findViewById(getActivity().getResources().getIdentifier("tv_temperature_three_days_data_time_0" + i, "id", getActivity().getPackageName()));
            mWeatherConditionIconsThreeDays.add(ivWeatherIcon);
            mWeatherConditionPrecipitationThreeDays.add(tvPrecipitation);
            mWeatherConditionTemperatureThreeHrs.add(tvTemperature);
            mWeatherConditionTimeThreeHrs.add(tvTime);
        }

        for (int i = 1; i <= 7; i++) {
            TextView tvDay = view.findViewById(getActivity().getResources().getIdentifier("tv_weather_condition_one_week_data_day_0" + i, "id", getActivity().getPackageName()));
            TextView tvDate = view.findViewById(getActivity().getResources().getIdentifier("tv_weather_condition_one_week_data_date_0" + i, "id", getActivity().getPackageName()));
            ImageView ivWeatherIcon = view.findViewById(getActivity().getResources().getIdentifier("iv_weather_condition_one_week_data_icon_0" + i, "id", getActivity().getPackageName()));
            mWeatherConditionDayOneWeek.add(tvDay);
            mWeatherConditionDateOneWeek.add(tvDate);
            mWeatherConditionIconsOneWeek.add(ivWeatherIcon);
        }

        mLineChartTemperature = view.findViewById(R.id.chart_line_chart_temperature);
        // disable description text
        mLineChartTemperature.getDescription().setEnabled(false);

        // enable touch gestures
        mLineChartTemperature.setTouchEnabled(false);

        mFragmentWeather.setVisibility(View.GONE);

        return view;
    }

    private void clearData() {
        mTargetLocationCWBDataThreeHours.clear();
        mTargetLocationCWBDataOneWeek.clear();
    }

    private void updateWeatherData() {
        if (getActivity() != null && mTargetLocationCWBDataThreeHours.size() >= 6 && mWeatherConditionDayOneWeek.size() >= 7) {
            ConstraintLayout actionBarHome = getActivity().findViewById(R.id.constraint_layout_action_bar_home);

            int id = getActivity().getResources().getIdentifier("ic_" + mTargetLocationCWBDataThreeHours.get(0).get("wxIcon"), "drawable", getActivity().getPackageName());
            mWeatherConditionIconNow.setImageResource(id);
            mWeatherConditionTextNow.setText(mTargetLocationCWBDataThreeHours.get(0).get("wx"));
            mPrecipitationTextNow.setText(new StringBuilder().append(mTargetLocationCWBDataThreeHours.get(0).get("precipitation")).append(" %").toString());
            mTemperatureNow.setText(new StringBuilder().append(mTargetLocationCWBDataThreeHours.get(0).get("T")).append(temperatureMeasureChar).append("C").toString());
            mApparentTemperatureNow.setText(new StringBuilder().append(mTargetLocationCWBDataThreeHours.get(0).get("AT")).append(temperatureMeasureChar).append("C").toString());

            for (int i = 0; i < 6; i++) {
                mWeatherConditionPrecipitationThreeDays.get(i).setText(new StringBuilder().append(mTargetLocationCWBDataThreeHours.get(i).get("precipitation")).append("%").toString());
                id = getActivity().getResources().getIdentifier("ic_" + mTargetLocationCWBDataThreeHours.get(i).get("wxIcon"), "drawable", getActivity().getPackageName());
                mWeatherConditionIconsThreeDays.get(i).setImageResource(id);
                mWeatherConditionTemperatureThreeHrs.get(i).setText(new StringBuilder().append(Objects.requireNonNull(mTargetLocationCWBDataThreeHours.get(i).get("T"))).append(temperatureMeasureChar).toString());

                int temperature = 0;
                int temperatureBgColor;

                try {
                    temperature = Integer.parseInt(mTargetLocationCWBDataThreeHours.get(i).get("T"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                if (temperature >= 35) {
                    temperatureBgColor = getActivity().getResources().getIdentifier("temperature_background_color_35", "color", getActivity().getPackageName());
                } else if (temperature <= 15) {
                    temperatureBgColor = getActivity().getResources().getIdentifier("temperature_background_color_15", "color", getActivity().getPackageName());
                } else {
                    temperatureBgColor = getActivity().getResources().getIdentifier("temperature_background_color_" + mTargetLocationCWBDataThreeHours.get(i).get("T"), "color", getActivity().getPackageName());
                }
                mWeatherConditionTemperatureThreeHrs.get(i).setBackgroundColor(getActivity().getResources().getColor(temperatureBgColor, null));
                mWeatherConditionTimeThreeHrs.get(i).setText(Objects.requireNonNull(mTargetLocationCWBDataThreeHours.get(i).get("time")));
            }

            for (int i = 0; i < 7; i++) {
                mWeatherConditionDayOneWeek.get(i).setText(mTargetLocationCWBDataOneWeek.get(i).get("dayOfWeek"));
                mWeatherConditionDateOneWeek.get(i).setText(mTargetLocationCWBDataOneWeek.get(i).get("dayOfMonth"));
                id = getActivity().getResources().getIdentifier("ic_" + mTargetLocationCWBDataOneWeek.get(i).get("wxIcon"), "drawable", getActivity().getPackageName());
                mWeatherConditionIconsOneWeek.get(i).setImageResource(id);
            }

            mLineChartTemperature.setData(mLineData);
            // get the legend (only possible after setting data)
            Legend l = mLineChartTemperature.getLegend();

            // draw legend entries as lines
            l.setForm(Legend.LegendForm.LINE);
            l.setEnabled(false);

            // 不需要 x 軸的說明以及格線
            XAxis xAxis = mLineChartTemperature.getXAxis();
            xAxis.setDrawLabels(false);
            xAxis.setDrawGridLines(false);

            // 不需要 y 軸的說明以及格線
            YAxis leftAxis = mLineChartTemperature.getAxisLeft();
            leftAxis.setDrawLabels(false);
            leftAxis.setDrawGridLines(false);

            // 不需要 y 軸的說明以及格線
            YAxis rightAxis = mLineChartTemperature.getAxisRight();
            rightAxis.setDrawLabels(false);
            rightAxis.setDrawGridLines(false);

            actionBarHome.setVisibility(View.VISIBLE);
            mFragmentWeather.setVisibility(View.VISIBLE);

            getActivity().getSupportFragmentManager().beginTransaction().hide(mLoadingFragment).commit();
            getActivity().getSupportFragmentManager().beginTransaction().show(this).commit();
        }
    }

    private void initCityAndDistrictSpinners() {
        if (getActivity() != null) {
            Spinner spinnerCity = getActivity().findViewById(R.id.spinner_city);
            Spinner spinnerDistrict = getActivity().findViewById(R.id.spinner_district);

            ArrayList<String> listCities = new ArrayList<>(mTaiwanCitiesDistricts.keySet());

            ArrayAdapter<String> adapterSpinnerCity = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, listCities);
            adapterSpinnerCity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            if (spinnerCity != null && spinnerDistrict != null) {
                spinnerCity.setAdapter(adapterSpinnerCity);
                spinnerCity.setSelection(adapterSpinnerCity.getPosition(mCurrentCity.trim()));

                spinnerCity.setOnTouchListener((v, event) -> {
                    mIsSpinnerCityUserClicked = true;
                    v.performClick();
                    return false;
                });

                spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedCity = parent.getItemAtPosition(position).toString();

                        if (mTaiwanCitiesDistricts.get(selectedCity) != null && getActivity() != null) {

                            ArrayList<String> districtList = new ArrayList<>(mTaiwanCitiesDistricts.get(selectedCity));

                            ArrayAdapter<String> adapterSpinnerDistrict = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, districtList);
                            adapterSpinnerDistrict.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerDistrict.setAdapter(adapterSpinnerDistrict);
                            if (!mIsSpinnerCityUserClicked && !mIsSpinnerDistrictUserClicked) {
                                spinnerDistrict.setSelection(adapterSpinnerDistrict.getPosition(mCurrentDistrict.trim()));
                            }

                            if (!selectedCity.equals(mPreviousSelectedCity) && (mIsSpinnerCityUserClicked || mIsSpinnerDistrictUserClicked)) {
                                ConstraintLayout constraintLayoutFragmentWeather = getActivity().findViewById(R.id.fragment_weather);
                                constraintLayoutFragmentWeather.setVisibility(View.GONE);
                            }

                            mPreviousSelectedCity = selectedCity;
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                spinnerDistrict.setOnTouchListener((v, event) -> {
                    mIsSpinnerDistrictUserClicked = true;
                    v.performClick();
                    return false;
                });

                spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (mIsSpinnerCityUserClicked || mIsSpinnerDistrictUserClicked) {
                            ConstraintLayout actionBarHome = getActivity().findViewById(R.id.constraint_layout_action_bar_home);
                            actionBarHome.setVisibility(View.GONE);
                            getActivity().getSupportFragmentManager().beginTransaction().show(mLoadingFragment).commit();
                            getActivity().getSupportFragmentManager().beginTransaction().hide(WeatherFragment.this).commit();
                            String selectedCity = spinnerCity.getSelectedItem().toString().trim();
                            String selectedDistrict = spinnerDistrict.getSelectedItem().toString().trim();
                            mExecutor.execute(() -> {
                                getCWBWeatherData(selectedCity, selectedDistrict, false);
                                mMainThreadHandler.post(WeatherFragment.this::updateWeatherData);
                            });
                            mIsSpinnerDistrictUserClicked = false;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        }
    }
}

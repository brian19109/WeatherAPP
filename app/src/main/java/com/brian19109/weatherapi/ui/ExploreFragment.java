package com.brian19109.weatherapi.ui;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.brian19109.weatherapi.GetLastLocationCallback;
import com.brian19109.weatherapi.R;
import com.brian19109.weatherapi.controller.CWBDataController;
import com.brian19109.weatherapi.controller.GoogleMapDistanceMatrixController;
import com.brian19109.weatherapi.database.ProjectRepository;
import com.brian19109.weatherapi.database.ProjectViewModel;
import com.brian19109.weatherapi.model.CustomClusterRenderer;
import com.brian19109.weatherapi.model.CustomInfoWindowAdapter;
import com.brian19109.weatherapi.model.DistanceMatrixWeatherData;
import com.brian19109.weatherapi.model.DistanceSplit;
import com.brian19109.weatherapi.model.Project;
import com.brian19109.weatherapi.model.WeatherClusterItem;
import com.brian19109.weatherapi.model.WeatherPlace;
import com.brian19109.weatherapi.util.Constants;
import com.brian19109.weatherapi.util.CustomizedSpinner;
import com.brian19109.weatherapi.util.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExploreFragment extends Fragment implements OnMapReadyCallback, GetLastLocationCallback {
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(1);
    private final String mProjectName;
    private final ArrayList<DistanceMatrixWeatherData> mCWBData = new ArrayList<>();
    private final SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN);
    private SupportMapFragment mSupportMapFragment;
    private View mMapView;
    private GoogleMap mMap;
    private LatLng mCurrentLocation;
    private Project mProject;
    private ClusterManager<WeatherClusterItem> mClusterManager;
    private CustomInfoWindowAdapter mCustomInfoWindowAdapter;
    private boolean mIsSpinnerProjectsUserClicked;

    public ExploreFragment(String projectName) {
        mProjectName = projectName;
        mIsSpinnerProjectsUserClicked = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore_map, container, false);
        ImageView ivSchedule = view.findViewById(R.id.iv_circle_schedule);

        initMap();

        ivSchedule.setImageDrawable(getScheduleIcon());
        ivSchedule.setOnClickListener(v -> {
            if (getActivity() != null) {
                ProjectListFragment projectListFragment = new ProjectListFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, projectListFragment).commit();
                if (getActivity() instanceof AppCompatActivity && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setCustomView(R.layout.action_bar_project_list);
                }
            }
        });

        if (getActivity() instanceof AppCompatActivity && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setCustomView(R.layout.action_bar_explore);
        }

        getProjectInformation();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initSpinnerProjects();
    }

    private LayerDrawable getScheduleIcon() {
        LayerDrawable finalDrawable = null;

        if (getActivity() != null) {
            Drawable circle = ContextCompat.getDrawable(getActivity(), R.drawable.ic_circle);
            Drawable icSchedule = ContextCompat.getDrawable(getActivity(), R.drawable.ic_schedule);

            finalDrawable = new LayerDrawable(new Drawable[]{circle, icSchedule});
            finalDrawable.setLayerInsetTop(0, 0);
            finalDrawable.setLayerGravity(1, Gravity.CENTER);
        }

        return finalDrawable;
    }

    private void initSpinnerProjects() {
        if (getActivity() != null) {
            CustomizedSpinner spinnerProjects = getActivity().findViewById(R.id.spinner_projects);

            new ViewModelProvider(this).get(ProjectViewModel.class).getAllProjects().observe(getViewLifecycleOwner(), projects -> {
                List<String> projectsList = new ArrayList<>();

                if (projects.size() == 0) {
                    projectsList.add("?????????????????????");
                } else {
                    projectsList.add("????????????");
                    for (Project project : projects) {
                        projectsList.add(project.getProjectName());
                    }
                }

                ArrayAdapter<String> adapterSpinnerProjects = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, projectsList);
                adapterSpinnerProjects.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProjects.setAdapter(adapterSpinnerProjects);

                spinnerProjects.setOnTouchListener((v, event) -> {
                    mIsSpinnerProjectsUserClicked = true;
                    v.performClick();
                    return false;
                });

                spinnerProjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (mIsSpinnerProjectsUserClicked && getActivity() != null) {
                            if (parent.getItemAtPosition(position).toString().trim().equals("?????????????????????")) {
                                if (getActivity() instanceof AppCompatActivity && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                                    ((AppCompatActivity) getActivity()).getSupportActionBar().setCustomView(R.layout.action_bar_editing_project);
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, new ProjectEditingFragment()).commit();
                                }
                            } else {
                                ExploreFragment exploreFragment = new ExploreFragment(parent.getItemAtPosition(position).toString().trim());
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, exploreFragment).commit();
                            }
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            });
        }
    }

    // ???????????? Map ?????????????????? onMapReady
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        clearData();
        mCustomInfoWindowAdapter = new CustomInfoWindowAdapter(getActivity());

        // ??????Map UI
        mMap = map;
        mMap.setMyLocationEnabled(true);
        // ??????????????????????????? http://www.tastones.com/zh-tw/stackoverflow/android/google-maps-api-v2-for-android/custom_google_map_styles/
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // ??????google???????????????????????????????????????????????????????????????????????????
        // ????????????????????????????????????????????????????????????????????????????????????
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 30, 150);
    }

    @Override
    public void onGetLastLocationSuccess(Location location) {
        if (location != null && mMap != null) {
            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            // ?????? zoom level ??????????????????
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.latitude, mCurrentLocation.longitude), 17));
        }
    }

    // ????????? Map ??????????????????????????????????????????????????????????????????????????? MapAsync?????? Maps ???????????????????????????????????????
    // ??????MapAsync https://doc.akka.io/docs/akka/current/stream/operators/Source-or-Flow/mapAsync.html
    // onMapReady ?????? Interface????????? implement ????????????
    private void initMap() {
        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_explore_map);
        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(this);
            mMapView = mSupportMapFragment.getView();
            Util.getCurrentLocation(getActivity(), this);
        }
    }

    private void clearData() {
        mCWBData.clear();
        if (mMap != null) {
            mMap.clear();
        }
        if (mClusterManager != null) {
            mClusterManager.clearItems();
        }
    }

    private void getProjectInformation() {
        if (getActivity() != null && mProjectName != null) {
            ProjectRepository projectRepository = new ProjectRepository(getActivity().getApplication());

            try {
                projectRepository.getProjectByName(mProjectName).observe(getViewLifecycleOwner(), project -> {
                    mProject = project;
                    try {
                        projectRepository.getAllPlacesByProjectName(mProjectName).observe(getViewLifecycleOwner(), places -> {
                            mExecutorService.execute(() -> {
                                LocalDateTime departureTime = LocalDateTime.parse(mProject.getDepartureDateTime(), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));

                                // ??????????????? index ??? 1 ?????????????????? place ???????????????
                                for (int placeIndex = 1; placeIndex < places.size(); placeIndex++) {
                                    Map<String, String> cityAndDistrict = Util.getCityAndDistrictByAddress(places.get(placeIndex).getAddress());

                                    if (cityAndDistrict.size() == 2 && cityAndDistrict.containsKey(Constants.CITY) && cityAndDistrict.containsKey(Constants.DISTRICT)) {
                                        String cityName = cityAndDistrict.get(Constants.CITY).replace("???", "???");
                                        String districtName = cityAndDistrict.get(Constants.DISTRICT);
                                        Constants.CWBDataCityID targetCityId = null;
                                        for (Constants.CWBDataCityID cityId : Constants.CWBDataCityID.values()) {
                                            if (cityId.getCityName().equals(cityName)) {
                                                targetCityId = cityId;
                                                break;
                                            }
                                        }

                                        // ?????? Google Map API (Distance Matrix Service) ???????????????????????????
                                        JSONObject jsonObjectDistanceMatrix = new GoogleMapDistanceMatrixController().getGoogleMaDistanceMatrixData(places.get(placeIndex).getTravelMode(), Util.getLatLngByAddress(places.get(placeIndex - 1).getAddress()), Util.getLatLngByAddress(places.get(placeIndex).getAddress()));

                                        // ??? duration ??????????????????????????????????????????????????????????????????
                                        String durationTime = null;

                                        try {
                                            durationTime = jsonObjectDistanceMatrix.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getString("text");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        //??????????????????duration???????????????????????????distance matrix return???JSON?????????????????????????????????????????????????????????????????????????????????ex:1 ??? 3 ??????
                                        //???duration????????????????????????????????????
                                        //1.xx???xx?????? 2.xx??? 3.xx??????xx?????? 4.xx??????
                                        //?????????????????????class????????????????????????????????????????????????????????????
                                        DistanceSplit durationDistanceSplit = new DistanceSplit(durationTime);

                                        String durationDay = durationDistanceSplit.getDay();
                                        String durationHour = durationDistanceSplit.getHour();
                                        String durationMinute = durationDistanceSplit.getMinute();

                                        // ???????????????????????????????????????????????????????????? predictArrivalTime ??? function ???????????????????????????????????????
                                        Date arrivalTimeDate = predictArrivalTime(Integer.parseInt(durationDay), Integer.parseInt(durationHour), Integer.parseInt(durationMinute), places.get(placeIndex - 1).getArrivalTime(), places.get(placeIndex - 1).getStayDuration());
                                        String arrivalTime = arrivalTimeDate == null ? "" : mFormatter.format(arrivalTimeDate);

                                        places.get(placeIndex).setArrivalTime(arrivalTime);

                                        Calendar calendar = Calendar.getInstance(Locale.TAIWAN);
                                        calendar.setTime(Date.from(departureTime.toInstant(ZoneOffset.of("+08:00"))));
                                        calendar.add(Calendar.DAY_OF_MONTH, 7);

                                        boolean isCWBOneWeekData;
                                        // ?????????????????????????????????????????????????????????
                                        JSONObject jsonObjectCWB;
                                        if (!arrivalTimeDate.after(calendar.getTime())) {
                                            jsonObjectCWB = new CWBDataController().getCWBData(Constants.CWBDataInterval.THREE_DAYS, targetCityId);
                                            isCWBOneWeekData = false;
                                        } else {
                                            jsonObjectCWB = new CWBDataController().getCWBData(Constants.CWBDataInterval.ONE_WEEK, targetCityId);
                                            isCWBOneWeekData = true;
                                        }
                                        JSONArray weatherDataForTownshipsDistricts = Util.getWeatherData(jsonObjectCWB);

                                        if (weatherDataForTownshipsDistricts != null) {
                                            for (int i = 0; i < weatherDataForTownshipsDistricts.length(); i++) {
                                                String currentDistrictOfWeatherData = "";
                                                try {
                                                    currentDistrictOfWeatherData = weatherDataForTownshipsDistricts.getJSONObject(i).get("locationName").toString();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                if (currentDistrictOfWeatherData.trim().equals(districtName)) {
                                                    LatLng destination = Util.getLatLngByAddress(places.get(placeIndex).getAddress());
                                                    String originAddress = null;
                                                    String destinationAddress = null;
                                                    String wx = null;
                                                    String wxIconValue = null;
                                                    String poP6h = null;
                                                    String apparentTemperature = null;
                                                    String temperature = null;

                                                    if (!isCWBOneWeekData) {
                                                        try {
                                                            wx = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getString("value");
                                                            wxIconValue = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(1).getString("value");//Wx icon value;
                                                            apparentTemperature = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getString("value");// AT
                                                            temperature = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(2).getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getString("value");// AT
                                                            poP6h = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(3).getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getString("value");//PoP6h
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else { // ???????????????????????????????????????????????????
                                                        try {
                                                            // ????????????
                                                            JSONArray temperatureDataTimeJSONArray = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time");
                                                            for (int timeJSONArrayIndex = 0; timeJSONArrayIndex < temperatureDataTimeJSONArray.length(); timeJSONArrayIndex++) {
                                                                String startTime = temperatureDataTimeJSONArray.getJSONObject(timeJSONArrayIndex).get("startTime").toString();
                                                                String endTime = temperatureDataTimeJSONArray.getJSONObject(timeJSONArrayIndex).get("endTime").toString();

                                                                Date startTimeOneWeek = Calendar.getInstance().getTime();
                                                                Date endTimeOneWeek = Calendar.getInstance().getTime();
                                                                startTimeOneWeek.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).parse(startTime).getTime());
                                                                endTimeOneWeek.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).parse(endTime).getTime());

                                                                if (arrivalTimeDate.after(startTimeOneWeek) && arrivalTimeDate.before(endTimeOneWeek)) {
                                                                    temperature = temperatureDataTimeJSONArray.getJSONObject(timeJSONArrayIndex).getJSONArray("elementValue").getJSONObject(0).getString("value");
                                                                }
                                                            }
                                                            // ??????????????????????????????????????????????????????????????????????????????????????????????????????
                                                            if (temperature == null) {
                                                                temperature = temperatureDataTimeJSONArray.getJSONObject(temperatureDataTimeJSONArray.length() - 1).getJSONArray("elementValue").getJSONObject(0).getString("value");
                                                            }

                                                            JSONArray weatherDataTimeJSONArray = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time");
                                                            // ????????????
                                                            for (int timeJSONArrayIndex = 0; timeJSONArrayIndex < weatherDataTimeJSONArray.length(); timeJSONArrayIndex++) {
                                                                String startTime = weatherDataTimeJSONArray.getJSONObject(timeJSONArrayIndex).get("startTime").toString();
                                                                String endTime = weatherDataTimeJSONArray.getJSONObject(timeJSONArrayIndex).get("endTime").toString();

                                                                Date startTimeOneWeek = Calendar.getInstance().getTime();
                                                                Date endTimeOneWeek = Calendar.getInstance().getTime();
                                                                startTimeOneWeek.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).parse(startTime).getTime());
                                                                endTimeOneWeek.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).parse(endTime).getTime());

                                                                if (arrivalTimeDate.after(startTimeOneWeek) && arrivalTimeDate.before(endTimeOneWeek)) {
                                                                    wx = weatherDataTimeJSONArray.getJSONObject(timeJSONArrayIndex).getJSONArray("elementValue").getJSONObject(0).getString("value");
                                                                    wxIconValue = weatherDataTimeJSONArray.getJSONObject(timeJSONArrayIndex).getJSONArray("elementValue").getJSONObject(1).getString("value");
                                                                }
                                                            }
                                                            // ??????????????????????????????????????????????????????????????????????????????????????????????????????
                                                            if (wx == null) {
                                                                wx = weatherDataTimeJSONArray.getJSONObject(weatherDataTimeJSONArray.length() - 1).getJSONArray("elementValue").getJSONObject(0).getString("value");
                                                                wxIconValue = weatherDataTimeJSONArray.getJSONObject(weatherDataTimeJSONArray.length() - 1).getJSONArray("elementValue").getJSONObject(1).getString("value");
                                                            }
                                                        } catch (JSONException | ParseException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    originAddress = places.get(0).getAddress();
                                                    destinationAddress = places.get(placeIndex).getAddress();

                                                    DistanceMatrixWeatherData currentDistanceMatrixWeatherData = new DistanceMatrixWeatherData();
                                                    currentDistanceMatrixWeatherData.setOrigin(Util.getLatLngByAddress(places.get(0).getAddress()));
                                                    currentDistanceMatrixWeatherData.setDestination(destination);
                                                    currentDistanceMatrixWeatherData.setWx(wx);
                                                    currentDistanceMatrixWeatherData.setWxIconValue(wxIconValue);
                                                    currentDistanceMatrixWeatherData.setApparentTemperature(apparentTemperature);
                                                    currentDistanceMatrixWeatherData.setTemperature(temperature);
                                                    currentDistanceMatrixWeatherData.setPoP6h(poP6h);
                                                    currentDistanceMatrixWeatherData.setOriginAddress(originAddress);
                                                    currentDistanceMatrixWeatherData.setDestinationAddress(destinationAddress);
                                                    currentDistanceMatrixWeatherData.setDurationTime(durationTime);
                                                    currentDistanceMatrixWeatherData.setArrivalTime(arrivalTime);
                                                    currentDistanceMatrixWeatherData.setDepartureTime(departureTime.format(DateTimeFormatter.ofPattern(mFormatter.toPattern())));

                                                    mCWBData.add(currentDistanceMatrixWeatherData);
                                                }
                                            }
                                        }
                                    }
                                }
                                mMainThreadHandler.post(ExploreFragment.this::showMarkers);
                            });
                        });
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Date predictArrivalTime(int incrementDay, int incrementHour, int incrementMinute, String previousPlaceArrivalTime, Duration previousPlaceStayDuration) {
        Calendar arrivalTimeCalendar = null;

        try {
            // ????????????????????????????????????
            // ?????? https://stackoverflow.com/questions/7670355/convert-date-time-for-given-timezone-java
            arrivalTimeCalendar = Calendar.getInstance();
            arrivalTimeCalendar.setTime(new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN).parse(previousPlaceArrivalTime));

            //???????????????Calender Object?????????????????????????????????????????????????????????
            //?????? https://www.geeksforgeeks.org/calendar-settime-method-in-java-with-examples/
            arrivalTimeCalendar.add(Calendar.DATE, incrementDay);
            arrivalTimeCalendar.add(Calendar.HOUR, incrementHour);
            arrivalTimeCalendar.add(Calendar.MINUTE, (int) (incrementMinute + previousPlaceStayDuration.toMinutes()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return arrivalTimeCalendar == null ? null : arrivalTimeCalendar.getTime();
    }

    // ?????? Maps ??? Markers
    private void showMarkers() {
        if (getActivity() != null) {
            mSupportMapFragment.getMapAsync(map -> {
                map.clear();
                // ?????????????????? zoom level ??????????????????????????????
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.975650, 120.973882), 7));

                for (int i = 0; i < mCWBData.size(); i++) {
                    // ?????? icon ??????
                    // ?????? Wx_icon_value ??????????????? icon????????? icon ?????? bitmap ?????????
                    String uri = "@drawable/ic_" + mCWBData.get(i).getWxIconValue();
                    int icon = getResources().getIdentifier(uri, null, getActivity().getPackageName());
                    BitmapDescriptor bitmap = MapFragment.generateBitmapDescriptorFromRes(getActivity(), icon);

                    // marker ?????????????????????
                    String snippet_content = "???????????????" + mCWBData.get(i).getDepartureTime() + "\n" +
                            "???????????????" + mCWBData.get(i).getDurationTime() + "\n" +
                            "???????????????" + mCWBData.get(i).getArrivalTime() + "\n" +
                            mCWBData.get(i).getWx() + "\n" +
                            mCWBData.get(i).getWxIconValue() + "\n" +
                            mCWBData.get(i).getPoP6h() + "%" + "\n" +
                            mCWBData.get(i).getApparentTemperature() + "\n" +
                            mCWBData.get(i).getTemperature();

                    //????????????google maps marker cluster,????????????????????????????????????????????????
                    //?????? https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering?hl=zh-tw

                    //??????????????????marker??????????????????????????????marker???InfoWindow????????????????????????????????????mClusterManager??????
                    double lat = mCWBData.get(i).getDestination().latitude;
                    double lon = mCWBData.get(i).getDestination().longitude;
                    String title = mCWBData.get(i).getDestinationAddress();
                    WeatherClusterItem newWeatherClusterItem = new WeatherClusterItem(lat, lon, title, snippet_content, bitmap);
                    mClusterManager.addItem(newWeatherClusterItem);
                }
            });

            mClusterManager = new ClusterManager<>(getActivity(), mMap);

            // ??????????????????????????????InfoWindow???????????????class?????????GoogleMap.InfoWindowAdapter???????????????
            mClusterManager.getMarkerCollection().setInfoWindowAdapter(mCustomInfoWindowAdapter);
            mMap.setOnCameraIdleListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);

            // ????????????????????????????????? marker ???????????? title,snippet,icon, ?????????????????????????????????,?????? google ??????????????????
            // ??????(*)?????????item?????????????????????????????????????????? icon ????????????????????????????????? marker ??????????????????????????????
            mClusterManager.setRenderer(new CustomClusterRenderer(getActivity(), mMap, mClusterManager));

            getActivity().getSupportFragmentManager().beginTransaction().show(this).commit();
        }
    }
}

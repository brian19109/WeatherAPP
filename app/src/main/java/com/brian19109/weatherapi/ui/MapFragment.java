package com.brian19109.weatherapi.ui;
//主要的程式都在這頁執行
//注意，請先至local properties更改SDK位置

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.brian19109.weatherapi.GetLastLocationCallback;
import com.brian19109.weatherapi.R;
import com.brian19109.weatherapi.controller.CWBDataController;
import com.brian19109.weatherapi.controller.GoogleMapDistanceMatrixController;
import com.brian19109.weatherapi.model.CustomClusterRenderer;
import com.brian19109.weatherapi.model.CustomInfoWindowAdapter;
import com.brian19109.weatherapi.model.DistanceMatrixWeatherData;
import com.brian19109.weatherapi.model.DistanceSplit;
import com.brian19109.weatherapi.model.WeatherClusterItem;
import com.brian19109.weatherapi.util.Constants;
import com.brian19109.weatherapi.util.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.tabs.TabLayout;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment implements OnMapReadyCallback, GetLastLocationCallback {

    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(1);
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final ArrayList<DistanceMatrixWeatherData> mCWBData = new ArrayList<>();
    private final SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN);
    private final Fragment mLoadingFragment;
    private CustomInfoWindowAdapter mCustomInfoWindowAdapter;
    private LatLng mCurrentLocation;
    private SupportMapFragment mSupportMapFragment;
    private View mMapView;
    private GoogleMap mMap;
    private String mCurrentTime;
    private ClusterManager<WeatherClusterItem> mClusterManager;
    private TabLayout mTravelModeTabLayout;
    // 汽車按鈕點擊監聽事件
    private final View.OnClickListener CAR = v -> {
        Util.setAllTabViewsUnClickedTint(mTravelModeTabLayout);
        Util.setTabViewIconClickedTint(v);
        clearData();
        showLoadingFragment();
        new DistanceAndWeatherDataGetter(mExecutorService, mMainThreadHandler).getData(Constants.TravelModes.DRIVING);
    };
    // 摩托車按鈕點擊監聽事件
    private final View.OnClickListener MOTORCYCLE = v -> {
        Util.setAllTabViewsUnClickedTint(mTravelModeTabLayout);
        Util.setTabViewIconClickedTint(v);
        clearData();
        showLoadingFragment();
        new DistanceAndWeatherDataGetter(mExecutorService, mMainThreadHandler).getData(Constants.TravelModes.MOTORCYCLING);
    };
    // 腳踏車按鈕點擊監聽事件
    private final View.OnClickListener BICYCLE = v -> {
        Util.setAllTabViewsUnClickedTint(mTravelModeTabLayout);
        Util.setTabViewIconClickedTint(v);
        clearData();
        showLoadingFragment();
        new DistanceAndWeatherDataGetter(mExecutorService, mMainThreadHandler).getData(Constants.TravelModes.BICYCLING);
    };
    // 步行按鈕點擊監聽事件
    private final View.OnClickListener WALK = v -> {
        Util.setAllTabViewsUnClickedTint(mTravelModeTabLayout);
        Util.setTabViewIconClickedTint(v);
        clearData();
        showLoadingFragment();
        new DistanceAndWeatherDataGetter(mExecutorService, mMainThreadHandler).getData(Constants.TravelModes.WALKING);
    };

    public MapFragment() {
        mLoadingFragment = new LoadingFragment();
    }

    //圖標處理
    //icon的處理，把Drawable convert to Bitmap,因marker的icon要求bitmap
    //參考 https://stackoverflow.com/questions/3035692/how-to-convert-a-drawable-to-a-bitmap
    public static BitmapDescriptor generateBitmapDescriptorFromRes(Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(
                0,
                0,
                70,
                70);
        Bitmap bitmap = Bitmap.createBitmap(
                70,
                70,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void showLoadingFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction().show(MapFragment.this.mLoadingFragment).commit();
            getActivity().getSupportFragmentManager().beginTransaction().hide(MapFragment.this).commit();
        }
    }

    private void clearData() {
        disableTravelModeTabs();
        mCWBData.clear();
        if (mMap != null) {
            mMap.clear();
        }
        if (mClusterManager != null) {
            mClusterManager.clearItems();
        }
    }

    private void disableTravelModeTabs() {
        // 先讓所有 travel mode 的 tab 可以點選
        enableTravelModeTabs();

        if (getActivity() != null) {
            TabLayout tabLayout = getActivity().findViewById(R.id.travelModeTabLayout);
            if (tabLayout != null) {
                ArrayList<View> clickableTabs = new ArrayList<>(tabLayout.getTouchables());

                for (View tab : clickableTabs) {
                    tab.setClickable(false);
                }
            }
        }
    }

    private void enableTravelModeTabs() {
        if (getActivity() != null) {
            TabLayout tabLayout = getActivity().findViewById(R.id.travelModeTabLayout);
            if (tabLayout != null) {
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    Objects.requireNonNull(tabLayout.getTabAt(i)).view.setClickable(true);
                }
            }
        }
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mTravelModeTabLayout = view.findViewById(R.id.travelModeTabLayout);

        initTravelModeTabLayout(mTravelModeTabLayout);
        initMap();

        return view;
    }

    // 主要做內容清除的動作，把mCWBData、Map的圖釘、marker cluster清空，Marker cluster後面提到，用途就是根據
    // Map的Zoom level自動把圖釘變成叢集
    // https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering?hl=zh-tw
    // 關於標記叢集 https://franksios.medium.com/swift-google-map-marker-clustering-%E6%A8%99%E8%A8%98%E5%8F%A2%E9%9B%86-a2402f9dcfb3

    private void initTravelModeTabLayout(TabLayout travelModeTabLayout) {
        travelModeTabLayout.setTabMode(TabLayout.MODE_FIXED);
        travelModeTabLayout.addTab(travelModeTabLayout.newTab().setIcon(R.drawable.ic_travel_mode_car));
        travelModeTabLayout.addTab(travelModeTabLayout.newTab().setIcon(R.drawable.ic_travel_mode_scooter));
        travelModeTabLayout.addTab(travelModeTabLayout.newTab().setIcon(R.drawable.ic_travel_mode_bicycle));
        travelModeTabLayout.addTab(travelModeTabLayout.newTab().setIcon(R.drawable.ic_travel_mode_walk));

        // 設定 tab 的點擊事件
        Objects.requireNonNull(travelModeTabLayout.getTabAt(0)).view.setOnClickListener(CAR);
        Objects.requireNonNull(travelModeTabLayout.getTabAt(1)).view.setOnClickListener(MOTORCYCLE);
        Objects.requireNonNull(travelModeTabLayout.getTabAt(2)).view.setOnClickListener(BICYCLE);
        Objects.requireNonNull(travelModeTabLayout.getTabAt(3)).view.setOnClickListener(WALK);

        // 設定 tab 的樣式
        travelModeTabLayout.setBackgroundColor(getResources().getColor(R.color.white, requireContext().getTheme()));

        Util.setAllTabViewsUnClickedTint(mTravelModeTabLayout);
        Objects.requireNonNull(travelModeTabLayout.getTabAt(0)).view.performClick();
    }

    // 初始化 Map 和相關按鈕事件，告知要放入哪個容器，並且一定要時做 MapAsync，因 Maps 是網路資料，要使用異步處理
    // 關於MapAsync https://doc.akka.io/docs/akka/current/stream/operators/Source-or-Flow/mapAsync.html
    // onMapReady 是個 Interface，需要 implement 進入實做
    private void initMap() {
        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(this);
            mMapView = mSupportMapFragment.getView();
            Util.getCurrentLocation(getActivity(), this);
        }
    }

    // 初始打開Map會執行底下的onMapReady
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        mCustomInfoWindowAdapter = new CustomInfoWindowAdapter(getActivity());

        // 設定Map UI
        mMap = map;
        mMap.setMyLocationEnabled(true);
        // 關於自定義地圖樣式 http://www.tastones.com/zh-tw/stackoverflow/android/google-maps-api-v2-for-android/custom_google_map_styles/
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // 本身google無提供變更方法，上網請教後得知只能使用底下方法移動
        // 此處為設定定位按鈕的位置，原本預設在右上角，移動至右下角
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 30, 150);

        // fusedLocation用來取得當下位置，並把經緯度存放起來在mMyLocation以便後續使用
        // 參考 https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient
        // https://stackoverflow.com/questions/49649432/fusedlocationclient-vs-locationmanager-in-initial-location
    }

    // 新增 Maps 的 Markers
    private void showMarkers() {
        if (getActivity() != null) {
            mSupportMapFragment.getMapAsync(map -> {
                map.clear();
                // 每次呼叫都把 zoom level 調整，方便俯視全台灣
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.975650, 120.973882), 7));

                for (int i = 0; i < mCWBData.size(); i++) {
                    // 圖標 icon 設定
                    // 根據 Wx_icon_value 取得對應的 icon，並把 icon 放入 bitmap 此變數
                    String uri = "@drawable/ic_" + mCWBData.get(i).getWxIconValue();
                    int icon = getResources().getIdentifier(uri, null, getActivity().getPackageName());
                    BitmapDescriptor bitmap = generateBitmapDescriptorFromRes(getActivity(), icon);

                    // marker 資訊視窗的內容
                    String snippet_content = "現在時間：" + mCWBData.get(i).getDepartureTime() + "\n" +
                            "所需時間：" + mCWBData.get(i).getDurationTime() + "\n" +
                            "預計扺達：" + mCWBData.get(i).getArrivalTime() + "\n" +
                            mCWBData.get(i).getWx() + "\n" +
                            mCWBData.get(i).getWxIconValue() + "\n" +
                            mCWBData.get(i).getPoP6h() + "%" + "\n" +
                            mCWBData.get(i).getApparentTemperature() + "\n" +
                            mCWBData.get(i).getTemperature();

                    //此處使用google maps marker cluster,會根據經緯度自動歸類為同一個叢集
                    //參考 https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering?hl=zh-tw

                    //而此時不管是marker的設定或點擊事件還是marker的InfoWindow的設定或點擊事件都會交由mClusterManager控管
                    double lat = mCWBData.get(i).getDestination().latitude;
                    double lon = mCWBData.get(i).getDestination().longitude;
                    String title = mCWBData.get(i).getDestinationAddress();
                    WeatherClusterItem newWeatherClusterItem = new WeatherClusterItem(lat, lon, title, snippet_content, bitmap);
                    mClusterManager.addItem(newWeatherClusterItem);
                }
            });

            mClusterManager = new ClusterManager<>(getActivity(), mMap);

            // 此處帶入自行客製化的InfoWindow，需寫一個class並實做GoogleMap.InfoWindowAdapter介面來更改
            mClusterManager.getMarkerCollection().setInfoWindowAdapter(mCustomInfoWindowAdapter);
            mMap.setOnCameraIdleListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);

            // 渲染器，這裡是用來設定 marker 要填入的 title,snippet,icon, 同樣需要實做介面來修改,這邊 google 做的蠻麻煩的
            // 前面(*)此處的item都已經有先放入經緯度、標題和 icon 了，所以我們只要對每個 marker 把這些資訊放進來就好
            mClusterManager.setRenderer(new CustomClusterRenderer(getActivity(), mMap, mClusterManager));

            enableTravelModeTabs();

            getActivity().getSupportFragmentManager().beginTransaction().hide(mLoadingFragment).commit();
            getActivity().getSupportFragmentManager().beginTransaction().show(this).commit();
        }
    }

    private Date predictArrivalTime(int incrementDay, int incrementHour, int incrementMinute) {
        Calendar arrivalTimeCalendar = null;
        mCurrentTime = mFormatter.format(Calendar.getInstance().getTime());

        try {
            // 先制定好要表達的時間格式
            // 參考 https://stackoverflow.com/questions/7670355/convert-date-time-for-given-timezone-java
            arrivalTimeCalendar = Calendar.getInstance();
            arrivalTimeCalendar.setTime(Objects.requireNonNull(mFormatter.parse(mCurrentTime)));

            //這邊再利用Calender Object來直接增加天、小時、分鐘，更為方便快速
            //參考 https://www.geeksforgeeks.org/calendar-settime-method-in-java-with-examples/
            arrivalTimeCalendar.add(Calendar.DATE, incrementDay);
            arrivalTimeCalendar.add(Calendar.HOUR, incrementHour);
            arrivalTimeCalendar.add(Calendar.MINUTE, incrementMinute);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return arrivalTimeCalendar == null ? null : arrivalTimeCalendar.getTime();
    }

    @Override
    public void onGetLastLocationSuccess(Location location) {
        if (location != null) {
            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        }
    }

    public class DistanceAndWeatherDataGetter {
        private final Executor mExecutor;
        private final Handler mResultHandler;

        public DistanceAndWeatherDataGetter(Executor executor, Handler resultHandler) {
            this.mExecutor = executor;
            this.mResultHandler = resultHandler;
        }

        public void getData(Constants.TravelModes travelMode) {
            mExecutor.execute(() -> {
                // 此處是需要做的事情，也就是子線程做的事情，要注意這邊做的事情當中
                // 不能有對UI做變動的事情，因為UI的改變是主線程在做的事情
                for (Constants.CWBDataCityID countryID : Constants.CWBDataCityID.values()) {
                    // 拿全臺灣鄉鎮市區的氣象資料
                    JSONObject jsonObjectCWB = new CWBDataController().getCWBData(Constants.CWBDataInterval.THREE_DAYS, countryID);
                    JSONArray weatherDataForTownshipsDistricts = Util.getWeatherData(jsonObjectCWB);

                    if (weatherDataForTownshipsDistricts != null) {
                        for (int i = 0; i < 2; i++) {
                            LatLng destination = null;
                            try {
                                destination = new LatLng(Double.parseDouble(weatherDataForTownshipsDistricts.getJSONObject(i).get("lat").toString()), Double.parseDouble(weatherDataForTownshipsDistricts.getJSONObject(i).get("lon").toString()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            String originAddress = null;
                            String destinationAddress = null;
                            String durationTime = null;
                            String arrivalTime = null;
                            String wx = null;
                            String wxIconValue = null;
                            String poP6h = null;
                            String apparentTemperature = null;
                            String temperature = null;

                            // 利用 Google Map API (Distance Matrix Service) 取得旅程時間及距離
                            JSONObject jsonObjectDistanceMatrix = new GoogleMapDistanceMatrixController().getGoogleMaDistanceMatrixData(travelMode, mCurrentLocation, destination);

                            if (jsonObjectDistanceMatrix != null) {
                                try {
                                    wx = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getString("value");
                                    wxIconValue = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(1).getString("value");//Wx icon value;
                                    apparentTemperature = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getString("value");// AT
                                    temperature = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(2).getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getString("value");// AT
                                    poP6h = weatherDataForTownshipsDistricts.getJSONObject(i).getJSONArray("weatherElement").getJSONObject(3).getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getString("value");//PoP6h

                                    originAddress = jsonObjectDistanceMatrix.getJSONArray("origin_addresses").getString(0);
                                    destinationAddress = Util.getWeatherDataCityName(jsonObjectCWB) + weatherDataForTownshipsDistricts.getJSONObject(i).get("locationName");
                                    // 此 duration 就是得知目前位置和傳送過來的經緯度的車程時間
                                    durationTime = jsonObjectDistanceMatrix.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getString("text");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                //而這邊一樣把duration做解析的動作，因為distance matrix return的JSON內容當中，時間格式並非是時間格式，單純以文字說明而已，ex:1 天 3 小時
                                //而duration只會出現以下幾種文字說明
                                //1.xx天xx小時 2.xx天 3.xx小時xx分鐘 4.xx分鐘
                                //因此另外寫一個class去解析他以取得天、小時、分鐘的數字是多少
                                DistanceSplit durationDistanceSplit = new DistanceSplit(durationTime);

                                String durationDay = durationDistanceSplit.getDay();
                                String durationHour = durationDistanceSplit.getHour();
                                String durationMinute = durationDistanceSplit.getMinute();

                                // 都得知到目的地要花幾天幾小時幾分鐘後，呼叫 predictArrivalTime 此 function 來計算實際抵達的時間為何時
                                Date arrivalTimeDate = predictArrivalTime(Integer.parseInt(durationDay), Integer.parseInt(durationHour), Integer.parseInt(durationMinute));
                                arrivalTime = arrivalTimeDate == null ? "" : mFormatter.format(arrivalTimeDate);

                                DistanceMatrixWeatherData currentDistanceMatrixWeatherData = new DistanceMatrixWeatherData();
                                currentDistanceMatrixWeatherData.setOrigin(mCurrentLocation);
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
                                currentDistanceMatrixWeatherData.setDepartureTime(mCurrentTime);

                                mCWBData.add(currentDistanceMatrixWeatherData);
                            }
                        }
                    }
                }

                // 這邊的事件就是主線程要做的事,待子線程的事情完成後就會馬上執行這邊的事
                mResultHandler.post(MapFragment.this::showMarkers);
            });
        }
    }
}
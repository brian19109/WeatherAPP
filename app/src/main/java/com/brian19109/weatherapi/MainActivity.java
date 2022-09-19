package com.brian19109.weatherapi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.brian19109.weatherapi.controller.NLSCMapsController;
import com.brian19109.weatherapi.ui.ExploreFragment;
import com.brian19109.weatherapi.ui.MapFragment;
import com.brian19109.weatherapi.ui.WeatherFragment;
import com.brian19109.weatherapi.util.Constants;
import com.brian19109.weatherapi.util.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Document;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements GetLastLocationCallback {

    private final String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
    private final int accessLocationRequestCode = 101;
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(1);
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private MapFragment mMapFragment;
    private WeatherFragment mWeatherFragment;
    private LatLng mCurrentLocation;
    private String mCurrentCity;
    private String mCurrentDistrict;
    private TabLayout mMainTabLayout;
    // 地圖按鈕點擊監聽事件
    private final View.OnClickListener MAP = v -> {
        Util.setAllTabViewsUnClickedTint(mMainTabLayout);
        Util.setTabViewIconClickedTint(v);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setCustomView(R.layout.action_bar_map);
        }
        loadMapFragment();

        if (mMapFragment != null) {
            mMapFragment.new DistanceAndWeatherDataGetter(mExecutorService, mMainThreadHandler).getData(Constants.TravelModes.DRIVING);
        }
    };

    // 首頁按鈕點擊監聽事件
    private final View.OnClickListener HOME = v -> {
        Util.setAllTabViewsUnClickedTint(mMainTabLayout);
        Util.setTabViewIconClickedTint(v);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setCustomView(R.layout.action_bar_home);
        }

        Util.getCurrentLocation(this, this);
    };

    // 探索按鈕點擊監聽事件
    private final View.OnClickListener EXPLORE = v -> {
        Util.setAllTabViewsUnClickedTint(mMainTabLayout);
        Util.setTabViewIconClickedTint(v);
        Objects.requireNonNull(getSupportActionBar()).setCustomView(R.layout.action_bar_explore);

        loadExploreFragment();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initActionBar();
        initTabLayout();

        TabLayout mainTabLayout = findViewById(R.id.menuTabLayout);
        if (hasAccessLocationPermission() && mainTabLayout.getTabAt(1) != null) {
            // 權限取得成功，載入 WeatherFragment
            Objects.requireNonNull(mainTabLayout.getTabAt(1)).view.performClick();
        }
    }

    private void initActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
    }

    private void initTabLayout() {
        mMainTabLayout = findViewById(R.id.menuTabLayout);

        mMainTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mMainTabLayout.addTab(mMainTabLayout.newTab().setIcon(R.drawable.ic_tab_maps));
        mMainTabLayout.addTab(mMainTabLayout.newTab().setIcon(R.drawable.ic_tab_home));
        mMainTabLayout.addTab(mMainTabLayout.newTab().setIcon(R.drawable.ic_tab_explore));

        mMainTabLayout.setBackgroundColor(getResources().getColor(R.color.blue_fluorescent, getApplicationContext().getTheme()));

        for (int i = 0; i < mMainTabLayout.getTabCount(); i++) {
            if (mMainTabLayout.getTabAt(i) != null) {
                Util.setTabViewIconUnClickedTint(mMainTabLayout.getTabAt(i).view);
                switch (i) {
                    case 0:
                        mMainTabLayout.getTabAt(0).view.setOnClickListener(MAP);
                        break;
                    case 1:
                        mMainTabLayout.getTabAt(1).view.setOnClickListener(HOME);
                        break;
                    case 2:
                        mMainTabLayout.getTabAt(2).view.setOnClickListener(EXPLORE);
                        break;
                    default:
                        mMainTabLayout.getTabAt(1).view.setOnClickListener(HOME);
                        break;
                }
            }
        }
    }

    // 確認是否已經取得定位權限
    private boolean hasAccessLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permission[0]) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission[0]}, accessLocationRequestCode);
            return false;
        } else {
            return true;
        }
    }

    // 載入 MapFragment
    private void loadMapFragment() {
        mMapFragment = mMapFragment == null ? new MapFragment() : mMapFragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, mMapFragment).commit();
    }

    // 載入 WeatherFragment
    private void loadWeatherFragment() {
        mWeatherFragment = new WeatherFragment(mCurrentCity, mCurrentDistrict);

        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, mWeatherFragment).commit();
    }

    private void loadExploreFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout,  new ExploreFragment(null)).commit();
    }

    // 每次的ActivityCompat.requestPermission會觸發此Method監聽return的結果，再依結果執行相對應事件
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == accessLocationRequestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 權限取得成功，載入MapFragment
                loadWeatherFragment();
            } else {
                // 權限取得失敗，告知User
                new AlertDialog.Builder(this)
                        .setTitle("權限請求失敗")
                        .setMessage("因無法取得相關定位權限，請稍後再試")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                                finish(); // 結束 app
                            }
                        })
                        .show();

                // 結束 app
                finish();
            }
        }
    }

    @Override
    public void onGetLastLocationSuccess(Location location) {
        if (location != null) {
            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            mExecutorService.execute(() -> {
                Document cityAndDistrictByLatLng = new NLSCMapsController().getCityAndDistrictByLatLng(mCurrentLocation);
                if (cityAndDistrictByLatLng != null) {
                    mCurrentCity = cityAndDistrictByLatLng.getDocumentElement().getElementsByTagName("ctyName").item(0).getTextContent();
                    mCurrentDistrict = cityAndDistrictByLatLng.getDocumentElement().getElementsByTagName("townName").item(0).getTextContent();
                }

                mMainThreadHandler.post(MainActivity.this::loadWeatherFragment);
            });
        }
    }
}
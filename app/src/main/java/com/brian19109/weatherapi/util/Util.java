package com.brian19109.weatherapi.util;

import static android.R.layout.simple_spinner_dropdown_item;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;

import com.brian19109.weatherapi.GetLastLocationCallback;
import com.brian19109.weatherapi.R;
import com.brian19109.weatherapi.ui.DestinationItemFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.Collections2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class Util {
    public static JSONArray getWeatherData(JSONObject cwbData) {
        JSONArray weatherDataForTownshipsDistricts = null;

        if (cwbData != null && cwbData.has("records")) {
            try {
                if (cwbData.getJSONObject("records").has("locations")) {
                    if (cwbData.getJSONObject("records").getJSONArray("locations").length() > 0) {
                        if (cwbData.getJSONObject("records").getJSONArray("locations").getJSONObject(0).has("location")) {
                            weatherDataForTownshipsDistricts = cwbData.getJSONObject("records").getJSONArray("locations").getJSONObject(0).getJSONArray("location");
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return weatherDataForTownshipsDistricts;
    }

    public static String getWeatherDataCityName(JSONObject cwbData) {
        String cityName = null;

        if (cwbData != null && cwbData.has("records")) {
            try {
                if (cwbData.getJSONObject("records").has("locations")) {
                    if (cwbData.getJSONObject("records").getJSONArray("locations").length() > 0) {
                        if (cwbData.getJSONObject("records").getJSONArray("locations").getJSONObject(0).has("locationsName")) {
                            cityName = cwbData.getJSONObject("records").getJSONArray("locations").getJSONObject(0).getString("locationsName");
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return cityName;
    }

    public static void getCurrentLocation(Activity activity, GetLastLocationCallback callback) {
        final int requestLocationCode = 101;

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requestLocationCode);
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, location -> {
                    if (location != null) {
                        callback.onGetLastLocationSuccess(location);
                    }
                })
                .addOnCanceledListener(() -> Toast.makeText(activity, "Failed to get current last location.", Toast.LENGTH_LONG).show());
    }

    public static void setAllTabViewsUnClickedTint(TabLayout tabLayout) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            if (tabLayout.getTabAt(i) != null) {
                setTabViewIconUnClickedTint(tabLayout.getTabAt(i).view);
            }
        }
    }

    public static void setTabViewIconClickedTint(View view) {
        if (view instanceof TabLayout.TabView && ((TabLayout.TabView) view).getTab() != null && ((TabLayout.TabView) view).getTab().getIcon() != null) {
            ((TabLayout.TabView) view).getTab().getIcon().setTint(view.getResources().getColor(R.color.medium_turquoise, null));
        }
    }

    public static void setTabViewIconUnClickedTint(View view) {
        if (view instanceof TabLayout.TabView && ((TabLayout.TabView) view).getTab() != null && ((TabLayout.TabView) view).getTab().getIcon() != null) {
            ((TabLayout.TabView) view).getTab().getIcon().setTint(view.getResources().getColor(R.color.carolina_blue, null));
        }
    }

    public static String formatNumberWithLeadingZeros(int number, int digits) {
        return String.format("%0" + digits + "d", number);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        // To get the correct window token, lets first get the currently focused view
        View view = activity.getCurrentFocus();

        // To get the window token when there is no currently focused view, we have a to create a view
        if (view == null) {
            view = new View(activity);
        }

        // 隱藏 soft keyboard
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getDayOfWeekInChinese(int dayOfTheWeek) {
        switch (dayOfTheWeek) {
            case 0:
                return "日";
            case 1:
                return "一";
            case 2:
                return "二";
            case 3:
                return "三";
            case 4:
                return "四";
            case 5:
                return "五";
            case 6:
                return "六";
            default:
                return "";
        }
    }

    public static String getPlaceAddress(Place place) {
        AddressComponents components = place.getAddressComponents();
        StringBuilder address = new StringBuilder();
        StringBuilder postcode = new StringBuilder();
        boolean placeNameNeeded = true;
        boolean isTaiwanPlace = true;

        if (place.getTypes() != null) {
            for (Place.Type type : place.getTypes()) {
                switch (type) {
                    case ADMINISTRATIVE_AREA_LEVEL_1:
                    case ADMINISTRATIVE_AREA_LEVEL_2:
                    case ADMINISTRATIVE_AREA_LEVEL_3:
                    case ADMINISTRATIVE_AREA_LEVEL_4:
                    case ADMINISTRATIVE_AREA_LEVEL_5:
                    case CONTINENT:
                    case COUNTRY:
                    case FLOOR:
                    case LOCALITY:
                    case POLITICAL:
                    case ROUTE:
                    case STREET_ADDRESS:
                    case STREET_NUMBER:
                        placeNameNeeded = false;
                        break;
                    default:
                        break;
                }
            }
        }

        // Get each component of the address from the place details,
        // and then fill-in the corresponding field on the form.
        // Possible AddressComponent types are documented at https://goo.gle/32SJPM1
        if (components != null) {
            for (AddressComponent component : components.asList()) {
                for (String type : component.getTypes()) {
                    switch (type) {
                        case "postal_code":
                            postcode.insert(0, component.getName());
                            break;
                        case "postal_code_suffix":
                            postcode.append("-").append(component.getName());
                            break;
                        case "country":
                            isTaiwanPlace = component.getName().equals("台灣");
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        if (place.getAddress() != null) {
            if (isTaiwanPlace) {
                String addressWithoutCountryName = place.getAddress().replace("台灣", "");
                address.append(addressWithoutCountryName.substring(addressWithoutCountryName.indexOf(postcode.toString()) + postcode.length()));
            } else {
                address.append(place.getAddress());
            }
        }

        if (place.getName() != null && placeNameNeeded) {
            address.append(" (").append(place.getName()).append(")");
        }

        return address.toString();
    }

    public static String getAddressByLatLng(LatLng targetLatLng) {
        String address = null;
        String URL = Constants.GOOGLE_GEOCODING_HOST + "?latlng=" + targetLatLng.latitude + "," + targetLatLng.longitude + "&language=zh-TW&key=" + Constants.GOOGLE_GEOCODING_API_AUTH_KEY;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();

        // 對此URL做request
        Request request = new Request.Builder()
                .url(URL)
                .method("GET", null)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                JSONObject jsonObjectGeocodingResult = new JSONObject(response.body().string());
                address = jsonObjectGeocodingResult.getJSONArray("results").getJSONObject(0).getString("formatted_address");
                // 回傳的地址格式為：zipcode台灣xxx，回傳去除 zip code & 台灣的剩餘地址字串
                address = address.substring(address.indexOf("台灣") + 2);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return address;
    }

    public static LatLng getLatLngByAddress(String address) {
        LatLng latLng = null;
        String URL = Constants.GOOGLE_GEOCODING_HOST + "?address=" + address + "&language=zh-TW&key=" + Constants.GOOGLE_GEOCODING_API_AUTH_KEY;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();

        // 對此URL做request
        Request request = new Request.Builder()
                .url(URL)
                .method("GET", null)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                JSONObject jsonObjectGeocodingResult = new JSONObject(response.body().string());
                double lat = Double.valueOf(jsonObjectGeocodingResult.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").get("lat").toString());
                double lng = Double.valueOf(jsonObjectGeocodingResult.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").get("lng").toString());
                latLng = new LatLng(lat, lng);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return latLng;
    }

    public static Map<String, String> getCityAndDistrictByAddress(String address) {
        Map<String, String> cityAndDistrict = new HashMap<>();
        LatLng targetLatLng = getLatLngByAddress(address);
        String URL = Constants.GOOGLE_GEOCODING_HOST + "?latlng=" + targetLatLng.latitude + "," + targetLatLng.longitude + "&language=zh-TW&key=" + Constants.GOOGLE_GEOCODING_API_AUTH_KEY;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();

        // 對此URL做request
        Request request = new Request.Builder()
                .url(URL)
                .method("GET", null)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                JSONObject jsonObjectGeocodingResult = new JSONObject(response.body().string());
                // compoundCode 例子：Q45X+PMF 台灣台東縣台東市
                String compoundCode = jsonObjectGeocodingResult.getJSONObject("plus_code").get("compound_code").toString();
                String city = compoundCode.substring(compoundCode.indexOf("台灣") + 2, compoundCode.indexOf("台灣") + 5);
                String district = compoundCode.substring(compoundCode.indexOf("台灣") + 5);
                cityAndDistrict.put(Constants.CITY, city);
                cityAndDistrict.put(Constants.DISTRICT, district);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return cityAndDistrict;
    }

    public static void initDestinationItem(ConstraintLayout constraintLayoutDestinationItem, Context context, ActivityResultLauncher<Intent> activityResultLauncher) {
        ConstraintLayout constraintLayoutStayDurationEditing = constraintLayoutDestinationItem.findViewById(R.id.constraintLayout_stay_duration_editing);
        ConstraintLayout constraintLayoutTravelModeOptions = constraintLayoutDestinationItem.findViewById(R.id.constraintLayout_travel_mode_options);
        ImageView ivTravelMode = constraintLayoutDestinationItem.findViewById(R.id.iv_travel_mode);
        ImageView ivTravelModeCar = constraintLayoutTravelModeOptions.findViewById(R.id.iv_travel_mode_car);
        ImageView ivTravelModeScooter = constraintLayoutTravelModeOptions.findViewById(R.id.iv_travel_mode_scooter);
        ImageView ivTravelModeBicycle = constraintLayoutTravelModeOptions.findViewById(R.id.iv_travel_mode_bicycle);
        ImageView ivTravelModeWalk = constraintLayoutTravelModeOptions.findViewById(R.id.iv_travel_mode_walk);
        Spinner spinnerHours = constraintLayoutDestinationItem.findViewById(R.id.spinner_stay_duration_hours);
        Spinner spinnerMinutes = constraintLayoutDestinationItem.findViewById(R.id.spinner_stay_duration_minutes);
        TextView tvDestinationAddress = constraintLayoutDestinationItem.findViewById(R.id.tv_destination_address);
        TextView tvStayDuration = constraintLayoutDestinationItem.findViewById(R.id.tv_stay_duration);
        TextView tvCancel = constraintLayoutDestinationItem.findViewById(R.id.tv_stay_duration_editing_cancel);
        TextView tvConfirm = constraintLayoutDestinationItem.findViewById(R.id.tv_stay_duration_editing_confirm);

        ArrayList<String> listHours = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            listHours.add(formatNumberWithLeadingZeros(i, 2));
        }

        ArrayAdapter<String> adapterSpinnerHours = new ArrayAdapter<>(constraintLayoutDestinationItem.getContext(), android.R.layout.simple_spinner_item, listHours);
        adapterSpinnerHours.setDropDownViewResource(simple_spinner_dropdown_item);
        spinnerHours.setAdapter(adapterSpinnerHours);

        ArrayList<String> listMinutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            listMinutes.add(formatNumberWithLeadingZeros(i, 2));
        }

        ArrayAdapter<String> adapterSpinnerMinutes = new ArrayAdapter<>(constraintLayoutDestinationItem.getContext(), android.R.layout.simple_spinner_item, listMinutes);
        adapterSpinnerMinutes.setDropDownViewResource(simple_spinner_dropdown_item);
        spinnerMinutes.setAdapter(adapterSpinnerMinutes);

        spinnerHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView)adapterView.getChildAt(0)).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerMinutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView)adapterView.getChildAt(0)).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ivTravelMode.setTag(Constants.TravelModes.DRIVING);
        ivTravelMode.setOnClickListener(view -> {
            constraintLayoutTravelModeOptions.setVisibility(View.VISIBLE);
            ivTravelMode.setColorFilter(ContextCompat.getColor(context.getApplicationContext(), R.color.maya_blue), PorterDuff.Mode.SRC_IN);
        });

        ivTravelModeCar.setOnClickListener(view -> {
            ivTravelMode.setImageResource(R.drawable.ic_travel_mode_car);
            ivTravelMode.setTag(Constants.TravelModes.DRIVING);
            ivTravelMode.setColorFilter(ContextCompat.getColor(context.getApplicationContext(), R.color.carolina_blue), PorterDuff.Mode.SRC_IN);
            constraintLayoutTravelModeOptions.setVisibility(View.GONE);
        });

        ivTravelModeScooter.setOnClickListener(view -> {
            ivTravelMode.setImageResource(R.drawable.ic_travel_mode_scooter);
            ivTravelMode.setTag(Constants.TravelModes.MOTORCYCLING);
            ivTravelMode.setColorFilter(ContextCompat.getColor(context.getApplicationContext(), R.color.carolina_blue), PorterDuff.Mode.SRC_IN);
            constraintLayoutTravelModeOptions.setVisibility(View.GONE);
        });

        ivTravelModeBicycle.setOnClickListener(view -> {
            ivTravelMode.setImageResource(R.drawable.ic_travel_mode_bicycle);
            ivTravelMode.setTag(Constants.TravelModes.BICYCLING);
            ivTravelMode.setColorFilter(ContextCompat.getColor(context.getApplicationContext(), R.color.carolina_blue), PorterDuff.Mode.SRC_IN);
            constraintLayoutTravelModeOptions.setVisibility(View.GONE);
        });

        ivTravelModeWalk.setOnClickListener(view -> {
            ivTravelMode.setImageResource(R.drawable.ic_travel_mode_walk);
            ivTravelMode.setTag(Constants.TravelModes.WALKING);
            ivTravelMode.setColorFilter(ContextCompat.getColor(context.getApplicationContext(), R.color.carolina_blue), PorterDuff.Mode.SRC_IN);
            constraintLayoutTravelModeOptions.setVisibility(View.GONE);
        });

        tvDestinationAddress.setOnClickListener(view -> {
            // Set the fields to specify which types of place data to return after the user has made a selection.
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS, Place.Field.TYPES);

            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, fields)
                    .setInitialQuery(tvDestinationAddress.getText().toString())
                    .build(context.getApplicationContext());

            activityResultLauncher.launch(intent);
        });

        tvStayDuration.setOnClickListener(view -> {
            constraintLayoutStayDurationEditing.setVisibility(View.VISIBLE);
        });

        tvCancel.setOnClickListener(view -> constraintLayoutStayDurationEditing.setVisibility(View.GONE));

        tvConfirm.setOnClickListener(view -> {
            tvStayDuration.setText("停留：" + spinnerHours.getSelectedItem().toString() + ":" + spinnerMinutes.getSelectedItem().toString());
            constraintLayoutStayDurationEditing.setVisibility(View.GONE);
        });
    }

    public static void addDestinationItem(Fragment parentFragment, int destinationItemSID) {
        if (parentFragment.getActivity() != null) {
            DestinationItemFragment destinationItemFragment = new DestinationItemFragment(destinationItemSID, null, null, null);
            parentFragment.getChildFragmentManager().beginTransaction().add(R.id.constraintLayout_destination_items_editing, destinationItemFragment, "destinationItemFragment" + destinationItemSID).commit();
            parentFragment.getChildFragmentManager().executePendingTransactions();
        }
    }

    public static void removeDestinationItemFragment(Fragment destinationItemFragment) {
        if (destinationItemFragment != null && destinationItemFragment.getActivity() != null) {
            Fragment parentFragment = destinationItemFragment.getParentFragment();
            if (parentFragment != null) {
                FragmentManager parentFragmentManager = destinationItemFragment.getParentFragmentManager();
                parentFragmentManager.beginTransaction().remove(destinationItemFragment).commit();
                parentFragmentManager.executePendingTransactions();
                adjustPositionDestinationItems(parentFragment);
            }
        }
    }

    public static void adjustPositionDestinationItems(Fragment parentFragment) {
        if (parentFragment != null && parentFragment.getView() != null) {
            List<Fragment> destinationItemFragments = parentFragment.getChildFragmentManager().getFragments();
            ConstraintLayout constraintLayoutDestinationItemsEditing = parentFragment.getView().findViewById(R.id.constraintLayout_destination_items_editing);
            List<View> destinationItemViews = new ArrayList<>();
            int[] viewIds = new int[destinationItemFragments.size()];

            constraintLayoutDestinationItemsEditing.removeAllViews();

            for (int i = 0; i < destinationItemFragments.size(); i++) {
                Fragment fragment = destinationItemFragments.get(i);
                if (fragment.getView() != null) {
                    fragment.getView().setId(View.generateViewId());
                    constraintLayoutDestinationItemsEditing.addView(fragment.getView());
                    destinationItemViews.add(fragment.getView());
                    viewIds[i] = fragment.getView().getId();
                    TextView destinationItemSIDTextView = fragment.getView().findViewById(R.id.tv_destination_sid);
                    destinationItemSIDTextView.setText(String.valueOf(i + 1));

                    ConstraintLayout constraintLayoutStayDurationEditing = fragment.getView().findViewById(R.id.constraintLayout_stay_duration_editing);
                    TextView stayDurationTextView = fragment.getView().findViewById(R.id.tv_stay_duration);
                    Spinner spinnerHours = fragment.getView().findViewById(R.id.spinner_stay_duration_hours);
                    Spinner spinnerMinutes = fragment.getView().findViewById(R.id.spinner_stay_duration_minutes);

                    // 最後一個停留地點不需設定停留時間
                    if (i == destinationItemFragments.size() - 1) {
                        stayDurationTextView.setClickable(false);
                        stayDurationTextView.setText(Constants.STAY_DURATION_FINAL_DESTINATION);
                    } else {
                        stayDurationTextView.setClickable(true);
                        stayDurationTextView.setText("停留：" + spinnerHours.getSelectedItem().toString() + ":" + spinnerMinutes.getSelectedItem().toString());
                        stayDurationTextView.setOnClickListener(v -> constraintLayoutStayDurationEditing.setVisibility(View.VISIBLE));
                    }
                }
            }

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayoutDestinationItemsEditing);

            if (destinationItemViews.size() >= 2) {
                constraintSet.createVerticalChain(constraintLayoutDestinationItemsEditing.getId(), ConstraintSet.TOP, constraintLayoutDestinationItemsEditing.getId(), ConstraintSet.BOTTOM, viewIds, null, ConstraintSet.CHAIN_SPREAD_INSIDE);
                constraintSet.applyTo(constraintLayoutDestinationItemsEditing);
            }

            ConstraintLayout constraintLayoutDestinationItemDisabled = parentFragment.getView().findViewById(R.id.constraintLayout_destination_item_disabled);
            TextView destinationItemDisabledSIDView = constraintLayoutDestinationItemDisabled.findViewById(R.id.tv_destination_sid);
            if (destinationItemDisabledSIDView != null) {
                destinationItemDisabledSIDView.setText(String.valueOf(destinationItemFragments.size() + 1));
            }
        }
    }
}
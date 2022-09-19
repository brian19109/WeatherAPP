package com.brian19109.weatherapi.ui;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.brian19109.weatherapi.GetLastLocationCallback;
import com.brian19109.weatherapi.R;
import com.brian19109.weatherapi.database.ProjectRepository;
import com.brian19109.weatherapi.model.Project;
import com.brian19109.weatherapi.model.WeatherPlace;
import com.brian19109.weatherapi.util.Constants;
import com.brian19109.weatherapi.util.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectEditingFragment extends Fragment implements GetLastLocationCallback {
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(1);
    private ConstraintLayout mConstraintLayoutDateTimePickers;
    private TextView mTextViewDepartureDateTime;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private LatLng mCurrentLocation;
    private String mCurrentAddress;
    private String mDepartureTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Util.getCurrentLocation(getActivity(), this);
        initPlacesClient();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_editing, container, false);

        if (getActivity() != null && getContext() != null) {
            EditText etProjectName = getActivity().findViewById(R.id.et_project_name);
            TextView tvBack = getActivity().findViewById(R.id.tv_back_on_action_bar_editing_project);
            ImageView ivEditProjectName = getActivity().findViewById(R.id.iv_edit_project_name);

            tvBack.setOnClickListener(v -> {
                ProjectListFragment projectListFragment = new ProjectListFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, projectListFragment).commit();
                if (getActivity() instanceof AppCompatActivity && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setCustomView(R.layout.action_bar_project_list);
                }
            });

            ivEditProjectName.setOnClickListener(v -> {
                if (etProjectName != null) {
                    etProjectName.setEnabled(true);
                }
            });

            etProjectName.setOnKeyListener((v, i, keyEvent) -> {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) && getActivity() != null) {
                    // Perform action on key press
                    etProjectName.setEnabled(false);
                    etProjectName.clearFocus();
                    Util.hideSoftKeyboard(getActivity());
                    return true;
                }
                return false;
            });

            String mProjectName = etProjectName.getText().toString().trim();

            if (!mProjectName.equals("請填入專案名稱")) {
                ProjectRepository projectRepository = new ProjectRepository(getActivity().getApplication());

                try {
                    projectRepository.getAllPlacesByProjectName(mProjectName).observe(getViewLifecycleOwner(), places -> {
                        if (places.size() != 0) {
                            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                                Util.removeDestinationItemFragment(fragment);
                            }

                            for (int i = 0; i < places.size(); i++) {
                                WeatherPlace place = places.get(i);
                                DestinationItemFragment destinationItemFragment = new DestinationItemFragment(i + 1, place.getTravelMode(), place.getAddress(), place.getStayDuration());
                                getChildFragmentManager().beginTransaction().add(R.id.constraintLayout_destination_items_editing, destinationItemFragment, "destinationItemFragment" + (i + 1)).commit();
                                getChildFragmentManager().executePendingTransactions();
                            }
                        }
                    });
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ConstraintLayout constraintLayoutDepartureDateTimeText = view.findViewById(R.id.constraintLayout_departure_date_time_text);

            mConstraintLayoutDateTimePickers = view.findViewById(R.id.constraintLayout_date_time_pickers);
            mTextViewDepartureDateTime = view.findViewById(R.id.tv_departure_date_time);
            mDatePicker = view.findViewById(R.id.datePicker);
            mTimePicker = view.findViewById(R.id.timePicker);
            TextView tvDateTimePickerCancel = view.findViewById(R.id.tv_date_time_picker_cancel);
            TextView tvDateTimePickerOK = view.findViewById(R.id.tv_date_time_picker_ok);

            if (constraintLayoutDepartureDateTimeText != null) {
                setDepartureDateTimeToNow();
                constraintLayoutDepartureDateTimeText.setOnClickListener(v -> {
                    if (mConstraintLayoutDateTimePickers != null && tvDateTimePickerCancel != null && tvDateTimePickerOK != null) {
                        Util.hideSoftKeyboard(this.getActivity());
                        mConstraintLayoutDateTimePickers.setVisibility(View.VISIBLE);
                        tvDateTimePickerCancel.setOnClickListener(viewTextViewCancel -> mConstraintLayoutDateTimePickers.setVisibility(View.GONE));
                        tvDateTimePickerOK.setOnClickListener(viewTextViewOK -> setTextViewDepartureDateTime());
                    }
                });
            }

            if (mTimePicker != null) {
                mTimePicker.setIs24HourView(true);
            }

            TextView tvOriginAddress = view.findViewById(R.id.tv_origin_address);

            ActivityResultLauncher<Intent> startOriginAddressAutocomplete = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    (ActivityResultCallback<ActivityResult>) result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            if (intent != null && getActivity() != null) {
                                Place place = Autocomplete.getPlaceFromIntent(intent);

                                if (tvOriginAddress != null) {
                                    tvOriginAddress.setText(Util.getPlaceAddress(place));
                                }
                            }
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            Toast.makeText(getContext(), "使用者取消地址輸入", Toast.LENGTH_LONG).show();
                        }
                    });

            // 設定「出發位置」輸入地址的點擊監聽事件
            tvOriginAddress.setOnClickListener(v -> {
                // Set the fields to specify which types of place data to return after the user has made a selection.
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS, Place.Field.TYPES);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .setInitialQuery(tvOriginAddress.getText().toString())
                        .build(getContext());

                startOriginAddressAutocomplete.launch(intent);
            });

            ConstraintLayout constraintLayoutDestinationItemDisabled = view.findViewById(R.id.constraintLayout_destination_item_disabled);
            ImageView ivTravelModeDestinationDisabled = constraintLayoutDestinationItemDisabled.findViewById(R.id.iv_travel_mode);
            TextView tvDestinationAddressDisabled = constraintLayoutDestinationItemDisabled.findViewById(R.id.tv_destination_address);
            TextView tvStayDurationDisabled = constraintLayoutDestinationItemDisabled.findViewById(R.id.tv_stay_duration);
            ImageView ivTrashCan = constraintLayoutDestinationItemDisabled.findViewById(R.id.iv_trash_can_delete_destination_item);

            constraintLayoutDestinationItemDisabled.setOnClickListener(v -> Util.addDestinationItem(this, getChildFragmentManager().getFragments().size() + 1));

            if (ivTravelModeDestinationDisabled != null) {
                ivTravelModeDestinationDisabled.setOnClickListener(v -> Util.addDestinationItem(this, getChildFragmentManager().getFragments().size() + 1));
            }

            if (tvDestinationAddressDisabled != null) {
                tvDestinationAddressDisabled.setOnClickListener(v -> Util.addDestinationItem(this, getChildFragmentManager().getFragments().size() + 1));
            }

            if (tvStayDurationDisabled != null) {
                tvStayDurationDisabled.setOnClickListener(v -> Util.addDestinationItem(this, getChildFragmentManager().getFragments().size() + 1));
            }

            ivTrashCan.setVisibility(View.GONE);

            TextView tvStartNavigate = view.findViewById(R.id.tv_start_navigate);
            tvStartNavigate.setOnClickListener(v -> {
                if (getActivity() != null) {
                    insertProject();
                }
            });

            // 新增第一個目標位置
            Util.addDestinationItem(this, 1);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setTextViewDepartureDateTime() {
        mConstraintLayoutDateTimePickers.setVisibility(View.GONE);

        Calendar departureCalendar = new GregorianCalendar(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());

        mTextViewDepartureDateTime.setText(mDatePicker.getYear() + "/" + Util.formatNumberWithLeadingZeros(mDatePicker.getMonth() + 1, 2) + "/" + Util.formatNumberWithLeadingZeros(mDatePicker.getDayOfMonth(), 2) + "  週" + Util.getDayOfWeekInChinese(departureCalendar.getTime().getDay()) + " " + Util.formatNumberWithLeadingZeros(mTimePicker.getHour(), 2) + ":" + Util.formatNumberWithLeadingZeros(mTimePicker.getMinute(), 2));
        mDepartureTime = mDatePicker.getYear() + "/" + Util.formatNumberWithLeadingZeros(mDatePicker.getMonth() + 1, 2) + "/" + Util.formatNumberWithLeadingZeros(mDatePicker.getDayOfMonth(), 2) + " " + Util.formatNumberWithLeadingZeros(mTimePicker.getHour(), 2) + ":" + Util.formatNumberWithLeadingZeros(mTimePicker.getMinute(), 2);
    }

    private void setDepartureDateTimeToNow() {
        Date currentDateTime = Calendar.getInstance().getTime();

        String currentTimeStamp = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN).format(currentDateTime);
        currentTimeStamp += (" 週").concat(Util.getDayOfWeekInChinese(currentDateTime.getDay())).concat(" ").concat(new SimpleDateFormat("HH:mm", Locale.TAIWAN).format(currentDateTime));

        mTextViewDepartureDateTime.setText(currentTimeStamp);
        mDepartureTime = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN).format(currentDateTime);
    }

    private void initPlacesClient() {
        if (getActivity() != null && !Places.isInitialized()) {
            Places.initialize(getActivity(), Constants.GOOGLE_PLACES_SDK_AUTH_KEY);

            // Create a new Places client instance.
            Places.createClient(getActivity().getApplicationContext());
        }
    }

    @Override
    public void onGetLastLocationSuccess(Location location) {
        if (location != null) {
            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            mExecutorService.execute(() -> {
                mCurrentAddress = Util.getAddressByLatLng(mCurrentLocation);
                mMainThreadHandler.post(ProjectEditingFragment.this::updateCurrentLocationAddress);
            });
        }
    }

    private void updateCurrentLocationAddress() {
        if (mCurrentAddress != null && getActivity() != null) {
            TextView tvOriginAddress = getActivity().findViewById(R.id.tv_origin_address);
            tvOriginAddress.setText(mCurrentAddress);
        }
    }

    private void insertProject() {
        if (getActivity() != null && isReadyToShowExploreFragment()) {
            EditText etProjectName = getActivity().findViewById(R.id.et_project_name);
            TextView tvOriginAddress = getActivity().findViewById(R.id.tv_origin_address);

            Project project = new Project(etProjectName.getText().toString(), mDepartureTime, tvOriginAddress.getText().toString());
            ProjectRepository projectRepository = new ProjectRepository(getActivity().getApplication());
            projectRepository.insertProject(project);

            // 新增出發位置到 Room 資料庫
            WeatherPlace originPlace = new WeatherPlace(etProjectName.getText().toString(), Constants.TravelModes.WALKING, tvOriginAddress.getText().toString(), Duration.ofMinutes(0));
            originPlace.setArrivalTime(mDepartureTime);
            projectRepository.insertPlace(originPlace);

            // 新增目標位置到 Room 資料庫
            for (int i = 0; i < getChildFragmentManager().getFragments().size(); i++) {
                Fragment fragment = getChildFragmentManager().getFragments().get(i);
                if (fragment != null && fragment.getTag() != null && fragment.getTag().startsWith("destinationItemFragment") && fragment.getView() != null) {
                    View destinationItemView = fragment.getView();
                    ImageView travelModeImageView = destinationItemView.findViewById(R.id.iv_travel_mode);
                    TextView destinationAddressTextView = destinationItemView.findViewById(R.id.tv_destination_address);
                    TextView stayDurationTextView = destinationItemView.findViewById(R.id.tv_stay_duration);
                    String stayDurationText = stayDurationTextView.getText().toString();
                    int stayHours = !stayDurationText.equals(Constants.STAY_DURATION_FINAL_DESTINATION) ? Integer.parseInt(stayDurationText.substring(stayDurationText.indexOf("停留：") + 3, stayDurationText.indexOf("停留：") + 5)) : 0;
                    int stayMinutes = !stayDurationText.equals(Constants.STAY_DURATION_FINAL_DESTINATION) ? Integer.parseInt(stayDurationText.substring(6)) : 0;

                    if (travelModeImageView != null && destinationAddressTextView != null) {
                        WeatherPlace destinationPlace = new WeatherPlace(etProjectName.getText().toString(), (Constants.TravelModes) travelModeImageView.getTag(), destinationAddressTextView.getText().toString(), Duration.ofMinutes(stayHours * 60L + stayMinutes));
                        projectRepository.insertPlace(destinationPlace);
                    }
                }
            }

            ExploreFragment exploreFragment = new ExploreFragment(etProjectName.getText().toString());
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, exploreFragment).commit();
        }
    }

    private boolean isReadyToShowExploreFragment() {
        for (int i = 0; i < getChildFragmentManager().getFragments().size(); i++) {
            Fragment fragment = getChildFragmentManager().getFragments().get(i);
            if (fragment != null && fragment.getTag() != null && fragment.getTag().startsWith("destinationItemFragment") && fragment.getView() != null) {
                View destinationItemView = fragment.getView();
                TextView destinationAddressTextView = destinationItemView.findViewById(R.id.tv_destination_address);

                if (TextUtils.isEmpty(destinationAddressTextView.getText())) {
                    Toast.makeText(getContext(), "目的地 " + (i + 1) + " 地址為空白，無法顯示行程天氣結果！", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }

        return true;
    }
}

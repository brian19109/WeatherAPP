package com.brian19109.weatherapi.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.brian19109.weatherapi.R;
import com.brian19109.weatherapi.util.Constants;
import com.brian19109.weatherapi.util.Util;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;

import java.time.Duration;

public class DestinationItemFragment extends Fragment {
    private final int mDestinationSID;
    private final Constants.TravelModes mTravelMode;
    private final String mAddress;
    private final Duration mStayDuration;

    public DestinationItemFragment(int id, Constants.TravelModes travelMode, String address, Duration stayDuration) {
        mDestinationSID = id;
        mTravelMode = travelMode;
        mAddress = address;
        mStayDuration = stayDuration;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.destination_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView destinationSIDTextView = view.findViewById(R.id.tv_destination_sid);
        if (destinationSIDTextView != null) {
            destinationSIDTextView.setText(String.valueOf(mDestinationSID));
        }

        if (mTravelMode != null) {
            ImageView travelModeImageView = view.findViewById(R.id.iv_travel_mode);
            switch (mTravelMode) {
                case MOTORCYCLING:
                    travelModeImageView.setImageResource(R.drawable.ic_travel_mode_scooter);
                    break;
                case BICYCLING:
                    travelModeImageView.setImageResource(R.drawable.ic_travel_mode_bicycle);
                    break;
                case WALKING:
                    travelModeImageView.setImageResource(R.drawable.ic_travel_mode_walk);
                    break;
                case DRIVING:
                default:
                    travelModeImageView.setImageResource(R.drawable.ic_travel_mode_car);
                    break;
            }
        }

        TextView destinationAddressTextView = view.findViewById(R.id.tv_destination_address);
        ActivityResultLauncher<Intent> startDestinationAddressAutocomplete = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (ActivityResultCallback<ActivityResult>) result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if (intent != null && getActivity() != null) {
                            Place place = Autocomplete.getPlaceFromIntent(intent);

                            if (destinationAddressTextView != null) {
                                destinationAddressTextView.setText(Util.getPlaceAddress(place));
                            }
                        }
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Toast.makeText(getContext(), "使用者取消地址輸入", Toast.LENGTH_LONG).show();
                    }
                });

        if (mAddress != null) {
            destinationAddressTextView.setText(mAddress);
        }

        ImageView ivTrashCan = view.findViewById(R.id.iv_trash_can_delete_destination_item);
        if (ivTrashCan != null) {
            // 至少要有一個目的地，所以第一個目的地不能被刪除。
            if (mDestinationSID == 1) {
                ivTrashCan.setVisibility(View.GONE);
            } else {
                ivTrashCan.setOnClickListener(v -> Util.removeDestinationItemFragment(this));
            }
        }

        Util.initDestinationItem(view.findViewById(R.id.constraintLayout_destination_item), getContext(), startDestinationAddressAutocomplete);
        Util.adjustPositionDestinationItems(getParentFragment());
    }
}

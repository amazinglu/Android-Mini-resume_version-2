package com.parabit.parabeacon.app.demo.layout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.parabit.mmrbt.api.BankLocation;
import com.parabit.parabeacon.app.demo.R;

public class BankLocationDetail extends BaseDemoFragment {

    private MapView mapView;

    public BankLocationDetail() {
        // Required empty public constructor
    }

    public static BankLocationDetail newInstance(Bundle bundle) {
        BankLocationDetail fragment = new BankLocationDetail();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(null);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_bank_location_detail, container, false);
        mapView = (MapView) fragmentView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        BankLocation currentLocation = getCurrentState().getSelectedLocation();

        if (currentLocation != null) {

            View selectedDetail = fragmentView.findViewById(R.id.selected_result);
            TextView name = (TextView) selectedDetail.findViewById(R.id.txt_location_name);
            TextView distance = (TextView) selectedDetail.findViewById(R.id.txt_location_distance);
            TextView address = (TextView) selectedDetail.findViewById(R.id.txt_location_address);
            TextView locationType = (TextView) selectedDetail.findViewById(R.id.txt_location_type);

            name.setText(currentLocation.getName());
            address.setText(currentLocation.getAddress());
            distance.setText(currentLocation.getDistance() + " mi");
            locationType.setText(currentLocation.getLocationType().name());

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    log().debug("Setting map location to " + currentLocation.getLatitude() + "," + currentLocation.getLongitude());
                    LatLng latlon = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    CameraPosition position = new CameraPosition.Builder()
                            .target(latlon) // Sets the new camera position
                            .zoom(12) // Sets the zoom
//                            .bearing(180) // Rotate the camera
//                            .tilt(30) // Set the camera tilt
                            .build(); // Creates a CameraPosition from the builder

                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 2000);


                    mapboxMap.addMarker(new MarkerOptions()
                            .position(latlon));

                }
            });
        }

        return fragmentView;
    }


}

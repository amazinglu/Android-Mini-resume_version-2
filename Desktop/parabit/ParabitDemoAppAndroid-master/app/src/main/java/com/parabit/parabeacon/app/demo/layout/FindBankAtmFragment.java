package com.parabit.parabeacon.app.demo.layout;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.parabit.mmrbt.ParabitBeaconSDK;
import com.parabit.mmrbt.api.BankLocation;
import com.parabit.mmrbt.api.LocationHandler;
import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.state.AppState;

import java.io.IOException;
import java.util.List;

/**
 * Created by williamsnyder on 8/28/17.
 */

public class FindBankAtmFragment extends BaseDemoFragment {

    private LocationListAdapter locationListAdapter;

    public static FindBankAtmFragment newInstance(Bundle bundle) {
        FindBankAtmFragment findBankAtmFragment = new FindBankAtmFragment();
        findBankAtmFragment.setArguments(bundle);
        return findBankAtmFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        log().debug("Loading Find/ATM view");
        View fragmentView = inflater.inflate(R.layout.fragment_find_atm_bank, container, false);

        RecyclerView resultsView = (RecyclerView) fragmentView.findViewById(R.id.atm_recycler);
        DividerItemDecoration divider = new DividerItemDecoration(resultsView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.horizontal_divider));
        resultsView.addItemDecoration(divider);

        final SearchView searchView = (SearchView) fragmentView.findViewById(R.id.atm_search_view);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                log().debug("Using search text:"+query);

                LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                double longitude = location != null ? location.getLongitude() : 0;
                double latitude = location != null ? location.getLatitude() : 0;

                log().debug("Looking for address match");
                Geocoder geocoder = new Geocoder(getApplication());
                try {
                    List<Address> match = geocoder.getFromLocationName(query, 1);
                    if (match.size() == 1) {
                        longitude = match.get(0).getLongitude();
                        latitude = match.get(0).getLatitude();
                        String address = (match.get(0).getAddressLine(0));
                        log().debug("Address match found:"+address);
                        log().debug("Searching using this location: " + latitude + "," + longitude);
                        searchView.setQuery(address, false);
                    } else {
                        log().debug("Address match not found.");
                        log().debug("Searching using current location: " + latitude + "," + longitude);
                    }
                } catch (IOException e) {
                    log().error("Unable to access geocoder:" + e.getLocalizedMessage());
                }

                ParabitBeaconSDK.getNearbyLocations(latitude, longitude, new LocationHandler() {
                    @Override
                    public void onSuccess(List<BankLocation> locations) {
                        log().debug("Found " + locations.size() + " location(s)");
                        locationListAdapter
                                = new LocationListAdapter(locations, getApplication());
                        resultsView.setAdapter(locationListAdapter);
                        locationListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String s) {
                        log().error("Unable to get locations:"+ s);
                    }
                });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

//        locations = BankLocationManager.getInstance("","").getNearbyLocations(0.0,0.0);
//        locationListAdapter
//                = new LocationListAdapter(locations, getApplication());
//        resultsView.setAdapter(locationListAdapter);
//        locationListAdapter.notifyDataSetChanged();

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
    }

    private class LocationListAdapter extends RecyclerView.Adapter<LocationViewHolder> {

        private List<BankLocation> locations;

        public LocationListAdapter(List<BankLocation> locations, Application application) {
            this.locations = locations;
        }

        @Override
        public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bank_location_result, parent, false);

            LocationViewHolder vh = new LocationViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(LocationViewHolder holder, int position) {

            if (locations == null) {
                return;
            }

            BankLocation location = locations.get(position);

            View v = holder.view;
            v.setClickable(true);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppState currentState = getCurrentState();
                    currentState.setSelectedLocation(location);
                    saveAppState();
                    log().debug("Showing detail for " + location.getName());
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(null);
                    BankLocationDetail nextFrag = BankLocationDetail.newInstance(new Bundle());
                    FindBankAtmFragment.this.getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, nextFrag,"detailFragment")
                            .addToBackStack(null)
                            .commit();

                }
            });


            holder.name.setText(location.getName());
            holder.address.setText(location.getAddress());
            holder.distance.setText(location.getDistance() + " mi");
            holder.locationType.setText(location.getLocationType().name());
        }

        @Override
        public int getItemCount() {

            if (locations != null) {
                return locations.size();
            }

            return 0;
        }
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView locationType;
        private TextView distance;
        private TextView address;

        private View view;

        public LocationViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;
            name = (TextView) itemView.findViewById(R.id.txt_location_name);
            distance = (TextView) itemView.findViewById(R.id.txt_location_distance);
            address = (TextView) itemView.findViewById(R.id.txt_location_address);
            locationType = (TextView) itemView.findViewById(R.id.txt_location_type);

        }
    }
}

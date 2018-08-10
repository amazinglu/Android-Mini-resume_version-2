// Copyright 2016 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.parabit.parabeacon.app.tech;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parabit.beacon.ParabitBeaconManager;
import com.parabit.beacon.api.BeaconInfo;
import com.parabit.parabeacon.app.tech.logging.ApplicationLogger;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants.*;
import com.parabit.parabeacon.app.tech.utils.SlotDataManager;
import com.parabit.parabeacon.app.tech.utils.Utils;

import net.hockeyapp.android.metrics.MetricsManager;

import org.zakariya.stickyheaders.SectioningAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Adapter for the recyclerView displaying all the results from scanning for beacons nearby in
 * ScanningActivity. Each list entry is a CardView whose structure changes depending on the
 * information available from the scan result. It is inflated for every frame which the beacon
 * is broadcasting with 1 row which displays this information.
 */

public class BeaconListAdapter2 extends SectioningAdapter {

    private Context context;
    private Section connectable;
    private Section other;
    private ParabitBeaconManager beaconManager;

    public BeaconListAdapter2(List<BeaconScanData> scanDataList, Context context) {
        this.context = context;
        setupSections();
        setData(scanDataList);
    }

    public void setParabitBeaconManager(ParabitBeaconManager beaconManager) {
        this.beaconManager = beaconManager;
    }

    /**
     * must be override when using SectioningAdapter
     * https://github.com/ShamylZakariya/StickyHeaders/issues/87
     * */
    @Override
    public GhostHeaderViewHolder onCreateGhostHeaderViewHolder(ViewGroup parent) {
        final View ghostView = new View(parent.getContext());
        ghostView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        return new GhostHeaderViewHolder(ghostView);
    }

    @Override
    public int getNumberOfSections() {
        return 2;
    }

    @Override
    public int getNumberOfItemsInSection(int sectionIndex) {
        if (sectionIndex == 0) {
            return connectable.scanDataList.size();
        } else {
            return other.scanDataList.size();
        }
    }

    @Override
    public boolean doesSectionHaveHeader(int sectionIndex) {
        return true;
    }

    @Override
    public boolean doesSectionHaveFooter(int sectionIndex) {
        if (sectionIndex == 0) {
            return connectable.scanDataList.size() == 0;
        } else {
            return other.scanDataList.size() == 0;
        }
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.layout_scan_result_small, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.layout_list_header, parent, false);
        return new HeaderViewHolder(v);
    }

    @Override
    public FooterViewHolder onCreateFooterViewHolder(ViewGroup parent, int footerType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.layout_list_footer, parent, false);
        return new FooterViewHolder(v);
    }

    @Override
    public void onBindItemViewHolder(SectioningAdapter.ItemViewHolder viewHolder, int sectionIndex, int itemIndex, int itemType) {
        Section s = null;

        ViewHolder vh = (ViewHolder) viewHolder;

        /**
         * section index == 0 => connectable
         * section index ！= 0 => other
         * */
        if (sectionIndex == 0) {
            s = connectable;
            vh.connectable = true;
        } else {
            s = other;
            vh.connectable = false;
        }

        BeaconScanData scanData = s.scanDataList.get(itemIndex);
        setupViewHolder(vh,scanData);
    }

    @Override
    public void onBindHeaderViewHolder(SectioningAdapter.HeaderViewHolder viewHolder, int sectionIndex, int headerType) {
        HeaderViewHolder hvh = (HeaderViewHolder) viewHolder;

        if (sectionIndex == 0){
            hvh.titleTextView.setText(connectable.name);

        } else {
            hvh.titleTextView.setText(other.name);
        }

    }

    public void onBindFooterViewHolder(SectioningAdapter.FooterViewHolder viewHolder, int sectionIndex, int footerType) {
        FooterViewHolder fvh = (FooterViewHolder) viewHolder;
        if (sectionIndex == 0){
            //fvh.textView.setText(s.footer);

        } else {
            //fvh.textView.setText(s.footer);
        }
    }


    public void setupViewHolder(ViewHolder holder, BeaconScanData scanData) {

        String namespace = "";
        String instanceId = "";
        String serialNumber = scanData.serialNumber;

        for (byte[] uidFrameType : scanData.uidFrameTypes) {
            namespace = SlotDataManager.getNamespaceFromSlotData(uidFrameType);
            instanceId = SlotDataManager.getInstanceFromSlotData(uidFrameType);
        }

        if (serialNumber == null && instanceId != null) {
            try {
                String sn = Utils.fromHexString(instanceId);
                if (sn != null) {
                    sn = sn.trim();
                    if (sn.length() > 0) {
                        serialNumber = sn;
                    }
                }
            } catch (Exception e) {
                //ignore
            }
        }

        holder.setScanData(scanData);
        holder.setBeaconSerialNumber(serialNumber);

        /**
         * when we set up the view of the adapter, we need to get the beacon info from the AWS using
         * beacon manager
         * */
        //check for registration
        if (beaconManager == null) {
            return;
        }

        final ViewHolder registeredHolder = holder;

        final String iid = instanceId != null ? instanceId : serialNumber;

        Callback<BeaconInfo> beaconInfoCallback = new Callback<BeaconInfo>() {
            @Override
            public void onResponse(Call<BeaconInfo> call, Response<BeaconInfo> response) {
                BeaconInfo beaconInfo = response.body();
                // the beacon is not register
                if (beaconInfo == null) {
                    Map<String, String> properties = new HashMap<>();
                    properties.put("INSTANCE_ID",iid);
                    MetricsManager.trackEvent("BEACON_NOT_REGISTERED",properties);
                }
                /**
                 * update the beaconInfo in the view holder
                 * */
                updateRegistrationState(registeredHolder, beaconInfo);
            }

            @Override
            public void onFailure(Call<BeaconInfo> call, Throwable t) {
                Map<String, String> properties = new HashMap<>();
                properties.put(Keys.SERIAL_NUMBER,iid);
                MetricsManager.trackEvent(Events.SN_UNKNOWN,properties);
                updateRegistrationState(registeredHolder, null);
            }
        };

        /**
         * get the beacon info from AWS
         * */
        if (serialNumber != null) {
            beaconManager.getParabitBeacon(serialNumber, beaconInfoCallback);
        } else {
            beaconManager.getParabitBeacon(namespace, instanceId, beaconInfoCallback);
        }

    }

    private void updateRegistrationState(ViewHolder registeredHolder, BeaconInfo beaconInfo) {
        if (beaconInfo != null) {
            registeredHolder.setBeaconInfo(beaconInfo);
        }
    }

    /**
     * there is two types of beacon: connectable and Advertising
     * Section class store all the beacons in one type
     * */
    private void setupSections(){
        connectable = new Section();
        connectable.name = "Connectable";
        connectable.scanDataList = new ArrayList<>();

        other = new Section();
        other.name = "Advertising";
        other.scanDataList = new ArrayList<>();
    }

    public void setData(List<BeaconScanData> scanData) {
        connectable.scanDataList.clear();
        other.scanDataList.clear();
        for (BeaconScanData beacon: scanData) {
            if (beacon.connectable) {
                connectable.scanDataList.add(beacon);
            } else {
                other.scanDataList.add(beacon);
            }
        }
        notifyAllSectionsDataSetChanged();
    }

    private class Section {
        String name;
        ArrayList<BeaconScanData> scanDataList = new ArrayList<>();
    }


    public class HeaderViewHolder extends SectioningAdapter.HeaderViewHolder {
        TextView titleTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        }
    }

    public class FooterViewHolder extends SectioningAdapter.FooterViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     *  View holder for each displaying card in the recycler view.
     */
    public class ViewHolder extends SectioningAdapter.ItemViewHolder {
        public static final String BEACON_ADDRESS = "com.bluetooth.beaconconfig.BEACON_ADDRESS";
        public static final String BEACON_NAME = "com.bluetooth.beaconconfig.BEACON_NAME";
        public static final String BEACON_RSSI = "com.bluetooth.beaconconfig.BEACON_RSSI";
        public static final String BEACON_INFO = "com.bluetooth.beaconconfig.BEACON_INFO_SUCCESS";
        public static final String BEACON_TITLE = "com.bluetooth.beaconconfig.BEACON_TITLE";
        public static final String BEACON_SUBTITLE = "com.bluetooth.beaconconfig.BEACON_SUBTITLE";

        public boolean connectable = false;

        private CardView cardView;
        private BeaconInfo beaconInfo;
        private BeaconScanData scanData;

        private TextView title;
        private TextView subtitle;
        private TextView distance;
        private TextView serialNumber;
        private Gson gson;

        public ViewHolder(View itemView) {
            super(itemView);
            this.cardView = (CardView) itemView.findViewById(R.id.card_view_small);
            cardView.setUseCompatPadding(true);
            this.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            /**
             * access door process:
             *
             * what to do when click a beacon
             * */
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    /**
                     * the beacon we select is not connectable
                     * */
                    if (connectable == false) {
                        ApplicationLogger.logEvent(Events.BEACON_TAP);
                        String title = "Not Connectable";
                        String message = "This beacon can only be configured in connectable mode";
                        new AlertDialog.Builder(view.getContext()).setTitle(title).setMessage(message)
                                .setPositiveButton("OK", null).show();
                        return;
                    }

                    /**
                     * access door process:
                     * collect the beacon info and go to BeaconConfigActivity
                     * */
                    Intent configBeacon =
                            new Intent(cardView.getContext(), BeaconConfigActivity.class);
                    configBeacon.putExtra(BEACON_ADDRESS, scanData.deviceAddress);
                    configBeacon.putExtra(BEACON_NAME, scanData.name);
                    configBeacon.putExtra(BEACON_RSSI, Integer.toString(scanData.rssi));

                    if (beaconInfo != null) {
                        configBeacon.putExtra(BEACON_INFO, gson.toJson(beaconInfo));
                    }

                    if (beaconInfo != null) {
                        configBeacon.putExtra(BEACON_TITLE, beaconInfo.getName());
                        configBeacon.putExtra(BEACON_SUBTITLE, beaconInfo.getLocation());
                    }

                    cardView.getContext().startActivity(configBeacon);
                }
            });

            title = (TextView) cardView.findViewById(R.id.scan_card_name);
            subtitle = (TextView) cardView.findViewById(R.id.scan_card_desc);
            distance = (TextView) cardView.findViewById(R.id.scan_card_rssi);
            serialNumber = (TextView) cardView.findViewById(R.id.scan_card_sn);

        }

        public void setScanData(BeaconScanData scanData) {
            this.scanData = scanData;
            this.distance.setText(Integer.toString(scanData.rssi));
        }

        public void setBeaconInfo(BeaconInfo beaconInfo) {
            this.beaconInfo = beaconInfo;
            this.title.setText(beaconInfo.getName());
            this.subtitle.setText(beaconInfo.getLocation());
        }

        public void setBeaconSerialNumber(String serialNumber){
            this.serialNumber.setText(serialNumber !=null ? serialNumber : "N/A");
        }

    }
}

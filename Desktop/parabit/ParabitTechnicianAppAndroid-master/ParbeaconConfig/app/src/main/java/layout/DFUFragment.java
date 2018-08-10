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

package layout;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parabit.parabeacon.app.tech.BeaconConfigActivity;
import com.parabit.parabeacon.app.tech.Constants;
import com.parabit.parabeacon.app.tech.R;
import com.parabit.beacon.dfu.DfuFileHelper;
import com.parabit.beacon.dfu.DfuService;
import com.parabit.parabeacon.app.tech.logging.ApplicationLogger;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Fragment for displaying and configuring information of a frame slot in the beacon. This is a
 * unifying fragment which can be used for a UID, URL, TLM or EID frame.
 *
 * the DFU of BLE
 * https://www.jianshu.com/p/2268cfedc051
 */
public class DFUFragment extends BeaconTabFragment  {
    public static final String TAG =  DFUFragment.class.getSimpleName();

    private TextView mTextPercentage;
    private TextView mTextUploading;
    private ProgressBar mProgressBar;
    private Button mButtonUpload;

    private Map<String, String> debugProps = new HashMap<>();

    /**
     * DFU update step: call back while doing the DFu update
     * */
    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_connecting);
            ApplicationLogger.logDebug(Events.DEBUG_DFU_CONNECTING, debugProps);
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_starting);
            ApplicationLogger.logDebug(Events.DEBUG_DFU_STARTING, debugProps);
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_switching_to_dfu);
            ApplicationLogger.logDebug(Events.DEBUG_DFU_ENABLING, debugProps);
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_validating);
            ApplicationLogger.logDebug(Events.DEBUG_DFU_VALIDATING, debugProps);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_disconnecting);
            ApplicationLogger.logDebug(Events.DEBUG_DFU_DISCONNECTING, debugProps);
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            updateInProcess = false;
            mTextPercentage.setText(R.string.dfu_status_completed);
            mTextPercentage.setVisibility(View.INVISIBLE);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(0);
            mProgressBar.setVisibility(View.INVISIBLE);
            mTextUploading.setVisibility(View.INVISIBLE);
            ApplicationLogger.logDebug(Events.DEBUG_DFU_COMPLETE, debugProps);
            ApplicationLogger.logEvent(Events.UPDATE_SUCCESS, debugProps);

            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onTransferCompleted();

                    // if this activity is still open and upload process was completed, cancel   the notification
                    /**
                     * seem like the DfuService is a foreground service that has a notification
                     * */
                    final NotificationManager manager = (NotificationManager)  DFUFragment.this.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            mTextPercentage.setText(R.string.dfu_status_aborted);
            updateInProcess = false;
            ApplicationLogger.logDebug(Events.DEBUG_DFU_ABORTED, debugProps);
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onUploadCanceled();

                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) DFUFragment.this.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(percent);
            mTextPercentage.setText(getString(R.string.dfu_uploading_percentage, percent));
            if (partsTotal > 1)
                mTextUploading.setText(getString(R.string.dfu_status_uploading_part, currentPart, partsTotal));
            else
                mTextUploading.setText(R.string.dfu_status_uploading);
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            showErrorMessage(message);
            updateInProcess = false;
            ApplicationLogger.logDebug(Events.DEBUG_DFU_ERROR, debugProps, new Exception(message));
            // We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) DFUFragment.this.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
        }
    };
    private BluetoothDevice mSelectedDevice;
    private String mSelectedAddress;
    private String mFilePath;
    private Uri mFileStreamUri;
    private TextView mTextVersion;
    private TextView mTextCurrentVersion;
    private String mSerialNumber;
    private String mFirmwareVersion;
    private String mFirmwareUpdateURL;
    private String mFirmwareUpdateRevision;
    private boolean updateInProcess;

    public static DFUFragment newInstance(Bundle bundle) {
        DFUFragment dfuFragment = new DFUFragment();
        dfuFragment.setArguments(bundle);
        dfuFragment.name = "Updates";
        dfuFragment.mSelectedAddress = bundle.getString(Constants.BEACON_ADDRESS);
        dfuFragment.mFirmwareVersion = bundle.getString(Constants.BEACON_FW);
        dfuFragment.mFirmwareUpdateURL = bundle.getString(Constants.FIRMWARE_UPDATE_URL);
        dfuFragment.mFirmwareUpdateRevision = bundle.getString(Constants.FIRMWARE_UPDATE_REVISION);
        dfuFragment.mSerialNumber = bundle.getString(Constants.BEACON_SN);
        dfuFragment.debugProps.put(Keys.SERIAL_NUMBER, dfuFragment.mSerialNumber);
        return dfuFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "DFUFragment.onCreateView");
        View v = inflater.inflate(R.layout.fragment_frame_slot,
                    (LinearLayout) container.findViewById(R.id.global_content), false);
        v.findViewById(R.id.dfu_fragment).setVisibility(View.VISIBLE);

        /**
         * DFU update step: register the ProgressListener
         *
         * registerProgressListener
         * during the DFU service, the mDfuProgressListener will be called to get the process or other state
         * */
        DfuServiceListenerHelper.registerProgressListener(this.getContext(), mDfuProgressListener);
        setUpFragment(v);
        return v;
    }

    public void onUploadCanceled() {
        showToast(R.string.dfu_aborted);
    }

    private void onTransferCompleted() {
        //clearUI(true)
        Map<String, String> props = new HashMap<>();
        props.put(Keys.SERIAL_NUMBER, mSerialNumber);
        props.put(Keys.CURRENT_FIRMWARE, mFirmwareVersion);
        props.put(Keys.NEW_FIRMWARE, mFirmwareUpdateRevision);
        BeaconConfigActivity parent = (BeaconConfigActivity) getActivity();
        /**
         * during the DFU update, the bluetooth device (beacon) will disconnect from the app
         * */
        parent.reconnectToGatt(BeaconConfigActivity.BeaconTabMap.DFU);
        showToast(R.string.dfu_success);

//        refreshFirmwareStatus()
//                read firmware mFirmwareVersion
//                check firmwareservice for info, latest, latesturl
//                show what version we are running
    }

    private void showErrorMessage(final String message) {
        //clearUI(false);
        /**
         * props is used for log only
         * */
        Map<String, String> props = new HashMap<>();
        props.put(Keys.CURRENT_FIRMWARE, mFirmwareVersion);
        props.put(Keys.NEW_FIRMWARE, mFirmwareUpdateRevision);
        props.put(Keys.ERROR_MESSAGE, message);
        ApplicationLogger.logDebug(Events.UPDATE_FAILED, props);
        showToast("Upload failed: " + message);
    }

    private void showToast(final int messageResId) {
        Toast.makeText(this.getActivity(), messageResId, Toast.LENGTH_SHORT).show();
    }

    private void showToast(final String message) {
        Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isDfuServiceRunning() {
        ActivityManager manager = (ActivityManager) this.getActivity().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DfuService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void setUpFragment(final View v) {
        /**
         * get the bluetoothAdapter service
         * */
        BluetoothManager bluetoothManager = (BluetoothManager) getContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        mSelectedDevice = bluetoothAdapter.getRemoteDevice(mSelectedAddress);

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DFUFragment.this.mTextUploading = (TextView) v.findViewById(R.id.textviewUploading);
                    DFUFragment.this.mTextPercentage = (TextView) v.findViewById(R.id.textviewProgress);
                    DFUFragment.this.mProgressBar = (ProgressBar) v.findViewById(R.id.progressbar_dfu);

                    DFUFragment.this.mButtonUpload = (Button) v.findViewById(R.id.update_button);
                    DFUFragment.this.mTextVersion = (TextView) v.findViewById(R.id.update_text);
                    DFUFragment.this.mTextCurrentVersion = (TextView) v.findViewById(R.id.current_text);

                    DFUFragment.this.mButtonUpload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startDownload();
                        }
                    });
                    DFUFragment.this.setupUpdateView();

                    name = "DFU";
                    v.findViewById(R.id.adv_int_info).setVisibility(View.INVISIBLE);
                    v.findViewById(R.id.adv_interval_seek_bar).setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    /**
     * DFU update step: check if there is new version
     *
     * tells the user if there is update available
     * base on if the mFirmwareUpdateURL is null
     * */
    private void setupUpdateView() {

        mTextCurrentVersion.setText("Current version: " + mFirmwareVersion);

        if (mFirmwareUpdateURL != null && differentFWAvailable()) {
            mTextVersion.setText("Parabeacon version " + mFirmwareUpdateRevision + " is available.");
            mButtonUpload.setVisibility(View.VISIBLE);
            mButtonUpload.setText("Update");
            mButtonUpload.setEnabled(true);
        } else {
            mTextVersion.setText("No updates are available.");
            mButtonUpload.setVisibility(View.INVISIBLE);
            mButtonUpload.setEnabled(false);
        }


    }

    private boolean differentFWAvailable() {
        if (mFirmwareVersion != null) {
            return !mFirmwareVersion.equals(mFirmwareUpdateRevision);
        } else {
            return mFirmwareUpdateRevision != null;
        }
    }

    /**
     * DFU update step: download the zipfile
     *
     * download the new Firmware using mFirmwareUpdateURL
     * */
    private void startDownload() {
        Map<String, String> props = new HashMap<>();
        props.put(Keys.CURRENT_FIRMWARE, mFirmwareVersion);
        props.put(Keys.NEW_FIRMWARE, mFirmwareUpdateRevision);
        ApplicationLogger.logEvent(Events.UPDATE_CHECK, props);
        new DownloadFirmwareTask().execute(mFirmwareUpdateURL);
    }

    /**
     * DFU update step: use DfuServiceInitiator to do the DFU update of the firmware  of the beacon door
     * */
    private void startUpload(File file) {
        mTextUploading.setText("Uploading...");
        mTextUploading.setVisibility(View.VISIBLE);

        mButtonUpload.setEnabled(false);

        /**
         * this is the absolute file path of the UPDATE_FOLDER
         * useless here
         * */
        mFilePath = DfuFileHelper.getSampleZipPath();

        // Save current state in order to restore it if user quit the Activity
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        final SharedPreferences.Editor editor = preferences.edit();

        final boolean keepBond = false;//preferences.getBoolean(SettingsFragment.SETTINGS_KEEP_BOND, false);

        /**
         * DfuServiceInitiator
         * 开启 DfuService 进行升级, as the same time the beacon will disconnect from the app in go into DFU mode
         *
         * file is the absolute file path of the firmware zipfile that we download
         * */
        final DfuServiceInitiator starter = new DfuServiceInitiator(mSelectedDevice.getAddress())
                .setDeviceName(mSelectedDevice.getName())
                .setKeepBond(keepBond);
        starter.setZip(file.getAbsolutePath());
        //starter.setZip(mFileStreamUri,mFilePath);

        updateInProcess = true;
        starter.start(this.getContext(), DfuService.class);
    }

    public boolean isDoingUpdate() {
        return updateInProcess;
    }

    private class DownloadFirmwareTask extends AsyncTask<String, Void, File> {

        private Exception error;

        public DownloadFirmwareTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mTextUploading.setVisibility(View.VISIBLE);
            mTextUploading.setText("Downloading firmware");
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
            ApplicationLogger.logDebug(Events.DEBUG_DFU_START_TRANSFER, debugProps);

        }

        /**
         * return the firmware zip file
         * */
        @Override
        protected File doInBackground(String... params) {
            String FILENAME = "parabit.zip";
            /**
             * store the new firmware into the cache storage
             * */
            File localRoot = getActivity().getCacheDir();
            File firmwareZip = new File(localRoot, FILENAME);


            try {
                /**
                 * download the new firmware zip file using the url
                 * */
                URL url = new URL(params[0]);
                /**
                 * InputStream get the fiemware data from url and put it into the buf
                 * */
                InputStream in = new BufferedInputStream(url.openStream());
                /**
                 * ByteArrayOutputStream write the data from buf to outputstream
                 * */
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1!=(n=in.read(buf)))
                {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                byte[] response = out.toByteArray();

                /**
                 * FileOutputStream write teh response into the File firmwareZip
                 * */
                FileOutputStream fos = new FileOutputStream(firmwareZip);
                fos.write(response);
                fos.close();

                ApplicationLogger.logDebug(Events.DEBUG_DFU_END_DOWNLOAD, debugProps);

                return firmwareZip;

            } catch (Exception ex) {
                ApplicationLogger.logDebug(Events.DEBUG_DFU_ERROR_DOWNLOAD, debugProps, ex);
                this.cancel(true);
                this.error = ex;
            }

            return firmwareZip;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mTextUploading.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressBar.setIndeterminate(false);
            Toast.makeText(getActivity(), "Error while downloading the firmware.", Toast.LENGTH_LONG).show();
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(final File file) {
            if (!isCancelled()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startUpload(file);
                    }
                });


            } else {
                mTextUploading.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                mProgressBar.setIndeterminate(false);
                Map<String, String> props = new HashMap<>();
                props.put(Keys.CURRENT_FIRMWARE, mFirmwareVersion);
                props.put(Keys.NEW_FIRMWARE, mFirmwareUpdateRevision);
                ApplicationLogger.logError(Events.UPDATE_FAILED, this.error);
                Toast.makeText(getActivity(), "Error while downloading the firmware.", Toast.LENGTH_LONG).show();
            }
        }

    }
}

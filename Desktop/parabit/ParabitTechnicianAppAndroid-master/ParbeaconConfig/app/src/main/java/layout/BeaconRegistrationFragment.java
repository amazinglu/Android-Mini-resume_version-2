package layout;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.parabit.parabeacon.app.tech.BeaconConfigActivity;
import com.parabit.parabeacon.app.tech.Constants;
import com.parabit.parabeacon.app.tech.R;
import com.parabit.parabeacon.app.tech.dialogs.BeaconInfoChangeDialog;
import com.parabit.parabeacon.app.tech.utils.SlotDataManager;
import com.parabit.parabeacon.app.tech.utils.UiUtils;
import com.parabit.beacon.ParabitBeaconManager;
import com.parabit.beacon.api.BeaconInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BeaconRegistrationFragment extends BeaconTabFragment  {

    public static final String TAG =  BeaconRegistrationFragment.class.getSimpleName();

    private BluetoothDevice mSelectedDevice;
    private String mSelectedAddress;
    private byte[] mSlotData;

    private View mViewBeaconInfo;
    private TextView mTextName;
    private TextView mTextDesc;

    private BeaconInfo beaconInfo;

    private String mName;
    private String mDesc;
    private Switch mRegistrationSwitch;

    private String mSerialNumber;

    public BeaconRegistrationFragment() {
        // Required empty public constructor
    }

    public static BeaconRegistrationFragment newInstance(Bundle bundle) {
        BeaconRegistrationFragment registrationFragment = new BeaconRegistrationFragment();
        registrationFragment.setArguments(bundle);
        registrationFragment.name = "Registration";
        registrationFragment.slotNumber = bundle.getInt(Constants.SLOT_NUMBER);
        registrationFragment.mSelectedAddress = bundle.getString(Constants.BEACON_ADDRESS);
        registrationFragment.mSlotData = bundle.getByteArray(Constants.SLOT_DATA);
        registrationFragment.mSerialNumber = bundle.getString(Constants.BEACON_SN);
        return registrationFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "BeaconRegistrationFragment.onCreateView");

        configurationListener = (ConfigurationListener) getActivity();

        BluetoothManager bluetoothManager = (BluetoothManager) getContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        mSelectedDevice = bluetoothAdapter.getRemoteDevice(mSelectedAddress);

        View v = inflater.inflate(R.layout.fragment_frame_slot,
                (LinearLayout) container.findViewById(R.id.global_content), false);
        v.findViewById(R.id.registration_fragment).setVisibility(View.VISIBLE);

        setUpFragment(v);
        return v;
    }

    protected void setUpFragment(final View v) {

//        if (getActivity() != null) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
                    final BeaconRegistrationFragment self = BeaconRegistrationFragment.this;

                    mRegistrationSwitch = (Switch) v.findViewById(R.id.switchBeaconReg);
                    self.mViewBeaconInfo = v.findViewById(R.id.registration_block);
                    self.mTextName = (TextView) v.findViewById(R.id.beacon_registered_name);
                    self.mTextDesc = (TextView) v.findViewById(R.id.beacon_registered_description);


                    String namespace = SlotDataManager.getNamespaceFromSlotData(self.mSlotData);
                    String instance = SlotDataManager.getInstanceFromSlotData(self.mSlotData);

                    checkForBeacon(namespace, instance);

                    View editTarget = v.findViewById(R.id.edit_beacon_info);
                    handleEditBeaconInfo(editTarget);

                    handleRegisterBeaconInfo(mRegistrationSwitch);

                    name = "Registration";
                    BeaconRegistrationFragment.super.setUpFragment(v);
                    v.findViewById(R.id.adv_int_info).setVisibility(View.INVISIBLE);
                    v.findViewById(R.id.adv_interval_seek_bar).setVisibility(View.INVISIBLE);
//                }
//            });
//        }
    }

    @Override
    public void saveChanges() {
        super.saveChanges();

        if (mRegistrationSwitch == null) {
            return;
        }

        boolean doRegister = mRegistrationSwitch.isChecked();

        if (!doRegister) {
            return;
        }

        if (this.beaconInfo == null) {
           return;
        }

        final BeaconRegistrationFragment self = BeaconRegistrationFragment.this;
        this.beaconInfo.setName(self.mName);
        this.beaconInfo.setLocation(self.mDesc);

        String token = getUserUtils().getFirmwareToken();
        String url = getUserUtils().getFirmwareURL();
        String appId = getUserUtils().getAppId();

        ParabitBeaconManager beaconManager = null;

        try {
            beaconManager = ParabitBeaconManager.getInstance(appId, url, token);
        } catch (Exception e) {
            showPopupMessage("Unable to connect to Beacon service");
            return;
        }

        beaconManager.updateBeacon(this.beaconInfo, new Callback<BeaconInfo>() {
            @Override
            public void onResponse(Call<BeaconInfo> call, Response<BeaconInfo> response) {
                self.beaconInfo = response.body();
                Log.d(TAG, "beacon saved success");

                if (self.beaconInfo != null && configurationListener != null){
                    Log.d(TAG, beaconInfo.getName() + ":" + beaconInfo.getLocation());
                    mTextName.setText(beaconInfo.getName());
                    mTextDesc.setText(beaconInfo.getLocation());
                    String namespace
                            = beaconInfo.getNamespace();
                    String instance
                            = beaconInfo.getInstanceID();
                    byte[] newSlotData = SlotDataManager.buildNewUidSlotData(namespace, instance);
                    configurationListener.slotDataChanged(slotNumber, newSlotData);
                }
            }

            @Override
            public void onFailure(Call<BeaconInfo> call, Throwable t) {
                Log.d(TAG, "beacon saved failed");
            }
        });

    }

    private void handleEditBeaconInfo(View editTarget) {

        final BeaconRegistrationFragment self = this;
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (self.beaconInfo == null) {
                    return;
                }
                BeaconInfoChangeDialog.show(self.beaconInfo.getName(), self.beaconInfo.getLocation(), getContext(),
                        new BeaconInfoChangeDialog.BeaconInfoChangeListener() {

                            @Override
                            public void setNewName(String name) {
                                self.mName = name;
                                self.mTextName.setText(name);
                            }

                            @Override
                            public void setNewDesc(String desc) {
                                self.mDesc = desc;
                                self.mTextDesc.setText(desc);
                            }
                        });
            }
        };

        editTarget.setOnClickListener(onClickListener);

    }

    private void handleRegisterBeaconInfo(View registerTarget) {

        this.mViewBeaconInfo.setEnabled(this.beaconInfo != null);

        final BeaconRegistrationFragment self = BeaconRegistrationFragment.this;
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (self.mRegistrationSwitch.isChecked()) {
                    registerBeacon();
                }
            }
        };

        registerTarget.setOnClickListener(onClickListener);

    }

    private void checkForBeacon(String namespace, String instance) {
        final BeaconRegistrationFragment self = this;
        final BeaconConfigActivity parent = (BeaconConfigActivity) getActivity();

        parent.disableDisplay();

        String token = getUserUtils().getBeaconToken();
        String url = getUserUtils().getBeaconURL();
        String appId = getUserUtils().getAppId();

        ParabitBeaconManager beaconManager = null;

        try {
            beaconManager = ParabitBeaconManager.getInstance(appId, url, token);
        } catch (Exception e) {
            showPopupMessage("Unable to connect to Beacon service");
            return;
        }

        beaconManager.getParabitBeacon(namespace, instance, new Callback<BeaconInfo>() {
            @Override
            public void onResponse(Call<BeaconInfo> call, Response<BeaconInfo> response) {
                self.beaconInfo = response.body();
                parent.enableDisplay();
                if (self.beaconInfo != null) {
                    Log.d(TAG, beaconInfo.getName() + ":" + beaconInfo.getLocation());
                    mTextName.setText(beaconInfo.getName());
                    mTextDesc.setText(beaconInfo.getLocation());
                    self.mRegistrationSwitch.setChecked(true);
                    self.mRegistrationSwitch.setEnabled(false);
                    self.mViewBeaconInfo.setEnabled(true);
                } else {
                    Log.d(TAG, "Beacon not found");
                }
            }

            @Override
            public void onFailure(Call<BeaconInfo> call, Throwable t) {
                parent.enableDisplay();
                Log.d(TAG, "Beacon not found");
            }
        });
    }

    private void registerBeacon() {
        final BeaconRegistrationFragment self = this;
        final BeaconConfigActivity parent = (BeaconConfigActivity) getActivity();
        parent.disableDisplay();

        final BeaconInfo beaconInfo = new BeaconInfo();
        beaconInfo.setMacAddress(mSelectedAddress);

        String token = getUserUtils().getBeaconToken();
        String url = getUserUtils().getBeaconURL();
        String appId = getUserUtils().getAppId();

        ParabitBeaconManager beaconManager = null;

        try {
            beaconManager = ParabitBeaconManager.getInstance(appId, url, token);
        } catch (Exception e) {
            showPopupMessage("Unable to connect to Beacon service");
            return;
        }

        beaconManager.registerBeacon(beaconInfo, new Callback<BeaconInfo>() {
            @Override
            public void onResponse(Call<BeaconInfo> call, Response<BeaconInfo> response) {
                self.beaconInfo = response.body();
                Log.d(TAG, "beacon registration success");
                parent.enableDisplay();
                if (self.beaconInfo != null) {
                    Log.d(TAG, beaconInfo.getName() + ":" + beaconInfo.getLocation());
                    mTextName.setText(beaconInfo.getName());
                    mTextDesc.setText(beaconInfo.getLocation());
                    mViewBeaconInfo.setEnabled(true);
                    mRegistrationSwitch.setEnabled(false);
                    saveNamespaceAndInstance();
                    UiUtils.showToast(self.getActivity(), "Beacon registered successfully.");
                } else {
                    Log.d(TAG, "beacon registration failed");
                    UiUtils.showToast(self.getActivity(), "Unable to register beacon.");
                }
            }

            @Override
            public void onFailure(Call<BeaconInfo> call, Throwable t) {
                Log.d(TAG, "beacon registration failed");
                parent.enableDisplay();
                UiUtils.showToast(self.getActivity(), "Unable to register beacon.");
            }
        });
    }

    private void saveNamespaceAndInstance() {
        BeaconConfigActivity parent = (BeaconConfigActivity) this.getActivity();
        parent.saveCurrentChanges();
    }

}

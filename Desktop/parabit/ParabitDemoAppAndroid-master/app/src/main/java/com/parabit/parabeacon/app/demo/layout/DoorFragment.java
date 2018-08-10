package com.parabit.parabeacon.app.demo.layout;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Application;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parabit.mmrbt.ParabitBeaconSDK;
import com.parabit.mmrbt.api.UnlockHandler;
import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.manager.LocalUnlockManager;
import com.parabit.parabeacon.app.demo.state.Door;

import java.util.List;
import java.util.Observer;

/**
 * Created by williamsnyder on 8/28/17.
 */

public class DoorFragment extends BaseDemoFragment {

    private DoorListAdapter doorListAdapter;

    private TextView mTextNoDoor;

    private Observer mDoorObserver;

    public static DoorFragment newInstance(Bundle bundle) {
        DoorFragment doorFragment = new DoorFragment();
        doorFragment.setArguments(bundle);
        return doorFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_door, container, false);
        setupFragment(fragmentView);
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getCurrentState().deleteObserver(mDoorObserver);
    }

    private void setupFragment(View fragmentView) {
        /**
         * mDoorObserver is the observer of the AppState
         * when there new door, update the door list
         * */
        mDoorObserver = (o, arg) -> getActivity().runOnUiThread(() -> updateDoorList(fragmentView));
        updateDoorList(fragmentView);
        getCurrentState().addObserver(mDoorObserver);

    }

    private void updateDoorList(View fragmentView) {
        RecyclerView resultsView = (RecyclerView) fragmentView.findViewById(R.id.door_recycler);
        List<Door> doors = getCurrentState().getAvailableDoors();
        doorListAdapter
                = new DoorListAdapter(doors, getApplication());
        resultsView.setAdapter(doorListAdapter);
        doorListAdapter.notifyDataSetChanged();

        mTextNoDoor = (TextView) fragmentView.findViewById(R.id.txtNoDoorAvailable);

        if (doors != null && doors.size() > 0) {
            mTextNoDoor.setVisibility(View.INVISIBLE);
            resultsView.setVisibility(View.VISIBLE);
        } else {
            mTextNoDoor.setVisibility(View.VISIBLE);
            resultsView.setVisibility(View.INVISIBLE);
        }
    }

    public void handleEnterDoorRegion(final Door door, final boolean autoUnlock) {
        System.out.println("you called?");
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (door != null) {
//                    mTextNoDoor.setVisibility(View.INVISIBLE);
//                    mCardViewDoor.setVisibility(View.VISIBLE);
//
//                    if (autoUnlock) {
//                        handleClickDoor();
//                    }
//
//                } else {
//                    mTextNoDoor.setVisibility(View.VISIBLE);
//                    mCardViewDoor.setVisibility(View.INVISIBLE);
//                }
//            }
//        });
    }

    /**
     * recycle view adapter for available door
     * */
    private class DoorListAdapter extends RecyclerView.Adapter<DoorViewHolder> {

        private List<Door> doors;

        public DoorListAdapter(List<Door> doors, Application application) {
            this.doors = doors;
        }

        @Override
        public DoorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.door_card, parent, false);

            DoorViewHolder vh = new DoorViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(DoorViewHolder holder, int position) {

            if (doors == null) {
                return;
            }

            Door door = doors.get(position);
            holder.setDoor(door);

        }

        @Override
        public int getItemCount() {

            if (doors != null) {
                return doors.size();
            }

            return 0;
        }
    }


    public class DoorViewHolder extends RecyclerView.ViewHolder {

        private Door door;
        private TextView name;
        private TextView location;
        private ImageView imgLockStatus;
        private Button mButtonDoor;

        private ProgressBar progressBar;
        private ValueAnimator anim = new ValueAnimator();

        private View view;

        public DoorViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;
            name = (TextView) itemView.findViewById(R.id.txt_door_name);
            location = (TextView) itemView.findViewById(R.id.txt_door_location);
            imgLockStatus = (ImageView) itemView.findViewById(R.id.img_lock_status);
            mButtonDoor = (Button) itemView.findViewById(R.id.btn_unlock);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_unlock);

        }

        public void setDoor(Door door) {
            this.door = door;
            updateView();
        }

        private void updateView() {
            this.name.setText(door.getName());
            this.location.setText(door.getLocation());
            progressBar.setVisibility(View.INVISIBLE);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClickDoor();
                }
            });

            mButtonDoor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClickDoor();
                }
            });
        }

        /**
         * what to do when click the door in the door list (recycler view)
         * */
        private void handleClickDoor() {
            log().info("Door unlock clicked");

            mButtonDoor.setText("Unlocking Door");

            anim.setIntValues(Color.WHITE, Color.LTGRAY|Color.alpha(25));
            anim.setEvaluator(new ArgbEvaluator());
            anim.setInterpolator(new FastOutLinearInInterpolator());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mButtonDoor.setBackgroundColor((Integer)valueAnimator.getAnimatedValue());
                }
            });

            anim.setDuration(1000);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.start();

            String serialNumber = door.getSerialNumber();
//            String serialNumber = "123456";

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            mButtonDoor.setText("Unlocking Door");

            log().info("demo mode: " + getCurrentState().isDemoMode());

            /**
             * show the app as s demo in wifi environment
             * use local network and server to handle the door unlock
             * */
            if (getCurrentState().isDemoMode()) {
                log().info("using local rabbit");
                String url = getString(R.string.local_rabbit_url);
                int doorOpenTime = getCurrentState().getDoorOpenTime();
                new LocalUnlockManager(url).unlock(serialNumber, doorOpenTime, new LocalUnlockManager.LocalUnlockCallback() {
                    @Override
                    public void onUnlockSuccess() {
                        log().info("Door unlocked successfully.");
                        updateDoorStatus(true);
                    }

                    @Override
                    public void onUnlockFailure() {
                        log().info("Door not unlocked.");
                        updateDoorStatus(false);
                    }

                    @Override
                    public void onError() {
                        log().info("Error during unlock.");
                        updateDoorStatus(false);
                    }
                });

                return;
            }

            /**
             * a remote unlock in real time use
             * */
            int duration = getCurrentState().getDoorOpenTime();
            ParabitBeaconSDK.unlock(serialNumber, duration, new UnlockHandler() {
                @Override
                public void onResult(boolean unlocked) {
                    updateDoorStatus(unlocked);
                }

                @Override
                public void onError(String s) {
                    log().error("Unable to unlock door:"+ s);
                    updateDoorStatus(false);
                }
            });
            return;
        }

        private void updateDoorStatus(final boolean unlocked) {
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                progressDialog.hide();

                    String message = unlocked ? "Door unlocked." : "Door locked";

                    anim.end();
                    //mButtonDoor.setText(message);
                    mButtonDoor.setBackgroundColor(Color.WHITE);
                    progressBar.setIndeterminate(false);

                    if (unlocked) {
                        countdownDoorOpen();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mButtonDoor.setText(R.string.remote_door_unlock);
                            }
                        }, 500);
                    }

                    Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                            message, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 80);
                    toast.show();
                }
            });

        }

        private void countdownDoorOpen() {
            int duration = getCurrentState().getDoorOpenTime();

            progressBar.setIndeterminate(false);
            final int tick = 1000;
            int max = duration*tick;
            progressBar.setVisibility(View.VISIBLE);

            progressBar.setMax(max);

            new CountDownTimer(max+(tick*2), tick) {

                public void onTick(long millisUntilFinished) {
                    int d = (int) Math.ceil(millisUntilFinished/tick) - 1;
                    progressBar.setProgress((d) * tick);
                    Log.d("TEST",""+d);

                    if (d == 0) {
                        mButtonDoor.setText(R.string.remote_door_unlock);
                        progressBar.setProgress(0);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }

                public void onFinish() {
                    mButtonDoor.setText(R.string.remote_door_unlock);
                    progressBar.setProgress(0);
                    progressBar.setVisibility(View.INVISIBLE);
                    Log.d("TEST","done");

                }
            }.start();
        }
    }
}

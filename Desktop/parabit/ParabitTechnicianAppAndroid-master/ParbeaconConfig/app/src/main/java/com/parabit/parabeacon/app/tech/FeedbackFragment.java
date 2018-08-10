package com.parabit.parabeacon.app.tech;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.parabit.beacon.ParabitFeedbackManager;
import com.parabit.beacon.api.BeaconInfo;
import com.parabit.beacon.feedback.Feedback;
import com.parabit.parabeacon.app.tech.logging.ApplicationLogger;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants.*;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class FeedbackFragment extends BaseFragment {

    private AppCompatEditText mTextFeedback;
    private ParabitFeedbackManager feedbackService;

    private FeedbackActivity.Mode mode = FeedbackActivity.Mode.General;

    public FeedbackFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment =  inflater.inflate(R.layout.fragment_feedback, container, false);

        setupView(fragment);

        String token = getAuthManager().getFeedbackToken();
        String url = getAuthManager().getFeedbackURL();

        try {
            feedbackService = ParabitFeedbackManager.getInstance(url, token);
        } catch (Exception e) {
            handleFeedbackAPIUnavailable(e);
        }

        return fragment;
    }

    private void setupView(View fragment) {
        mTextFeedback = (AppCompatEditText)fragment.findViewById(R.id.txt_feedback);
        mTextFeedback.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        ((Button)fragment.findViewById(R.id.btn_send_feedback)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback(mode);
            }
        });
    }

    private void handleFeedbackAPIUnavailable(Exception e) {
        ApplicationLogger.logError(Events.API_FAILURE,e);
        showPopupMessage("Unable to access Feedback service.");
    }

    public void setMode(FeedbackActivity.Mode mode) {
        this.mode = mode;
    }

    public void sendFeedback(FeedbackActivity.Mode mode) {
        String username = getAuthManager().getUsername();
        String message =  mTextFeedback.getText().toString();

        Feedback feedback = null;

        if (mode == FeedbackActivity.Mode.General) {
            feedback = Feedback.createGeneral(message, username);
        }

        if (mode == FeedbackActivity.Mode.Problem) {
            feedback = Feedback.createProblem(message, username);
        }

        if (feedback == null) {
            showFeedbackFailedMessage("Unrecognized message type");
        }

        BeaconInfo currentBeacon = getCurrentState().getSelectedBeacon();
        if (currentBeacon != null) {
            feedback.getContext().put("getSelectedBeacon",currentBeacon.getSerialNumber());
        }

        getParabitFeedbackManager().sendFeedback(feedback, new Callback<Feedback>() {
            @Override
            public void onResponse(Call<Feedback> call, Response<Feedback> response) {
                Map<String, String> properties = new HashMap<>();
                properties.put(ParabitLogConstants.Keys.REQUEST_DURATION,
                        Long.toString(response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis()));
                if (response.isSuccessful()) {
                    if (FeedbackFragment.this.mode == FeedbackActivity.Mode.General) {
                        ApplicationLogger.logEvent(Events.FEEDBACK_SUCCESS, properties);
                    } else {
                        ApplicationLogger.logEvent(Events.REPORT_PROBLEM_SUCCESS, properties);
                    }
                    showFeedbackSuccessMessage();
                } else {
                    ApplicationLogger.logEvent(Events.FEEDBACK_FAILED,properties);
                    showFeedbackFailedMessage(response.message());
                }
            }

            @Override
            public void onFailure(Call<Feedback> call, Throwable t) {
                if (FeedbackFragment.this.mode == FeedbackActivity.Mode.General) {
                    ApplicationLogger.logError(Events.FEEDBACK_FAILED,t);
                } else {
                    ApplicationLogger.logError(Events.REPORT_PROBLEM_FAILED,t);
                }
                String message = t.getMessage();
                showFeedbackFailedMessage(message);
            }
        });
    }

    private void showFeedbackFailedMessage(String message) {
        String title = "Unable to send message";
        new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    private void showFeedbackSuccessMessage() {
        String title = "Message Sent";
        String message = "Your message has been sent.";
        new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().finish();
                    }
                }).show();
    }


    private ParabitFeedbackManager getParabitFeedbackManager() {
        return feedbackService;
    }
}

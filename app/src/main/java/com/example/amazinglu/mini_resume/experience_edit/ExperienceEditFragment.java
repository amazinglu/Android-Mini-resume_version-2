package com.example.amazinglu.mini_resume.experience_edit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.amazinglu.mini_resume.R;
import com.example.amazinglu.mini_resume.education_edit.EducationEditFragment;
import com.example.amazinglu.mini_resume.model.Experience;
import com.example.amazinglu.mini_resume.util.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class ExperienceEditFragment extends Fragment {

    public static final String KEY_EXPERIENCE_ACTION = "action";
    public static final String KEY_EXPERIENCE_ACTION_ADD = "action_add";
    public static final String KEY_EXPERIENCE_ACTION_EDIT = "action_edit";
    public static final String KEY_EXPERIENCE = "experience";
    public static final String KEY_EXPERIENCE_ID = "experience_id";

    public static final int REQ_EXP_DELETE_CONFIRM_CODE = 21;

    private static final int FRAG_START_DATE = 11;
    private static final int FRAG_END_DATE = 12;

    private Experience experience;
    private EditText companyName, workTitle, startDate, endDate, description;
    private Button delete;
    private int dateSetFrag;
    private String action;

    public static ExperienceEditFragment newInstance(Bundle args) {
        ExperienceEditFragment fragment = new ExperienceEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.experience_edit, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        bindView(view);

        action = getArguments().getString(KEY_EXPERIENCE_ACTION);
        if (action.equals(KEY_EXPERIENCE_ACTION_ADD)) {
            experience = new Experience();
            delete.setVisibility(View.GONE);
        } else {
            experience = getArguments().getParcelable(KEY_EXPERIENCE);
            // set the view
            companyName.setText(experience.companyName);
            workTitle.setText(experience.workTitle);
            startDate.setText(DateUtils.dateToString(experience.startDate));
            endDate.setText(DateUtils.dateToString(experience.endDate));
            description.setText(experience.experience_description);
            delete.setVisibility(View.VISIBLE);
        }
    }

    private void bindView(View view) {
        companyName = (EditText) view.findViewById(R.id.experience_edit_company_name);
        workTitle = (EditText) view.findViewById(R.id.experience_edit_work_title);
        startDate = (EditText) view.findViewById(R.id.experience_edit_start_date);
        endDate = (EditText) view.findViewById(R.id.experience_edit_end_date);
        description = (EditText) view.findViewById(R.id.experience_edit_description);
        delete = (Button) view.findViewById(R.id.experience_edit_delete);

        final DateSetListener dateSetListener = new DateSetListener();
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                if (action.equals(KEY_EXPERIENCE_ACTION_EDIT)) {
                    c.setTime(experience.startDate);
                }
                dateSetFrag = FRAG_START_DATE;
                Dialog dialog = new DatePickerDialog(getContext(), dateSetListener,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                if (action.equals(KEY_EXPERIENCE_ACTION_EDIT)) {
                    c.setTime(experience.endDate);
                }
                dateSetFrag = FRAG_END_DATE;
                Dialog dialog = new DatePickerDialog(getContext(), dateSetListener,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExperienceDeleteDialog dialog = ExperienceDeleteDialog.newInstance();
                dialog.setTargetFragment(ExperienceEditFragment.this, REQ_EXP_DELETE_CONFIRM_CODE);
                dialog.show(getFragmentManager(), ExperienceDeleteDialog.TAG);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_EXP_DELETE_CONFIRM_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra(KEY_EXPERIENCE_ID, experience.id);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menus_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.edit_save:
                saveAndExit();
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAndExit() {

        if (action.equals(KEY_EXPERIENCE_ACTION_EDIT)) {
            System.out.println("amazing");
        }
        experience.companyName = companyName.getText().toString();
        experience.workTitle = workTitle.getText().toString();
        experience.experience_description = description.getText().toString();

        Intent intent = new Intent();
        intent.putExtra(KEY_EXPERIENCE, experience);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    class DateSetListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            if (dateSetFrag == FRAG_START_DATE) {
                experience.startDate = c.getTime();
                startDate.setText(DateUtils.dateToString(experience.startDate));
            } else if (dateSetFrag == FRAG_END_DATE) {
                experience.endDate = c.getTime();
                endDate.setText(DateUtils.dateToString(experience.endDate));
            }
        }
    }
}

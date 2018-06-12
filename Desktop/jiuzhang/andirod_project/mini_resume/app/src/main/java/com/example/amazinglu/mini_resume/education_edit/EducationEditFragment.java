package com.example.amazinglu.mini_resume.education_edit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyboardShortcutGroup;
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
import com.example.amazinglu.mini_resume.model.Education;
import com.example.amazinglu.mini_resume.util.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EducationEditFragment extends Fragment {

    public static final String KEY_EDUCATION= "education";
    public static final String KEY_EDUCATION_ACTION= "education_edit_action";
    public static final String KEY_EDUCATION_ADD= "education_add";
    public static final String KEY_EDUCATION_EDIT= "education_edit";
    public static final String KEY_EDUCATION_ID= "education_id";

    public static final int REQ_EDU_DELETE_CONFIRM_CODE = 20;


    private static final int FLAG_START_DATE = 11;
    private static final int FLAG_END_DATE = 12;

    private int datePickFlag;

    private Education education;
    private EditText schoolName, major, startDate, endDate, Description, gpa;
    private Button delete;

    protected static EducationEditFragment newInstance(Bundle args) {
        EducationEditFragment educationEditFragment = new EducationEditFragment();
        educationEditFragment.setArguments(args);
        return educationEditFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 为了显示option menus里面的button, 这个一定要有
         * */
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.education_edit, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        schoolName = (EditText) view.findViewById(R.id.educatio_edit_name);
        major = (EditText) view.findViewById(R.id.educatio_edit_major);
        startDate = (EditText) view.findViewById(R.id.education_edit_start_date);
        endDate = (EditText) view.findViewById(R.id.education_edit_end_date);
        Description = (EditText) view.findViewById(R.id.education_edit_description);
        gpa = (EditText) view.findViewById(R.id.education_edit_gpa);
        delete = (Button) view.findViewById(R.id.education_edit_delete);

        final String action = getArguments().getString(KEY_EDUCATION_ACTION);

        if (action.equals(KEY_EDUCATION_EDIT)) {
            education = getArguments().getParcelable(KEY_EDUCATION);
            schoolName.setText(education.schoolName);
            major.setText(education.major);
            startDate.setText(DateUtils.dateToString(education.startDate));
            endDate.setText(DateUtils.dateToString(education.endDate));
            Description.setText(education.educationDescription);
            gpa.setText(Double.toString(education.gpa));
            delete.setVisibility(View.VISIBLE);
        } else {
            education = new Education();
            delete.setVisibility(View.GONE);
        }

        /**
         * how to choose a date using DatePickerDialog
         * use Calender to gerenate the time in Date format
         * use dialog to allow user to choose a date
         * onDateSet method in DatePickerDialog.OnDateSetListener will be call once user choose the date
         * and click "ok"
         * we can get the date user choose in this method
         * */
        // on click listener of start date and end date
        final DateSetListener dateSetListener = new DateSetListener();
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                if (action == KEY_EDUCATION_EDIT) {
                    c.setTime(education.startDate);
                }
                datePickFlag = FLAG_START_DATE;
                Dialog dialog = new DatePickerDialog(getActivity(), dateSetListener,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        // on click listener of end date
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                if (action == KEY_EDUCATION_EDIT) {
                    c.setTime(education.endDate);
                }
                datePickFlag = FLAG_END_DATE;
                Dialog dialog = new DatePickerDialog(getActivity(), dateSetListener,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        // on click listener of delete button
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EducationDeleteDialog dialog = EducationDeleteDialog.newInstance();
                dialog.setTargetFragment(EducationEditFragment.this, REQ_EDU_DELETE_CONFIRM_CODE);
                dialog.show(getFragmentManager(), EducationDeleteDialog.TAG);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_EDU_DELETE_CONFIRM_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra(KEY_EDUCATION_ID, education.id);
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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAndExit() {
        if (education == null) {
            education = new Education();
        }

        education.schoolName = schoolName.getText().toString();
        education.major = major.getText().toString();
        education.gpa = Double.parseDouble(gpa.getText().toString());
        education.educationDescription = Description.getText().toString();

        Intent intent = new Intent();
        intent.putExtra(KEY_EDUCATION, education);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    class DateSetListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            if (datePickFlag == FLAG_START_DATE) {
                education.startDate = c.getTime();
                startDate.setText(DateUtils.dateToString(education.startDate));
            } else if (datePickFlag == FLAG_END_DATE) {
                education.endDate = c.getTime();
                endDate.setText(DateUtils.dateToString(education.endDate));
            }
        }
    }
}

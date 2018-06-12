package com.example.amazinglu.mini_resume.main_list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.amazinglu.mini_resume.R;
import com.example.amazinglu.mini_resume.education_edit.EducationEditFragment;
import com.example.amazinglu.mini_resume.experience_edit.ExperienceEditFragment;
import com.example.amazinglu.mini_resume.model.Education;
import com.example.amazinglu.mini_resume.model.Experience;
import com.example.amazinglu.mini_resume.model.Project;
import com.example.amazinglu.mini_resume.model.Summary;
import com.example.amazinglu.mini_resume.summary_edit.SummaryEditFragment;
import com.example.amazinglu.mini_resume.util.ModelUtil;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainListFragment extends Fragment {

    public static final int REQ_CODE_EDUCATION_EDIT = 100;
    public static final int REQ_CODE_SUMMARY_EDIT = 101;
    public static final int REQ_CODE_EXPERIENCE_EDIT = 102;

    private static final String PREF_SUMMARY_KEY = "summary";
    private static final String PREF_EDUCATION_KEY = "educations";
    private static final String PREF_EXPERIENCE_KEY = "experience";
    private static final String PREF_PROJECT_KEY = "projects";

    private RecyclerView recyclerView;
    private MainListAdapter adapter;

    // data
    private List<Education> eduList;
    private List<Experience> expList;
    private List<Project> proList;
    private Summary summary;

    public static MainListFragment newInstance() {
        MainListFragment mainListFragment = new MainListFragment();
        return mainListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fregment_recycler_view, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.main_fragment_recycler_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        loadData();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        /**
         * key point 3:
         * 为什么可以在fragment中直接adapter.notifyDataSetChanged()
         * 因为在fragment中的data和adapter中的是一个地址的
         * 所以如果此时data是null的话，之后再new，两个地方的data就不是一个地址了，adapter是没有办法更新的
         * */
        adapter = new MainListAdapter(eduList, expList, proList, summary, getContext(),this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_EDUCATION_EDIT && resultCode == Activity.RESULT_OK) {
            String educationId = data.getStringExtra(EducationEditFragment.KEY_EDUCATION_ID);
            if (educationId != null) {
                deleteEducation(educationId);
            } else  {
                Education education = data.getParcelableExtra(EducationEditFragment.KEY_EDUCATION);
                updateEducation(education);
            }
        } else if (requestCode == REQ_CODE_SUMMARY_EDIT && resultCode == Activity.RESULT_OK) {
            Summary newSum = data.getParcelableExtra(SummaryEditFragment.KEY_SUMMRY);
            updateSummary(newSum);
        } else if (requestCode == REQ_CODE_EXPERIENCE_EDIT && resultCode == Activity.RESULT_OK) {
            String experienceId = data.getStringExtra(ExperienceEditFragment.KEY_EXPERIENCE_ID);
            if (experienceId != null) {
                deleteExperience(experienceId);
            } else {
                Experience experience = data.getParcelableExtra(ExperienceEditFragment.KEY_EXPERIENCE);
                updateExperience(experience);
            }
        }
    }

    private void loadData() {
        summary = ModelUtil.read(getContext(), PREF_SUMMARY_KEY, new TypeToken<Summary>(){});
        if (summary == null) {
            summary = new Summary();
        }
        eduList = ModelUtil.read(getContext(), PREF_EDUCATION_KEY, new TypeToken<List<Education>>(){});
        if (eduList == null) {
            eduList = new ArrayList<>();
        }
        expList = ModelUtil.read(getContext(), PREF_EXPERIENCE_KEY, new TypeToken<List<Experience>>(){});
        if (expList == null) {
            expList = new ArrayList<>();
        }
        proList = ModelUtil.read(getContext(), PREF_PROJECT_KEY, new TypeToken<List<Project>>(){});
        if (proList == null) {
            proList = new ArrayList<>();
        }
    }

    private void deleteExperience(String experienceId) {
        for (int i = 0; i < expList.size(); ++i) {
            if (expList.get(i).id.equals(experienceId)) {
                expList.remove(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }
        ModelUtil.save(getContext(), PREF_EXPERIENCE_KEY, expList);
    }

    private void updateSummary(Summary newSum) {
        summary.name = newSum.name;
        summary.email = newSum.email;
        summary.phoneNum = newSum.phoneNum;
        summary.userIamgeUri = newSum.userIamgeUri;
        adapter.notifyDataSetChanged();
        ModelUtil.save(getContext(), PREF_SUMMARY_KEY, summary);
    }

    private void deleteEducation(String id) {
        for (int i = 0; i < eduList.size(); ++i) {
            if (eduList.get(i).id.equals(id)) {
                eduList.remove(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }
        ModelUtil.save(getContext(), PREF_EDUCATION_KEY, eduList);
    }

    private void updateEducation(Education education) {
        boolean found = false;
        for (int i = 0; i < eduList.size(); ++i) {
            if (eduList.get(i).id.equals(education.id)) {
                found = true;
                eduList.set(i, education);
                adapter.notifyDataSetChanged();
                break;
            }
        }

        if (!found) {
            eduList.add(education);
            adapter.notifyDataSetChanged();
        }

        ModelUtil.save(getContext(), PREF_EDUCATION_KEY, eduList);
    }

    private void updateExperience(Experience experience) {
        boolean find = false;
        for (int i = 0; i < expList.size(); ++i) {
            if (expList.get(i).id.equals(experience.id)) {
                expList.set(i, experience);
                adapter.notifyDataSetChanged();
                find = true;
                break;
            }
        }

        if (!find) {
            expList.add(experience);
            adapter.notifyDataSetChanged();
        }

        ModelUtil.save(getContext(), PREF_EXPERIENCE_KEY, expList);
    }
}

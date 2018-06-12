package com.example.amazinglu.mini_resume.main_list;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.amazinglu.mini_resume.R;
import com.example.amazinglu.mini_resume.education_edit.EducationEditActivity;
import com.example.amazinglu.mini_resume.education_edit.EducationEditFragment;
import com.example.amazinglu.mini_resume.experience_edit.ExperienceEditActivity;
import com.example.amazinglu.mini_resume.experience_edit.ExperienceEditFragment;
import com.example.amazinglu.mini_resume.model.Education;
import com.example.amazinglu.mini_resume.model.Experience;
import com.example.amazinglu.mini_resume.model.Project;
import com.example.amazinglu.mini_resume.model.Summary;
import com.example.amazinglu.mini_resume.summary_edit.SummaryEditActivity;
import com.example.amazinglu.mini_resume.summary_edit.SummaryEditFragment;
import com.example.amazinglu.mini_resume.util.DateUtils;
import com.example.amazinglu.mini_resume.util.ImageUtil;

import java.util.List;

public class MainListAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SUMMARY = 1;
    private static final int VIEW_TYPE_EDUCATION = 2;
    private static final int VIEW_TYPE_EXPERIENCE = 3;
    private static final int VIEW_TYPE_PROJECT = 4;

    private static final String HEADER_EDUCATION = "EDUCATION";
    private static final String HEADER_EXPERIENCE = "EXPERIENCE";
    private static final String HEADER_PROJECT = "PROJECT";

    private List<Education> eduList;
    private List<Experience> expList;
    private List<Project> proList;
    private Summary summary;

    private Context context;
    private MainListFragment mainListFragment;

    public MainListAdapter(List<Education> eduList, List<Experience> expList,
                           List<Project> proList, Summary summary, Context context, MainListFragment fragment) {
        this.eduList = eduList;
        this.expList = expList;
        this.proList = proList;
        this.summary = summary;
        this.context = context;
        this.mainListFragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SUMMARY) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.summary_detail, parent, false);
            return new SummaryViewHolder(view);
        } else if (viewType == VIEW_TYPE_EDUCATION) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.education_detail, parent, false);
            return new EducationViewHolder(view);
        } else if (viewType == VIEW_TYPE_EXPERIENCE) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.experience_detail, parent, false);
            return new ExperienceViewHolder(view);
        } else if (viewType == VIEW_TYPE_PROJECT) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.project_detail, parent, false);
            return new ProjectViewHolder(view);
        } else {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.header, parent, false);
            return new TitleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_SUMMARY) {
            SummaryViewHolder summaryViewHolder = (SummaryViewHolder) holder;
            if (summary != null) {
                summaryViewHolder.userName.setText(summary.name);
                summaryViewHolder.userEmail.setText(summary.email);
                summaryViewHolder.userphoneNum.setText(summary.phoneNum);
                if (summary.userIamgeUri == null) {
                    summaryViewHolder.userImage.setImageResource(R.drawable.user_ghost);
                } else {
                    ImageUtil.loadImageLocal(getContext(), summary.userIamgeUri, summaryViewHolder.userImage);
                }
            }
            summaryViewHolder.summaryEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), SummaryEditActivity.class);
                    intent.putExtra(SummaryEditFragment.KEY_SUMMRY, summary);
                    mainListFragment.startActivityForResult(intent, MainListFragment.REQ_CODE_SUMMARY_EDIT);
                }
            });

        } else if (viewType == VIEW_TYPE_EDUCATION) {
            EducationViewHolder educationViewHolder = (EducationViewHolder) holder;
            int gap = 2;
            final Education eduData = eduList.get(position - gap);
            educationViewHolder.school_name.setText(eduData.schoolName);
            educationViewHolder.major.setText(eduData.major);
            educationViewHolder.gpa.setText(Double.toString(eduData.gpa));
            educationViewHolder.eduDescription.setText(eduData.educationDescription);
            educationViewHolder.educationDuration.setText(DateUtils.dateToString(eduData.startDate)
                    + " - " + DateUtils.dateToString(eduData.endDate));
            educationViewHolder.educationEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), EducationEditActivity.class);
                    intent.putExtra(EducationEditFragment.KEY_EDUCATION, eduData);
                    intent.putExtra(EducationEditFragment.KEY_EDUCATION_ACTION, EducationEditFragment.KEY_EDUCATION_EDIT);
                    mainListFragment.startActivityForResult(intent, MainListFragment.REQ_CODE_EDUCATION_EDIT);
                }
            });

        } else if (viewType == VIEW_TYPE_EXPERIENCE) {
            ExperienceViewHolder experienceViewHolder = (ExperienceViewHolder) holder;
            int gap = 3 + eduList.size();
            final Experience expData = expList.get(position - gap);
            experienceViewHolder.companyName.setText(expData.companyName);
            experienceViewHolder.workTitle.setText(expData.workTitle);
            experienceViewHolder.expDuration.setText(DateUtils.dateToString(expData.startDate) + " - "
                    + DateUtils.dateToString(expData.endDate));
            experienceViewHolder.expDescription.setText(expData.experience_description);
            experienceViewHolder.expEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ExperienceEditActivity.class);
                    intent.putExtra(ExperienceEditFragment.KEY_EXPERIENCE_ACTION,
                            ExperienceEditFragment.KEY_EXPERIENCE_ACTION_EDIT);
                    intent.putExtra(ExperienceEditFragment.KEY_EXPERIENCE, expData);
                    mainListFragment.startActivityForResult(intent, MainListFragment.REQ_CODE_EXPERIENCE_EDIT);
                }
            });
        } else if (viewType == VIEW_TYPE_PROJECT) {
            // TODO: the project edit interface and event handle
            ProjectViewHolder projectViewHolder = (ProjectViewHolder) holder;
            int gap = 4 + eduList.size() + expList.size();
            Project proData = proList.get(position - gap);
            projectViewHolder.projectName.setText(proData.projectName);
            projectViewHolder.proDescription.setText(proData.projectDescription);
            projectViewHolder.proDuration.setText(proData.startDate.toString() + proData.endDate.toString());
//            projectViewHolder.proUrl.setText(proData.projectUrl);
            // TODO: edit button
        } else {
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            if (position == 1) {
                titleViewHolder.headerName.setText(HEADER_EDUCATION);
                titleViewHolder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), EducationEditActivity.class);
                        intent.putExtra(EducationEditFragment.KEY_EDUCATION_ACTION, EducationEditFragment.KEY_EDUCATION_ADD);
                        mainListFragment.startActivityForResult(intent, MainListFragment.REQ_CODE_EDUCATION_EDIT);
                    }
                });
            } else if (position == 2 + eduList.size()) {
                titleViewHolder.headerName.setText(HEADER_EXPERIENCE);
                titleViewHolder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), ExperienceEditActivity.class);
                        intent.putExtra(ExperienceEditFragment.KEY_EXPERIENCE_ACTION,
                                ExperienceEditFragment.KEY_EXPERIENCE_ACTION_ADD);
                        mainListFragment.startActivityForResult(intent, MainListFragment.REQ_CODE_EXPERIENCE_EDIT);
                    }
                });
            } else {
                titleViewHolder.headerName.setText(HEADER_PROJECT);
                // TODO: Add button
            }
        }
    }

    @Override
    public int getItemCount() {
        // one sumary, 3 header and the size of the content
        return 1 + 3 + eduList.size() + expList.size() + proList.size();
    }

    /**
     * 因为这个程序的view type非常多，如何判断position和view type的关系非常关键
     * */
    @Override
    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
        if (position == 0) {
            return VIEW_TYPE_SUMMARY;
        } else if (position == 1 || position == 2 + eduList.size()
                || position == 3 + eduList.size() + expList.size()) {
            return VIEW_TYPE_HEADER;
        } else if (position >= 2 && position <= 1 + eduList.size()) {
            return VIEW_TYPE_EDUCATION;
        } else if (position >= 3 + eduList.size() && position <= 2 + eduList.size() + expList.size()) {
            return  VIEW_TYPE_EXPERIENCE;
        } else {
            return VIEW_TYPE_PROJECT;
        }
    }

    public Context getContext() {
        return context;
    }
}

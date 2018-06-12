package com.example.amazinglu.mini_resume.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.amazinglu.mini_resume.util.DateUtils;

import java.util.Date;
import java.util.UUID;

public class Experience implements Parcelable{
    public String id;
    public String companyName;
    public String workTitle;
    public String experience_description;
    public Date startDate, endDate;

    public Experience() {
        id = UUID.randomUUID().toString();
    }

    protected Experience(Parcel in) {
        id = in.readString();
        companyName = in.readString();
        workTitle = in.readString();
        startDate = DateUtils.stringToDate(in.readString());
        endDate = DateUtils.stringToDate(in.readString());
        experience_description = in.readString();
    }

    public static final Creator<Experience> CREATOR = new Creator<Experience>() {
        @Override
        public Experience createFromParcel(Parcel in) {
            return new Experience(in);
        }

        @Override
        public Experience[] newArray(int size) {
            return new Experience[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(companyName);
        parcel.writeString(workTitle);
        parcel.writeString(DateUtils.dateToString(startDate));
        parcel.writeString(DateUtils.dateToString(endDate));
        parcel.writeString(experience_description);
    }
}

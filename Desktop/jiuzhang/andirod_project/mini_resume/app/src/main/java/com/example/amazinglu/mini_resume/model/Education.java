package com.example.amazinglu.mini_resume.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.amazinglu.mini_resume.util.DateUtils;

import java.util.Date;
import java.util.UUID;

public class Education implements Parcelable{
    public String id;
    public String schoolName;
    public String major;
    public String educationDescription;
    public double gpa;
    public Date startDate, endDate;

    public Education() {
        id = UUID.randomUUID().toString();
    }

    protected Education(Parcel in) {
        id = in.readString();
        schoolName = in.readString();
        major = in.readString();
        startDate = DateUtils.stringToDate(in.readString());
        endDate = DateUtils.stringToDate(in.readString());
        educationDescription = in.readString();
        gpa = in.readDouble();
    }

    public static final Creator<Education> CREATOR = new Creator<Education>() {
        @Override
        public Education createFromParcel(Parcel in) {
            return new Education(in);
        }

        @Override
        public Education[] newArray(int size) {
            return new Education[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(schoolName);
        parcel.writeString(major);
        parcel.writeString(DateUtils.dateToString(startDate));
        parcel.writeString(DateUtils.dateToString(endDate));
        parcel.writeString(educationDescription);
        parcel.writeDouble(gpa);
    }
}

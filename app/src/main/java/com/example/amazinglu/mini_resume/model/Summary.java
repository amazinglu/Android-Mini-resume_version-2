package com.example.amazinglu.mini_resume.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Summary implements Parcelable{
    public String name;
    public String email;
    public String phoneNum;
    public Uri userIamgeUri;

    public Summary(){
        name = "";
        email = "";
        phoneNum = "";
    }

    protected Summary(Parcel in) {
        name = in.readString();
        email = in.readString();
        phoneNum = in.readString();
        // read Uri from parcelable
        userIamgeUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<Summary> CREATOR = new Creator<Summary>() {
        @Override
        public Summary createFromParcel(Parcel in) {
            return new Summary(in);
        }

        @Override
        public Summary[] newArray(int size) {
            return new Summary[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeString(phoneNum);
        // write Uri to parcelable
        parcel.writeParcelable(userIamgeUri, i);
    }
}

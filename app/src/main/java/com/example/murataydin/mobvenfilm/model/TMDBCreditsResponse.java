package com.example.murataydin.mobvenfilm.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class TMDBCreditsResponse implements Parcelable {
    @SerializedName("crew") private ArrayList<Crew> crew;


    public ArrayList<Crew> getCrew() {
        return crew;
    }

    public void setCrew(ArrayList<Crew> crew) {
        this.crew = crew;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.crew);
    }

    public TMDBCreditsResponse() {
    }

    protected TMDBCreditsResponse(Parcel in) {
        this.crew = in.createTypedArrayList(Crew.CREATOR);
    }

    public static final Creator<TMDBCreditsResponse> CREATOR = new Creator<TMDBCreditsResponse>() {
        @Override
        public TMDBCreditsResponse createFromParcel(Parcel source) {
            return new TMDBCreditsResponse(source);
        }

        @Override
        public TMDBCreditsResponse[] newArray(int size) {
            return new TMDBCreditsResponse[size];
        }
    };
}

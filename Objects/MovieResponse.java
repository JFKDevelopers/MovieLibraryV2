package jfkdevelopers.navdrawertestapp.Objects;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MovieResponse {
    @SerializedName("page")
    private int page;
    @SerializedName("results")
    private ArrayList<BasicMovie> results;
    @SerializedName("total_results")
    public int total_results;
    @SerializedName("total_pages")
    public int total_pages;

    public ArrayList<BasicMovie> getResults() {
        return results;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public int getPage() {
        return page;
    }
}
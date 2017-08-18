package jfkdevelopers.navdrawertestapp.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DBMovie implements Serializable, Parcelable {

    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("genres")
    private String genres;
    @SerializedName("id")
    private int id;
    @SerializedName("overview")
    private String overview;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("runtime")
    private int runtime;
    @SerializedName("tagline")
    private String tagline;
    @SerializedName("title")
    private String title;
    @SerializedName("director")
    private String director;
    @SerializedName("credits")
    private String credits;
    @SerializedName("user_rating")
    public float userRating;

    public DBMovie(String backdropPath, String genres, int id, String overview, String posterPath,
                   String releaseDate, int runtime, String tagline, String title, String director, String credits, float userRating) {
        this.backdropPath = backdropPath;
        this.genres = genres;
        this.id = id;
        this.overview = overview;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.tagline = tagline;
        this.title = title;
        this.director = director;
        this.credits = credits;
        this.userRating = userRating;
    }

    public DBMovie() {

    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public float getUserRating() {
        return userRating;
    }

    public void setUserRating(float userRating) {
        this.userRating = userRating;
    }

    public static Creator<DBMovie> getCREATOR() {
        return CREATOR;
    }

    @Override
    public String toString() {
        return "Movie{" +
                ", backdropPath='" + backdropPath + '\'' +
                ", genres=" + genres +
                ", id=" + id +
                ", overview='" + overview + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", runtime=" + runtime +
                ", tagline='" + tagline + '\'' +
                ", title='" + title + '\'' +
                ", userRating=" + userRating +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.backdropPath);
        dest.writeString(this.genres);
        dest.writeInt(this.id);
        dest.writeString(this.overview);
        dest.writeString(this.posterPath);
        dest.writeString(this.releaseDate);
        dest.writeInt(this.runtime);
        dest.writeString(this.tagline);
        dest.writeString(this.title);
        dest.writeString(this.credits);
        dest.writeFloat(this.userRating);
    }

    private DBMovie(Parcel in) {
        this.backdropPath = in.readString();
        this.genres = in.readString();
        this.id = in.readInt();
        this.overview = in.readString();
        this.posterPath = in.readString();
        this.releaseDate = in.readString();
        this.runtime = in.readInt();
        this.tagline = in.readString();
        this.title = in.readString();
        this.credits = in.readString();
        this.userRating = in.readFloat();
    }

    public static final Creator<DBMovie> CREATOR = new Creator<DBMovie>() {
        @Override
        public DBMovie createFromParcel(Parcel source) {
            return new DBMovie(source);
        }

        @Override
        public DBMovie[] newArray(int size) {
            return new DBMovie[size];
        }
    };
}
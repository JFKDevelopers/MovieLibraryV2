package jfkdevelopers.navdrawertestapp.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BasicMovie implements Serializable, Parcelable
{
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("adult")
    private boolean adult;
    @SerializedName("overview")
    private String overview;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("genre_ids")
    private List<Integer> genreIds;
    @SerializedName("id")
    private int id;
    @SerializedName("original_title")
    private String originalTitle;
    @SerializedName("original_language")
    private String originalLanguage;
    @SerializedName("title")
    private String title;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("popularity")
    private double popularity;
    @SerializedName("vote_count")
    private int voteCount;
    @SerializedName("video")
    private boolean video;
    @SerializedName("vote_average")
    private double voteAverage;
    @SerializedName("user_rating")
    private float userRating;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.posterPath);
        dest.writeByte(this.adult ? (byte) 1 : (byte) 0);
        dest.writeString(this.overview);
        dest.writeString(this.releaseDate);
        dest.writeList(this.genreIds);
        dest.writeInt(this.id);
        dest.writeString(this.originalTitle);
        dest.writeString(this.originalLanguage);
        dest.writeString(this.title);
        dest.writeString(this.backdropPath);
        dest.writeDouble(this.popularity);
        dest.writeInt(this.voteCount);
        dest.writeByte(this.video ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.voteAverage);
        dest.writeFloat(this.userRating);
    }

    public BasicMovie() {
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getPopularity() {
        return popularity;
    }

    public float getUserRating() {
        return userRating;
    }

    public void setUserRating(float userRating) {
        this.userRating = userRating;
    }

    protected BasicMovie(Parcel in) {
        this.posterPath = in.readString();
        this.adult = in.readByte() != 0;
        this.overview = in.readString();
        this.releaseDate = in.readString();
        this.genreIds = new ArrayList<>();
        in.readList(this.genreIds, Integer.class.getClassLoader());
        this.id = in.readInt();
        this.originalTitle = in.readString();
        this.originalLanguage = in.readString();
        this.title = in.readString();
        this.backdropPath = in.readString();
        this.popularity = in.readDouble();
        this.voteCount = in.readInt();
        this.video = in.readByte() != 0;
        this.voteAverage = in.readDouble();
        this.userRating = in.readFloat();
    }

    public static final Creator<BasicMovie> CREATOR = new Creator<BasicMovie>() {
        @Override
        public BasicMovie createFromParcel(Parcel source) {
            return new BasicMovie(source);
        }

        @Override
        public BasicMovie[] newArray(int size) {
            return new BasicMovie[size];
        }
    };

    public String toJsonString() {
        String genres = "[";
        for(Integer i:getGenreIds()){
            genres = genres+i+",";
        }
        genres = genres.length()>1?genres.substring(0,genres.length()-1):genres;
        genres = genres + "]";
        String poster = posterPath==null? "null":"\"\\"+posterPath.replace("\"","\\\"")+"\"";
        String plot = overview==null? "null":"\""+overview.replace("\"","\\\"")+"\"";
        String tmp =  "{" +
                "\"poster_path\":" + poster +
                ",\"adult\":" + adult +
                ",\"overview\":" + plot +
                ",\"release_date\":\"" + releaseDate + '\"' +
                ",\"genre_ids\":" + genres +
                ",\"id\":" + id +
                ",\"original_title\":\"" + originalTitle + '\"' +
                ",\"original_language\":\"" + originalLanguage + '\"' +
                ",\"title\":\"" + title + '\"' +
                ",\"backdrop_path\":\"\\" + backdropPath + '\"' +
                ",\"popularity\":" + popularity +
                ",\"vote_count\":" + voteCount +
                ",\"video\":" + video +
                ",\"vote_average\":" + voteAverage +
                '}';

        tmp = tmp.replace("\"null\"","null");
        tmp = tmp.replace("\"\\null\"","null");
        return tmp;
    }


}
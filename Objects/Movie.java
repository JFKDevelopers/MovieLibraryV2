package jfkdevelopers.navdrawertestapp.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Movie implements Serializable, Parcelable {

    @SerializedName("adult")
    private boolean adult;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("belongs_to_collection")
    private BelongsToCollection belongsToCollection;
    @SerializedName("budget")
    private long budget;
    @SerializedName("genres")
    private List<Genres> genres;
    @SerializedName("homepage")
    private String homepage;
    @SerializedName("id")
    private int id;
    @SerializedName("imdb_id")
    private String imdbId;
    @SerializedName("original_language")
    private String originalLanguage;
    @SerializedName("original_title")
    private String originalTitle;
    @SerializedName("overview")
    private String overview;
    @SerializedName("popularity")
    private double popularity;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("production_companies")
    private List<ProductionCompanies> productionCompanies;
    @SerializedName("production_countries")
    private List<ProductionCountries> productionCountries;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("revenue")
    private long revenue;
    @SerializedName("runtime")
    private int runtime;
    @SerializedName("spoken_languages")
    private List<SpokenLanguages> spokenLanguages;
    @SerializedName("status")
    private String status;
    @SerializedName("tagline")
    private String tagline;
    @SerializedName("title")
    private String title;
    @SerializedName("video")
    private boolean video;
    @SerializedName("vote_average")
    private double voteAverage;
    @SerializedName("vote_count")
    private int voteCount;
    @SerializedName("credits")
    private final Credits credits;
	
    public Movie(boolean adult, String backdropPath, BelongsToCollection belongsToCollection, int budget,
                 List<Genres> genres, String homepage, int id, String imdbId, String originalLanguage,
                 String originalTitle, String overview, double popularity, String posterPath,
                 List<ProductionCompanies> productionCompanies, List<ProductionCountries> productionCountries,
                 String releaseDate, int revenue, int runtime, List<SpokenLanguages> spokenLanguages,
                 String status, String tagline, String title, boolean video, double voteAverage, int voteCount, Credits credits) {
        this.adult = adult;
        this.backdropPath = backdropPath;
        this.belongsToCollection = belongsToCollection;
        this.budget = budget;
        this.genres = genres;
        this.homepage = homepage;
        this.id = id;
        this.imdbId = imdbId;
        this.originalLanguage = originalLanguage;
        this.originalTitle = originalTitle;
        this.overview = overview;
        this.popularity = popularity;
        this.posterPath = posterPath;
        this.productionCompanies = productionCompanies;
        this.productionCountries = productionCountries;
        this.releaseDate = releaseDate;
        this.revenue = revenue;
        this.runtime = runtime;
        this.spokenLanguages = spokenLanguages;
        this.status = status;
        this.tagline = tagline;
        this.title = title;
        this.video = video;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.credits = credits;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public List<Genres> getGenres() {
        return genres;
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

    public String getPosterPath() {
        return posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public String getTagline() {
        return tagline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Cast> getCast() {
        return credits.getCast();
    }

    private List<Crew> getCrew(){
        return credits.getCrew();
    }

    public String getDirector(){
        for(Crew c:getCrew()){
            if(c.job.equals("Director")){
                return c.name;
            }
        }
        return "N/A";
    }
    @Override
    public String toString() {
        return "Movie{" +
                "adult=" + adult +
                ", backdropPath='" + backdropPath + '\'' +
                ", belongsToCollection='" + belongsToCollection + '\'' +
                ", budget=" + budget +
                ", genres=" + genres +
                ", homepage='" + homepage + '\'' +
                ", id=" + id +
                ", imdbId='" + imdbId + '\'' +
                ", originalLanguage='" + originalLanguage + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", overview='" + overview + '\'' +
                ", popularity=" + popularity +
                ", posterPath='" + posterPath + '\'' +
                ", productionCompanies=" + productionCompanies +
                ", productionCountries=" + productionCountries +
                ", releaseDate='" + releaseDate + '\'' +
                ", revenue=" + revenue +
                ", runtime=" + runtime +
                ", spokenLanguages=" + spokenLanguages +
                ", status='" + status + '\'' +
                ", tagline='" + tagline + '\'' +
                ", title='" + title + '\'' +
                ", video=" + video +
                ", voteAverage=" + voteAverage +
                ", voteCount=" + voteCount +
                '}';
    }

    public String toJSONString(){
        Genres g = new Genres();
        ProductionCompanies p1 = new ProductionCompanies();
        ProductionCountries p2 = new ProductionCountries();
        SpokenLanguages s = new SpokenLanguages();

        String belongsToCollectionStr = this.belongsToCollection!=null?this.belongsToCollection.toString():"null";
        String genreStr = this.genres != null? g.toString(this.genres):"null";
        String prodCoStr = this.productionCompanies !=null ?  p1.toString(this.productionCompanies):"null";
        String prodCnStr = this.productionCountries !=null ?  p2.toString(this.productionCountries):"null";
        String spokenLngStr = this.spokenLanguages !=null ? s.toString(this.spokenLanguages):"null";
        String creditStr = this.credits !=null ? this.credits.toString():"null";
        String backdroppathStr = this.backdropPath !=null ? this.backdropPath.replace("\"","\\\""):"null";
        String out = "{\"adult\":"+this.adult+","+
                "\"backdrop_path\":\""+backdroppathStr+"\","+
                "\"belongs_to_collection\":"+belongsToCollectionStr+","+
                "\"budget\":"+this.budget+","+
                "\"genres\":"+genreStr+","+
                "\"homepage\":\""+this.homepage.replace("\"","\\\"")+"\","+
                "\"id\":"+this.id+","+
                "\"imdb_id\":\""+this.imdbId+"\","+
                "\"original_language\":\""+this.originalLanguage.replace("\"","\\\"")+"\","+
                "\"original_title\":\""+this.originalTitle.replace("\"","\\\"")+"\","+
                "\"overview\":\""+this.overview.replace("\"","\\\"")+"\","+
                "\"popularity\":"+this.popularity+","+
                "\"poster_path\":\""+this.posterPath.replace("\"","\\\"")+"\","+
                "\"production_companies\":"+prodCoStr+","+
                "\"production_countries\":"+prodCnStr+","+
                "\"release_date\":\""+this.releaseDate.replace("\"","\\\"")+"\","+
                "\"revenue\":"+this.revenue+","+
                "\"runtime\":"+this.runtime+","+
                "\"spoken_languages\":"+spokenLngStr+","+
                "\"status\":\""+this.status.replace("\"","\\\"")+"\","+
                "\"tagline\":\""+this.tagline.replace("\"","\\\"")+"\","+
                "\"title\":\""+this.title.replace("\"","\\\"")+"\","+
                "\"video\":"+this.video+","+
                "\"vote_average\":"+this.voteAverage+","+
                "\"vote_count\":"+this.voteCount+","+
                "\"credits\":"+creditStr+"}";
        out = out.replace("\"null\"","null");
        return out;
    }

    public static class BelongsToCollection implements Serializable{
        @SerializedName("id")
        public int id;
        @SerializedName("name")
        public String name;
        @SerializedName("poster_path")
        public String posterPath;
        @SerializedName("backdrop_path")
        public String backdropPath;
        @Override
        public String toString(){
            return "{"+"\"id\":"+this.id+","+
                    "\"name\":\""+this.name+"\","+
                    "\"poster_path\":\""+this.posterPath+"\","+
                    "\"backdrop_path\":\""+this.backdropPath+"\"}";
        }
    }

    public static class Genres implements Serializable{
        @SerializedName("id")
        public int id;
        @SerializedName("name")
        public String name;

        public String toString(List<Genres> genres){
            String out = "[";
            for(Genres g:genres){
                out = out + "{\"id\":"+g.id+","+
                        "\"name\":\""+g.name+"\"},";
            }
            if(genres.size()>0) return out.substring(0,out.length()-1) + "]";
            else return out + "]";
        }

    }

    public static class ProductionCompanies implements Serializable{
        @SerializedName("name")
        public String name;
        @SerializedName("id")
        public int id;

        public String toString(List<ProductionCompanies> pcs){
            String out = "[";
            for(ProductionCompanies p:pcs){
                out = out + "{\"name\":\""+p.name+"\","+
                        "\"id\":"+p.id+"},";
            }
            if (pcs.size()>0) return out.substring(0,out.length()-1) + "]";
            else return out + "]";
        }
    }

    public static class ProductionCountries implements Serializable{
        @SerializedName("iso_3166_1")
        public String iso31661;
        @SerializedName("name")
        public String name;

        public String toString(List<ProductionCountries> pcs){
            String out = "[";
            for(ProductionCountries p:pcs){
                out = out + "{\"iso_3166_1\":\""+p.iso31661+"\","+
                        "\"name\":\""+p.name+"\"},";
            }
            if (pcs.size()>0) return out.substring(0,out.length()-1) + "]";
            else return out + "]";
        }
    }

    public static class SpokenLanguages implements Serializable{
        @SerializedName("iso_639_1")
        public String iso6391;
        @SerializedName("name")
        public String name;

        public String toString(List<SpokenLanguages> sls){
            String out = "[";
            for(SpokenLanguages s:sls){
                out = out + "{\"iso_639_1\":\""+s.iso6391+"\","+
                        "\"name\":\""+s.name+"\"},";
            }

            if(sls.size()>0) return out.substring(0,out.length()-1) + "]";
            else return out + "]";
        }
    }

    public static class Cast implements Serializable{
        @SerializedName("cast_id")
        public int cast_id;
        @SerializedName("character")
        public String character;
        @SerializedName("credit_id")
        public String credit_id;
        @SerializedName("id")
        public int id;
        @SerializedName("name")
        public String name;
        @SerializedName("order")
        public int order;
        @SerializedName("profile_path")
        public String profile_path;
    }

    public static class Crew implements Serializable{
        @SerializedName("credit_id")
        public String credit_id;
        @SerializedName("department")
        public String department;
        @SerializedName("id")
        public int id;
        @SerializedName("job")
        public String job;
        @SerializedName("name")
        public String name;
        @SerializedName("profile_path")
        public String profile_path;
    }

    public static class Credits implements Serializable{
        @SerializedName("cast")
        public List<Cast> cast;
        @SerializedName("crew")
        public List<Crew> crew;

        public List<Cast> getCast() {
            return cast;
        }

        public List<Crew> getCrew() {
            return crew;
        }

        @Override
        public String toString(){
            String out = "{\"cast\":[";
            for(Cast c: cast){
                out = out + "{\"cast_id\":"+c.cast_id+","+
                        "\"character\":\""+c.character.replace("\"","\\\"")+"\","+
                        "\"credit_id\":\""+c.credit_id+"\","+
                        "\"id\":"+c.id+","+
                        "\"name\":\""+c.name+"\","+
                        "\"order\":"+c.order+","+
                        "\"profile_path\":\""+c.profile_path+"\"},";
            }

            if(cast.size()>0) out = out.substring(0,out.length()-1) + "],\"crew\":[";
            else out = out + "],";

            for(Crew c: crew){
                out = out + "{\"credit_id\":\""+c.credit_id+"\","+
                        "\"department\":\""+c.department+"\","+
                        "\"id\":"+c.id+","+
                        "\"job\":\""+c.job+"\","+
                        "\"name\":\""+c.name+"\","+
                        "\"profile_path\":\""+c.profile_path+"\"},";
            }
            if(cast.size()>0) out = out.substring(0,out.length()-1) + "]}";
            else out = out + "]}";
            return out;
        }
    }

    public static class MovieResult {
        private List<Movie> results;

        public List<Movie> getResults() {
            return results;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.adult ? (byte) 1 : (byte) 0);
        dest.writeString(this.backdropPath);
        dest.writeSerializable(this.belongsToCollection);
        dest.writeLong(this.budget);
        dest.writeList(this.genres);
        dest.writeString(this.homepage);
        dest.writeInt(this.id);
        dest.writeString(this.imdbId);
        dest.writeString(this.originalLanguage);
        dest.writeString(this.originalTitle);
        dest.writeString(this.overview);
        dest.writeDouble(this.popularity);
        dest.writeString(this.posterPath);
        dest.writeList(this.productionCompanies);
        dest.writeList(this.productionCountries);
        dest.writeString(this.releaseDate);
        dest.writeLong(this.revenue);
        dest.writeInt(this.runtime);
        dest.writeList(this.spokenLanguages);
        dest.writeString(this.status);
        dest.writeString(this.tagline);
        dest.writeString(this.title);
        dest.writeByte(this.video ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.voteAverage);
        dest.writeInt(this.voteCount);
        dest.writeSerializable(this.credits);
    }

    private Movie(Parcel in) {
        this.adult = in.readByte() != 0;
        this.backdropPath = in.readString();
        this.belongsToCollection = (BelongsToCollection) in.readSerializable();
        this.budget = in.readLong();
        this.genres = new ArrayList<>();
        in.readList(this.genres, Genres.class.getClassLoader());
        this.homepage = in.readString();
        this.id = in.readInt();
        this.imdbId = in.readString();
        this.originalLanguage = in.readString();
        this.originalTitle = in.readString();
        this.overview = in.readString();
        this.popularity = in.readDouble();
        this.posterPath = in.readString();
        this.productionCompanies = new ArrayList<>();
        in.readList(this.productionCompanies, ProductionCompanies.class.getClassLoader());
        this.productionCountries = new ArrayList<>();
        in.readList(this.productionCountries, ProductionCountries.class.getClassLoader());
        this.releaseDate = in.readString();
        this.revenue = in.readLong();
        this.runtime = in.readInt();
        this.spokenLanguages = new ArrayList<>();
        in.readList(this.spokenLanguages, SpokenLanguages.class.getClassLoader());
        this.status = in.readString();
        this.tagline = in.readString();
        this.title = in.readString();
        this.video = in.readByte() != 0;
        this.voteAverage = in.readDouble();
        this.voteCount = in.readInt();
        this.credits = (Credits) in.readSerializable();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
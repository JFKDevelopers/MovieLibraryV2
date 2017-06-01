package jfkdevelopers.navdrawertestapp.Interfaces;

import jfkdevelopers.navdrawertestapp.BuildConfig;
import jfkdevelopers.navdrawertestapp.Objects.Movie;
import jfkdevelopers.navdrawertestapp.Objects.MovieResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RestApi{
    String API_KEY = BuildConfig.API_KEY;
    @GET("movie/popular?api_key="+API_KEY+"&language=en-US")
    Call<MovieResponse> getPopularList(@Query("page") int page);

    @GET("movie/now_playing?api_key="+API_KEY+"&language=en-US")
    Call<MovieResponse> getNowPlayingList(@Query("page") int page);

    @GET("movie/{id}?api_key="+API_KEY+"&append_to_response=credits")
    Call<Movie> getMovie(@Path("id") int id);

    @GET("search/movie?api_key="+API_KEY+"&sort_by=popularity.desc")
    Call<MovieResponse> getSearchResults(@Query("query") String query,@Query("page") int page);
}
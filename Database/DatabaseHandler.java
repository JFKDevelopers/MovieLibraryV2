package jfkdevelopers.navdrawertestapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import jfkdevelopers.navdrawertestapp.Interfaces.RestApi;
import jfkdevelopers.navdrawertestapp.Objects.DBMovie;
import jfkdevelopers.navdrawertestapp.Objects.Movie;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DatabaseHandler extends SQLiteOpenHelper{
    //database version
    private static final int DATABASE_VERSION = 2;
    //database name
    private static final String DATABASE_NAME = "moviesManager";
    //table name
    private static final String TABLE_MOVIES = "movies";
    //table column names
	private static final String KEY_ID = "Id";
	private static final String KEY_USERRATING = "UserRating";
	private static final String KEY_TITLE = "Title";
	private static final String KEY_TAGLINE = "TagLine";
	private static final String KEY_POSTERPATH = "PosterPath";
	private static final String KEY_BACKDROPPATH = "BackdropPath";
	private static final String KEY_GENRES = "Genres";
	private static final String KEY_PLOT = "Plot";
	private static final String KEY_DIRECTOR = "Director";
	private static final String KEY_CAST = "Cast";
	private static final String KEY_RUNTIME = "Runtime";
	private static final String KEY_RELEASEDATE = "ReleaseDate";
    private static final String KEY_OWNED = "Owned";
    private static final String KEY_REVIEW = "Review";

    public DatabaseHandler(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    //creating tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("DB","onCreate");
        String CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_MOVIES + " ("
				+ KEY_ID + " INTEGER PRIMARY KEY, "
				+ KEY_USERRATING + " FLOAT, "
				+ KEY_TITLE + " STRING, "
				+ KEY_TAGLINE + " STRING, "
				+ KEY_POSTERPATH + " STRING, "
				+ KEY_BACKDROPPATH + " STRING, "
				+ KEY_GENRES + " STRING, "
				+ KEY_PLOT + " STRING, "
				+ KEY_DIRECTOR + " STRING, "
				+ KEY_CAST + " STRING, "
				+ KEY_RUNTIME + " INTEGER, "
				+ KEY_RELEASEDATE + " STRING, "
                + KEY_OWNED + " INTEGER, "
                + KEY_REVIEW + " TEXT" + ")";
				
        db.execSQL(CREATE_MOVIES_TABLE);
    }



    //CRUD (Create, Read, Update, Delete) Operations
    //add movie, downloads from TMDB the movie details and saves in database.
    public void addMovie(int id, final float rating){
        final SQLiteDatabase db = this.getWritableDatabase();
        final ContentValues values = new ContentValues();
        String BASE_URL = "http://api.themoviedb.org/3/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final RestApi service = retrofit.create(RestApi.class);
        Call<Movie> call = service.getMovie(id);
        call.enqueue(new Callback<Movie>(){
            @Override
            public void onResponse(Call<Movie> call, retrofit2.Response<Movie> response){
                Movie movie = response.body();
                String genres = "";
                try {
                    for (Movie.Genres g : movie.getGenres()) {
                        genres = genres + g.getName() + ", ";
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                if (genres.equals("")) genres = "N/A";
                String cast = "";
                try {
                    if (movie.getCast() != null) {
                        for (Movie.Cast c : movie.getCast()) {
                            String temp = String.format("%s  -  %s", c.name, c.character);
                            if (c.character.equals("")) temp = c.name;
                            cast = cast + temp + "\n";

                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                if (cast.equals("")) cast = "N/A";
                values.put(KEY_ID,movie.getId());
				values.put(KEY_USERRATING,rating);
				values.put(KEY_TITLE,movie.getTitle());
				values.put(KEY_TAGLINE,movie.getTagline());
				values.put(KEY_POSTERPATH,movie.getPosterPath());
				values.put(KEY_BACKDROPPATH,movie.getBackdropPath());
				values.put(KEY_GENRES,genres);
				values.put(KEY_PLOT,movie.getOverview());
				values.put(KEY_DIRECTOR,movie.getDirector());
				values.put(KEY_CAST,cast);
				values.put(KEY_RUNTIME,movie.getRuntime());
				values.put(KEY_RELEASEDATE,movie.getReleaseDate());
                values.put(KEY_OWNED,"false");
                values.put(KEY_REVIEW,"");
				db.insert(TABLE_MOVIES,null,values);
            }
            @Override
            public void onFailure(Call<Movie> call, Throwable t){
                t.printStackTrace();
            }
        });
    }

    //add new movie - used for the undo after deleting function.
	public void addMovie(int id, DBMovie movie, float rating){//TODO update this method, no longer using JSON string
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID,id);
        values.put(KEY_USERRATING,rating);
        values.put(KEY_TITLE,movie.getTitle());
        values.put(KEY_TAGLINE,movie.getTagline());
        values.put(KEY_POSTERPATH,movie.getPosterPath());
        values.put(KEY_BACKDROPPATH,movie.getBackdropPath());
        values.put(KEY_GENRES,movie.getGenres());
        values.put(KEY_PLOT,movie.getOverview());
        values.put(KEY_DIRECTOR,movie.getDirector());
        values.put(KEY_CAST,movie.getCredits());
        values.put(KEY_RUNTIME,movie.getRuntime());
        values.put(KEY_RELEASEDATE,movie.getReleaseDate());
        values.put(KEY_OWNED,"false");
        values.put(KEY_OWNED,"");
        //Inserting row
        db.insert(TABLE_MOVIES, null, values);
        db.close(); //closing database connection
    }

    //check if movie in table
    public boolean movieInTable(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MOVIES + " where Id = ?", new String[]{String.valueOf(id)});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
	/*public String getMovie(int id){
        Log.e("DB","querying DB for movie details");
        String selectQuery = "SELECT * FROM " + TABLE_MOVIES + " WHERE id="+id+"";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        if(cursor != null) {
            cursor.moveToFirst();
            String str = cursor.getString(1);
			Log.e("DB",str);
            cursor.close();
            return str;
        }
        else {
            cursor.close();
            return null;
        }
    }*/
    
	/* public String getStr(int id){
        Log.e("DB","querying DB for movie details");
        String selectQuery = "SELECT * FROM " + TABLE_MOVIES + " WHERE id="+id+"";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        String out = "";
        if(cursor!=null){
            cursor.moveToFirst();
            out = cursor.getString(1);
        }
        Log.e("DB","j"+out);
        return out;
    } */
	
   /*  public void getMovie(int id){
		Log.e("DB","querying DB for movie details");
        String selectQuery = "SELECT * FROM " + TABLE_MOVIES + " WHERE id="+id+"";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        Movie m = null;
        if(cursor != null) {
            Log.e("DB","rows returned - " + cursor.getCount());
            cursor.moveToFirst();
            //Log.e("DB",cursor.getString(1));
            try{
                if (cursor.getString(1).substring(0, 1).equals("{")) {
                    Gson gson = new Gson();
                    m = gson.fromJson(cursor.getString(1), Movie.class);
                }
            }catch(Exception e){
                Log.e("DB","{ was not first line 122" + e.getMessage());
            }
            cursor.close();
        }
        movie = m;
    } */

    public ArrayList<DBMovie> getAllMovies(){ //TODO update this method
        ArrayList<DBMovie> movieList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_MOVIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        //looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    DBMovie movie = new DBMovie(cursor.getString(5),cursor.getString(6),Integer.parseInt(cursor.getString(0)),cursor.getString(7),
                            cursor.getString(4),cursor.getString(11),Integer.parseInt(cursor.getString(10)),cursor.getString(3),
                            cursor.getString(2),cursor.getString(8),cursor.getString(9),Float.parseFloat(cursor.getString(1)));
                    movieList.add(movie);
                }catch(Exception e){
                    Log.e("DB","ERROR");
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return movieList;
    }

//    //updating single movie
//    public int updateMovie(Movie movie){
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(KEY_ID, movie.getId());
//
//        return db.update(TABLE_MOVIES, values, KEY_IMDBID + " = ?",
//                new String[] { movie.getImdbID()});
//    }

    public void rateMovie(int id, float userRating){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE movies SET userRating="+userRating+" WHERE Id="+id+"");
    }

    public void addNote(int id, String note){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE movies SET review=\""+note+"\" WHERE Id="+id+"";
        Log.e("this",query);
        db.execSQL(query);
    }

    //deleting single movie
    public void deleteMovie(DBMovie movie) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MOVIES, KEY_ID + " = ?",
                new String[] { Integer.toString(movie.getId())});
        db.close();
    }

    public void deleteAllMovies(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_MOVIES);
        db.close();
    }

    /*//getting movies count
    public int getMoviesCount() {
        String countQuery  = "SELECT  * FROM " + TABLE_MOVIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery,null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }*/

    public String getTableAsString(){
        //HashMap<String,Movie> movieList = new HashMap<String,Movie>();
        String selectQuery = "SELECT  * FROM " + TABLE_MOVIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String tableString = String.format("Table %s:\n",TABLE_MOVIES);
        //looping through all rows and adding to list
        if(cursor.moveToFirst()) {
            String[] columnNames = cursor.getColumnNames();
            do{
                for(String name:columnNames){
                    if(!name.equals("Cast"))
                        tableString+=String.format("%s: %s\n",name,cursor.getString(cursor.getColumnIndex(name)));
                }
                tableString+="\n";
            }while(cursor.moveToNext());
        }
        cursor.close();
        return tableString;
    }
    public float getRating(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT UserRating FROM movies WHERE Id="+id;
        float userRating = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                userRating = cursor.getFloat(cursor.getColumnIndex("UserRating"));
            }
            return userRating;
        }finally{
            if(cursor!=null) cursor.close();
        }
    }

    public DBMovie getMovie(int id){
        String selectQuery = "SELECT * FROM " + TABLE_MOVIES + " WHERE id="+id+"";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        DBMovie m = null;
        if(cursor != null) {
            cursor.moveToFirst();
            try{
                    m = new DBMovie(cursor.getString(5),cursor.getString(6),Integer.parseInt(cursor.getString(0)),cursor.getString(7),
                        cursor.getString(4),cursor.getString(11),Integer.parseInt(cursor.getString(10)),cursor.getString(3),
                        cursor.getString(2),cursor.getString(8),cursor.getString(9),Float.parseFloat(cursor.getString(1)));
            }catch(Exception e){
                e.printStackTrace();
            }
            cursor.close();
        }
        return m;
    }

    public String getNote(int id){
        String selectQuery = "SELECT "+ KEY_REVIEW + " FROM " + TABLE_MOVIES + " WHERE id="+id+"";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        String s = "";
        if(cursor!=null){
            cursor.moveToFirst();
            try{
                s = cursor.getString(0);
            }catch(Exception e){
                e.printStackTrace();
            }
            cursor.close();
        }
        return s;
    }
    //upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        if (newVersion>oldVersion){
            db.execSQL("ALTER TABLE " + TABLE_MOVIES + " ADD COLUMN " + KEY_OWNED + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_MOVIES + " ADD COLUMN " + KEY_REVIEW + " TEXT DEFAULT ''");
        }
    }

    public int getDBSize() {
        String countQuery = "SELECT  * FROM " + TABLE_MOVIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

}
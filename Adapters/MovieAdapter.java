package jfkdevelopers.navdrawertestapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jfkdevelopers.navdrawertestapp.Activities.DetailActivity;
import jfkdevelopers.navdrawertestapp.Activities.MainActivity;
import jfkdevelopers.navdrawertestapp.Activities.SearchActivity;
import jfkdevelopers.navdrawertestapp.Database.DatabaseHandler;
import jfkdevelopers.navdrawertestapp.Fragments.MovieFragment;
import jfkdevelopers.navdrawertestapp.Fragments.NowPlayingFragment;
import jfkdevelopers.navdrawertestapp.Fragments.PopularFragment;
import jfkdevelopers.navdrawertestapp.Objects.BasicMovie;
import jfkdevelopers.navdrawertestapp.Objects.Movie;
import jfkdevelopers.navdrawertestapp.R;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{
    public final static String SER_KEY = "com.jfkdevelopers.navdrawertestapp.ser";
    private static final int PENDING_REMOVAL_TIMEOUT = 3000; //3 sec
    private List<BasicMovie> movies;
    private final List<BasicMovie> moviesPendingRemoval;
    private final Context context;
    private final SparseArray<BasicMovie> movieMap = new SparseArray<>();
    private final SparseArray<String> genreMap = new SparseArray<>();
    private final Fragment fragment;
    DatabaseHandler db;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private List<Integer> movieIds;
    public MovieAdapter(Context context, ArrayList<BasicMovie> movies, @Nullable Fragment fragment) {
        this.movies = movies;
        this.context = context;
        this.moviesPendingRemoval = new ArrayList<>();
        this.fragment = fragment;
        db = new DatabaseHandler(context);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final ImageView movieImage;
        final TextView movieTitle;
        final TextView movieYear;
        final TextView movieRating;
        final TextView movieGenre;
        final TextView movieUserRating;
        final ImageView star;
        final ImageButton addBtn;
        public int id = -1;
        public ViewHolder(View v){
            super(v);
            movieImage = (ImageView) v.findViewById(R.id.cover);
            movieImage.setOnClickListener(this);
            movieTitle = (TextView) v.findViewById(R.id.title);
            movieTitle.setOnClickListener(this);
            movieYear = (TextView) v.findViewById(R.id.year);
            movieYear.setOnClickListener(this);
            movieRating = (TextView) v.findViewById(R.id.rating);
            movieRating.setOnClickListener(this);
            movieGenre = (TextView) v.findViewById(R.id.genre);
            movieGenre.setOnClickListener(this);
            movieUserRating = (TextView) v.findViewById(R.id.userStarRating);
            movieUserRating.setOnClickListener(this);
            star = (ImageView) v.findViewById(R.id.starImg);
            star.setOnClickListener(this);
            addBtn = (ImageButton) v.findViewById(R.id.addButton);
            if(context instanceof SearchActivity){
                addBtn.setVisibility(View.VISIBLE);
                addBtn.setOnClickListener(this);
            }
            else if(!((MainActivity)context).getCURRENT_TAG().equals("MovieFragment")){
				addBtn.setVisibility(View.VISIBLE);
				addBtn.setOnClickListener(this);
			}
        }
        @Override
        public void onClick(View view){
            //Integer position = (Integer) view.getTag();
            switch(view.getId()) {
                case R.id.addButton:
                    if(context instanceof SearchActivity){
                        ((SearchActivity)context).addToSelected(movieMap.get(id));
                        removeAt(getAdapterPosition());
                    }
                    else if(fragment instanceof PopularFragment){
                        BasicMovie m = movies.get(getAdapterPosition());
                        ((PopularFragment) fragment).addMovie(m);
                        removeAt(getAdapterPosition());
                    }
                    else if(fragment instanceof NowPlayingFragment){
                        BasicMovie m = movies.get(getAdapterPosition());
                        ((NowPlayingFragment) fragment).addMovie(m);
                        removeAt(getAdapterPosition());
                    }
                    break;
				default:
					Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("id",id);
                    if(fragment instanceof MovieFragment)
                        intent.putExtra("src",1);
                    context.startActivity(intent);
					break;
            }
        }
    }
    private void removeAt(int position){
        movies.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,movies.size());
    }

    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        final BasicMovie movie = movies.get(position);
        try {
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                    .placeholder(R.drawable.filmstrip)
                    .error(R.drawable.filmstrip)
                    .into(holder.movieImage);
            holder.movieTitle.setText(movie.getTitle());
            holder.movieRating.setText("");
            if(movie.getReleaseDate().length()>=4) holder.movieYear.setText(movie.getReleaseDate().substring(0,4));
            else holder.movieYear.setText("n/a");
            setGenres();
            String genres = "";
            for(Integer i:movie.getGenreIds()){
                genres = genres + genreMap.get(i) + ", ";
            }
            if(genres.length()>2) genres = genres.substring(0,genres.length()-2);
            holder.movieGenre.setText(genres);
            holder.id = movie.getId();
            final String userRatingStr = Float.toString(db.getRating(movie.getId()));
            holder.movieUserRating.setText(userRatingStr);
            movieMap.put(movie.getId(),movie);

            if(!(context instanceof MainActivity)){
                holder.movieUserRating.setVisibility(View.INVISIBLE);
                holder.star.setVisibility(View.INVISIBLE);
                holder.addBtn.setVisibility(View.VISIBLE);
            }
            else if(((MainActivity)context).getCURRENT_TAG().equals("MovieFragment")) {
                holder.movieUserRating.setVisibility(View.VISIBLE);
                holder.star.setVisibility(View.VISIBLE);
                holder.addBtn.setVisibility(View.INVISIBLE);
            }
            else{
                holder.movieUserRating.setVisibility(View.INVISIBLE);
                holder.star.setVisibility(View.INVISIBLE);
                holder.addBtn.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount(){
        return movies.size();
    }

    public void remove(final int position, final DatabaseHandler db, final RecyclerView rv){

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        movieIds = new ArrayList<>();

        final BasicMovie movie = movies.get(position);
        if(!moviesPendingRemoval.contains(movie)){
            moviesPendingRemoval.add(movie);
        }
        if(movies.contains(movie) && fragment instanceof MovieFragment){
            Snackbar snackbar;
            if(moviesPendingRemoval.size()==1)
                snackbar = Snackbar.make(rv, movies.get(position).getTitle() + " deleted", 5000);
            else
                snackbar = Snackbar.make(rv, moviesPendingRemoval.size() + " movies deleted", 5000);

                    snackbar.setAction(context.getString(R.string.undo),new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            for(BasicMovie m : moviesPendingRemoval) {
                                movies.add(position, m);
                                db.addMovie(m.getId(), m.toJsonString());
                                notifyDataSetChanged();
                                rv.scrollToPosition(position);
                            }
                            moviesPendingRemoval.clear();
                            ((MovieFragment) fragment).sortMovies(((MovieFragment) fragment).getSortPref());
                        }
                    });
            snackbar.show();

            db.deleteMovie(movies.get(position));
            movies.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateList(List<BasicMovie> mList){
        movies = mList;
        notifyDataSetChanged();
    }

    private void setGenres(){
        genreMap.put(28,"Action");
        genreMap.put(12,"Adventure");
        genreMap.put(16,"Animation");
        genreMap.put(35,"Comedy");
        genreMap.put(80,"Crime");
        genreMap.put(99,"Documentary");
        genreMap.put(18,"Drama");
        genreMap.put(10751,"Family");
        genreMap.put(14,"Fantasy");
        genreMap.put(36,"History");
        genreMap.put(27,"Horror");
        genreMap.put(10402,"Music");
        genreMap.put(9648,"Mystery");
        genreMap.put(10749,"Romance");
        genreMap.put(878,"Science Fiction");
        genreMap.put(10770,"TV Movie");
        genreMap.put(53,"Thriller");
        genreMap.put(10752,"War");
        genreMap.put(37,"Western");
    }

    /*
    public MovieAdapter(){
        this.moviesPendingRemoval = new ArrayList<>();
    }

       public void setClickListener(ItemClickListener itemClickListener){
        this.clickListener = itemClickListener;
    }

      private void setAnimation(View viewToAnimate, int position){
        if(position>lastPosition){
            Animation animation = AnimationUtils.loadAnimation(context,android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
    */
}
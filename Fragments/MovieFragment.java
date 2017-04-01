package jfkdevelopers.navdrawertestapp.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jfkdevelopers.navdrawertestapp.Adapters.MovieAdapter;
import jfkdevelopers.navdrawertestapp.Database.DatabaseHandler;
import jfkdevelopers.navdrawertestapp.Objects.BasicMovie;
import jfkdevelopers.navdrawertestapp.R;

public class MovieFragment extends Fragment implements SearchView.OnQueryTextListener{
    private SharedPreferences sharedPreferences;
    private static final String MyPREFERENCES = "MyPrefs";
    private final CharSequence[] sortOptions = {"Title A-Z","Title Z-A","Year"};
    private int sortPref;

    // TODO: Customize parameters
    //int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private static final String TAG = "MovieFragment";
    private ArrayList<BasicMovie> movies;
    private MovieAdapter mAdapter;
    private RecyclerView rv;
    private DatabaseHandler db;
    public MovieFragment() {}
    /*Context mContext;*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movies = getArguments().getParcelableArrayList("movies");
        mAdapter = new MovieAdapter(getActivity(),movies, this);
        db = new DatabaseHandler(getActivity());
        setHasOptionsMenu(true);
        sharedPreferences = getActivity().getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        sortPref = sharedPreferences.getInt("sortPref",0);
        if(movies.size()>1) sortMovies(sortPref);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        view.setTag(TAG);
        Context context = view.getContext();
        rv = (RecyclerView) view.findViewById(R.id.movieRecyclerView);
        /*if (mColumnCount <= 1) {*/
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            rv.setLayoutManager(layoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),layoutManager.getOrientation());
            rv.addItemDecoration(dividerItemDecoration);
       /* } else {
            rv.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }*/

        mAdapter = new MovieAdapter(context, movies, this);
        rv.setAdapter(mAdapter);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
        final SearchView sv = new SearchView(getActivity());
        sv.setQueryHint(getActivity().getResources().getString(R.string.search_hint));
        sv.setOnQueryTextListener(this);
        menu.add("Search").setIcon(R.drawable.magnify)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        MenuItemCompat.setOnActionExpandListener(menu.findItem(0), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                setItemsVisibility(menu);
                sv.setMaxWidth(Integer.MAX_VALUE);
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    sv.setBackgroundColor(getResources().getColor(android.R.color.white,getActivity().getTheme()));
                else
                    sv.setBackgroundColor(getResources().getColor(android.R.color.white));
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onCreateOptionsMenu(menu,inflater);
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    sv.setBackgroundColor(getResources().getColor(R.color.colorPrimary,getActivity().getTheme()));
                else
                    sv.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                return true;
            }
        });
        menu.getItem(0).setActionView(sv);
    }

    private void setItemsVisibility(Menu menu){
        for(int i=0;i<menu.size();i++){
            menu.getItem(i).setVisible(false);
        }
    }

    @Override
    public boolean onQueryTextChange(String filterTxt){
        final ArrayList<BasicMovie> filteredList = filter(filterTxt);
        mAdapter.updateList(filteredList);
        /*mAdapter.notifyDataSetChanged();*/
        return true;
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {

        void onListFragmentInteraction(Uri uri);
    }

    public static String getFragTag(){
        return TAG;
    }

    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                xMark = ContextCompat.getDrawable(getActivity(), R.drawable.delete);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) getActivity().getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                //int position = viewHolder.getAdapterPosition();
                //MovieAdapter movieAdapter = (MovieAdapter) rv.getAdapter();
                /*if (movieAdapter.isUndoOn() && movieAdapter.isPendingRemoval(position)) {
                    return 0;
                }*/
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                MovieAdapter adapter = (MovieAdapter) rv.getAdapter();
                adapter.remove(swipedPosition,db,rv);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                if (viewHolder.getAdapterPosition() == -1) {
                    return;
                }
                if (!initiated) {
                    init();
                }
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicHeight();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemtouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemtouchHelper.attachToRecyclerView(rv);
    }

    private void setUpAnimationDecoratorHelper() {
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                if (!initiated) init();
                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {
                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle
                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;
                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();
                    // this we need to find out
                    int top = 0;
                    int bottom = 0;
                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }
                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }
                    background.setBounds(left, top, right, bottom);
                    background.draw(c);
                }
                super.onDraw(c, parent, state);
            }
        });
    }
    private ArrayList<BasicMovie> filter(String filter){
        ArrayList<BasicMovie> temp = new ArrayList<>();
        filter = filter.toUpperCase();
        for(BasicMovie m:movies){
            if(m.getTitle().toUpperCase().contains(filter)||m.getReleaseDate().toUpperCase().substring(0,4).contains(filter))
                temp.add(m);
        }
        return temp;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort) {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            final AlertDialog sortDialog;
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Sort By");

            dialog.setSingleChoiceItems(sortOptions,sortPref,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int item){
                    switch(item){
                        case 0: //Sort A-Z
                            Collections.sort(movies,new Comparator<BasicMovie>(){
                                @Override
                                public int compare(BasicMovie m1, BasicMovie m2){
                                    return m1.getTitle().compareTo(m2.getTitle());
                                }
                            });
                            editor.putInt("sortPref",0);
                            sortPref = 0;
                            break;
                        case 1: //Sort Z-A
                            Collections.sort(movies,new Comparator<BasicMovie>(){
                                @Override
                                public int compare(BasicMovie m1, BasicMovie m2){
                                    return m2.getTitle().compareTo(m1.getTitle());
                                }
                            });
                            editor.putInt("sortPref",1);
                            sortPref = 1;
                            break;
                        case 2: //Sort by year
                            Collections.sort(movies,new Comparator<BasicMovie>(){
                                @Override
                                public int compare(BasicMovie m1, BasicMovie m2){
                                    String y1 = m1.getReleaseDate().substring(0,4);
                                    String y2 = m2.getReleaseDate().substring(0,4);
                                    if(!y1.equals(y2)) return y1.compareTo(y2);
                                    return y1.compareTo(y2);
                                }
                            });
                            editor.putInt("sortPref",2);
                            sortPref = 2;
                            break;
                        default:
                            break;
                    }
                    editor.apply();
                    dialog.dismiss();
                    db.deleteAllMovies();
                    for(BasicMovie m:movies){
                        db.addMovie(m.getId(),m.toJsonString());
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
            sortDialog = dialog.create();
            sortDialog.show();
        }
        else if(id==R.id.action_deleteAll){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);
            builder.setTitle(R.string.confirmDialog);
            builder.setPositiveButton(R.string.confirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            movies.clear();
                            db.deleteAllMovies();
                            mAdapter.notifyDataSetChanged();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            mAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    public void sortMovies(int item){
        switch(item){
            case 0: //Sort A-Z
                Collections.sort(movies,new Comparator<BasicMovie>(){
                    @Override
                    public int compare(BasicMovie m1, BasicMovie m2){
                        return m1.getTitle().compareTo(m2.getTitle());
                    }
                });
                break;
            case 1: //Sort Z-A
                Collections.sort(movies,new Comparator<BasicMovie>(){
                    @Override
                    public int compare(BasicMovie m1, BasicMovie m2){
                        return m2.getTitle().compareTo(m1.getTitle());
                    }
                });
                break;
            case 2: //Sort by year
                Collections.sort(movies,new Comparator<BasicMovie>(){
                    @Override
                    public int compare(BasicMovie m1, BasicMovie m2){
                        String y1 = m1.getReleaseDate().substring(0,4);
                        String y2 = m2.getReleaseDate().substring(0,4);
                        if(!y1.equals(y2)) return y1.compareTo(y2);
                        return y1.compareTo(y2);
                    }
                });
                break;
            default:
                break;
        }
    }
    public int getSortPref(){
        return sortPref;
    }
}

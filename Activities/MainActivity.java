package jfkdevelopers.navdrawertestapp.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import jfkdevelopers.navdrawertestapp.Adapters.MovieAdapter;
import jfkdevelopers.navdrawertestapp.Database.DatabaseHandler;
import jfkdevelopers.navdrawertestapp.Fragments.MovieFragment;
import jfkdevelopers.navdrawertestapp.Fragments.NowPlayingFragment;
import jfkdevelopers.navdrawertestapp.Fragments.PopularFragment;
import jfkdevelopers.navdrawertestapp.Objects.DBMovie;
import jfkdevelopers.navdrawertestapp.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MovieFragment.OnListFragmentInteractionListener, PopularFragment.OnListFragmentInteractionListener,
        NowPlayingFragment.OnListFragmentInteractionListener{

    private static final String EXTRA_MESSAGE = "com.jfkdevelopers.navdrawertestapp.MESSAGE";
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private String CURRENT_TAG = MovieFragment.getFragTag();
    private int navItemIndex=0;
    private final Context context = this;
    private DatabaseHandler db;
    private ArrayList<DBMovie> movies;
    private final ArrayList<Integer> backStackIndex = new ArrayList<>();
    private boolean doubleBackToExitPressedOnce = false;
	private Bundle instanceState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		instanceState = savedInstanceState;
		
        db = new DatabaseHandler(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog searchDialog;
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(R.string.searchDialogTitle);
                final EditText input = new EditText(context);
                input.setHint(R.string.searchDialogHint);
                dialog.setView(input);
                dialog.setPositiveButton(R.string.search_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface di, int item) {
                        String message = input.getText().toString().trim();
                        if (message.length() >= 1) {
                            if (connectedToNetwork()) {
                                Intent intent = new Intent(context, SearchActivity.class);
                                intent.putExtra(EXTRA_MESSAGE, message);
                                startActivity(intent);
                            } else Toast.makeText(getApplicationContext(),
                                    R.string.noInternetMessage,
                                    Toast.LENGTH_LONG)
                                    .show();
                        } else Toast.makeText(getApplicationContext(),
                                R.string.noTitleError,
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
                dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface di, int which) {
                        di.cancel();
                    }
                });
                searchDialog = dialog.create();
                searchDialog.show();
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
          @Override
            public void onDrawerOpened(View drawerView){
              super.onDrawerOpened(drawerView);
              InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Context.INPUT_METHOD_SERVICE);
              inputMethodManager.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);
          }
        @Override
            public void onDrawerClosed(View drawerView){
                super.onDrawerClosed(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (instanceState==null)
            navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume(); // Always call the superclass method first
        if(instanceState==null){
			if(navItemIndex==0) {
				//Open the movie fragment
				openFrag(0);
			}
			else {
				loadFragment(true);
			}
		}
    }

    private void openFrag(int id){
        if (id == R.id.nav_movies || id == 0){
            navItemIndex = 0;
            if(movies==null || movies.size()!=db.getDBSize()) {
                getData();
            }
            CURRENT_TAG = MovieFragment.getFragTag();
            loadFragment(false);
        } else if (id == R.id.nav_popular || id == 1) {
            if(navItemIndex!=1) {
                navItemIndex = 1;
                CURRENT_TAG = PopularFragment.getFragTag();
                loadFragment(false);
            }
        } else if (id == R.id.nav_nowplaying || id == 2) {
            if(navItemIndex!=2) {
                navItemIndex = 2;
                CURRENT_TAG = NowPlayingFragment.getFragTag();
                loadFragment(false);
            }
        }
    }

    private void loadFragment(final boolean onResume){
        if(navItemIndex==0){
            fab.show();
        }
        else{
            fab.hide();
        }
        Handler mHandler = new Handler();
        Runnable mPendingRunnable = new Runnable(){
            @Override
            public void run(){
                Fragment fragment = getFragment();
                Bundle bundle = new Bundle();
                if(navItemIndex==0){
                    bundle.putParcelableArrayList("movies",movies);
                    fragment.setArguments(bundle);
                    String title = (String) getResources().getText(R.string.title_main);
                    title+=movies!=null? " (" + movies.size() + ")" : "";
                    toolbar.setTitle(title);
                }
                else if(navItemIndex==1){
                    toolbar.setTitle(R.string.popular);
                }
                else if(navItemIndex==2){
                    toolbar.setTitle(R.string.now_playing);
                }
                if (!onResume) backStackIndex.add(navItemIndex);
                if(navItemIndex>=0) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                    //fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        };

        mHandler.post(mPendingRunnable);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(navItemIndex==0) {
            fab.hide();
            if (doubleBackToExitPressedOnce) {
                finish();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            View view = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(view, "Click back again to exit", 2000);
            View sbView = snackbar.getView();
            sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            snackbar.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                    fab.show();
                }
            }, 2000);
        }
        else if(backStackIndex.size()>1) {
            backStackIndex.remove(backStackIndex.size()-1);
            openFrag(backStackIndex.get(backStackIndex.size()-1));
            backStackIndex.remove(backStackIndex.size()-1);
        }
        else{
            fab.hide();
            if (doubleBackToExitPressedOnce) {
                finish();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            View view = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(view,"Click back again to exit",2000);
            View sbView = snackbar.getView();
            sbView.setBackgroundColor(ContextCompat.getColor(context,R.color.colorAccent));
            snackbar.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                    fab.show();
                }
            }, 2000);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        int count = 0;
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                @SuppressWarnings("unchecked")
                ArrayList<DBMovie> moviesToAdd = (ArrayList<DBMovie>) data.getSerializableExtra(MovieAdapter.SER_KEY);
                for(DBMovie m: moviesToAdd){
                    if(!db.movieInTable(m.getId())) {
                        movies.add(m);
                        db.addMovie(m.getId(), 0);
                        count++;
                    }
                }
                Snackbar.make(findViewById(android.R.id.content),count+" movies added",Snackbar.LENGTH_LONG)
                        .show();
                loadFragment(false);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        openFrag(id);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Fragment getFragment() {
        switch (navItemIndex) {
            case 0:
                return new MovieFragment();
            case 1:
                return new PopularFragment();
            case 2:
                return new NowPlayingFragment();
            default:
                return new MovieFragment();
        }
    }



    @Override
    public void onListFragmentInteraction(Uri uri) {
    }

    private boolean connectedToNetwork() {
        boolean connected = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ||
                    activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                connected = true;
            }
        }
        return connected;
    }

    public String getCURRENT_TAG(){
        return CURRENT_TAG;
    }

    private void getData(){
        movies = db.getAllMovies();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String str: children) {
                boolean success = deleteDir(new File(dir, str));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }else {
            return dir != null && dir.isFile() && dir.delete();
        }
    }

    @Override
    public void onDestroy(){
        deleteCache(this);
        super.onDestroy();
    }

    @Override
    public void onPause(){
        super.onPause();
    }
}
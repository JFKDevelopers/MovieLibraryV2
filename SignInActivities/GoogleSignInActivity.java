package jfkdevelopers.navdrawertestapp.SignInActivities;

/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import jfkdevelopers.navdrawertestapp.BuildConfig;
import jfkdevelopers.navdrawertestapp.Database.DatabaseHandler;
import jfkdevelopers.navdrawertestapp.Objects.DBMovie;
import jfkdevelopers.navdrawertestapp.Objects.User;
import jfkdevelopers.navdrawertestapp.R;

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
public class GoogleSignInActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private final Context context = this;
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private final String ADMOB_KEY = BuildConfig.ADMOB_KEY;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    private DatabaseReference mDatabaseReference;
    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ImageView mGoogImgView;
    private InterstitialAd mInterstitialAd;
    private DatabaseHandler db;
    private ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);
        mGoogImgView = (ImageView) findViewById(R.id.googlePic);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);
        findViewById(R.id.upload_button).setOnClickListener(this);
        findViewById(R.id.download_button).setOnClickListener(this);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        MobileAds.initialize(this,ADMOB_KEY);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); //TODO: update to real ad before release
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("CFD3C44EB125B2309A418B8E211ED932").build());//TODO remove test device before release
        pDialog = new ProgressDialog(context);
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                            String str = "";
                            if(user!=null) {
                                if (user.getPhotoUrl() != null) {
                                    str = user.getPhotoUrl().toString();
                                }
                                writeNewUser(user.getUid(), user.getDisplayName(), user.getEmail(), str);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(GoogleSignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getDisplayName(),user.getEmail()));
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .override(500,500)
                    .into(mGoogImgView);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);

        } else {
            mStatusTextView.setText(R.string.signed_out);
            mGoogImgView.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.default_prof_img));
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        } else if (i == R.id.sign_out_button) {
            signOut();
        } else if (i == R.id.disconnect_button) {
            revokeAccess();
        } else {
            db = new DatabaseHandler(this);
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
            final FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
            if (i == R.id.upload_button){
                final ArrayList<DBMovie> movies = getIntent().getParcelableArrayListExtra("movies");
                if(mFirebaseUser!=null) {
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            //code to upload data to FireBase
                            mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieRatings").removeValue();
                            mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieNotes").removeValue();
                            for (final DBMovie m : movies) {
                                mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieRatings").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        if (!snapshot.hasChild(Integer.toString(m.getId()))) {
                                            mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieRatings").child(Integer.toString(m.getId())).setValue(db.getRating(m.getId()));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("error adding to user", databaseError.toString());
                                    }
                                });
                                mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieNotes").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        if (!snapshot.hasChild(Integer.toString(m.getId()))) {
                                            mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieNotes").child(Integer.toString(m.getId())).setValue(db.getNote(m.getId()));
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("error adding to user", databaseError.toString());
                                    }
                                });
                            }
                            Toast.makeText(GoogleSignInActivity.this, "Sync complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.e("AdErr", "The interstitial wasn't loaded yet.");
                    }
                }
                else{
                    Toast.makeText(GoogleSignInActivity.this, "An error has occured. Try signing out and signing back in.",
                            Toast.LENGTH_SHORT).show();
                }


            } else if (i == R.id.download_button){

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    //code to download data from FireBase*/
                db.deleteAllMovies();
                mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("CFD3C44EB125B2309A418B8E211ED932").build());
                final HashMap<Integer,Float> movieIds = new HashMap<>();
                final HashMap<Integer,String> movieNotes = new HashMap<>();
                pDialog.setMessage("Syncing...");
                pDialog.setCancelable(false);
                if(mFirebaseUser!=null) {
                    pDialog.show();
                    mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieRatings").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                if (!movieIds.containsKey(Integer.parseInt(child.getKey()))) {
                                    Float userRating = (float) 0.0;
                                    if (child.getValue() instanceof Double)
                                        userRating = ((Double) child.getValue()).floatValue();
                                    else if (child.getValue() instanceof Long)
                                        userRating = ((Long) child.getValue()).floatValue();
                                    movieIds.put(Integer.parseInt(child.getKey()), userRating);
                                }
                            }
                            Thread t1 = new Thread(new Runnable() {
                                public void run() {
                                    int size = movieIds.size();
                                    int waitTime = size > 30 ? 255 : 0;
                                    for (final Integer key : movieIds.keySet()) {
                                        Log.e("Key", key + "");
                                        db.addMovie(key, movieIds.get(key));
                                        try {
                                            Thread.sleep(waitTime);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    handler.sendEmptyMessage(0);
                                }
                            });
                            t1.start();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("error loading data", databaseError.toString());
                        }
                    });

                    mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieNotes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                if (!movieNotes.containsKey(Integer.parseInt(child.getKey()))) {
                                    String userNote = "";
                                    userNote = (String) child.getValue();
                                    movieNotes.put(Integer.parseInt(child.getKey()), userNote);
                                }
                            }
                            Thread t2 = new Thread(new Runnable() {
                                public void run() {
                                    int size = movieNotes.size();
                                    int waitTime = size > 30 ? 255 : 0;
                                    for (final Integer key : movieNotes.keySet()) {
                                        Log.e("Key", key + "");
                                        db.addNote(key, movieNotes.get(key));
                                        try {
                                            Thread.sleep(waitTime);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    handler.sendEmptyMessage(0);
                                }
                            });
                            t2.start();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("error loading data", databaseError.toString());
                        }
                    });
                }
            }
            });
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.e("AdErr", "The interstitial wasn't loaded yet.");
                }
            }
        }
    }

    private void writeNewUser(final String userId, String name, String email, String photoUrl) {
        final User user = new User(name, email, photoUrl);
        if(mDatabaseReference!=null) {
            mDatabaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.hasChild(userId)) {
                        mDatabaseReference.child("users").child(userId).setValue(user);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("error creating user", databaseError.toString());
                }
            });
        }
    }

    public final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pDialog.dismiss();
            Toast.makeText(GoogleSignInActivity.this,"Sync Complete",Toast.LENGTH_LONG).show();
        }
    };

}
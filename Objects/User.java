package jfkdevelopers.navdrawertestapp.Objects;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String username;
    public String email;
    public Uri photoUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, Uri photoUrl) {
        this.username = username;
        this.email = email;
        this.photoUrl = photoUrl;
    }
}

package com.huyentran.tweets.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

/**
 * User model.
 */
@Parcel
public class User {

    private long uid;
    private String name;
    private String screenName;
    private String profileImageUrl;

    public User() {
        // empty constructor for Parceler
    }

    public static User fromJson(JSONObject json) {
        User user = new User();
        try {
            user.uid = json.getLong("id");
            user.name = json.getString("name");
            user.screenName = String.format("@%s", json.getString("screen_name"));
            user.profileImageUrl = json.getString("profile_image_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    public long getUid() {
        return this.uid;
    }

    public String getName() {
        return this.name;
    }

    public String getScreenName() {
        return this.screenName;
    }

    public String getProfileImageUrl() {
        return this.profileImageUrl;
    }
}

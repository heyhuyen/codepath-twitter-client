package com.huyentran.tweets.models;

import com.huyentran.tweets.db.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Tweet model.
 */
@Table(database = MyDatabase.class)
@Parcel(analyze={Tweet.class})
public class Tweet extends BaseModel {

    @Column
    @PrimaryKey
    private long uid;

    @Column
    private String body;

    @Column
    private String createdAt;

    @Column
    @ForeignKey
    private User user;

    public Tweet() {
        // empty constructor for Parceler
    }

    public static Tweet fromJson(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        try {
            tweet.body = jsonObject.getString("text");
            tweet.uid = jsonObject.getLong("id");
            tweet.createdAt = jsonObject.getString("created_at");
            tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tweet;
    }

    public static ArrayList<Tweet> fromJsonArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject tweetJson = jsonArray.getJSONObject(i);
                Tweet tweet = fromJson(tweetJson);
                if (tweet != null) {
                    tweets.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        return tweets;
    }

    public long getUid() {
        return this.uid;
    }

    public String getBody() {
        return this.body;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public User getUser() {
        return this.user;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

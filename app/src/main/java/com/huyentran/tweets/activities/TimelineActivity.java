package com.huyentran.tweets.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.huyentran.tweets.R;
import com.huyentran.tweets.TwitterApplication;
import com.huyentran.tweets.TwitterClient;
import com.huyentran.tweets.adapters.TweetsArrayAdapter;
import com.huyentran.tweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter tweetsAdapter;
    private RecyclerView rvTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        setupViews();
        this.client = TwitterApplication.getRestClient();
        populateTimeline();
    }

    private void setupViews() {
        this.rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        this.tweets = new ArrayList<>();
        this.tweetsAdapter = new TweetsArrayAdapter(this, this.tweets);
        this.rvTweets.setAdapter(this.tweetsAdapter);
        this.rvTweets.setLayoutManager(new LinearLayoutManager(this));
    }

    private void populateTimeline() {
        this.client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("ON SUCCESS", response.toString());
                tweets.addAll(Tweet.fromJsonArray(response));
                tweetsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("ON FAILURE", errorResponse.toString());
            }
        });
    }
}

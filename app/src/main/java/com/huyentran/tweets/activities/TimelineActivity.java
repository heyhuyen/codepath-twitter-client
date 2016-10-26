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
import com.huyentran.tweets.utils.EndlessRecyclerViewScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter tweetsAdapter;
    private RecyclerView rvTweets;

    long curMaxId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        setupViews();
        this.client = TwitterApplication.getRestClient();
        this.curMaxId = -1;
        populateTimeline(this.curMaxId);
    }

    private void setupViews() {
        this.rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        this.tweets = new ArrayList<>();
        this.tweetsAdapter = new TweetsArrayAdapter(this, this.tweets);
        this.rvTweets.setAdapter(this.tweetsAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        this.rvTweets.setLayoutManager(layoutManager);
        this.rvTweets.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                populateTimeline(curMaxId);
            }
        });
    }

    private void populateTimeline(long maxId) {
        this.client.getHomeTimeline(maxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("ON SUCCESS", response.toString());
                List<Tweet> tweetResults = Tweet.fromJsonArray(response);
                tweets.addAll(tweetResults);
                curMaxId = tweetResults.get(tweetResults.size() - 1).getUid();
                tweetsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("ON FAILURE", errorResponse.toString());
            }
        });
    }
}

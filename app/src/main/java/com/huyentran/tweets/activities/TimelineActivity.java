package com.huyentran.tweets.activities;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.huyentran.tweets.R;
import com.huyentran.tweets.TwitterApplication;
import com.huyentran.tweets.TwitterClient;
import com.huyentran.tweets.adapters.TweetsArrayAdapter;
import com.huyentran.tweets.fragment.ComposeFragment;
import com.huyentran.tweets.models.Tweet;
import com.huyentran.tweets.models.User;
import com.huyentran.tweets.utils.EndlessRecyclerViewScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ComposeFragment.ComposeFragmentListener {

    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter tweetsAdapter;
    private RecyclerView rvTweets;
    private SwipeRefreshLayout swipeContainer;

    private User user;
    long curMaxId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.tweets = new ArrayList<>();
        setupViews();
        this.client = TwitterApplication.getRestClient();
        getAuthenticatedUser();
        this.curMaxId = -1;
        populateTimeline(this.curMaxId);
    }

    private void setupViews() {
        // recycler view + adapter
        this.rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
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

        // pull to refresh swipe container
        this.swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        this.swipeContainer.setOnRefreshListener(() -> {
            populateTimeline(curMaxId);
        });
        this.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void getAuthenticatedUser() {
        this.client.getAuthenticatedUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("ON SUCCESS", response.toString());
                user = User.fromJson(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());

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
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.miCompose) {
            launchCompose();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Launches {@link com.huyentran.tweets.fragment.ComposeFragment} modal overlay.
     */
    private void launchCompose() {
        Toast.makeText(this, "compose!", Toast.LENGTH_SHORT).show();
        ComposeFragment composeDialogFragment =
                ComposeFragment.newInstance(this.user);
//        composeDialogFragment.setStyle(
//                DialogFragment.STYLE_NORMAL, R.style.AppDialogTheme);
        composeDialogFragment.show(getSupportFragmentManager(), "composeDialogFragment");
    }

    @Override
    public void onComposeSuccess(Tweet tweet) {
        this.tweets.add(0, tweet);
        this.tweetsAdapter.notifyDataSetChanged();
    }
}

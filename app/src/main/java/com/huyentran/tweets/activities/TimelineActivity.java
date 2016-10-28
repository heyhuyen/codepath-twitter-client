package com.huyentran.tweets.activities;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.huyentran.tweets.db.MyDatabase;
import com.huyentran.tweets.R;
import com.huyentran.tweets.TwitterApplication;
import com.huyentran.tweets.TwitterClient;
import com.huyentran.tweets.adapters.TweetsArrayAdapter;
import com.huyentran.tweets.fragment.ComposeFragment;
import com.huyentran.tweets.models.Tweet;
import com.huyentran.tweets.models.Tweet_Table;
import com.huyentran.tweets.models.User;
import com.huyentran.tweets.utils.EndlessRecyclerViewScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity
        implements ComposeFragment.ComposeFragmentListener, TwitterClient.TwitterClientListener {

    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter tweetsAdapter;
    private RecyclerView rvTweets;
    private SwipeRefreshLayout swipeContainer;
    private Snackbar snackbar;

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
        this.client.registerListener(TimelineActivity.this);
        getAuthenticatedUser(); // needed for composing tweets
        initTimeline();
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
                Log.d("DEBUG",
                        String.format("Endless Scroll: onLoadMore(%d, %d)", page, totalItemsCount));
                populateTimeline(curMaxId);
            }
        });

        // pull to refresh swipe container
        this.swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        this.swipeContainer.setOnRefreshListener(() -> {
            Log.d("DEBUG", "Swipe Refresh");
            this.curMaxId = -1;
            populateTimeline(this.curMaxId);
        });
        this.swipeContainer.setColorSchemeResources(
                R.color.blue,
                R.color.black,
                android.R.color.holo_orange_light);

        // internet snackbar
        this.snackbar = Snackbar.make(this.rvTweets, R.string.error_internet,
                Snackbar.LENGTH_INDEFINITE);

        // compose floating action button
        FloatingActionButton fabCompose = (FloatingActionButton) findViewById(R.id.fabCompose);
        fabCompose.setOnClickListener(v -> launchCompose());
    }

    private void getAuthenticatedUser() {
        this.client.getAuthenticatedUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG",
                        String.format("getAuthenticatedUser success: %s", response.toString()));
                user = User.fromJson(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
                if (!snackbar.isShownOrQueued()) {
                    // TODO: retry
                }
            }
        });
    }

    /**
     * Initializes the timeline with tweets. If the db contains saved tweets, they are loaded.
     * Otherwise, fresh tweets are loaded from an API request.
     */
    private void initTimeline() {
        this.curMaxId = -1;
        // check db for saved tweets
        List<Tweet> savedTweets = SQLite.select().from(Tweet.class)
                .orderBy(Tweet_Table.uid, false).queryList();
        if (savedTweets.isEmpty()) {
            Log.d("DEBUG", "No saved tweets. Fetching fresh tweets from API");
            populateTimeline(this.curMaxId);
        } else {
            Log.d("DEBUG", String.format("Loading %d saved tweets)", savedTweets.size()));
            appendTweets(savedTweets);
        }
    }

    /**
     * Appends the given tweets to the list of tweets and updates the adapter and curMaxId.
     */
    private void appendTweets(List<Tweet> tweets) {
        this.tweets.addAll(tweets);
        this.tweetsAdapter.notifyDataSetChanged();
        this.curMaxId = tweets.get(tweets.size() - 1).getUid();
        Log.d("DEBUG",
                String.format("%d tweets appended. New maxId: %d", tweets.size(), this.curMaxId));
    }

    /**
     * Sends an async request to fetch tweets for the authenticated user's home timeline
     */
    private void populateTimeline(long maxId) {
        Log.d("DEBUG", String.format("populateTimeline with maxId: %d", maxId));
        if (maxId < 0) {
            Log.d("DEBUG", "Clearing tweets...");
            this.tweets.clear();
        }

        this.client.getHomeTimeline(maxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("DEBUG", String.format("getHomeTimeline success: %s", response.toString()));
                List<Tweet> tweetResults = Tweet.fromJsonArray(response);
                appendTweets(tweetResults);
                swipeContainer.setRefreshing(false);
                persistTweets(tweetResults);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
                swipeContainer.setRefreshing(false);
            }
        });
    }

    /**
     * Persists tweets locally to SQlite DB.
     */
    private void persistTweets(List<Tweet> tweets) {
        Log.d("DEBUG", String.format("Trying to persist %d tweets", tweets.size()));
        FlowManager.getDatabase(MyDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<Tweet>() {
                            @Override
                            public void processModel(Tweet tweet) {
                                tweet.getUser().save();
                                tweet.save();
                            }
                        })
                        .addAll(tweets).build())
                .error((transaction, error) -> {
                    Log.d("DEBUG", String.format("Error persisting %d tweets", tweets.size()));
                })
                .success(transaction -> {
                    Log.d("DEBUG", String.format("Persisted %d tweets", tweets.size()));
                })
                .build().execute();
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

//        if (id == R.id.miCompose) {
//            launchCompose();
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Launches {@link com.huyentran.tweets.fragment.ComposeFragment} modal overlay.
     */
    private void launchCompose() {
        // TODO: check that the user is not null
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

    @Override
    public void onInternetConnected() {
        this.snackbar.dismiss();
    }

    @Override
    public void onInternetDisconnected() {
        this.snackbar.show();
        if (this.swipeContainer.isRefreshing()) {
            this.swipeContainer.setRefreshing(false);
        }
    }
}

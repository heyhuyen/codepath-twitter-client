package com.huyentran.tweets;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class TwitterClient extends OAuthBaseClient {
    public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
    public static final String REST_URL = "https://api.twitter.com/1.1";
    public static final String REST_CONSUMER_KEY = "30EutN8mDo4EE4ztMKtvjKFwf";
    public static final String REST_CONSUMER_SECRET = "FLBYJFQwGtxSssJWSWoQkWynjuxZOvl1vnNH6e3pnXxNQs2uBQ";
    public static final String REST_CALLBACK_URL = "oauth://cptweetsbyht";

    private static final String API_HOME_TIMELINE = "statuses/home_timeline.json";
    private static final int DEFAULT_COUNT = 25;
    private static final int DEFAULT_SINCE_ID = 1;

    public TwitterClient(Context context) {
        super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
    }

    public void getHomeTimeline(long maxId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(API_HOME_TIMELINE);
        RequestParams params = new RequestParams();
        params.put("count", DEFAULT_COUNT);
        params.put("since_id", DEFAULT_SINCE_ID);
        if (maxId > 0) {
            params.put("max_id", maxId);
        }
        this.client.get(apiUrl, params, handler);
    }
}
package com.huyentran.tweets.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huyentran.tweets.R;
import com.huyentran.tweets.models.Tweet;
import com.huyentran.tweets.utils.TweetDateUtils;

import java.util.List;

/**
 * Custom adapter that takes {@link Tweet} objects and turns them into views to display in a list.
 */
public class TweetsArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TEXT = 0;
    private final int PHOTO = 1;

    private List<Tweet> mTweets;
    private Context mContext;

    public TweetsArrayAdapter(Context context, List<Tweet> tweets) {
        this.mTweets = tweets;
        this.mContext = context;
    }

    private Context getContext() {
        return this.mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case PHOTO:
                View photoView = inflater.inflate(R.layout.item_tweet_photo, parent, false);
                viewHolder = new TweetPhotoViewHolder(photoView);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.item_tweet, parent, false);
                viewHolder = new TweetTextViewHolder(defaultView);
                break;
        }
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case PHOTO:
                TweetPhotoViewHolder photoVh = (TweetPhotoViewHolder) viewHolder;
                configurePhotoViewHolder(photoVh, position);
                break;
            default:
                TweetTextViewHolder vh = (TweetTextViewHolder) viewHolder;
                configureTextViewHolder(vh, position);
                break;
        }
    }

    private void configurePhotoViewHolder(TweetPhotoViewHolder viewHolder, int position) {
        Tweet tweet = this.mTweets.get(position);
        viewHolder.binding.setTweet(tweet);
        viewHolder.binding.setUser(tweet.getUser());
        viewHolder.binding.setMedia(tweet.getMedia());
        viewHolder.binding.executePendingBindings();

        TextView tvTime = viewHolder.getTvTime();
        tvTime.setText(TweetDateUtils.getRelativeTimeAgo(tweet.getCreatedAt()));

        // replace media url in body text
        TextView tvBody = viewHolder.getTvBody();
        String textBody = tvBody.getText().toString();
        tvBody.setText(textBody.replace(tweet.getMedia().getUrl(), ""));
    }

    private void configureTextViewHolder(TweetTextViewHolder viewHolder, int position) {
        Tweet tweet = this.mTweets.get(position);
        viewHolder.binding.setTweet(tweet);
        viewHolder.binding.setUser(tweet.getUser());
        viewHolder.binding.executePendingBindings();

        TextView tvTime = viewHolder.getTvTime();
        tvTime.setText(TweetDateUtils.getRelativeTimeAgo(tweet.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return this.mTweets.size();
    }

    @Override
    public int getItemViewType(int position) {
        Tweet tweet = this.mTweets.get(position);
        if (tweet.getMedia() != null) {
            return PHOTO;
        }
        return TEXT;
    }
}

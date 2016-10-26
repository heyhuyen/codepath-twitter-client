package com.huyentran.tweets.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huyentran.tweets.R;
import com.huyentran.tweets.models.Tweet;
import com.huyentran.tweets.models.User;
import com.huyentran.tweets.utils.TweetDateUtils;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Array Adapter that takes {@link Tweet} objects and turns them into views to display in a list.
 */
public class TweetsArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TEXT = 0;

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
            default:
                View defaultView = inflater.inflate(R.layout.item_tweet, parent, false);
                viewHolder = new TextViewHolder(defaultView);
                break;
        }
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            default:
                TextViewHolder vh = (TextViewHolder) viewHolder;
                configureTextViewHolder(vh, position);
                break;
        }
    }

    private void configureTextViewHolder(TextViewHolder viewHolder, int position) {
        Tweet tweet = this.mTweets.get(position);
        User user = tweet.getUser();

        ImageView ivProfilePic = viewHolder.getIvProfilePic();
        ivProfilePic.setImageResource(0);
        String profilePicUrl = user.getProfileImageUrl();
        if (!TextUtils.isEmpty(profilePicUrl)) {
            Glide.with(getContext()).load(profilePicUrl)
                    .centerCrop()
                    .bitmapTransform(new RoundedCornersTransformation(this.mContext, 5, 0))
                    .into(ivProfilePic);
        }

        TextView tvUserName = viewHolder.getTvUserName();
        tvUserName.setText(user.getName());

        TextView tvScreenName = viewHolder.getTvScreenName();
        tvScreenName.setText(user.getScreenName());

        TextView tvTime = viewHolder.getTvTime();
        tvTime.setText(TweetDateUtils.getRelativeTimeAgo(tweet.getCreatedAt()));

        TextView tvBody = viewHolder.getTvBody();
        tvBody.setText(tweet.getBody());
    }

    @Override
    public int getItemCount() {
        return this.mTweets.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TEXT;
    }
}

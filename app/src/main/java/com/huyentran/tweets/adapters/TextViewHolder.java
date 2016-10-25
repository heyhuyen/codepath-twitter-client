package com.huyentran.tweets.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.huyentran.tweets.R;

/**
 * ViewHolder class for text only tweets.
 */
public class TextViewHolder extends RecyclerView.ViewHolder {
    private ImageView ivProfilePic;
    private TextView tvUserName;
    private TextView tvScreenName;
    private TextView tvBody;

    public TextViewHolder(View itemView) {
        super(itemView);
        this.ivProfilePic = (ImageView) itemView.findViewById(R.id.ivProfilePic);
        this.tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
        this.tvScreenName = (TextView) itemView.findViewById(R.id.tvScreenName);
        this.tvBody = (TextView) itemView.findViewById(R.id.tvBody);
    }

    public ImageView getIvProfilePic() {
        return this.ivProfilePic;
    }

    public void setIvProfilePic(ImageView ivProfilePic) {
        this.ivProfilePic = ivProfilePic;
    }

    public TextView getTvUserName() {
        return this.tvUserName;
    }

    public void setTvUserName(TextView tvUserName) {
        this.tvUserName = tvUserName;
    }

    public TextView getTvScreenName() {
        return this.tvScreenName;
    }

    public void setTvScreenName(TextView tvScreenName) {
        this.tvScreenName = tvScreenName;
    }

    public TextView getTvBody() {
        return this.tvBody;
    }

    public void setTvBody(TextView tvBody) {
        this.tvBody = tvBody;
    }
}

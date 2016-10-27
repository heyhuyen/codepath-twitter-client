package com.huyentran.tweets.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huyentran.tweets.R;
import com.huyentran.tweets.TwitterApplication;
import com.huyentran.tweets.TwitterClient;
import com.huyentran.tweets.models.Tweet;
import com.huyentran.tweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Modal overlay for composing tweets.
 */
public class ComposeFragment extends DialogFragment {

    private static final int MAX_CHAR_COUNT = 140;

    private User user;
    private EditText etBody;
    private TextView tvCharCount;

    private ComposeFragmentListener listener;
    public interface ComposeFragmentListener {
        void onComposeSuccess(Tweet tweet);
    }

    public ComposeFragment() {
        // empty constructor
    }

    public static ComposeFragment newInstance(User user) {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(user));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compose, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        this.user = Parcels.unwrap(getArguments().getParcelable("user"));
        this.listener = (ComposeFragmentListener) getActivity();
        setupViews(view);
        return view;
    }

    /**
     * Wiring and setup of view and view-related components.
     */
    private void setupViews(View view) {
        ImageView ivProfilePic = (ImageView) view.findViewById(R.id.ivProfilePic);
        TextView tvUserName = (TextView) view.findViewById(R.id.tvUserName);
        TextView tvScreenName = (TextView) view.findViewById(R.id.tvScreenName);
        ivProfilePic.setImageResource(0);
        String profilePicUrl = this.user.getProfileImageUrl();
        if (!TextUtils.isEmpty(profilePicUrl)) {
            Glide.with(getContext()).load(profilePicUrl)
                    .centerCrop()
                    .bitmapTransform(new RoundedCornersTransformation(getContext(), 5, 0))
                    .into(ivProfilePic);
        }
        tvUserName.setText(this.user.getName());
        tvScreenName.setText(this.user.getScreenName());
        this.etBody = (EditText) view.findViewById(R.id.etBody);
        this.tvCharCount = (TextView) view.findViewById(R.id.tvCharCount);
        this.etBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Fires right as the text is being changed (even supplies the range of text)
                int charsLeft = MAX_CHAR_COUNT - s.length();
                tvCharCount.setText(String.valueOf(charsLeft));
                int colorRed, colorNormal, colorLine;
                if (android.os.Build.VERSION.SDK_INT < 23) {
                    colorRed = getResources().getColor(android.R.color.holo_red_dark);
                    colorNormal = getResources().getColor(R.color.colorTextSecondary);
                    colorLine = getResources().getColor(R.color.colorAccent);
                } else {
                    colorRed = getResources().getColor(android.R.color.holo_blue_light, null);
                    colorNormal = getResources().getColor(R.color.colorTextSecondary, null);
                    colorLine = getResources().getColor(R.color.colorAccent, null);
                }
                if (charsLeft < 0) {
                    tvCharCount.setTextColor(colorRed);
                    etBody.getBackground().setColorFilter(colorRed, PorterDuff.Mode.SRC_ATOP);
                } else {
                    tvCharCount.setTextColor(colorNormal);
                    etBody.getBackground().setColorFilter(colorLine, PorterDuff.Mode.SRC_ATOP);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // Fires right before text is changing
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Fires right after the text has changed
            }
        });
        setupButtons(view);
    }

    private void setupButtons(View view) {
        Button btnSave = (Button) view.findViewById(R.id.btnTweet);
        ImageButton btnCancel = (ImageButton) view.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String tweetBody = etBody.getText().toString();
            TwitterClient client = TwitterApplication.getRestClient();
            client.postUpdate(tweetBody, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("ON SUCCESS", response.toString());
                    listener.onComposeSuccess(Tweet.fromJson(response));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            });
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }
}

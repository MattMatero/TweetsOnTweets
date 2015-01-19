package me.matero.tweetsontweets;

import java.io.InputStream;
import java.net.URL;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;


public class UpdateAdapter extends SimpleCursorAdapter {
	public final static String TWIT_KEY = "yb74tffgCZ0lH0OuGv3cWD4oe";
	public final static String TWIT_SECRET = "UOuVuCvYMJ9vfA0MKMQqaI5g4nfsQBEAYd4nmoqbhaAWTu9Dgd";
	static final String[] from = {"update_text", "user_screen", "update_time",}; //db columns to map to views
	static final int[] to = {R.id.updateText, R.id.userScreen, R.id.updateTime,}; //view ids
	private String LOG_TAG = "UpdateAdapter";

	public UpdateAdapter(Context context, Cursor c){
		super(context,R.layout.update, c, from, to,0);
	}

	public void bindView(View row, Context context, Cursor cursor){
		super.bindView(row, context, cursor);

		try{
			URL profileURL = new URL(cursor.getString(cursor.getColumnIndex("user_image")));
			ImageView profilePic = (ImageView)row.findViewById(R.id.userImg);
			profilePic.setImageDrawable(Drawable.createFromStream((InputStream)profileURL.getContent(), ""));
		}catch(Exception e){
			Log.e(LOG_TAG + " no image", e.getMessage());
		}
		
		long postedAt = cursor.getLong(cursor.getColumnIndex("update_time"));
		TextView textCreatedAt = (TextView)row.findViewById(R.id.updateTime);
		textCreatedAt.setText(DateUtils.getRelativeTimeSpanString(postedAt)+ " ");
		
		long statusID = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
		String statusName = cursor.getString(cursor.getColumnIndex("user_screen"));

		//sets up status data as a tag for both retweet and reply buttons
		StatusData tweetData = new StatusData(statusID,statusName);
		
		row.findViewById(R.id.retweet).setTag(tweetData);
		row.findViewById(R.id.reply).setTag(tweetData);

		//sets click listeners for retweet and reply buttons
		row.findViewById(R.id.retweet).setOnClickListener(tweetListener);
		row.findViewById(R.id.reply).setOnClickListener(tweetListener);

		//sets click for users screen name
		row.findViewById(R.id.userScreen).setOnClickListener(tweetListener);

	}

	private OnClickListener tweetListener = new OnClickListener(){
		public void onClick(View v){
			switch(v.getId()){
			case R.id.reply:
				Intent replyIntent = new Intent(v.getContext(),Tweet.class);
				StatusData theData = (StatusData)v.getTag();
				replyIntent.putExtra("tweetID", theData.getID());
				replyIntent.putExtra("tweetUser",theData.getUser());
				v.getContext().startActivity(replyIntent);
				break;
			case R.id.retweet:
				Context appCont = v.getContext(); 
				//get preferences for user access 
				SharedPreferences tweetPrefs = appCont.getSharedPreferences("TweetsOnTweetsPrefs", 0); 
				String userToken = tweetPrefs.getString("user_token", null); 
				String userSecret = tweetPrefs.getString("user_secret", null); 

				//create new Twitter configuration 
				Configuration twitConf = new ConfigurationBuilder() 
				.setOAuthConsumerKey(TWIT_KEY) 
				.setOAuthConsumerSecret(TWIT_SECRET) 
				.setOAuthAccessToken(userToken) 
				.setOAuthAccessTokenSecret(userSecret) 
				.build(); 

				//create Twitter instance for retweeting 
				Twitter retweetTwitter = new TwitterFactory(twitConf).getInstance();
				StatusData tweetData = (StatusData)v.getTag();
				try{
					retweetTwitter.retweetStatus(tweetData.getID());
					CharSequence text = "Retweeted!";
					Toast.makeText(appCont,text,Toast.LENGTH_SHORT).show();
				}catch(TwitterException te){
					Log.e(LOG_TAG, te.getMessage());
				}
				break;
			case R.id.userScreen:
				TextView tv = (TextView)v.findViewById(R.id.userScreen);
				String userScreenName = tv.getText().toString();
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://twitter.com/"+ userScreenName));
				v.getContext().startActivity(browserIntent);
				break;
			default:
				break;
			}
		}
	};
}

package me.matero.tweetsontweets;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class Tweet extends Activity implements OnClickListener {
	public final static String TWIT_KEY = "yb74tffgCZ0lH0OuGv3cWD4oe";
	public final static String TWIT_SECRET = "UOuVuCvYMJ9vfA0MKMQqaI5g4nfsQBEAYd4nmoqbhaAWTu9Dgd";
	private SharedPreferences tweetPrefs;
	private Twitter tweetTwitter;
	private long tweetID = 0;
	private String tweetName = "";


	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweet);
	}

	public void onResume(){
		super.onResume();
		setUpTweet();
	}

	private void setUpTweet(){
		tweetPrefs = getSharedPreferences("TweetsOnTweetsPrefs",0);
		String userToken = tweetPrefs.getString("user_token",null);
		String userSecret = tweetPrefs.getString("user_secret",null);

		Configuration twitconf = new ConfigurationBuilder()
		.setOAuthConsumerKey(TWIT_KEY)
		.setOAuthConsumerSecret(TWIT_SECRET)
		.setOAuthAccessToken(userToken)
		.setOAuthAccessTokenSecret(userSecret)
		.build();

		tweetTwitter = new TwitterFactory(twitconf).getInstance();
		Bundle extras = getIntent().getExtras();
		if(extras != null){
			tweetID = extras.getLong("tweetID");
			tweetName = extras.getString("tweetUser");

			EditText reply = (EditText)findViewById(R.id.tweetText);
			reply.setText("@"+tweetName+" ");
			reply.setSelection(reply.getText().length());
		}else{
			EditText reply = (EditText)findViewById(R.id.tweetText);
			reply.setText("");
		}

		LinearLayout tweetClicker = (LinearLayout)findViewById(R.id.homeButton);
		tweetClicker.setOnClickListener(this);
		Button tweetButton = (Button)findViewById(R.id.doTweet);
		tweetButton.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		EditText tweetText = (EditText)findViewById(R.id.tweetText);
		switch(v.getId()){
		case R.id.doTweet:
			String toTweet = tweetText.getText().toString();
			try{
				if(tweetName.length()>0){
					tweetTwitter.updateStatus(new StatusUpdate(toTweet).inReplyToStatusId(tweetID));
				}else{
					tweetTwitter.updateStatus(toTweet);
				}
				tweetText.setText("");
			}catch(TwitterException te){
				Log.e("Tweet", te.getMessage());
			}
			break;
		case R.id.homeButton:
			 startActivity(new Intent(Tweet.this,AndroidDatabaseManager.class));
			break;
		default:
			break;
		}
		finish();
	}

}

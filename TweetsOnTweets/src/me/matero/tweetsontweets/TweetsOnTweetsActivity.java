package me.matero.tweetsontweets;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TweetsOnTweetsActivity extends Activity implements OnClickListener {
	public final static String TWIT_KEY = "yb74tffgCZ0lH0OuGv3cWD4oe";
	public final static String TWIT_SECRET = "UOuVuCvYMJ9vfA0MKMQqaI5g4nfsQBEAYd4nmoqbhaAWTu9Dgd";
	public final static String TWIT_URL = "oauth://ToT-android";
	
	private Twitter niceTwitter;
	private RequestToken niceRequestToken;
	private SharedPreferences nicePrefs;
	private String LOG_TAG = "TweetsOnTweetsActivity"; //error log
	
	private ListView homeTimeLine;
	private DataHelper timeLineHelper;
	private SQLiteDatabase timeLineDB;
	private Cursor timeLineCursor;
	private UpdateAdapter timeLineAdapter;
	//ProfileImage.ImageSize imageSize = ProfileImage.NORMAL;
	private BroadcastReceiver statusReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		nicePrefs = getSharedPreferences("TweetsOnTweetsPrefs",0);
		if(nicePrefs.getString("user_token",null) ==  null){
			setContentView(R.layout.activity_tweets_on_tweets);
			niceTwitter = new TwitterFactory().getInstance();
			niceTwitter.setOAuthConsumer(TWIT_KEY, TWIT_SECRET);
			try{
				niceRequestToken = niceTwitter.getOAuthRequestToken(TWIT_URL);
			}catch(TwitterException te){
				Log.e(LOG_TAG, "TE " + te.getMessage());
			}
			
			Button signIn = (Button)findViewById(R.id.signin);
			signIn.setOnClickListener(this);
		}else{
			setUpTimeLine();
		}
		
	
	}

	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		Uri twitURI = intent.getData();
		if(twitURI!=null && twitURI.toString().startsWith(TWIT_URL)){
			String oaVerifier = twitURI.getQueryParameter("oauth_verifier");
			//attempt to get access token
			try{
				AccessToken accToken = niceTwitter.getOAuthAccessToken(niceRequestToken,oaVerifier);
				nicePrefs.edit()
				.putString("user_token", accToken.getToken())
				.putString("user_secret", accToken.getTokenSecret())
				.commit();
				
				//display timeline after logging in
				setUpTimeLine();
			}catch(TwitterException te){
				Log.e(LOG_TAG, "failed to get access token: " + te.getMessage());
			}
		}
	}
	
	private void setUpTimeLine(){
		setContentView(R.layout.timeline);	
		
		try{
			homeTimeLine = (ListView)findViewById(R.id.homeList);
			timeLineHelper = new DataHelper(this);
			timeLineDB = timeLineHelper.getReadableDatabase();
			timeLineCursor = timeLineDB.query("home",null, null, null, null, null, "update_time DESC");
			startManagingCursor(timeLineCursor);
			timeLineAdapter = new UpdateAdapter(this, timeLineCursor);
			homeTimeLine.setAdapter(timeLineAdapter);
			statusReceiver = new TwitterUpdateReceiver();
			registerReceiver(statusReceiver, new IntentFilter("TWITTER_UPDATES"));	
			this.getApplicationContext().startService(new Intent(this.getApplicationContext(),TimeLineService.class));
		}catch(Exception e){
			Log.e(LOG_TAG,"**Failed to fetch timeline " + e.getMessage() +" %% " + timeLineDB.toString());
		}
		LinearLayout tweetClicker = (LinearLayout)findViewById(R.id.tweetButton);
		tweetClicker.setOnClickListener(this);
	}
	
	public void onClick(View v){
			 switch(v.getId()){
			 case R.id.signin:
				 String authURL = niceRequestToken.getAuthenticationURL();
				 startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));
				 break;
			 case R.id.tweetButton:
				 startActivity(new Intent(this,Tweet.class));
				 break;
			default:
				break;
			 }
		 }
	 
	public void onDestroy(){
		super.onDestroy();
		try{
			stopService(new Intent(this,TimeLineService.class));
			unregisterReceiver(statusReceiver);
			timeLineDB.close();
		}catch(Exception e){
			Log.e(LOG_TAG,"unable to stop service or receiver " + e.getMessage());
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweets_on_tweets, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	class TwitterUpdateReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent){
			int rowLimit = 100;
			if(DatabaseUtils.queryNumEntries(timeLineDB, "home") > rowLimit){
				String deleteQuery = "DELETE FROM home WHERE " + BaseColumns._ID + " NOT IN " +
						"(SELECT " + BaseColumns._ID+ " FROM home ORDER BY " + "update_time DESC " +
						"limit " + rowLimit+")";
				
				timeLineDB.execSQL(deleteQuery);
			}
			timeLineCursor = timeLineDB.query("home",null,null,null,null,null,"update_time DESC");
			startManagingCursor(timeLineCursor);
			timeLineAdapter = new UpdateAdapter(context,timeLineCursor);
			homeTimeLine.setAdapter(timeLineAdapter);
			
		}
	}
}

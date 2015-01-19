package me.matero.tweetsontweets;

import twitter4j.Twitter;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import twitter4j.Status; 
import twitter4j.TwitterFactory; 
import twitter4j.conf.Configuration; 
import twitter4j.conf.ConfigurationBuilder;
import java.util.List;

public class TimeLineService extends Service{
	public final static String TWIT_KEY = "yb74tffgCZ0lH0OuGv3cWD4oe";
	public final static String TWIT_SECRET = "UOuVuCvYMJ9vfA0MKMQqaI5g4nfsQBEAYd4nmoqbhaAWTu9Dgd";
	private Twitter timeLineTwitter;
	private DataHelper helper;
	private SQLiteDatabase DB;
	private SharedPreferences prefs;
	private Handler handler;
	private static int mins = 1; 
	private static final long FETCH_DELAY = mins *(60*1000);
	private String LOG_TAG = "TimeLineService";
	private TimeLineUpdater updater;
	
	public void onCreate(){
		super.onCreate();
		prefs = getSharedPreferences("TweetsOnTweetsPrefs",0);
		helper = new DataHelper(this);
		DB = helper.getWritableDatabase();
		
		String userToken = prefs.getString("user_token",null);
		String userSecret = prefs.getString("user_secret", null);
		
		Configuration twitConf = new ConfigurationBuilder()
		.setOAuthConsumerKey(TWIT_KEY)
		.setOAuthConsumerSecret(TWIT_SECRET)
		.setOAuthAccessToken(userToken)
		.setOAuthAccessTokenSecret(userSecret)
		.build();
		timeLineTwitter = new TwitterFactory(twitConf).getInstance();
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		handler = new Handler();
		updater = new TimeLineUpdater();
		handler.post(updater);
		return START_STICKY;
	}
	
	public void onDestroy(){
		super.onDestroy();
		handler.removeCallbacks(updater);
		DB.close();
	}
	
	public IBinder onBind(Intent intent){
		return null;
	}
	
	class TimeLineUpdater implements Runnable{
		public void run(){
			
			boolean statusChanged = false;
			try{
				List<Status> homeTimeLine = timeLineTwitter.getHomeTimeline();
				Log.d("**TWEETS**",homeTimeLine.get(0).toString());
				for(Status statusUpdate : homeTimeLine){
					ContentValues timeLineValues = DataHelper.getValues(statusUpdate);
					DB.insertOrThrow("home",null, timeLineValues);
					Log.e("Should insert", statusUpdate.getUser().getScreenName());
					statusChanged = true;
				}
			}catch(Exception e){
				Log.e(LOG_TAG + "**timeline updater Exception** ",  e.getMessage());
			}
			if(statusChanged){
				sendBroadcast(new Intent("TWITTER_UPDATES"));
			}
			handler.postDelayed(TimeLineUpdater.this, FETCH_DELAY);
		}
	}
}

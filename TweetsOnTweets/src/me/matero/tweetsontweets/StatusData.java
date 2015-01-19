package me.matero.tweetsontweets;

public class StatusData {
	private long tweetID;
	private String tweetUser;
	
	public StatusData(long ID, String screenName){
		tweetID = ID;
		tweetUser = screenName;
	}
	
	public long getID(){
		return tweetID;
	}
	
	public String getUser(){
		return tweetUser;
	}
}

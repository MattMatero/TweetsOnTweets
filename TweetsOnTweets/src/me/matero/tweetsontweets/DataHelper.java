package me.matero.tweetsontweets;

import java.util.ArrayList;

import twitter4j.Status;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context; 
import android.database.sqlite.SQLiteDatabase; 
import android.database.sqlite.SQLiteOpenHelper; 
import android.provider.BaseColumns; 
import android.util.Log;

public class DataHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "home";
	private static final String HOME_COL = BaseColumns._ID;
	private static final String UPDATE_COL = "update_text";
	private static final String USER_COL = "user_screen";
	private static final String TIME_COL = "update_time";
	private static final String USER_IMG = "user_image";
	
	private static final String DATABASE_CREATE = "CREATE TABLE home " +"("+ HOME_COL + " INTEGER NOT NULL " +
			"PRIMARY KEY, " + UPDATE_COL + " TEXT, " + USER_COL + " TEXT, " +
			TIME_COL + " INTEGER, " + USER_IMG + " TEXT)";
	
	public DataHelper(Context c){
		super(c,DATABASE_NAME,null,DATABASE_VERSION);
	}
	
	public void onCreate(SQLiteDatabase db){
		Log.v("**Ceating DB**", "Creating db now");
		db.execSQL(DATABASE_CREATE);
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		Log.v("**UPDATING**", "updating DB now");
		db.execSQL("DROP TABLE IF EXISTS home");
		db.execSQL("VACUUM");
		onCreate(db);
	}
	
	public static ContentValues getValues(Status status){
		//prep values
		ContentValues homeValues = new ContentValues();
		
		try{
			homeValues.put(HOME_COL, status.getId());
			homeValues.put(UPDATE_COL, status.getText());
			homeValues.put(USER_COL, status.getUser().getScreenName());
			homeValues.put(TIME_COL, status.getCreatedAt().getTime());
			homeValues.put(USER_IMG, status.getUser().getProfileImageURL());
		}catch(Exception e){
			Log.e("DataHelper", e.getMessage());
		}
		return homeValues;
	}
	
	public ArrayList<Cursor> getData(String Query){
		//get writable database
		SQLiteDatabase sqlDB = this.getWritableDatabase();
		String[] columns = new String[] { "mesage" };
		//an array list of cursor to save two cursors one has results from the query 
		//other cursor stores error message if any errors are triggered
		ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
		MatrixCursor Cursor2= new MatrixCursor(columns);
		alc.add(null);
		alc.add(null);
		
		
		try{
			String maxQuery = Query ;
			//execute the query results will be save in Cursor c
			Cursor c = sqlDB.rawQuery(maxQuery, null);
			

			//add value to cursor2
			Cursor2.addRow(new Object[] { "Success" });
			
			alc.set(1,Cursor2);
			if (null != c && c.getCount() > 0) {

				
				alc.set(0,c);
				c.moveToFirst();
				
				return alc ;
			}
			return alc;
		} catch(SQLException sqlEx){
			Log.d("printing exception", sqlEx.getMessage());
			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
			alc.set(1,Cursor2);
			return alc;
		} catch(Exception ex){

			Log.d("printing exception", ex.getMessage());

			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[] { ""+ex.getMessage() });
			alc.set(1,Cursor2);
			return alc;
		}

		
	}
}

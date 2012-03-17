package racenet.racenet;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class Database extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "racenet.racenet.db";

    Database(Context context) {
    	
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	
        db.execSQL("CREATE TABLE settings(key TEXT, value TEXT)");
        
        ContentValues values = new ContentValues();
        values.put("key", "user_name");
        values.put("value", "");
        db.insert("settings", null, values);

		values = new ContentValues();
		values.put("key", "user_id");
		values.put("value", "");
		db.insert("settings", null, values);
		
		values = new ContentValues();
		values.put("key", "user_flags");
		values.put("value", "");
		db.insert("settings", null, values);
		
		db.execSQL("CREATE TABLE news(id INTEGER, title TEXT, body TEXT)");
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
	}
	
	public String getUsername() {
	    SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.query("settings", new String[]{"value"},
	        "key = 'user_name'", null, null, null, null);
	    c.moveToFirst();
	    String username = c.getString(0);
	    c.close();
	    database.close();
	    return username;

	}

    public void setUsername(String name) {
   	 
    	ContentValues values = new ContentValues();
    	values.put("value", name);
    	
    	SQLiteDatabase database = getWritableDatabase();
    	database.update("settings", values, "key = 'user_name'", null);
    	database.close();
    }
    
    public String getUserID() {
   	 
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.query("settings", new String[]{"value"},
	        "key = 'user_id'", null, null, null, null);
	    c.moveToFirst();
	    String userId = c.getString(0);
	    c.close();
	    database.close();
	    return userId;
    }
    
    public void setUserID(String id) {
   	 
    	ContentValues values = new ContentValues();
    	values.put("value", id);
    	
    	SQLiteDatabase database = getWritableDatabase();
    	database.update("settings", values, "key = 'user_id'", null);
    	database.close();
    }
    
    public String getUserFlags() {
   	 
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.query("settings", new String[]{"value"},
	        "key = 'user_flags'", null, null, null, null);
	    c.moveToFirst();
	    String userFlags = c.getString(0);
	    c.close();
	    database.close();
	    return userFlags;
    }
    
    public void setUserFlags(String flags) {
   	 
    	ContentValues values = new ContentValues();
    	values.put("value", flags);
    	
    	SQLiteDatabase database = getWritableDatabase();
    	database.update("settings", values, "key = 'user_flags'", null);
    	database.close();
    }
    
    public void addNews(String title, String body) {
    	
    	ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("body", body);
        
        SQLiteDatabase database = getWritableDatabase();
        database.insert("news", null, values);
    	database.close();
    }
    
    public void clearNews() {
    	
    	 SQLiteDatabase database = getWritableDatabase();
    	 database.delete("news", null, null);
    	 database.close();
    }
    
    public String[] getLatestNews() {
    	
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.query("news", new String[]{"title", "body"},
	        null, null, null, null, "id DESC");
	    c.moveToFirst();
	    String[] news = {c.getString(0), c.getString(1)};
	    c.close();
	    database.close();    	
    	return news;
    }
}


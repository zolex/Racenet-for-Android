package org.racenet.models;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class Database extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "org.racenet.db";

    public Database(Context context) {
    	
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	
        db.execSQL("CREATE TABLE settings(key TEXT, value TEXT, PRIMARY KEY(key))");
        
        for (String key: new String[]{"user_name", "user_id", "user_flags", "icon", "sound"}) {
        
	        ContentValues values = new ContentValues();
	        values.put("key", key);
	        if (key.equals("sound")) {
	        	values.put("value", "content://settings/system/notification_sound");
	        } else if(key.equals("icon")) {
	        	values.put("value", "true");
	        } else {
	        	values.put("value", "");
	        }
	        db.insert("settings", null, values);
        }
		
        db.execSQL("CREATE TABLE news(id INTEGER, title TEXT, body TEXT, PRIMARY KEY(id))");
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
	}
	
	public void set(String key, String value) {
		
		ContentValues values = new ContentValues();
    	values.put("value", value);
    	
    	SQLiteDatabase database = getWritableDatabase();
    	database.update("settings", values, "key = '"+ key + "'", null);
    	database.close();
	}
	
	public String get(String key) {
		
		SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.query("settings", new String[]{"value"},
	        "key = '"+ key + "'", null, null, null, null);
	    c.moveToFirst();
	    String value = c.getString(0);
	    c.close();
	    database.close();
	    return value;
	}

    public void addNews(String title, String body) {
    	
    	ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("body", body);
        
        SQLiteDatabase database = getWritableDatabase();
        database.insert("news", null, values);
    	database.close();
    }
    
    public void deleteAllNews() {
    	
    	SQLiteDatabase database = getWritableDatabase();
    	database.delete("news", null, null);
    	database.close();
    }
    
    public void deleteNews(int id) {
    	
    	SQLiteDatabase database = getWritableDatabase();
	   	database.delete("news", "id = " + id, null);
	   	database.close();
    }
    
    public List<NewsItem> getAllNews() {
    	
    	List<NewsItem> news = new ArrayList<NewsItem>();
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.query("news", new String[]{"id", "title", "body"},
	        null, null, null, null, "id DESC");
	    
	    c.moveToFirst();
	    while(!c.isAfterLast()) {
	    	
	    	NewsItem item = new NewsItem();
	    	item.setId(c.getInt(0));
	    	item.setTitle(c.getString(1));
	    	item.setBody(c.getString(2));
	    	news.add(item);
	    	
	    	c.moveToNext();
	    }
	    
	    c.close();
	    database.close();    	
    	return news;
    }
    
    public int countNews() {
    	
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.rawQuery("SELECT count(*) FROM news", null);
	    c.moveToFirst();
	    int num = c.getInt(0);
	    c.close();
	    database.close();    	
    	return num;
    }
    
    public void addPost(String subject, String poster, String body) {
    	
    	ContentValues values = new ContentValues();
    	values.put("subject", subject);
    	values.put("poster", poster);
        values.put("body", body);
        
        SQLiteDatabase database = getWritableDatabase();
        database.insert("posts", null, values);
    	database.close();
    }
    
    public String countPosts() {
    	
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.rawQuery("SELECT count(*) FROM posts", null);
	    c.moveToFirst();
	    String num = c.getString(0);
	    c.close();
	    database.close();    	
    	return num;
    }
}


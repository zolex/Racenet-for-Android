package org.racenet.models;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public final class Database extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "org.racenet.db";

    public Database(Context context) {
    	
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	
        db.execSQL("CREATE TABLE settings(key TEXT, value TEXT, PRIMARY KEY(key))");
        
        for (String key: new String[]{"user_name", "user_id", "user_flags", "icon", "sound", "ping"}) {
        
	        ContentValues values = new ContentValues();
	        values.put("key", key);
	        if (key.equals("sound")) {
	        	values.put("value", "content://settings/system/notification_sound");
	        } else if(key.equals("icon")) {
	        	values.put("value", "true");
	        } else if(key.equals("ping")) {
	        	values.put("value", "3000");
	        } else {
	        	values.put("value", "");
	        }
	        db.insert("settings", null, values);
        }
		
        db.execSQL("CREATE TABLE news(id INTEGER, title TEXT, body TEXT, PRIMARY KEY(id))");
        db.execSQL("CREATE TABLE records(id INTEGER, player TEXT, map TEXT, time INTEGER, old_points INTEGER, new_points INTEGER, PRIMARY KEY(id))");
        db.execSQL("CREATE TABLE users(id INTEGER, username TEXT, player_id INTEGER, name TEXT, simplified TEXT, PRIMARY KEY(id))");
        db.execSQL("CREATE TABLE messages(id INTEGER, user_id INTEGER, name TEXT, text TEXT, PRIMARY KEY(id))");
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
    
    public void addRecord(String player, String map, int time, int oldPoints, int newPoints) {
    	
    	ContentValues values = new ContentValues();
        values.put("player", player);
        values.put("map", map);
        values.put("time", time);
        values.put("old_points", oldPoints);
        values.put("new_points", newPoints);
        
        SQLiteDatabase database = getWritableDatabase();
        database.insert("records", null, values);
    	database.close();
    }
    
    public void deleteAllRecords() {
    	
    	SQLiteDatabase database = getWritableDatabase();
    	database.delete("records", null, null);
    	database.close();
    }
    
    public void deleteRecord(int id) {
    	
    	SQLiteDatabase database = getWritableDatabase();
	   	database.delete("records", "id = " + id, null);
	   	database.close();
    }
    
    public List<RecordItem> getAllRecords() {
    	
    	List<RecordItem> records = new ArrayList<RecordItem>();
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.query("records", new String[]{"id", "player", "map", "time", "old_points", "new_points"},
	        null, null, null, null, "id DESC");
	    
	    c.moveToFirst();
	    while(!c.isAfterLast()) {
	    	
	    	RecordItem item = new RecordItem();
	    	item.setId(c.getInt(0));
	    	item.setPlayer(c.getString(1));
	    	item.setMap(c.getString(2));
	    	item.setTime(c.getInt(3));
	    	item.setOldPoints(c.getInt(4));
	    	item.setNewPoints(c.getInt(5));
	    	records.add(item);
	    	
	    	c.moveToNext();
	    }
	    
	    c.close();
	    database.close();    	
    	return records;
    }
    
    public int countRecords() {
    	
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.rawQuery("SELECT count(*) FROM records", null);
	    c.moveToFirst();
	    int num = c.getInt(0);
	    c.close();
	    database.close();    	
    	return num;
    }
    
    public int sumPoints() {
    	
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.rawQuery("SELECT SUM(old_points - new_points) FROM records", null);
	    c.moveToFirst();
	    int num = c.getInt(0);
	    c.close();
	    database.close();    	
    	return num;
    }
    
    public void addUser(int id, String username, int playerId, String name, String simplified) {
    	
    	ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("username", username);
        values.put("player_id", playerId);
        values.put("name", name);
        values.put("simplified", simplified);
        
        SQLiteDatabase database = getWritableDatabase();
        database.insert("users", null, values);
    	database.close();
    }
    
    public void clearUsers() {
    	
    	SQLiteDatabase database = getWritableDatabase();
    	database.delete("users", null, null);
    	database.close();
    }
    
    public List<UserItem> getAllUsers() {
    	
    	List<UserItem> users = new ArrayList<UserItem>();
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.query("users", new String[]{"id", "username", "player_id", "name", "simplified",},
	        null, null, null, null, "simplified ASC");
	    
	    c.moveToFirst();
	    while(!c.isAfterLast()) {
	    	
	    	UserItem item = new UserItem();
	    	item.setId(c.getInt(0));
	    	item.setUsername(c.getString(1));
	    	item.setPlayerId(c.getInt(2));
	    	item.setName(c.getString(3));
	    	item.setSimplified(c.getString(4));
	    	users.add(item);
	    	
	    	c.moveToNext();
	    }
	    
	    c.close();
	    database.close();    	
    	return users;
    }
    
    public int countUsers() {
    	
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.rawQuery("SELECT count(*) FROM users", null);
	    c.moveToFirst();
	    int num = c.getInt(0);
	    c.close();
	    database.close();    	
    	return num;
    }
    
    public void addMessage(int userId, String name, String text) {
    	
    	ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("name", name);
        values.put("text", text);
        
        SQLiteDatabase database = getWritableDatabase();
        database.insert("messages", null, values);
    	database.close();
    }
    
    public List<ChatItem> getMessages(int userId) {
    	
    	List<ChatItem> messages = new ArrayList<ChatItem>();
    	SQLiteDatabase database = getReadableDatabase();
	    Cursor c = database.query("messages", new String[]{"id", "name", "text"},
	        "user_id = " + userId, null, null, null, "id ASC");
	    
	    c.moveToFirst();
	    while(!c.isAfterLast()) {
	    	
	    	ChatItem item = new ChatItem();
	    	item.setId(c.getInt(0));
	    	item.setName(c.getString(1));
	    	item.setText(c.getString(2));
	    	messages.add(item);
	    	
	    	c.moveToNext();
	    }
	    
	    c.close();
	    database.close();    	
    	return messages;
    } 
}


package org.racenet;

import java.security.acl.Owner;

import org.racenet.models.Database;
import org.racenet.models.UserListAdapter;
import org.racenet.services.MQTTService;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class UserListActivity extends ListActivity {

	private static int MENU_ITEM_LOGIN = 0;
	private static int MENU_ITEM_LOGOUT = 1;
	private Database db;
	UserListAdapter mAdapter;
    private BroadcastReceiver broadcastReceiver;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.userlist);
    	db = new Database(getApplicationContext());
        mAdapter = new UserListAdapter(this);
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				
				Intent i = new Intent(UserListActivity.this, UserChatActivity.class);
				i.putExtra("user_id", (int)id);
				startActivityForResult(i, 0);
			}
		});
        
        broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context c, Intent i) {
				
				mAdapter = new UserListAdapter(UserListActivity.this);
		        setListAdapter(mAdapter);
			}
        };
    }

    public void onStart() {
    	
    	super.onStart();
    	registerReceiver(broadcastReceiver, new IntentFilter(MQTTService.UPDATE_USERLIST_ACTION));
    }
    
    public void onStop() {
    	
    	super.onStop();
    	unregisterReceiver(broadcastReceiver);
    }
    
    @Override
	public boolean onPrepareOptionsMenu (Menu menu) {

		if (db.get("user_id").matches("")) {
		
			menu.getItem(MENU_ITEM_LOGIN).setVisible(true);
			menu.getItem(MENU_ITEM_LOGOUT).setVisible(false);
		
		} else {
			
			menu.getItem(MENU_ITEM_LOGIN).setVisible(false);
			menu.getItem(MENU_ITEM_LOGOUT).setVisible(true);
		}
		
	    return true;
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.chat, menu);
	    
	    MenuItem login = menu.getItem(MENU_ITEM_LOGIN);
	    login.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
		        Intent i = new Intent(UserListActivity.this, LoginActivity.class);
		        startActivity(i);
				return true;
			}
		});
	    
	    MenuItem logout = menu.getItem(MENU_ITEM_LOGOUT);
	    logout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
		        db.set("user_id", "");
		        db.set("user_name", "");
		        db.set("user_flags", "");
		        db.clearUsers();
		        
		        mAdapter = new UserListAdapter(UserListActivity.this);
		        setListAdapter(mAdapter);
		        
		        new AlertDialog.Builder(UserListActivity.this)
			        .setMessage("Logged out.")
			        .setNeutralButton("OK", null)
			        .show();
		        
		        stopService(new Intent(UserListActivity.this, MQTTService.class));
		        
		        return true;
			}
		});
	    
		return true;
	}
}

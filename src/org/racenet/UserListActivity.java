package org.racenet;

import java.security.acl.Owner;

import org.racenet.models.Database;
import org.racenet.models.UserListAdapter;
import org.racenet.services.MQTTService;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class UserListActivity extends ListActivity {

	UserListAdapter mAdapter;
    private BroadcastReceiver broadcastReceiver;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.userlist);
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
}

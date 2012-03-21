package org.racenet;

import org.racenet.helpers.IsServiceRunning;
import org.racenet.models.ChatListAdapter;
import org.racenet.models.Database;
import org.racenet.services.MQTTService;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UserChatActivity extends ListActivity {

	private static int MENU_ITEM_LOGIN = 0;
	private static int MENU_ITEM_LOGOUT = 1;
	
	private ChatListAdapter mAdapter;
    private BroadcastReceiver broadcastReceiver;
    private TextView text;
    private Button button;
    private int userId;
    private int ownId;
    private String ownName;
    private Database db;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.chatlist);
    	db = new Database(getApplicationContext());
    	userId = getIntent().getIntExtra("user_id", 0);
    	ownId = Integer.parseInt(db.get("user_id"));
    	ownName = db.get("user_name");
    	
    	if (ownId == 0 || !IsServiceRunning.check("org.racenet.services.MQTTService", getApplicationContext())) {
    		
        	new AlertDialog.Builder(UserChatActivity.this)
		        .setMessage("Please login")
		        .setNeutralButton("OK", null)
		        .show();
    	}
    	
    	mAdapter = new ChatListAdapter(this, userId);
        setListAdapter(mAdapter);
        
        text = (TextView)findViewById(R.id.text);
        button = (Button)findViewById(R.id.send);
        button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				if (!text.getText().equals("")) {
				
					if (!MQTTService.client.getChannel().isConnected()) {
						
						Toast.makeText(UserChatActivity.this, "Please try again...", Toast.LENGTH_SHORT).show();
						return;
					}
					
					String xml = "<?xml version=\"1.0\"?><chat><userid><![CDATA[" + ownId +
							"]]></userid><name><![CDATA[" + ownName + 
							"]]></name><text><![CDATA[" + text.getText()  +
							"]]></text></chat>";
					
					db.addMessage(userId, ownName, text.getText().toString());
					
					mAdapter = new ChatListAdapter(UserChatActivity.this, userId);
			        setListAdapter(mAdapter);
					
					text.setText("");
					
					MQTTService.client.publish("user_" + userId, xml);
				}
			}
		});
        
        broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context c, Intent i) {
				
				mAdapter = new ChatListAdapter(UserChatActivity.this, userId);
		        setListAdapter(mAdapter);
			}
        };
    }

    public void onStart() {
    	
    	super.onStart();
    	registerReceiver(broadcastReceiver, new IntentFilter(MQTTService.UPDATE_CHAT_ACTION));
    }
    
    public void onStop() {
    	
    	super.onStop();
    	unregisterReceiver(broadcastReceiver);
    }
}

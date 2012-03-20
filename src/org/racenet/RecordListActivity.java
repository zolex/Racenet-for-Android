package org.racenet;

import org.racenet.models.Database;
import org.racenet.models.RecordListAdapter;
import org.racenet.services.MQTTService;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.Toast;

public class RecordListActivity extends ExpandableListActivity {

	RecordListAdapter mAdapter;
    private static int MENU_ITEM_DELETE = 0;
    private BroadcastReceiver broadcastReceiver;
    private Database db;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.recordlist);
        mAdapter = new RecordListAdapter(this);
        setListAdapter(mAdapter);
        registerForContextMenu(getExpandableListView());
        broadcastReceiver = new BroadcastReceiver() {
            
        	@Override
            public void onReceive(Context context, Intent intent) {
            	
            	mAdapter.notifyDataSetChanged();
            }
        };
        
        db = new Database(getApplicationContext());
    }

    public void onStart() {
    	
    	super.onStart();
    	registerReceiver(broadcastReceiver, new IntentFilter(MQTTService.UPDATE_RECORDS_ACTION));
    	if(db.countRecords() == 0) {
    		
    		Toast.makeText(RecordListActivity.this, "Record list is empty", Toast.LENGTH_SHORT).show();
    	}
    }
    
    public void onStop() {
    	
    	super.onStop();
    	unregisterReceiver(broadcastReceiver);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo)menuInfo;       
		final String title = ((TextView)info.targetView).getText().toString();
		menu.setHeaderTitle(title);
		menu.add(0, 0, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();       
        final String title = ((TextView)info.targetView).getText().toString();
        final int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);       	
        
        new AlertDialog.Builder(RecordListActivity.this)
	        .setMessage("Do you really want to delete '" + title + "'?")
	        .setPositiveButton("Yes", new OnClickListener() {
				
				public void onClick(DialogInterface arg0, int arg1) {
					
					db.deleteRecord((int)mAdapter.getGroupId(groupPos));
					mAdapter.notifyDataSetChanged();
					Toast.makeText(RecordListActivity.this, "Deleted '" + title + "'", Toast.LENGTH_SHORT).show();
				}
			})
			.setNegativeButton("No", null)
	        .show();

        return true;

    }
    
    @Override
	public boolean onPrepareOptionsMenu (Menu menu) {

		if (new Database(getApplicationContext()).countRecords() == 0) {
		
			menu.getItem(0).setEnabled(false);
		
		} else {
			
			menu.getItem(0).setEnabled(true);
		}
		
	    return true;
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.records, menu);
	    
	    MenuItem delete = menu.getItem(MENU_ITEM_DELETE);
	    delete.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
				new AlertDialog.Builder(RecordListActivity.this)
			        .setMessage("Do you really want to delete all records?")
			        .setPositiveButton("Yes", new OnClickListener() {
						
						public void onClick(DialogInterface arg0, int arg1) {
							
							db.deleteAllRecords();
							mAdapter.notifyDataSetChanged();
							Toast.makeText(RecordListActivity.this, "Deleted all records", Toast.LENGTH_SHORT).show();
						}
					})
					.setNegativeButton("No", null)
			        .show();
				
				return true;
			}
		});
	    
		return true;
	}
}

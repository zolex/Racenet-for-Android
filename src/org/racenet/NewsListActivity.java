package org.racenet;

import org.racenet.models.Database;
import org.racenet.models.NewsListAdapter;
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

public class NewsListActivity extends ExpandableListActivity {

	NewsListAdapter mAdapter;
    private static int MENU_ITEM_DELETE = 0;
    private BroadcastReceiver broadcastReceiver;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.newslist);
        mAdapter = new NewsListAdapter(this);
        setListAdapter(mAdapter);
        registerForContextMenu(getExpandableListView());
        broadcastReceiver = new BroadcastReceiver() {
            
        	@Override
            public void onReceive(Context context, Intent intent) {
            	
            	mAdapter.notifyDataSetChanged();
            }
        };
    }

    public void onStart() {
    	
    	super.onStart();
    	registerReceiver(broadcastReceiver, new IntentFilter(MQTTService.UPDATE_NEWS_ACTION));
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
        
        new AlertDialog.Builder(NewsListActivity.this)
	        .setMessage("Do you really want to delete '" + title + "'?")
	        .setPositiveButton("Yes", new OnClickListener() {
				
				public void onClick(DialogInterface arg0, int arg1) {
					
					new Database(getApplicationContext()).deleteNews((int)mAdapter.getGroupId(groupPos));
					mAdapter.notifyDataSetChanged();
					Toast.makeText(NewsListActivity.this, "Deleted '" + title + "'", Toast.LENGTH_SHORT).show();
				}
			})
			.setNegativeButton("No", null)
	        .show();

        return true;

    }
    
    @Override
	public boolean onPrepareOptionsMenu (Menu menu) {

		if (new Database(getApplicationContext()).countNews() == 0) {
		
			menu.getItem(0).setEnabled(false);
		
		} else {
			
			menu.getItem(0).setEnabled(true);
		}
		
	    return true;
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.news, menu);
	    
	    MenuItem delete = menu.getItem(MENU_ITEM_DELETE);
	    delete.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
				new AlertDialog.Builder(NewsListActivity.this)
			        .setMessage("Do you really want to delete all news?")
			        .setPositiveButton("Yes", new OnClickListener() {
						
						public void onClick(DialogInterface arg0, int arg1) {
							
							new Database(getApplicationContext()).deleteAllNews();
							mAdapter.notifyDataSetChanged();
							Toast.makeText(NewsListActivity.this, "Deleted all news", Toast.LENGTH_SHORT).show();
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

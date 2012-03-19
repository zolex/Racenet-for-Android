package org.racenet;

import java.util.List;

import org.racenet.models.Database;
import org.racenet.services.MQTTService;
import org.racenet.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Main Screen of the application
 * @author al
 */
public class StartActivity extends Activity {
	
	private Database db;
	
	private static int MENU_ITEM_LOGIN = 0;
	private static int MENU_ITEM_LOGOUT = 1;
	private static int MENU_ITEM_NEWS = 2;
	private static int MENU_ITEM_SETTINGS = 3;
	private static int MENU_ITEM_EXIT = 4;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
		super.onCreate(savedInstanceState);
		
		// workaround to avoid multiple instances of this activity
		if (!isTaskRoot()) {
		    final Intent intent = getIntent();
		    final String intentAction = intent.getAction(); 
		    if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
		        finish();
		        return;       
		    }
		}
		
        this.setContentView(R.layout.start);
       
        deleteDatabase("org.racenet.db");
        db = new Database(getApplicationContext());
        
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setMessage("Loading data...");
		pd.setCancelable(false);
        
        WebView ranking = (WebView)findViewById(R.id.ranking);
        ranking.setBackgroundColor(0x00000000);
        ranking.setWebViewClient(new WebViewClient() {
        	
        	@Override
        	public void onPageFinished(WebView view, String url) {
        	       
        		pd.dismiss();
                String userID = db.get("user_id");
                String username = db.get("user_name");
                
                if (!userID.matches("")) {
                	
                	new AlertDialog.Builder(StartActivity.this)
        		        .setMessage("Logged in as '" + username + "'.")
        		        .setNeutralButton("OK", null)
        		        .show();
                	
                	if (!StartActivity.isServiceRunning("org.racenet.services.MQTTService", getApplicationContext())) {
                		
                		startService(new Intent(StartActivity.this, MQTTService.class));
                	}
                	
                } else {
                	
                	new AlertDialog.Builder(StartActivity.this)
	    		        .setMessage("Log in to enable the push service.")
	    		        .setNeutralButton("OK", null)
	    		        .show();
                }
        	}

        });
        
		pd.show();
		
        ranking.loadUrl("http://www.warsow-race.net/ranking/android/num/100");
    }
	
	public static boolean isServiceRunning(String serviceClassName, Context context){
		
	    final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
	    final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
	    for (RunningServiceInfo runningServiceInfo : services) {
	    	if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
	            return true;
	        }
	    }
	    return false;
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
	    inflater.inflate(R.menu.main, menu);
	    
	    MenuItem exit = menu.getItem(MENU_ITEM_EXIT);
	    exit.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
				askExit();
				return true;
			}
		});
	    
	    MenuItem news = menu.getItem(MENU_ITEM_NEWS);
	    news.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
				Intent i = new Intent(StartActivity.this, NewsListActivity.class);
			    startActivity(i);
			    return true;
			}
		});
	    
	    MenuItem settings = menu.getItem(MENU_ITEM_SETTINGS);
	    settings.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
				Intent i = new Intent(StartActivity.this, SettingsActivity.class);
			    startActivity(i);
			    return true;
			}
		});
	    
	    MenuItem login = menu.getItem(MENU_ITEM_LOGIN);
	    login.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
		        Intent i = new Intent(StartActivity.this, LoginActivity.class);
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
		        
		        new AlertDialog.Builder(StartActivity.this)
			        .setMessage("Logged out.")
			        .setNeutralButton("OK", null)
			        .show();
		        
		        stopService(new Intent(StartActivity.this, MQTTService.class));
		        
		        return true;
			}
		});
	    
		return true;
	}
	
	@Override
	public void onBackPressed() {
		
	    askExit();
	}
	
	public void askExit() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Do you really want to exit?")
	    	.setCancelable(false)
	        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        	
	        	public void onClick(DialogInterface dialog, int id) {
	        		
	        		finish();
	            }
	        })
	        .setNegativeButton("No", new DialogInterface.OnClickListener() {
	        	
	        	public void onClick(DialogInterface dialog, int id) {
	        		
	        		dialog.cancel();
	            }
	        });
	    
	    AlertDialog alert = builder.create();
	    alert.show();
	}
}
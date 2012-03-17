package racenet.racenet;

import java.util.List;

import racenet.racenet.MQTTService;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
public class Start extends Activity {
	
	private Database db;
	
	private static int MENU_ITEM_LOGIN = 0;
	private static int MENU_ITEM_LOGOUT = 1;
	private static int MENU_ITEM_SETTINGS = 2;
	private static int MENU_ITEM_EXIT = 3;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
		// workaround to avoid multiple instances of this activity
		if (!isTaskRoot()) {
		    final Intent intent = getIntent();
		    final String intentAction = intent.getAction(); 
		    if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
		        finish();
		        return;       
		    }
		}
		
        super.onCreate(savedInstanceState);        
        this.setContentView(R.layout.start);
        
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
        		
                String userID = db.getUserID();
                String username = db.getUsername();
                
                if (!userID.matches("")) {
                	
                	new AlertDialog.Builder(Start.this)
        		        .setMessage("Logged in as '" + username + "'.")
        		        .setNeutralButton("OK", null)
        		        .show();
                	
                	if (!isServiceRunning("racenet.racenet.MQTTService")) {
                		
                		startService(new Intent(Start.this, MQTTService.class));
                	}
                	
                } else {
                	
                	new AlertDialog.Builder(Start.this)
	    		        .setMessage("Log in to enable the push service.")
	    		        .setNeutralButton("OK", null)
	    		        .show();
                }
        	}

        });
        
		pd.show();
		
        ranking.loadUrl("http://www.warsow-race.net/ranking/android/num/100");
    }
	
	public boolean isServiceRunning(String serviceClassName){
		
	    final ActivityManager activityManager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
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

		if (db.getUserID().matches("")) {
		
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
	    
	    MenuItem settings = menu.getItem(MENU_ITEM_SETTINGS);
	    settings.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
				Intent i = new Intent(Start.this, Settings.class);
			    startActivity(i);
			    return true;
			}
		});
	    
	    MenuItem login = menu.getItem(MENU_ITEM_LOGIN);
	    login.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
		        Intent i = new Intent(Start.this, Login.class);
		        startActivity(i);
				return true;
			}
		});
	    
	    MenuItem logout = menu.getItem(MENU_ITEM_LOGOUT);
	    logout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				
		        db.setUserID("");
		        db.setUsername("");
		        db.setUserFlags("");
		        
		        new AlertDialog.Builder(Start.this)
			        .setMessage("Logged out.")
			        .setNeutralButton("OK", null)
			        .show();
		        
		        // FIXME: service dissappears from statusbar
		        // but still receives push-messages
		        stopService(new Intent(Start.this, MQTTService.class));
		        
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
	    builder.setMessage("Are you sure you want to exit?")
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
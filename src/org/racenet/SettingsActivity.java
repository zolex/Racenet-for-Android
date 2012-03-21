package org.racenet;

import org.racenet.helpers.IsServiceRunning;
import org.racenet.models.Database;
import org.racenet.services.MQTTService;
import org.racenet.R;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	
	private Database db;
	private NotificationManager manager;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        db = new Database(getApplicationContext());
        
        OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference pref, Object value) {
				
				if (pref.getKey().equals("icon")) {
					
					if (value.toString() == "true") {
						
						if (IsServiceRunning.check("org.racenet.services.MQTTService", getApplicationContext())) {
							
					        manager.notify(MQTTService.SERVICE_NOTIFICATION,
					        		MQTTService.getServiceNotification(getApplicationContext(), SettingsActivity.this));
						}
						
					} else {
						
						manager.cancel(MQTTService.SERVICE_NOTIFICATION);
					}

				} else if (pref.getKey().equals("ping")) {
					
					if(!value.toString().matches("^[0-9]+$")) {
						
						new AlertDialog.Builder(SettingsActivity.this)
	        		        .setMessage("Must be a numeric value.")
	        		        .setNeutralButton("OK", null)
	        		        .show();
						
						return false;
						
					} else {
						
						new AlertDialog.Builder(SettingsActivity.this)
	        		        .setMessage("Logout and login to apply the change.")
	        		        .setNeutralButton("OK", null)
	        		        .show();
					}
				}
				
				db.set(pref.getKey(), value.toString());
				return true;
			}
		};
        
		findPreference("icon").setOnPreferenceChangeListener(listener);
		findPreference("sound").setOnPreferenceChangeListener(listener);
		findPreference("ping").setOnPreferenceChangeListener(listener);
    }
}

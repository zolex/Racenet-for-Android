package org.racenet;

import org.racenet.models.Database;
import org.racenet.services.MQTTService;
import org.racenet.R;

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
						
						if (StartActivity.isServiceRunning("org.racenet.services.MQTTService", getApplicationContext())) {
							
					        manager.notify(MQTTService.SERVICE_NOTIFICATION,
					        		MQTTService.getServiceNotification(getApplicationContext(), SettingsActivity.this));
						}
						
					} else {
						
						manager.cancel(MQTTService.SERVICE_NOTIFICATION);
					}

				} 
				
				db.set(pref.getKey(), value.toString());
				return true;
			}
		};
        
		findPreference("icon").setOnPreferenceChangeListener(listener);
		findPreference("sound").setOnPreferenceChangeListener(listener);
    }
}

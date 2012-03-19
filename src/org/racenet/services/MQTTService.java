package org.racenet.services;

import java.io.IOException;
import java.io.StringReader;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.racenet.models.Database;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.racenet.NewsListActivity;
import org.racenet.StartActivity;
import org.racenet.R;

import com.albin.mqtt.NettyClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class MQTTService extends Service {
	
    private NotificationManager manager;
    private Database db;
    private static NettyClient client;
    private Timer pinger = new Timer();
    private boolean waitingForPong = false;
    
    public static final String UPDATE_NEWS_ACTION = "org.racenet.MQTTService.updateNewsAction";
    private final Handler broadcastHandler = new Handler();
    private final Intent broadcastIntent = new Intent(UPDATE_NEWS_ACTION);
    
    public static int SERVICE_NOTIFICATION = 1;
    public static int RECORD_NOTIFICATION = 2;
    public static int NEWS_NOTIFICATION = 3;
    public static int ERROR_NOTIFICATION = 4;
    
    @Override
    public void onCreate() {
    	
    	super.onCreate();
    	manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        db = new Database(getApplicationContext());
		
        connect();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
    	super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	
    	super.onDestroy();
    	
    	client.disconnect();
    	pinger.cancel();
    	
    	manager.cancel(SERVICE_NOTIFICATION);
        manager.cancel(RECORD_NOTIFICATION);
        manager.cancel(NEWS_NOTIFICATION);
    }

	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}
	
	private boolean connect() {
		
        if (db.get("user_id").equals("")) {
        	
        	return false;
        }
		
        client = new NettyClient("android_" + db.get("user_name"));
    	client.setKeepAlive(0);
		client.setListener(new MQTTListener(this));
    	client.connect("78.46.92.230", 1883);
    	
    	int interval = Integer.parseInt(db.get("ping"));
		pinger.schedule(new TimerTask() {
			
			@Override
			public void run() {
				
				if (!waitingForPong) {
					
					client.ping();
					waitingForPong = true;
					Log.d("MQTT", "Ping sent");
				
				} else {
					
					Log.d("MQTT", "Connection lost, trying to reconnect");
					waitingForPong = false;
					cancel();
					connect();
				}
			}
		}, interval, interval);
		
    	return true;
	}
	
	/* Listener callback */
	public void onConnect() {
    	
		Log.d("MQTT", "Connected");
		
		client.subscribe("user_"+ db.get("user_id"));
		client.subscribe("news");
		if(db.get("icon").equals("true")) {
			
			manager.notify(SERVICE_NOTIFICATION, MQTTService.getServiceNotification(getApplicationContext(), MQTTService.this));
		}
    }
	
	/* Listener callback */
    public void onDisconnect() {
    
    	Log.d("MQTT", "Disconnected");
    }
    
    /* Listener callback */
    public void onPong() {
    	
    	waitingForPong = false;
    	Log.d("MQTT", "Received pong");
    }

    public static Notification getServiceNotification(Context intentContext, Context pendingContext) {
    	
    	Notification notification = new Notification(R.drawable.ic_launcher, null, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        Intent notifyIntent = new Intent(intentContext, StartActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(pendingContext, 0, notifyIntent, 0);
        notification.setLatestEventInfo(pendingContext, "Racenet ", null, contentIntent);
        return notification;
    }
	
	public void receiveMessage(String topic, String xml) {
		
		String errorMessage = "";
		
		try {
			
			StringReader inStream = new StringReader(xml);
			InputSource inSource = new InputSource(inStream);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(inSource);
			
			if (topic.equals("news")) {
				
				if (doc.getElementsByTagName("news").getLength() == 0) {
					
					return;
				}
				
				Node news = doc.getElementsByTagName("news").item(0);
				
				if (news.getChildNodes().getLength() != 2) {
					
					return;
				}
					
				String title = news.getChildNodes().item(0).getFirstChild().getNodeValue();
				String body = news.getChildNodes().item(1).getFirstChild().getNodeValue();
				
				db.addNews(title, body);
				
				int num = db.countNews();
				if (num > 1) {
					
					title = num + " News";
				}
				
				Notification notification = new Notification(R.drawable.news, null, System.currentTimeMillis());
				notification.sound = Uri.parse(db.get("sound"));
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				Intent notifyIntent = new Intent(getApplicationContext(), NewsListActivity.class);
		        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
				notification.setLatestEventInfo(MQTTService.this, "Racenet News", title, contentIntent);
				MQTTService.this.manager.notify(NEWS_NOTIFICATION, notification);

				
		        broadcastHandler.post(new Runnable() {
		        	
			    	public void run() {    
			    		
			    		sendBroadcast(MQTTService.this.broadcastIntent);
			    	}
			    });
				
			} else { // user_x:private message
				
				if (doc.getElementsByTagName("record").getLength() == 1) {
					
					Node record = doc.getElementsByTagName("record").item(0);
					if (record.getChildNodes().getLength() != 5) {
						
						return;
					}
					
					String player = record.getChildNodes().item(0).getFirstChild().getNodeValue();
					String map = record.getChildNodes().item(1).getFirstChild().getNodeValue();
					String time = record.getChildNodes().item(2).getFirstChild().getNodeValue();
					String oldPoints = record.getChildNodes().item(3).getFirstChild().getNodeValue();
					String newPoints = record.getChildNodes().item(4).getFirstChild().getNodeValue();
					
					int points = (Integer.parseInt(oldPoints) - Integer.parseInt(newPoints));
					String message = player + " stole " + points + " points on " + map;
					
					Notification notification = new Notification(R.drawable.pokal, null, System.currentTimeMillis());
					notification.sound = Uri.parse(db.get("sound"));
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					Intent notifyIntent = new Intent(getApplicationContext(), StartActivity.class);
			        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
					notification.setLatestEventInfo(MQTTService.this, "New record", message, contentIntent);
					MQTTService.this.manager.notify(RECORD_NOTIFICATION, notification);
				}
			}
			
		} catch (SAXException e) {
			
			errorMessage = "SAXException";

		} catch (IOException e) {
			
			errorMessage = "IOException";
			
		} catch (ParserConfigurationException e) {
			
			errorMessage = "ParserConfigurationException";

		} catch (FactoryConfigurationError e) {
			
			errorMessage = "FactoryConfigurationError";
		}
		
		if (!errorMessage.equals("")) {
			
			Notification notification = new Notification(R.drawable.error, null, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent notifyIntent = new Intent(MQTTService.this, MQTTService.class);
			PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
			notification.setLatestEventInfo(MQTTService.this, "Racenet Error", errorMessage, contentIntent);
			MQTTService.this.manager.notify(ERROR_NOTIFICATION, notification);
		}
	}
}

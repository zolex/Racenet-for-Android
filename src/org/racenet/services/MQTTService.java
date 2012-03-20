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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.racenet.NewsListActivity;
import org.racenet.RecordListActivity;
import org.racenet.StartActivity;
import org.racenet.R;
import org.racenet.UserChatActivity;

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
    public static NettyClient client;
    private Timer pinger = new Timer();
    private boolean waitingForPong = false;
    
    public static final String UPDATE_NEWS_ACTION = "org.racenet.MQTTService.updateNewsAction";
    public static final String UPDATE_RECORDS_ACTION = "org.racenet.MQTTService.updateRecordsAction";
    public static final String UPDATE_USERLIST_ACTION = "org.racenet.MQTTService.updateUserlistAction";
    public static final String UPDATE_CHAT_ACTION = "org.racenet.MQTTService.updateChatlistAction";
    
    private final Handler newsBroadcastHandler = new Handler();
    private final Intent newsBroadcastIntent = new Intent(UPDATE_NEWS_ACTION);
    
    private final Handler recordsBroadcastHandler = new Handler();
    private final Intent recordsBroadcastIntent = new Intent(UPDATE_RECORDS_ACTION);
    
    private final Handler userlistBroadcastHandler = new Handler();
    private final Intent userlistBroadcastIntent = new Intent(UPDATE_USERLIST_ACTION);
    
    private final Handler chatBroadcastHandler = new Handler();
    private final Intent chatBroadcastIntent = new Intent(UPDATE_CHAT_ACTION);
    
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
		
		String userId = db.get("user_id");
        if (userId.equals("")) {
        	
        	return false;
        }
        
        int interval = Integer.parseInt(db.get("ping"));
		
        client = new NettyClient("user_" + userId);
    	client.setKeepAlive(interval / 500);
		client.setListener(new MQTTListener(this));
    	client.connect("78.46.92.230", 1883);
    	
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
		client.subscribe("broadcast");
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
		
		// ugly workaround for nettyclient sending a random character
		if (!xml.startsWith("<")) {
			xml = xml.substring(2);
		}
		
		try {
			
			StringReader inStream = new StringReader(xml);
			InputSource inSource = new InputSource(inStream);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(inSource);
			
			if (topic.equals("broadcast")) {
				
				if (doc.getElementsByTagName("news").getLength() > 0) {

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
	
			        newsBroadcastHandler.post(new Runnable() {
			        	
				    	public void run() {    
				    		
				    		sendBroadcast(MQTTService.this.newsBroadcastIntent);
				    	}
				    });
			        
				} else if (doc.getElementsByTagName("userlist").getLength() > 0) {
					
					db.clearUsers();
					
					Node userlist = doc.getElementsByTagName("userlist").item(0);
					NodeList users = userlist.getChildNodes();
					for(int n = 0; n < users.getLength(); n++) {
						
						Node user = users.item(n);
						int id = Integer.parseInt(user.getChildNodes().item(0).getFirstChild().getNodeValue());
						String username = user.getChildNodes().item(1).getFirstChild().getNodeValue();
						int playerId = Integer.parseInt(user.getChildNodes().item(2).getFirstChild().getNodeValue());
						String name = user.getChildNodes().item(3).getFirstChild().getNodeValue();
						String simplified = user.getChildNodes().item(4).getFirstChild().getNodeValue();
						
						db.addUser(id, username, playerId, name, simplified);
					}
					
					userlistBroadcastHandler.post(new Runnable() {
			        	
				    	public void run() {    
				    		
				    		sendBroadcast(MQTTService.this.userlistBroadcastIntent);
				    	}
				    });
				}
				
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
					
					db.addRecord(player, map, Integer.parseInt(time), Integer.parseInt(oldPoints), Integer.parseInt(newPoints));
					
					int points = (Integer.parseInt(oldPoints) - Integer.parseInt(newPoints));
					String message = player + " stole " + points + " points on " + map;
					
					int num = db.countRecords();
					if (num > 1) {
						
						message = "Lost " + db.sumPoints() + "points on " + num + " maps";
					}
					
					Notification notification = new Notification(R.drawable.pokal, null, System.currentTimeMillis());
					notification.sound = Uri.parse(db.get("sound"));
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					Intent notifyIntent = new Intent(getApplicationContext(), RecordListActivity.class);
			        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
					notification.setLatestEventInfo(MQTTService.this, "New record", message, contentIntent);
					MQTTService.this.manager.notify(RECORD_NOTIFICATION, notification);
					
					recordsBroadcastHandler.post(new Runnable() {
			        	
				    	public void run() {    
				    		
				    		sendBroadcast(MQTTService.this.recordsBroadcastIntent);
				    	}
				    });
					
				} else if (doc.getElementsByTagName("chat").getLength() == 1) {
					
					Node record = doc.getElementsByTagName("chat").item(0);
					if (record.getChildNodes().getLength() != 3) {
						
						return;
					}
					
					String userId = record.getChildNodes().item(0).getFirstChild().getNodeValue();
					String name = record.getChildNodes().item(1).getFirstChild().getNodeValue();
					String text = record.getChildNodes().item(2).getFirstChild().getNodeValue();
					
					db.addMessage(Integer.parseInt(userId), name, text);
					
					String message = "from " + name;
					
					
					Notification notification = new Notification(R.drawable.news, null, System.currentTimeMillis());
					notification.sound = Uri.parse(db.get("sound"));
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					Intent notifyIntent = new Intent(getApplicationContext(), UserChatActivity.class);
					notifyIntent.putExtra("user_id", Integer.parseInt(userId));
			        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
					notification.setLatestEventInfo(MQTTService.this, "New Message", message, contentIntent);
					MQTTService.this.manager.notify(RECORD_NOTIFICATION, notification);
					
					chatBroadcastHandler.post(new Runnable() {
			        	
				    	public void run() {    
				    		
				    		sendBroadcast(MQTTService.this.chatBroadcastIntent);
				    	}
				    });
				}
			}
			
		} catch (SAXException e) {
			
			errorMessage = xml;

		} catch (IOException e) {
			
			errorMessage = "IOException";
			
		} catch (ParserConfigurationException e) {
			
			errorMessage = "ParserConfigurationException";

		} catch (FactoryConfigurationError e) {
			
			errorMessage = "FactoryConfigurationError";
		}
		
		if (!errorMessage.equals("")) {
			
			Log.d("MQTTSERVICEERROR", errorMessage);
			
			Notification notification = new Notification(R.drawable.error, null, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent notifyIntent = new Intent(MQTTService.this, MQTTService.class);
			PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
			notification.setLatestEventInfo(MQTTService.this, "Racenet Error", errorMessage, contentIntent);
			MQTTService.this.manager.notify(ERROR_NOTIFICATION, notification);
		}
	}
}

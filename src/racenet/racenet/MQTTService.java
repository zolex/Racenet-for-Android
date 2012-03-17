package racenet.racenet;

import java.io.IOException;
import java.io.StringReader;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.albin.mqtt.NettyClient;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MQTTService extends Service {
	
    private NotificationManager manager;
    private Database db;
    private static NettyClient client;
    private Timer keepAlive;
    
    private static int SERVICE_NOTIFICATION = 1;
    private static int RECORD_NOTIFICATION = 2;
    private static int NEWS_NOTIFICATION = 3;
    private static int ERROR_NOTIFICATION = 4;
    
    private boolean forceStop = false;
    private String userID; 
    
    @Override
    public void onCreate() {
    	
    	super.onCreate();
    	
        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        db = new Database(getApplicationContext());
        
        userID = db.getUserID();
        String userName = db.getUsername();
        if (userID == "") {
        	
        	return;
        }
        
		client = new NettyClient("android_" + userName);
		client.setListener(new MQTTListener(this));
		connectClient();
		
		keepAlive = new Timer();
		keepAlive.schedule(new TimerTask() {
			
			public void run() {
				
				client.ping();
			}
			
		}, 1000, 1000);
        
        Notification notification = new Notification(R.drawable.ic_launcher, "Racenet service started", System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        Intent notifyIntent = new Intent(getApplicationContext(), Start.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
        notification.setLatestEventInfo(this, "Racenet ", "", contentIntent);
        manager.notify(SERVICE_NOTIFICATION, notification);
    }

    private void connectClient() {
    	
    	client.connect("78.46.92.230", 1883);
		client.subscribe("user_"+ userID);
		client.subscribe("news");
    }
    
    public void onDisconnect() {
    	
    	Notification notification = new Notification(R.drawable.error, "Disconnected", System.currentTimeMillis());
    	notification.defaults |= Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Intent notifyIntent = new Intent(getApplicationContext(), MQTTService.class);
        PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
        notification.setLatestEventInfo(this, "Racenet Disconnected ", "", contentIntent);
        manager.notify(ERROR_NOTIFICATION, notification);
        
        if (!forceStop) {
        
        	connectClient();
        }
        
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
    	super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	
    	super.onDestroy();
    	
    	keepAlive.cancel();
    	forceStop = true;
    	client.disconnect();
    	
    	manager.cancel(SERVICE_NOTIFICATION);
        manager.cancel(RECORD_NOTIFICATION);
        manager.cancel(NEWS_NOTIFICATION);
    }

	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}
	
	public void receiveMessage(String topic, String xml) {
		
		String errorMessage = "";
		
		try {
			
			StringReader inStream = new StringReader(xml);
			InputSource inSource = new InputSource(inStream);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(inSource);
			
			if (topic.matches("news")) {
				
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
				
				Notification notification = new Notification(R.drawable.news, null, System.currentTimeMillis());
				notification.defaults |= Notification.DEFAULT_SOUND;
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				Intent notifyIntent = new Intent(getApplicationContext(), News.class);
		        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
				notification.setLatestEventInfo(MQTTService.this, "Racenet News", title, contentIntent);
				MQTTService.this.manager.notify(NEWS_NOTIFICATION, notification);
				
			} else {
			
				Notification notification = new Notification(R.drawable.pokal, null, System.currentTimeMillis());
				notification.defaults |= Notification.DEFAULT_SOUND;
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				Intent notifyIntent = new Intent(getApplicationContext(), Start.class);
		        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
				notification.setLatestEventInfo(MQTTService.this, "New record", xml, contentIntent);
				MQTTService.this.manager.notify(RECORD_NOTIFICATION, notification);
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
		
		if (!errorMessage.matches("")) {
			
			Notification notification = new Notification(R.drawable.error, null, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent notifyIntent = new Intent(MQTTService.this, MQTTService.class);
			PendingIntent contentIntent = PendingIntent.getActivity(MQTTService.this, 0, notifyIntent, 0);
			notification.setLatestEventInfo(MQTTService.this, "Racenet Error", errorMessage, contentIntent);
			MQTTService.this.manager.notify(ERROR_NOTIFICATION, notification);
		}
	}
}

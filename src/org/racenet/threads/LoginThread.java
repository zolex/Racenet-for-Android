package org.racenet.threads;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.racenet.helpers.InputStreamToString;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LoginThread extends Thread {

	private Handler handler;
	private String username;
	private String password;
	
	public LoginThread(String u, String p, Handler h) {
		
		this.username = u;
		this.password = p;
		this.handler = h;
	}
	
	@Override
    public void run() {         

		HttpClient client = new DefaultHttpClient();				
	    HttpPost post = new HttpPost("http://www.warsow-race.net/tools/remoteauth.php");
	    
	    Message msg = new Message();
	    Bundle b = new Bundle();
	    
	    try {
	    	
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("username", this.username));
	        nameValuePairs.add(new BasicNameValuePair("password", this.password));
	        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        HttpResponse response = client.execute(post);
	        
	        b.putString("xml", InputStreamToString.convert(response.getEntity().getContent()));
	        msg.what = 1;       
	        
	    } catch (ClientProtocolException e) {
	    	
	    	b.putString("exception", "ClientProtocolException");
	    	msg.setData(b);
	    	msg.what = 0;

	    } catch (IOException e) {

	    	b.putString("exception", "IOException");
	    	msg.setData(b);
	    	msg.what = 0;
    		
	    }
	    
	    msg.setData(b);
        handler.sendMessage(msg);
    }
}

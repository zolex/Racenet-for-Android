package org.racenet;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.racenet.models.Database;
import org.racenet.services.MQTTService;
import org.racenet.threads.LoginThread;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.racenet.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Login.this Screen of the application
 * @author al
 */
public class LoginActivity extends Activity {
	
	private Database db;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.login);
        
        final Button login = (Button)findViewById(R.id.login);
	    login.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
				pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				pd.setMessage("Loggin in...");
				pd.setCancelable(false);
				pd.show();
				
				EditText username = (EditText)findViewById(R.id.username);
				EditText password = (EditText)findViewById(R.id.password);

				final LoginThread t = new LoginThread(username.getText().toString(), password.getText().toString(), new Handler() {
			    	
			    	@Override
			        public void handleMessage(Message msg) {
			    		
			    		switch (msg.what) {
			    			
			    			case 0:
			    				
			    				new AlertDialog.Builder(LoginActivity.this)
						            .setMessage("Internal error: " + msg.getData().getString("exception"))
						            .setNeutralButton("OK", null)
						            .show();
			    				break;
			    				
			    			case 1:
			    				
			    				StringReader inStream = new StringReader(msg.getData().getString("xml"));
					    		InputSource inSource = new InputSource(inStream);
								DocumentBuilder builder;
								try {
									
									builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
									Document doc = builder.parse(inSource);
									
									NodeList err = doc.getElementsByTagName("error");
									if (err.getLength() > 0) {
										
										new AlertDialog.Builder(LoginActivity.this)
								            .setMessage(err.item(0).getFirstChild().getNodeValue())
								            .setNeutralButton("OK", null)
								            .show();
										
									} else {
										
										Node identity = doc.getElementsByTagName("identity").item(0);
										String userId = identity.getChildNodes().item(0).getFirstChild().getNodeValue();
										String userName = identity.getChildNodes().item(1).getFirstChild().getNodeValue();
										String userFlags = identity.getChildNodes().item(2).getFirstChild().getNodeValue();
										
										new AlertDialog.Builder(LoginActivity.this)
								            .setMessage("Login successful!")
								            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
							                    public void onClick(DialogInterface dialog, int whichButton) {
							
							                    	finish();
							                    }
							                }).show();
										
										db = new Database(getApplicationContext());
										db.set("user_id", userId);
										db.set("user_name", userName);
										db.set("user_flags", userFlags);
										
										startService(new Intent(LoginActivity.this, MQTTService.class));
									}
									
								} catch (SAXException e) {
									
									new AlertDialog.Builder(LoginActivity.this)
							            .setMessage("Internal error: SAXException")
							            .setNeutralButton("OK", null)
							            .show();
	
								} catch (IOException e) {
									
									new AlertDialog.Builder(LoginActivity.this)
							            .setMessage("Internal error: IOException")
							            .setNeutralButton("OK", null)
							            .show();
	
								} catch (ParserConfigurationException e) {
									
									new AlertDialog.Builder(LoginActivity.this)
							            .setMessage("Internal error: ParserConfigurationException")
							            .setNeutralButton("OK", null)
							            .show();
	
								} catch (FactoryConfigurationError e) {
									
									new AlertDialog.Builder(LoginActivity.this)
							            .setMessage("Internal error: FactoryConfigurationError")
							            .setNeutralButton("OK", null)
							            .show();
	
								}
			    				
			    				break;
			    		}
			    		
			        	pd.dismiss();
			        }
			    });
			    
				t.start();
			}
		});
    }
	
	@Override
	public void onBackPressed() {
		
	    finish();
	}
}
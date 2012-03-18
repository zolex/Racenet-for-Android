package org.racenet.services;

import com.albin.mqtt.MqttListener;

public class MQTTListener implements MqttListener {

	private MQTTService service;
	
	MQTTListener(MQTTService a) {
		
		this.service = a;
	}
	
	public void pong() {
		
		this.service.onPong();
	}
	
	public void connected() {
		
		this.service.onConnect();
	}

	public void disconnected() {
		
		this.service.onDisconnect();
	}

	public void publishArrived(String topic, byte[] data) {
		
		this.service.receiveMessage(topic, new String(data));
	}
	
}

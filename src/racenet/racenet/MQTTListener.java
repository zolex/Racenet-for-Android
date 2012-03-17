package racenet.racenet;

import com.albin.mqtt.MqttListener;

public class MQTTListener implements MqttListener {

	private MQTTService service;
	
	MQTTListener(MQTTService a) {
		
		this.service = a;
	}
	
	public void connected() {
		
	}

	public void disconnected() {
		
		this.service.onDisconnect();
	}

	public void publishArrived(String topic, byte[] data) {
		
		this.service.receiveMessage(topic, new String(data));
	}
	
}

package com.xjtu.multithread;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class mqttserver {
	private String host = "tcp://localhost:61613";
	private String userName = "admin";
	private String passWord = "password";
	private MqttClient client;
	private static mqttserver instance = new mqttserver();
	private MqttTopic topic;
	private String myTopic = "Topics/xjtu/serverToPhone";
	private MqttMessage message;
	
	public static mqttserver getInstance(){
		return instance;
	}
	private mqttserver(){
		try {
			client = new MqttClient(host, "Server", new MemoryPersistence());
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setUserName(userName);
			options.setPassword(passWord.toCharArray());
			options.setConnectionTimeout(10);
			options.setKeepAliveInterval(20);
			client.setCallback(new MqttCallback() {

				@Override
				public void connectionLost(Throwable arg0) {
					// TODO 自动生成的方法存根
					
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken arg0) {
					// TODO 自动生成的方法存根
					
				}

				@Override
				public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
					// TODO 自动生成的方法存根
					
				}
			});
			client.connect(options);
		} catch (MqttException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	public void sedMessage(){
		try {
			message = new MqttMessage();
			message.setQos(1);
			message.setRetained(true);
			message.setPayload(("I send a message to phone, Time:"+System.currentTimeMillis()).getBytes());
			topic = client.getTopic(myTopic);
			MqttDeliveryToken token = topic.publish(message);//鍙戝竷涓婚
			token.waitForCompletion();
		} catch (MqttPersistenceException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	public void sedMessage(String filename){
		try {
			message = new MqttMessage();
			message.setQos(1);
			message.setRetained(true);
			message.setPayload(("I send a message to phone, Time:"+ filename +System.currentTimeMillis()).getBytes());
			topic = client.getTopic(myTopic);
			MqttDeliveryToken token = topic.publish(message);//鍙戝竷涓婚
			token.waitForCompletion();
		} catch (MqttPersistenceException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	
}

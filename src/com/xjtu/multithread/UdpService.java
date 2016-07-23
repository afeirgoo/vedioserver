package com.xjtu.multithread;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @˵�� UDP���ӷ���ˣ�����һ��������һ���̴߳���
 * @author afeirgoo
 * @version 1.0
 * @since   2016-6-26
 */
public class UdpService {
	public static void main(String[] args) {
		try {
			init();
			while(true){
				try {
					byte[] buffer = new byte[1024 * 16]; // ������
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					receive(packet);
					new Thread(new ServiceImpl(packet)).start();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//Thread.sleep(1 * 1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * �������ݰ����÷���������߳�����
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	public static DatagramPacket receive(DatagramPacket packet) throws Exception {
		try {
			datagramSocket.receive(packet);
			return packet;
		} catch (Exception e) {
			throw e;
		}
	}
	/**
	 * ����Ӧ�����͸������
	 * @param bt
	 * @throws IOException
	 */
	public static void response(DatagramPacket packet) {
		try {
			datagramSocket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * ��ʼ������
	 * @throws SocketException
	 */
	public static void init(){
		try {
			socketAddress = InetAddress.getLocalHost();
			int port = 8088;  			
			//����DatagramSocket����
			datagramSocket = new DatagramSocket(port, socketAddress);
			
			//datagramSocket.setSoTimeout(1000);
			System.out.println("������Ѿ�����");
		} catch (Exception e) {
			datagramSocket = null;
			System.err.println("���������ʧ��");
			e.printStackTrace();
		}
	}
	public static void UpdateIpAddrInfo(int device, InetAddress addr,int portno,int ty)
	{
		int loop;
		for(loop=0;loop<clientAddressList.size();loop++)
		{			
			if(device == clientAddressList.get(loop).getid())
			{
				clientAddressList.get(loop).setip(addr);
				clientAddressList.get(loop).setport(portno);
				clientAddressList.get(loop).settype((short)ty);
				return;
			}
		}
		ClientAddr caddr = new ClientAddr(device,addr,portno,(short)ty);
		clientAddressList.add(caddr);	 
	     return;	
	}
	public static void UpdateIpAddrMap(int device, InetAddress addr,int portno,int ty)
	{
		 if(clientAddressMap.containsKey(String.valueOf(device)))
		 {
			 //���device�Ѿ�����������ﲻ���Ƿ�һ�����������²���
			 ClientAddr caddr = clientAddressMap.get(String.valueOf(device));
			 caddr.setip(addr);
			 caddr.setport(portno);
			 caddr.settype((short)ty);
		 }
		 else
		 {
			 //���device�״η�����Ϣ
			 ClientAddr newaddr = new ClientAddr(device,addr,portno,(short)ty);
			 clientAddressMap.put(String.valueOf(device), newaddr);
		 }
	     return;	
	}
	/**
	 * ��CMD�����͸��豸��
	 * @param bt
	 * @throws IOException
	 */
	/*
	public static void sendCMD(int deviceid,byte cmdindxe) 
	{
		//���ȱ���clientAddressList�����豸ID��Ӧ�ĵ�ַ���ҵ��˾Ϳ��Է���cmd���Ҳ�����ֱ���˳�
		int loop;
		for(loop=0;loop<clientAddressList.size();loop++)
		{			
			if(deviceid == clientAddressList.get(loop).getid())
			{
				break;
			}
		}
		try {
			//��װdatagramSocket
			@SuppressWarnings("resource")
			DatagramSocket cmdgramSocket = new DatagramSocket(clientAddressList.get(loop).getport(), clientAddressList.get(loop).getip());
			byte[] cmdbuffer = new byte[6]; // ������
			DatagramPacket cmdpacket = new DatagramPacket(cmdbuffer, cmdbuffer.length);
			
			ByteBuffer bf = ByteBuffer.allocate(6);
			short temp = 6;
    	    bf.put(StreamTool.short2byte(temp));    // �ܳ���   //���ܻ���Ҫȷ�ϣ��޸�
    	    bf.put((byte) 2);    //2����Ӧ16������2H����ʾ�ð��İ汾��
    	    bf.put((byte) 53);    //S����ӦASC�Ǵ�д��S����ʾ�ð����豸����Ϊ�������·�
    	    bf.put((byte) 26);    //26����Ӧ16������1AH����ʾ�ð���APP�����ȷ�ϰ�    	   
    	    bf.put(cmdindxe);     //����   	       	    
    	    cmdpacket.setData(bf.array());    	    
    	    cmdgramSocket.send(cmdpacket);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	public static void sendCMD(int deviceid,byte cmdindxe) 
	{
		ClientAddr caddr;
		//������clientAddressMap�в����豸ID��Ӧ�ĵ�ַ���ҵ��˾Ϳ��Է���cmd���Ҳ�����ֱ���˳�
		 if(clientAddressMap.containsKey(String.valueOf(deviceid)))
		 {
			 //���device�Ѿ�����������ﲻ���Ƿ�һ�����������²���
			 caddr = clientAddressMap.get(String.valueOf(deviceid));
			 
		 }
		 else
		 {
			 return;
		 }
		try {
			//��װdatagramSocket
			@SuppressWarnings("resource")			
			byte[] cmdbuffer = new byte[6]; // ������
			DatagramPacket cmdpacket = new DatagramPacket(cmdbuffer, cmdbuffer.length);
			cmdpacket.setAddress(caddr.getip());
			cmdpacket.setPort(caddr.getport());
			ByteBuffer bf = ByteBuffer.allocate(6);
			short temp = 6;
    	    bf.put(StreamTool.short2byte(temp));    // �ܳ���   //���ܻ���Ҫȷ�ϣ��޸�
    	    bf.put((byte) 2);    //2����Ӧ16������2H����ʾ�ð��İ汾��
    	    bf.put((byte) 53);    //S����ӦASC�Ǵ�д��S����ʾ�ð����豸����Ϊ�������·�
    	    bf.put((byte) 26);    //26����Ӧ16������1AH����ʾ�ð���APP�����ȷ�ϰ�    	   
    	    bf.put(cmdindxe);     //����   	       	    
    	    cmdpacket.setData(bf.array());    	    
    	    datagramSocket.send(cmdpacket);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//���ƿ�������
	public static void sendPwrCMD(int deviceid,byte[] cmd) 
	{
		ClientAddr caddr;
		//������clientAddressMap�в����豸ID��Ӧ�ĵ�ַ���ҵ��˾Ϳ��Է���cmd���Ҳ�����ֱ���˳�
		 if(clientAddressMap.containsKey(String.valueOf(deviceid)))
		 {
			 //���device�Ѿ�����������ﲻ���Ƿ�һ�����������²���
			 caddr = clientAddressMap.get(String.valueOf(deviceid));
			 
		 }
		 else
		 {
			 return;
		 }
		try {
			//��װdatagramSocket
			@SuppressWarnings("resource")			
			byte[] cmdbuffer = new byte[6]; // ������
			DatagramPacket cmdpacket = new DatagramPacket(cmdbuffer, cmdbuffer.length);
			cmdpacket.setAddress(caddr.getip());
			cmdpacket.setPort(caddr.getport());
			ByteBuffer bf = ByteBuffer.allocate(6);
			short temp = 6;
    	    bf.put(StreamTool.short2byte(temp));    // �ܳ���   //���ܻ���Ҫȷ�ϣ��޸�
    	    bf.put((byte) 2);    //2����Ӧ16������2H����ʾ�ð��İ汾��
    	    bf.put((byte) 53);    //S����ӦASC�Ǵ�д��S����ʾ�ð����豸����Ϊ�������·�
    	    bf.put((byte) 8);    //8����Ӧ16������08H����ʾ�ð���APP����    	   
    	    bf.put(cmd);     //����   	
    	    bf.put((byte) 5);     //��β��5H   	    
    	    cmdpacket.setData(bf.array());    	    
    	    datagramSocket.send(cmdpacket);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static InetAddress socketAddress = null; // �����������ַ
	private static DatagramSocket datagramSocket = null; // ���Ӷ���
	public static int sendLen = 1200;
	private static LinkedList<ClientAddr> clientAddressList = new LinkedList<ClientAddr>(); //�����е�UDP���Ӷ���������
	private static HashMap<String,ClientAddr> clientAddressMap = new HashMap<String,ClientAddr>(); //�����е�UDP���Ӷ���������
}

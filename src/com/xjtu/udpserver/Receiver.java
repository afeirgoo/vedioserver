package com.xjtu.udpserver;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * �������˳���
 * @author ccna_zhang
 *
 */
public class Receiver {

	public static void main(String[] args) {
		while (true)
	    {
			
			try {
				InetAddress address = InetAddress.getLocalHost();
				int port = 8080;  
				
				//����DatagramSocket����
				DatagramSocket socket = new DatagramSocket(port, address);
				
				byte[] buf = new byte[1024];  //����byte����
				DatagramPacket packet = new DatagramPacket(buf, buf.length);  //����DatagramPacket����				
				socket.receive(packet);  //ͨ���׽��ֽ�������
				
				String getMsg = new String(buf, 0, packet.getLength());
				System.out.println("�ͻ��˷��͵�����Ϊ��" + getMsg);
				
				//�ӷ��������ظ��ͻ�������
				InetAddress clientAddress = packet.getAddress(); //��ÿͻ��˵�IP��ַ
				int clientPort = packet.getPort(); //��ÿͻ��˵Ķ˿ں�
				SocketAddress sendAddress = packet.getSocketAddress();
				String feedback = "Received";
				byte[] backbuf = feedback.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(backbuf, backbuf.length, sendAddress); //��װ���ظ��ͻ��˵�����
				socket.send(sendPacket);  //ͨ���׽��ַ�������������
				
				socket.close();  //�ر��׽���
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}

package com.xjtu.multithread;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
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
					byte[] buffer = new byte[1024 * 2]; // ������
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					receive(packet);
					new Thread(new ServiceImpl(packet)).start();
				} catch (Exception e) {
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
			
			datagramSocket.setSoTimeout(1 * 1000);
			System.out.println("������Ѿ�����");
		} catch (Exception e) {
			datagramSocket = null;
			System.err.println("���������ʧ��");
			e.printStackTrace();
		}
	}
	private static InetAddress socketAddress = null; // �����������ַ
	private static DatagramSocket datagramSocket = null; // ���Ӷ���
	public static int sendLen = 1000;
}

package com.xjtu.udpserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
public class thi4Client {
	public static void main(String[] args) throws IOException {

		DatagramSocket filereceive = new DatagramSocket(1234);

		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		System.out.println("****���������Ѿ����ȴ��ͻ��˷�������******");
		filereceive.receive(packet);// �������ݱ�ǰֱ����
		System.out.println("�ͻ��˴�ʼ��ͻ��˴����ļ�");

		//System.out.println("�ҷ������ͻ���˵��"+(new String(data, 0, data.length)) + packet.getAddress()+"   "+packet.getPort()+"   "+packet.getSocketAddress());
		/* * ��ͻ�����Ӧ���� */

		String filename = "e:\\ForTransformTest.txt";
		File file = new File(filename);
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));

		// �����ļ���·��
		DatagramSocket filesend = new DatagramSocket();
		//System.out.println(new String(filename.getBytes(), 0, filename.getBytes().length));
		DatagramPacket packet2 = new DatagramPacket(filename.getBytes(), filename.getBytes().length, packet.getAddress(),1234);
		filesend.send(packet2);
		
		// 2.���������ֽڶ�ȡ�ļ��������
		byte[] data2 = new byte[1024];
		while (br.read(data2) != -1) {
			//System.out.println(new String(data2,0,data2.length));
			DatagramPacket packet3 = new DatagramPacket(data2, data2.length, packet.getAddress(),1234);
			filesend.send(packet3);
		}
		System.out.println("Done!");
		filesend.close();
	}
}
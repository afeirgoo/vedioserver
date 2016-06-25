package com.xjtu.udpserver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class thi4Server {
	public static void main(String[] args) throws IOException {
		try {
			InetAddress address = InetAddress.getByName("192.168.1.100");
			int port = 1234;
			byte[] data = "hello".getBytes();

			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			DatagramSocket filesend = new DatagramSocket();

			System.out.println("before sending");
			filesend.send(packet);
			System.out.println("after sending" + " " + packet.getSocketAddress());

			DatagramSocket filerecieve = new DatagramSocket(port);
			byte[] data2 = new byte[1024];
			DatagramPacket packetfilename = new DatagramPacket(data2, data2.length);

			System.out.println("before receiving");
			filerecieve.receive(packetfilename);//¿¨
			System.out.println("after receiving");

			String filename = new String(data2, 0, packetfilename.getLength());
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename));

			byte[] data3 = new byte[4096];
			DatagramPacket packetfilecontain = new DatagramPacket(data3, data3.length);
			{
				filerecieve.receive(packetfilecontain);
				String filecontain = new String(data3, 0, packetfilecontain.getLength());
				osw.write(filecontain);
			}
			osw.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
}
package com.xjtu.multithread;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
/**
 * @说明 UDP连接服务端，这里一个包就做一个线程处理
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
					byte[] buffer = new byte[1024 * 2]; // 缓冲区
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
	 * 接收数据包，该方法会造成线程阻塞
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
	 * 将响应包发送给请求端
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
	 * 初始化连接
	 * @throws SocketException
	 */
	public static void init(){
		try {
			socketAddress = InetAddress.getLocalHost();
			int port = 8088;  			
			//创建DatagramSocket对象
			datagramSocket = new DatagramSocket(port, socketAddress);
			
			datagramSocket.setSoTimeout(1 * 1000);
			System.out.println("服务端已经启动");
		} catch (Exception e) {
			datagramSocket = null;
			System.err.println("服务端启动失败");
			e.printStackTrace();
		}
	}
	private static InetAddress socketAddress = null; // 服务监听个地址
	private static DatagramSocket datagramSocket = null; // 连接对象
	public static int sendLen = 1000;
}

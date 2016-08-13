package com.xjtu.multithread;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
					byte[] buffer = new byte[1024 * 16]; // 缓冲区
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
			
			//datagramSocket.setSoTimeout(1000);
			System.out.println("服务端已经启动");
		} catch (Exception e) {
			datagramSocket = null;
			System.err.println("服务端启动失败");
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
			 //这个device已经保存过，这里不管是否一样，都做更新操作
			 ClientAddr caddr = clientAddressMap.get(String.valueOf(device));
			 caddr.setip(addr);
			 caddr.setport(portno);
			 caddr.settype((short)ty);
		 }
		 else
		 {
			 //这个device首次发送消息
			 ClientAddr newaddr = new ClientAddr(device,addr,portno,(short)ty);
			 clientAddressMap.put(String.valueOf(device), newaddr);
		 }
	     return;	
	}
	public static void UpdateDeviceIpAddrDb(int deviceid, InetAddress addr,int portno,int ty) throws SQLException
	{
		DbHelper.connect("192.168.1.100/mydb", "root", "root");
		String querysql = "SELECT * from DeviceAddress where device_id = '";
		querysql += String.valueOf(deviceid);
		querysql += "';";
		List<HashMap<String, String>> rs = DbHelper
                .query(querysql);
		if(rs.size() > 0)
		{
			System.out.println(rs.get(0).get("ip_addr"));
			System.out.println(addr.getAddress().toString());
			System.out.println(String.valueOf(inet_aton(addr.getAddress())));
			if(rs.get(0).get("ip_addr").equals(String.valueOf(inet_aton(addr.getAddress()))) && rs.get(0).get("portno").equals(String.valueOf(portno)))				
			//if(rs.get(0).get("ip_addr") == String.valueOf(inet_aton(addr.getAddress())) && rs.get(0).get("portno") == String.valueOf(portno) )
			{
				//这是跟现有的一致，不做操作
				System.out.println("the same as dataDB");
			}
			else
			{
				//IP or port not same
				String updatesql = "update DeviceAddress set ip_addr = ";
				updatesql += String.valueOf(inet_aton(addr.getAddress()));
				updatesql += ", portno = ";
				updatesql += String.valueOf(portno);
				updatesql += " where device_id = '";
				updatesql += String.valueOf(deviceid);
				updatesql += "';";
				DbHelper.excutesql(updatesql);
			}
		}
		else {
			//这个device首次发送消息
			String insertsql = "insert into DeviceAddress values(";
			insertsql += String.valueOf(deviceid);
			insertsql += ",";
			insertsql += String.valueOf(inet_aton(addr.getAddress()));
			insertsql += ",";
			insertsql += String.valueOf(portno);
			insertsql += ",1";
			insertsql += ");";
			DbHelper.excutesql(insertsql);
		}
		
	    return;	
	}
	public static void UpdateUserAppIpAddrDb(int userid, InetAddress addr,int portno,int ty) throws SQLException
	{
		DbHelper.connect("192.168.1.100/mydb", "root", "root");
		String querysql = "SELECT * from UserInfo where user_id = '";
		querysql += String.valueOf(userid);
		querysql += "';";
		List<HashMap<String, String>> rs = DbHelper
                .query(querysql);
		if(rs.size() > 0)
		{
			//System.out.println(rs.get(0).get("ip_addr"));
			//System.out.println(addr.getAddress().toString());
			//System.out.println(String.valueOf(inet_aton(addr.getAddress())));
			if(rs.get(0).get("ip_addr").equals(String.valueOf(inet_aton(addr.getAddress()))) && rs.get(0).get("portno").equals(String.valueOf(portno)))				
			//if(rs.get(0).get("ip_addr") == String.valueOf(inet_aton(addr.getAddress())) && rs.get(0).get("portno") == String.valueOf(portno) )
			{
				//这是跟现有的一致，不做操作
				System.out.println("the same as dataDB");
			}
			else
			{
				//IP or port not same
				String updatesql = "update UserInfo set ip_addr = ";
				updatesql += String.valueOf(inet_aton(addr.getAddress()));
				updatesql += ", portno = ";
				updatesql += String.valueOf(portno);
				updatesql += " where user_id = '";
				updatesql += String.valueOf(userid);
				updatesql += "';";
				DbHelper.excutesql(updatesql);
			}
		}
		else {
			//这个device首次发送消息
			String insertsql = "insert into UserInfo values(";
			insertsql += String.valueOf(userid);
			insertsql += ",";
			insertsql += String.valueOf(inet_aton(addr.getAddress()));
			insertsql += ",";
			insertsql += String.valueOf(portno);
			insertsql += ",1";
			insertsql += ");";
			DbHelper.excutesql(insertsql);
		}
		
	    return;	
	}
	public static long inet_aton(byte[] add) {
		//byte[] bytes = add.getAddress();
		long result = 0;
		for (byte b : add) {
			if ((b & 0x80L) != 0) {
				result += 256L + b;
			} else {
				result += b;
			}
			result <<= 8;
		}
		result >>= 8;
		return result;
	}
	public static String inet_ntoa(long add) {
		return ((add & 0xff000000) >> 24) + "." + ((add & 0xff0000) >> 16)
				+ "." + ((add & 0xff00) >> 8) + "." + ((add & 0xff));
	}
	/**
	 * 将CMD包发送给设备端
	 * @param bt
	 * @throws IOException
	 */
	/*
	public static void sendCMD(int deviceid,byte cmdindxe) 
	{
		//首先遍历clientAddressList查找设备ID对应的地址，找到了就可以发送cmd，找不到就直接退出
		int loop;
		for(loop=0;loop<clientAddressList.size();loop++)
		{			
			if(deviceid == clientAddressList.get(loop).getid())
			{
				break;
			}
		}
		try {
			//组装datagramSocket
			@SuppressWarnings("resource")
			DatagramSocket cmdgramSocket = new DatagramSocket(clientAddressList.get(loop).getport(), clientAddressList.get(loop).getip());
			byte[] cmdbuffer = new byte[6]; // 缓冲区
			DatagramPacket cmdpacket = new DatagramPacket(cmdbuffer, cmdbuffer.length);
			
			ByteBuffer bf = ByteBuffer.allocate(6);
			short temp = 6;
    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
    	    bf.put((byte) 2);    //2，对应16进制是2H，表示该包的版本号
    	    bf.put((byte) 53);    //S，对应ASC是大写的S，表示该包的设备类型为服务器下发
    	    bf.put((byte) 26);    //26，对应16进制是1AH，表示该包是APP命令的确认包    	   
    	    bf.put(cmdindxe);     //命令   	       	    
    	    cmdpacket.setData(bf.array());    	    
    	    cmdgramSocket.send(cmdpacket);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendCMD(int deviceid,byte cmdindxe) 
	{
		ClientAddr caddr;
		//首先在clientAddressMap中查找设备ID对应的地址，找到了就可以发送cmd，找不到就直接退出
		 if(clientAddressMap.containsKey(String.valueOf(deviceid)))
		 {
			 //这个device已经保存过，这里不管是否一样，都做更新操作
			 caddr = clientAddressMap.get(String.valueOf(deviceid));
			 
		 }
		 else
		 {
			 return;
		 }
		try {
			//组装datagramSocket
			@SuppressWarnings("resource")			
			byte[] cmdbuffer = new byte[6]; // 缓冲区
			DatagramPacket cmdpacket = new DatagramPacket(cmdbuffer, cmdbuffer.length);
			cmdpacket.setAddress(caddr.getip());
			cmdpacket.setPort(caddr.getport());
			ByteBuffer bf = ByteBuffer.allocate(6);
			short temp = 6;
    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
    	    bf.put((byte) 2);    //2，对应16进制是2H，表示该包的版本号
    	    bf.put((byte) 53);    //S，对应ASC是大写的S，表示该包的设备类型为服务器下发
    	    bf.put((byte) 26);    //26，对应16进制是1AH，表示该包是APP命令的确认包    	   
    	    bf.put(cmdindxe);     //命令   	       	    
    	    cmdpacket.setData(bf.array());    	    
    	    datagramSocket.send(cmdpacket);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	public static void sendCMD(int deviceid,byte cmdindxe) 
	{		
		String ipaddr;
		int port;
		//在数据库中查询这个设备的IP端口,找到了就可以发送cmd，找不到就直接退出
		DbHelper.connect("192.168.1.100/mydb", "root", "root");
		String querysql = "SELECT * from DeviceAddress where device_id = '";
		querysql += String.valueOf(deviceid);
		querysql += "';";
		List<HashMap<String, String>> rs = DbHelper.query(querysql);
		if(rs.size() > 0)
		{
			ipaddr = inet_ntoa(Long.parseLong(rs.get(0).get("ip_addr")));
			port = Integer.parseInt(rs.get(0).get("portno"));
			
		}		
		else
		{
			return;
		}
		
		try {
			//组装datagramSocket
			@SuppressWarnings("resource")			
			byte[] cmdbuffer = new byte[6]; // 缓冲区
			DatagramPacket cmdpacket = new DatagramPacket(cmdbuffer, cmdbuffer.length);
			cmdpacket.setAddress(InetAddress.getByName(ipaddr));
			cmdpacket.setPort(port);
			ByteBuffer bf = ByteBuffer.allocate(6);
			short temp = 6;
    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
    	    bf.put((byte) 2);    //2，对应16进制是2H，表示该包的版本号
    	    bf.put((byte) 53);    //S，对应ASC是大写的S，表示该包的设备类型为服务器下发
    	    bf.put((byte) 26);    //26，对应16进制是1AH，表示该包是APP命令的确认包    	   
    	    bf.put(cmdindxe);     //命令   	       	    
    	    cmdpacket.setData(bf.array());    	    
    	    datagramSocket.send(cmdpacket);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//控制开关命令
	/*
	public static void sendPwrCMD(int deviceid,byte[] cmd) 
	{
		ClientAddr caddr;
		//首先在clientAddressMap中查找设备ID对应的地址，找到了就可以发送cmd，找不到就直接退出
		 if(clientAddressMap.containsKey(String.valueOf(deviceid)))
		 {
			 //这个device已经保存过，这里不管是否一样，都做更新操作
			 caddr = clientAddressMap.get(String.valueOf(deviceid));
			 
		 }
		 else
		 {
			 return;
		 }
		try {
			//组装datagramSocket
			@SuppressWarnings("resource")			
			byte[] cmdbuffer = new byte[6+cmd.length]; // 缓冲区
			DatagramPacket cmdpacket = new DatagramPacket(cmdbuffer, cmdbuffer.length);
			cmdpacket.setAddress(caddr.getip());
			cmdpacket.setPort(caddr.getport());
			ByteBuffer bf = ByteBuffer.allocate(6+cmd.length);
			short temp = (short)(6 + cmd.length);
    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
    	    bf.put((byte) 2);    //2，对应16进制是2H，表示该包的版本号
    	    bf.put((byte) 53);    //S，对应ASC是大写的S，表示该包的设备类型为服务器下发
    	    bf.put((byte) 8);    //8，对应16进制是08H，表示该包是APP命令    	   
    	    bf.put(cmd);     //命令   	
    	    bf.put((byte) 5);     //结尾的5H   	    
    	    cmdpacket.setData(bf.array());    	    
    	    datagramSocket.send(cmdpacket);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	public static void sendPwrCMD(int userid,int deviceid,byte[] cmd) 
	{
		String ipaddr;
		int port;
		//在数据库中查询这个设备的IP端口,找到了就可以发送cmd，找不到就直接退出
		DbHelper.connect("192.168.1.100/mydb", "root", "root");
		String querysql = "SELECT * from DeviceAddress where device_id = '";
		querysql += String.valueOf(deviceid);
		querysql += "';";
		List<HashMap<String, String>> rs = DbHelper.query(querysql);
		if(rs.size() > 0)
		{
			ipaddr = inet_ntoa(Long.parseLong(rs.get(0).get("ip_addr")));
			port = Integer.parseInt(rs.get(0).get("portno"));
			
		}		
		else
		{
			return;
		}
		try {
			//组装datagramSocket
			@SuppressWarnings("resource")			
			byte[] cmdbuffer = new byte[14+cmd.length]; // 缓冲区
			DatagramPacket cmdpacket = new DatagramPacket(cmdbuffer, cmdbuffer.length);
			cmdpacket.setAddress(InetAddress.getByName(ipaddr));
			cmdpacket.setPort(port);
			ByteBuffer bf = ByteBuffer.allocate(14+cmd.length);
			short temp = (short)(14 + cmd.length);
    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
    	    bf.put((byte) 2);    //2，对应16进制是2H，表示该包的版本号
    	    bf.put((byte) 53);    //S，对应ASC是大写的S，表示该包的设备类型为服务器下发
    	    bf.put((byte) 8);    //8，对应16进制是08H，表示该包是APP命令    	 
    	    bf.put(StreamTool.int2byte(userid));    // 总包数
    	    bf.put(StreamTool.int2byte(deviceid)); // 当前包的索引
    	    bf.put(cmd);     //命令   	
    	    bf.put((byte) 5);     //结尾的5H   	    
    	    cmdpacket.setData(bf.array());    	    
    	    datagramSocket.send(cmdpacket);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void sendCMDACK2App(int userid,int deviceid,byte[] cmd) 
	{
		String ipaddr;
		int port;
		//在数据库中查询这个设备的IP端口,找到了就可以发送cmd，找不到就直接退出
		DbHelper.connect("192.168.1.100/mydb", "root", "root");
		String querysql = "SELECT * from UserInfo where user_id = '";
		querysql += String.valueOf(userid);
		querysql += "';";
		List<HashMap<String, String>> rs = DbHelper.query(querysql);
		if(rs.size() > 0)
		{
			ipaddr = inet_ntoa(Long.parseLong(rs.get(0).get("ip_addr")));
			port = Integer.parseInt(rs.get(0).get("portno"));
			
		}		
		else
		{
			return;
		}
		try {
			//组装datagramSocket
			@SuppressWarnings("resource")			
			byte[] cmdbuffer = new byte[14+cmd.length]; // 缓冲区
			DatagramPacket cmdpacket = new DatagramPacket(cmdbuffer, cmdbuffer.length);
			cmdpacket.setAddress(InetAddress.getByName(ipaddr));
			cmdpacket.setPort(port);
			ByteBuffer bf = ByteBuffer.allocate(14+cmd.length);
			short temp = (short)(14 + cmd.length);
    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
    	    bf.put((byte) 2);    //2，对应16进制是2H，表示该包的版本号
    	    bf.put((byte) 53);    //S，对应ASC是大写的S，表示该包的设备类型为服务器下发
    	    bf.put((byte) 9);    //9，对应16进制是09H，表示该包是设备返回的反馈包    	 
    	    bf.put(StreamTool.int2byte(userid));    // 总包数
    	    bf.put(StreamTool.int2byte(deviceid)); // 当前包的索引
    	    bf.put(cmd);     //命令   	
    	    bf.put((byte) 5);     //结尾的5H   	    
    	    cmdpacket.setData(bf.array());    	    
    	    datagramSocket.send(cmdpacket);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static InetAddress socketAddress = null; // 服务监听个地址
	private static DatagramSocket datagramSocket = null; // 连接对象
	public static int sendLen = 1200;
	private static LinkedList<ClientAddr> clientAddressList = new LinkedList<ClientAddr>(); //把所有的UDP连接都保存起来
	private static HashMap<String,ClientAddr> clientAddressMap = new HashMap<String,ClientAddr>(); //把所有的UDP连接都保存起来
}

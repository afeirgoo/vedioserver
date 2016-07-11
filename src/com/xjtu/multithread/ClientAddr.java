package com.xjtu.multithread;

import java.net.InetAddress;

public class ClientAddr {
	private int    deviceid;
	private InetAddress ip;  //客户端的IP地址
	private int    port;//客户端端口
	private short  type; //区分是手机app还是车机设备
	public ClientAddr(int device,InetAddress ipaddr,int portno,short ty)
	{
		this.deviceid = device;
		this.ip = ipaddr;
		this.port = portno;
		this.type = ty;
	}
	public int getid()
	{
		return this.deviceid;				
	}
	public InetAddress getip()
	{
		return this.ip;				
	}
	public int getport()
	{
		return this.port;				
	}
	public short gettype()
	{
		return this.type;				
	}
	public void setid(int id)
	{
		this.deviceid = id;				
	}
	public void setip(InetAddress iip)
	{
		this.ip = iip;				
	}
	public void setport(int portn)
	{
		this.port = portn;				
	}
	public void settype(short t)
	{
		this.type = t;				
	}
}

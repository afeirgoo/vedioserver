package com.xjtu.multithread;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.nio.ByteBuffer; 
import java.nio.ByteOrder; 
/**
 * @说明 打印收到的数据包，并且将数据原封返回，中间设置休眠表示执行耗时
 * @author afeirgoo
 * @version 1.0
 * @since   2016-6-26
 */
class ServiceImpl implements Runnable {
	private static final int paketheadlen = 19;
	private static final int ackpaketheadlen = 12;
	private static final int ackheartpaketheadlen = 26;
	private DatagramPacket packet;
	public ServiceImpl(DatagramPacket packet){
		this.packet = packet;
	}
	public void run() {
		try {
			byte[] bt = new byte[packet.getLength()];
			System.arraycopy(packet.getData(), 0, bt, 0, packet.getLength());
			System.out.println(packet.getAddress().getHostAddress() + "：" + packet.getPort() + "：" + Arrays.toString(bt));
			//处理收到的数据报文
			parsepacket(bt);			
			// 设置回复的数据，原数据返回，以便客户端知道是那个客户端发送的数据
			packet.setData(bt);
			//UdpService.response(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.gc();
	}
	
	/**
     * 
     * <pre>
     * 检查这个数据包是什么，心跳、图片
     * </pre>
     * 
     * @param arr
     * @return
     */
    public int parsepacket(byte[] pt)
    {
    	int ret = 0;
    	int deviceID = 0;
    	int UserID = 0;
    	int msgid = 0;
    	//String filePath = "C:\\tomcat\\webapps\\VedioServer\\images\\";    
    	String filePath = "D:\\apache-tomcat-8.0.33\\webapps\\VedioServer\\images\\";
    	//所有数据包的前5个字节，都是该数据包的长度和3字节包头,如果数据少于5个字节，就不用解析了，丢掉
    	if(null == pt || pt.length <= 5)
    	{
    		return ret;
    	}
    	byte[] sbtTemp = new byte[2];   
    	
	    System.arraycopy(pt, 0, sbtTemp, 0, 2);  //取出2个字节	
	    ByteBuffer buffer =  ByteBuffer.wrap(sbtTemp); 	    
	    short len = buffer.getShort();	     
	    byte[] str_MM;
	    byte[] str_STA;
	    byte flag;
	    int   userid;
    	if(len != pt.length)
    	{
    		return ret;
    	}    	
    	byte versionid = pt[2];
    	byte devicetype = pt[3];
    	switch(pt[4])
    	{
	    	case 1: 
	    	    //这是一个心跳包,要提取ID维护设备状态
	    		ret = 1;
	    		byte[] btTemp = new byte[4];
	    	    System.arraycopy(pt, 5, btTemp, 0, 4);  //取出4个字节
	    	    buffer =  ByteBuffer.wrap(btTemp); 
	    	    deviceID = buffer.getInt();//设备ID	    	     
	    	    str_MM = new byte[16];
	    	    System.arraycopy(pt, 9, str_MM, 0, 16);  //取出16个字节
	    	    str_STA = new byte[len - 26];
	    	    System.arraycopy(pt, 25, str_STA, 0, (len - 26));  //取出(len - 26)个字节
	    	    flag = pt[len - 1]; //标志位是最后一字节	    	   
	    	    //构造返回包
	    	    ByteBuffer bf = ByteBuffer.allocate(ackheartpaketheadlen);
	    	    short temp = ackheartpaketheadlen;
	    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
	    	    bf.put(versionid);    //版本号
	    	    bf.put(devicetype);    //设备类型
	    	    bf.put((byte) 1);    //1对应16进制是1H，表示该包是心跳包的确认包
	    	    bf.put((byte) 1);    //确认结果
	    	    String time = (new Timestamp(new Date().getTime())).toString().subSequence(0, 19).toString();	    	    
	    	    bf.put(time.getBytes());    // 时间戳 	    
	    	    
	    	    bf.put((byte) 5);     //成功   	       	    
	    	    //这里实际上要给发送方回复, 以便客户端继续发送
	    	    packet.setData(bf.array());
	    	    UdpService.response(packet);
	    	    
	    	    
	    	    /*	    	   
				try {
					Thread.sleep(50 * 1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    	     ByteBuffer endbf = ByteBuffer.allocate(3 + paketheadlen);
		    	 endbf.put(StreamTool.short2byte((short)(3 + paketheadlen))); // 总长度
		    	 endbf.put((byte) 1);  //版本号
		    	 endbf.put((byte) 1);  //设备类型
		    	 endbf.put((byte) 10);  //数据类型，10表示为图片
		    	 endbf.put(StreamTool.int2byte(67));            // 设备ID		    	
		    	 endbf.put(StreamTool.short2byte((short)3));               // 净数据长度
		    	 byte[] buf = new byte[3];
		    	 buf = "end".getBytes();
		    	 endbf.put(buf);
			     byte[] endbySd = endbf.array();
			     packet.setData(bf.array());
		    	 UdpService.response(packet);
		    	*/
	    	    //更新车机的IP地址	    	    
	    	    //UdpService.UpdateIpAddrMap(deviceID,packet.getAddress(),packet.getPort(),1);
				try {
					UdpService.UpdateDeviceIpAddrDb(deviceID,packet.getAddress(),packet.getPort(),1);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    		break;
	    	case 8: 
	    	    //这是一个服务器命令,要提取ID维护设备状态
	    		ret = 1;
	    		btTemp = new byte[4];
	    	    System.arraycopy(pt, 5, btTemp, 0, 4);  //取出4个字节
	    	    buffer =  ByteBuffer.wrap(btTemp); 
	    	    UserID = buffer.getInt();//设备ID	 
	    	    btTemp = new byte[4];
	    	    System.arraycopy(pt, 9, btTemp, 0, 4);  //取出4个字节
	    	    buffer =  ByteBuffer.wrap(btTemp); 
	    	    deviceID = buffer.getInt();     //设备ID，是指将这条消息转给id设备	
	    	    str_STA = new byte[len - 14];
	    	    System.arraycopy(pt, 13, str_STA, 0, (len - 14));  //取出(len - 14)个字节
	    	    flag = pt[len - 1]; //标志位是最后一字节	    	   
	    	    //构造返回包
	    	    bf = ByteBuffer.allocate(ackheartpaketheadlen);
	    	    temp = ackheartpaketheadlen;
	    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
	    	    bf.put(versionid);    //版本号
	    	    bf.put(devicetype);    //设备类型
	    	    bf.put((byte) 8);    //8对应16进制是8H，表示该包是APP命令的确认包
	    	    bf.put((byte) 1);    //确认结果
	    	    time = (new Timestamp(new Date().getTime())).toString().subSequence(0, 19).toString();	    	    
	    	    bf.put(time.getBytes());    // 时间戳 	    
	    	    
	    	    bf.put((byte) 5);     //成功   	       	    
	    	    //这里实际上要给发送方回复, 以便客户端继续发送
	    	    packet.setData(bf.array());
	    	    UdpService.response(packet);   	    
	    	    //这是APP发的控制命令，所以不用保存更新ip记录
	    	    UdpService.sendPwrCMD(UserID,deviceID,str_STA);  //服务器转发APP命令
	    	    try {
					UdpService.UpdateUserAppIpAddrDb(UserID,packet.getAddress(),packet.getPort(),1);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    		break;
	    	case 9:
	    		//这是设备执行命令之后给服务器的返回信息,用于控制者知道设备执行情况
	    		ret = 9;	    		
	    		btTemp = new byte[4];
	    	    System.arraycopy(pt, 5, btTemp, 0, 4);  //取出4个字节
	    	    buffer =  ByteBuffer.wrap(btTemp); 
	    	    int userID = buffer.getInt();	    	    
	    	    btTemp = new byte[4];
	    	    System.arraycopy(pt, 9, btTemp, 0, 4);  //取出4个字节
	    	    buffer =  ByteBuffer.wrap(btTemp); 
	    	    int ID = buffer.getInt();	    	    
	    	    str_MM = new byte[16];
	    	    System.arraycopy(pt, 13, str_MM, 0, 16);  //取出16个字节
	    	    str_STA = new byte[len - 30];
	    	    System.arraycopy(pt, 29, str_STA, 0, (len - 30));  //取出(len - 26)个字节
	    	    flag = pt[len - 1]; //标志位是最后一字节
	    	    
	    	    ByteBuffer ackbf = ByteBuffer.allocate(7);
	    	    short templen = 7;
	    	    ackbf.put(StreamTool.short2byte(templen));    // 总长度   //可能还需要确认，修改
	    	    ackbf.put(versionid);    //版本号
	    	    ackbf.put(devicetype);    //设备类型
	    	    ackbf.put((byte) 9);    //9对应16进制是9H，表示该包是设备执行结果包的确认包
	    	    ackbf.put((byte) 1);    //确认结果 	       
	    	    
	    	    ackbf.put((byte) 5);     //成功   	       	    
	    	    //这里实际上要给发送方回复, 以便客户端继续发送
	    	    packet.setData(ackbf.array());
	    	    
	    	    UdpService.response(packet);  		
	    	    UdpService.sendCMDACK2App(userID,ID,str_STA);  //服务器转发APP命令
	    	    //UdpService.UpdateIpAddrInfo(deviceID,packet.getAddress(),packet.getPort(),1);
	    	    //UdpService.UpdateIpAddrMap(deviceID,packet.getAddress(),packet.getPort(),1);
	    	    try {
					UdpService.UpdateDeviceIpAddrDb(deviceID,packet.getAddress(),packet.getPort(),1);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    		break;
	    	case 10:
	    		//这是发图片,要提取图片数据，等待完全收完后重组图片
	    		try{
		    		ret = 10;		    		
		    	    btTemp = new byte[4];
		    	    System.arraycopy(pt, 5, btTemp, 0, 4);  //取出4个字节
		    	    buffer =  ByteBuffer.wrap(btTemp); 
		    	    deviceID = buffer.getInt();  //设备id 
		    	    
		    	    byte[] smallbtTemp = new byte[2];
		    	    System.arraycopy(pt, 9, smallbtTemp, 0, 2);  //取出2个字节
		    	    buffer =  ByteBuffer.wrap(smallbtTemp); 		    	    
		    	    short totalpacketnum = buffer.getShort();
		    	    smallbtTemp = new byte[2];
		    	    System.arraycopy(pt, 11, smallbtTemp, 0, 2);  //取出2个字节
		    	    buffer =  ByteBuffer.wrap(smallbtTemp);		    	    
		    	    short currentpacketnum = buffer.getShort();
		    	    System.out.println("Server rev " + currentpacketnum);
		    	    btTemp = new byte[4];
		    	    System.arraycopy(pt, 13, btTemp, 0, 4);   //发送的图片名，不能太长，浪费资源
		    	    buffer =  ByteBuffer.wrap(btTemp);		    	    
		    	    Integer name = buffer.getInt();
		    	    String nameStr = name.toString();		    	    
		    	    btTemp = new byte[4];
		    	    System.arraycopy(pt, 17, btTemp, 0, 2);  //取出2个字节
		    	    //
		    	    buffer =  ByteBuffer.wrap(btTemp);
		    	    short datalength = buffer.getShort();		    	    
		    	    filePath += String.valueOf(deviceID); //按照设备ID将图片分别存放
		    	    filePath += "\\";
		    	    //如果设备ID的文件夹不存在就创建
		    	    try {
		    	    	   if (!(new File(filePath).isDirectory())) {
		    	    	    new File(filePath).mkdir();
		    	    	   }
		    	    	  } catch (SecurityException e) {
		    	    	   e.printStackTrace();
		    	    }
		    	    File file = new File(filePath + nameStr + ".jpg");
		    	    if(!file.exists()) 
		    	    {
		    	    	file.createNewFile(); // 不存在就创建新文件
		    	    }
		    	    else
		    	    {
		    	    	file.delete();
		    	    	file.createNewFile();
		    	    }
		    	    byte[] btFile = new byte[pt.length - paketheadlen];
		    	    //目前定的私有协议包头有19字节，所以从第19字节开始取数据，依次放入文件
		    	    
		    	    System.arraycopy(pt, paketheadlen, btFile, 0, pt.length - paketheadlen);
		    	    try { 
			    	    RandomAccessFile fdf = new RandomAccessFile(filePath + nameStr + ".jpg", "rw");
			    	    fdf.seek(currentpacketnum * UdpService.sendLen); // 跳过索引部分 ，因为如果图片没发完，每次发送的大小是固定的			    	    
			    	    //每次都执行随机写入，并且关闭文件句柄
			    	    fdf.write(btFile);  //写入文件
			    	    fdf.close();
		    	    } catch (Exception e) {  
		                e.printStackTrace();  
		            }  
		    	  //如果收到的是end包，就给终端APP发推送消息
		    	    String srt2=new String(btFile,"UTF-8");	
		    	    if(totalpacketnum == currentpacketnum || srt2.equals("end"))
		    	    {	//System.out.println(srt2);  	    	
						System.out.println("文件接收完毕");		
						//推送消息给终端APP
						mqttserver myMqtt = mqttserver.getInstance();
						myMqtt.sedMessage(nameStr,deviceID);
						break;
					}
		    	    //返回确认包
		    	    bf = ByteBuffer.allocate(ackpaketheadlen);
		    	    temp = ackpaketheadlen;
		    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
		    	    bf.put((byte) 11);    //11，对应16进制是BH，表示该包是图片的确认包
		    	    bf.put(StreamTool.short2byte(totalpacketnum));    // 总包数
		    	    bf.put(StreamTool.short2byte(currentpacketnum)); // 当前包的索引
		    	    bf.put(StreamTool.int2byte(name));  // 名称
		    	    bf.put((byte) 1);     //成功   	       	    
		    	    //这里实际上要给发送方回复, 以便客户端继续发送
		    	    packet.setData(bf.array());
		    	    System.out.println(Arrays.toString(bf.array()));
		    	    UdpService.response(packet);
		    	    
		    	    //UdpService.UpdateIpAddrMap(deviceID,packet.getAddress(),packet.getPort(),1);
		    	    try {
						UdpService.UpdateDeviceIpAddrDb(deviceID,packet.getAddress(),packet.getPort(),1);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    		}catch (Exception e) {
	    			e.printStackTrace();
	    		} 
	    		
	    		break;
	    	case 26:
	    		//这是app发起的控制命令
	    		try{
		    		ret = 26;
		    		btTemp = new byte[4];
		    	    System.arraycopy(pt, 5, btTemp, 0, 4);  //取出4个字节
		    	    buffer =  ByteBuffer.wrap(btTemp); 
		    	    userid = buffer.getInt();  //Userid 
		    	    
		    		btTemp = new byte[4];
		    	    System.arraycopy(pt, 9, btTemp, 0, 4);  //取出4个字节
		    	    buffer =  ByteBuffer.wrap(btTemp); 
		    	    deviceID = buffer.getInt();  //设备ID		
		    	    flag = pt[len - 1]; //标志位是最后一字节    
		    	    
		    	    //返回确认包
		    	    bf = ByteBuffer.allocate(14);
		    	    temp = 14;
		    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
		    	    bf.put((byte) 2);    //2，对应16进制是2H，表示该包的版本号
		    	    bf.put((byte) 53);    //S，对应ASC是大写的S，表示该包的设备类型为服务器下发
		    	    bf.put((byte) 26);    //26，对应16进制是1AH，表示该包是APP命令的确认包
		    	    bf.put(StreamTool.int2byte(userid));    // 总包数
		    	    bf.put(StreamTool.int2byte(deviceID)); // 当前包的索引
		    	    bf.put((byte) 1);     //成功   	       	    
		    	    //这里实际上要给发送方回复, 以便客户端继续发送
		    	    packet.setData(bf.array());
		    	    System.out.println(Arrays.toString(bf.array()));
		    	    UdpService.response(packet);
		    	    try {
						UdpService.UpdateUserAppIpAddrDb(userid,packet.getAddress(),packet.getPort(),1);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		    	    //处理过返回包，就要将命令下发到车机设备
		    	    UdpService.sendCMD(deviceID,flag);
		    	    
	    		}catch (Exception e) {
	    			e.printStackTrace();
	    		} 
	    		
	    		break;
	    	case 27:
	    		//这是车机发送的GIS信息
	    		try{
		    		ret = 27;
		    		btTemp = new byte[4];
		    	    System.arraycopy(pt, 5, btTemp, 0, 4);  //取出4个字节
		    	    buffer =  ByteBuffer.wrap(btTemp); 
		    	    deviceID = buffer.getInt();  //设备id 		    	    
		    		btTemp = new byte[64];
		    	    System.arraycopy(pt, 9, btTemp, 0, 64);  //取出64个字节
		    	    flag = pt[len - 1]; //标志位是最后一字节    		    	    
		    	    //返回确认包
		    	    bf = ByteBuffer.allocate(10);
		    	    temp = 10;
		    	    bf.put(StreamTool.short2byte(temp));    // 总长度   //可能还需要确认，修改
		    	    bf.put((byte) 2);     //2，对应16进制是2H，表示该包的版本号
		    	    bf.put((byte) 41);    //A，对应ASC是大写的A，表示该包的设备类型为服务器下发
		    	    bf.put((byte) 27);    //27，对应16进制是1BH，表示该包是车机GIS信息的确认包		    	    
		    	    bf.put(StreamTool.int2byte(deviceID)); // 当前包的索引
		    	    bf.put((byte) 1);     //成功   	       	    
		    	    //这里实际上要给发送方回复, 以便客户端继续发送
		    	    packet.setData(bf.array());
		    	    System.out.println(Arrays.toString(bf.array()));
		    	    UdpService.response(packet);    	
		    	    try {
						UdpService.UpdateDeviceIpAddrDb(deviceID,packet.getAddress(),packet.getPort(),1);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		    	    
	    		}catch (Exception e) {
	    			e.printStackTrace();
	    		} 
	    		
	    		break;
    	default:
    		ret = 0;
    	}
    	return ret;    	
    }
    
} 
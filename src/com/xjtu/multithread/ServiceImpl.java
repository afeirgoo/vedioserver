package com.xjtu.multithread;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @说明 打印收到的数据包，并且将数据原封返回，中间设置休眠表示执行耗时
 * @author 郭宇飞
 * @version 1.0
 * @since
 */
class ServiceImpl implements Runnable {
	private static final int paketheadlen = 23;
	private static final int ackpaketheadlen = 12;
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
			
			//Thread.sleep(1 * 1000); // 5秒才返回，标识服务端在处理数据
			// 设置回复的数据，原数据返回，以便客户端知道是那个客户端发送的数据
			packet.setData(bt);
			//UdpService.response(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
    	String filePath = "D:\\temp\\";
    	//所有数据包的前5个字节，都是该数据包的长度和3字节包头,如果数据少于5个字节，就不用解析了，丢掉
    	if(null == pt || pt.length <= 5)
    	{
    		return ret;
    	}
    	byte[] btTemp = new byte[4];
    	btTemp = new byte[4];
	    System.arraycopy(pt, 0, btTemp, 0, 4);  //取出4个字节
	    int len = StreamTool.bytesToInt(btTemp);  //数据包总长度    	
    	if(len != pt.length)
    	{
    		return ret;
    	}    	
    	
    	switch(pt[6])
    	{
	    	case 1: 
	    	    //这是一个心跳包,要提取ID维护设备状态
	    		ret = 1;
	    		break;
	    	case 9:
	    		//这是设备执行命令之后给服务器的返回信息,用于控制者知道设备执行情况
	    		ret = 9;
	    		break;
	    	case 10:
	    		//这是发图片,要提取图片数据，等待完全收完后重组图片
	    		try{
		    		ret = 10;
		    		btTemp = new byte[4];
		    	    System.arraycopy(pt, 7, btTemp, 0, 4);  //取出4个字节
		    	    int deviceID = StreamTool.bytesToInt(btTemp);  //设备ID
		    	    byte[] smallbtTemp = new byte[2];
		    	    System.arraycopy(pt, 11, smallbtTemp, 0, 2);  //取出2个字节
		    	    short totalpacketnum = StreamTool.byteToShort(smallbtTemp);
		    	    smallbtTemp = new byte[2];
		    	    System.arraycopy(pt, 13, smallbtTemp, 0, 2);  //取出2个字节
		    	    short currentpacketnum = StreamTool.byteToShort(smallbtTemp);   //当前包序号
		    	    System.out.println("Server rev " + currentpacketnum);
		    	    btTemp = new byte[4];
		    	    System.arraycopy(pt, 15, btTemp, 0, 4);   //发送的图片名，不能太长，浪费资源
		    	    Integer name = StreamTool.bytesToInt(btTemp);
		    	    String nameStr = name.toString();		    	    
		    	    btTemp = new byte[4];
		    	    System.arraycopy(pt, 19, btTemp, 0, 4);  //取出4个字节
		    	    short datalength = StreamTool.byteToShort(btTemp);   //纯图片数据的长度
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
		    	    if(!file.exists()) file.createNewFile(); // 不存在就创建新文件
		    	    RandomAccessFile fdf = new RandomAccessFile(filePath + nameStr + ".jpg", "rw");
		    	    fdf.seek(currentpacketnum * UdpService.sendLen); // 跳过索引部分 ，因为如果图片没发完，每次发送的大小是固定的
		    	    //目前定的私有协议包头有23字节，所以从第23字节开始取数据，依次放入文件
		    	    byte[] btFile = new byte[pt.length - paketheadlen];
		    	    System.arraycopy(pt, paketheadlen, btFile, 0, pt.length - paketheadlen);
		    	    //如果收到的是end包，就给终端APP发推送消息
		    	    String srt2=new String(btFile,"UTF-8");		    	    
		    	    if(srt2.equals("end")) {		    	    	
						System.out.println("文件接收完毕");		
						//推送消息给终端APP
						mqttserver myMqtt = mqttserver.getInstance();
						myMqtt.sedMessage();
						break;
					}
		    	    fdf.write(btFile);  //写入文件
		    	    fdf.close();
		    	    ByteBuffer bf = ByteBuffer.allocate(ackpaketheadlen);
		    	    short temp = ackpaketheadlen;
		    	    bf.put(StreamTool.shortToByte(temp));    // 总长度   //可能还需要确认，修改
		    	    bf.put((byte) 11);    //11，对应16进制是BH，表示该包是图片的确认包
		    	    bf.put(StreamTool.shortToByte(totalpacketnum));    // 总包数
		    	    bf.put(StreamTool.shortToByte(currentpacketnum)); // 当前包的索引
		    	    bf.put(StreamTool.intToByte(name));  // 名称
		    	    bf.put((byte) 1);     //成功   	       	    
		    	    //这里实际上要给发送方回复, 以便客户端继续发送
		    	    packet.setData(bf.array());
		    	    UdpService.response(packet);
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
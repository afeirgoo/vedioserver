package com.xjtu.udpserver;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
/**
 * UDP服务类
 * @author java小强
 */
public class LogFileSerGprs {
 private static int sendLen = 2048;
 private byte[] buffer = new byte[sendLen + 4 + 4 + 4]; // 内容 + 长度 + 索引 + 名称
 private static DatagramSocket ds = null;
 private DatagramPacket packet = null;
 private InetSocketAddress socketAddress = null;
 private static String filePath = "D:\\";
 public static void main(String[] args) throws Exception {
  String serverHost = "127.0.0.1";
  int serverPort = 3344;
  LogFileSerGprs udpServerSocket = new LogFileSerGprs(serverHost, serverPort);
  while (true) {
   byte[] re = udpServerSocket.receive();
   System.out.println("收到（" + re.length + "）：" + Arrays.toString(re));
   if(null != re && re.length > 13){
    byte[] btTemp = new byte[4];
    System.arraycopy(re, 0, btTemp, 0, 4); // 数组源，数组源拷贝的开始位子，目标，目标填写的开始位子，拷贝的长度
    int len = StreamTool.bytesToInt(btTemp);
    if(len == re.length){ // 标记的长度是否正确
     btTemp = new byte[4];
     System.arraycopy(re, 4, btTemp, 0, 4);
     int index = StreamTool.bytesToInt(btTemp);
     if(index >= 0){ // 标记的索引正确
      btTemp = new byte[4];
      System.arraycopy(re, 8, btTemp, 0, 4);
      Integer name = StreamTool.bytesToInt(btTemp);
      String nameStr = name.toString();
      File file = new File(filePath + nameStr + ".txt");
      if(!file.exists()) file.createNewFile(); // 不存在就创建新文件
      RandomAccessFile fdf = new RandomAccessFile(filePath + nameStr + ".txt", "rw");
      fdf.seek(index * sendLen); // 跳过索引部分
      byte[] btFile = new byte[re.length - 12];
      System.arraycopy(re, 12, btFile, 0, re.length - 12);
      fdf.write(btFile);
       
      ByteBuffer bf = ByteBuffer.allocate(16);
      bf.put(StreamTool.intToByte(16));    // 总长度
      bf.put(StreamTool.intToByte(index)); // 索引
      bf.put(StreamTool.intToByte(name));  // 名称
      bf.put(StreamTool.intToByte(1));     // 成功
      udpServerSocket.response(bf.array());
     }
    }
   }
  }  
 }
 /**
  * 构造函数，绑定主机和端口
  */
 public LogFileSerGprs(String host, int port) throws Exception {
  socketAddress = new InetSocketAddress(host, port);
  ds = new DatagramSocket(socketAddress);
  System.out.println("服务端启动!");
 }
 /**
  * 接收数据包，该方法会造成线程阻塞
  */
 public final byte[] receive() throws IOException {
  packet = new DatagramPacket(buffer, buffer.length);
  ds.receive(packet);
  byte[] re = new byte[packet.getLength()];
  System.arraycopy(packet.getData(), 0, re, 0, packet.getLength());
  return re;
 }
 /**
  * 将响应包发送给请求端
  */
 public final void response(byte[] info) throws IOException {
  System.out.println("客户端地址 : " + packet.getAddress().getHostAddress() + ",端口：" + packet.getPort());
  DatagramPacket dp = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
  dp.setData(info);
  ds.send(dp);
 }
}
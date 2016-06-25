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
 * UDP������
 * @author javaСǿ
 */
public class LogFileSerGprs {
 private static int sendLen = 2048;
 private byte[] buffer = new byte[sendLen + 4 + 4 + 4]; // ���� + ���� + ���� + ����
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
   System.out.println("�յ���" + re.length + "����" + Arrays.toString(re));
   if(null != re && re.length > 13){
    byte[] btTemp = new byte[4];
    System.arraycopy(re, 0, btTemp, 0, 4); // ����Դ������Դ�����Ŀ�ʼλ�ӣ�Ŀ�꣬Ŀ����д�Ŀ�ʼλ�ӣ������ĳ���
    int len = StreamTool.bytesToInt(btTemp);
    if(len == re.length){ // ��ǵĳ����Ƿ���ȷ
     btTemp = new byte[4];
     System.arraycopy(re, 4, btTemp, 0, 4);
     int index = StreamTool.bytesToInt(btTemp);
     if(index >= 0){ // ��ǵ�������ȷ
      btTemp = new byte[4];
      System.arraycopy(re, 8, btTemp, 0, 4);
      Integer name = StreamTool.bytesToInt(btTemp);
      String nameStr = name.toString();
      File file = new File(filePath + nameStr + ".txt");
      if(!file.exists()) file.createNewFile(); // �����ھʹ������ļ�
      RandomAccessFile fdf = new RandomAccessFile(filePath + nameStr + ".txt", "rw");
      fdf.seek(index * sendLen); // ������������
      byte[] btFile = new byte[re.length - 12];
      System.arraycopy(re, 12, btFile, 0, re.length - 12);
      fdf.write(btFile);
       
      ByteBuffer bf = ByteBuffer.allocate(16);
      bf.put(StreamTool.intToByte(16));    // �ܳ���
      bf.put(StreamTool.intToByte(index)); // ����
      bf.put(StreamTool.intToByte(name));  // ����
      bf.put(StreamTool.intToByte(1));     // �ɹ�
      udpServerSocket.response(bf.array());
     }
    }
   }
  }  
 }
 /**
  * ���캯�����������Ͷ˿�
  */
 public LogFileSerGprs(String host, int port) throws Exception {
  socketAddress = new InetSocketAddress(host, port);
  ds = new DatagramSocket(socketAddress);
  System.out.println("���������!");
 }
 /**
  * �������ݰ����÷���������߳�����
  */
 public final byte[] receive() throws IOException {
  packet = new DatagramPacket(buffer, buffer.length);
  ds.receive(packet);
  byte[] re = new byte[packet.getLength()];
  System.arraycopy(packet.getData(), 0, re, 0, packet.getLength());
  return re;
 }
 /**
  * ����Ӧ�����͸������
  */
 public final void response(byte[] info) throws IOException {
  System.out.println("�ͻ��˵�ַ : " + packet.getAddress().getHostAddress() + ",�˿ڣ�" + packet.getPort());
  DatagramPacket dp = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
  dp.setData(info);
  ds.send(dp);
 }
}
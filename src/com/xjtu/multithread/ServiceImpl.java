package com.xjtu.multithread;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.nio.ByteBuffer; 
import java.nio.ByteOrder; 
/**
 * @˵�� ��ӡ�յ������ݰ������ҽ�����ԭ�ⷵ�أ��м��������߱�ʾִ�к�ʱ
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
			System.out.println(packet.getAddress().getHostAddress() + "��" + packet.getPort() + "��" + Arrays.toString(bt));
			//�����յ������ݱ���
			parsepacket(bt);
			
			//Thread.sleep(1 * 1000); // 5��ŷ��أ���ʶ������ڴ�������
			// ���ûظ������ݣ�ԭ���ݷ��أ��Ա�ͻ���֪�����Ǹ��ͻ��˷��͵�����
			packet.setData(bt);
			//UdpService.response(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
     * 
     * <pre>
     * ���������ݰ���ʲô��������ͼƬ
     * </pre>
     * 
     * @param arr
     * @return
     */
    public int parsepacket(byte[] pt)
    {
    	int ret = 0;
    	int deviceID = 0;
    	String filePath = "C:\\Server\\";
    	//�������ݰ���ǰ5���ֽڣ����Ǹ����ݰ��ĳ��Ⱥ�3�ֽڰ�ͷ,�����������5���ֽڣ��Ͳ��ý����ˣ�����
    	if(null == pt || pt.length <= 5)
    	{
    		return ret;
    	}
    	byte[] btTemp = new byte[2];
    	btTemp = new byte[2];
	    System.arraycopy(pt, 0, btTemp, 0, 2);  //ȡ��2���ֽ�	
	    ByteBuffer buffer =  ByteBuffer.wrap(btTemp); 
	    //System.out.println(buffer.getShort()); 
	    //int len = buffer.getShort();
	    int len = StreamTool.byteToShort(btTemp);  //���ݰ��ܳ���    
	    byte[] str_MM;
	    byte[] str_STA;
	    byte flag;
    	if(len != pt.length)
    	{
    		return ret;
    	}    	
    	byte versionid = pt[2];
    	byte devicetype = pt[3];
    	switch(pt[4])
    	{
	    	case 1: 
	    	    //����һ��������,Ҫ��ȡIDά���豸״̬
	    		ret = 1;
	    		btTemp = new byte[4];
	    	    System.arraycopy(pt, 5, btTemp, 0, 4);  //ȡ��4���ֽ�
	    	    buffer =  ByteBuffer.wrap(btTemp); 
	    	    deviceID = buffer.getInt();
	    	    //deviceID = StreamTool.bytesToInt(btTemp);  //�豸ID
	    	    str_MM = new byte[16];
	    	    System.arraycopy(pt, 9, str_MM, 0, 16);  //ȡ��16���ֽ�
	    	    str_STA = new byte[len - 26];
	    	    System.arraycopy(pt, 25, str_STA, 0, (len - 26));  //ȡ��(len - 26)���ֽ�
	    	    flag = pt[len - 1]; //��־λ�����һ�ֽ�
	    	    //System.out.println(len);	
	    	    //System.out.println(str_MM.toString());	
	    	    //System.out.println(str_STA.toString());	
	    	    //���췵�ذ�
	    	    ByteBuffer bf = ByteBuffer.allocate(ackheartpaketheadlen);
	    	    short temp = ackheartpaketheadlen;
	    	    bf.put(StreamTool.shortToByte(temp));    // �ܳ���   //���ܻ���Ҫȷ�ϣ��޸�
	    	    bf.put(versionid);    //�汾��
	    	    bf.put(devicetype);    //�豸����
	    	    bf.put((byte) 1);    //1��Ӧ16������1H����ʾ�ð�����������ȷ�ϰ�
	    	    bf.put((byte) 1);    //ȷ�Ͻ��
	    	    String time = (new Timestamp(new Date().getTime())).toString().subSequence(0, 19).toString();	    	    
	    	    bf.put(time.getBytes());    // ʱ��� 	    
	    	    
	    	    bf.put((byte) 5);     //�ɹ�   	       	    
	    	    //����ʵ����Ҫ�����ͷ��ظ�, �Ա�ͻ��˼�������
	    	    packet.setData(bf.array());
	    	    UdpService.response(packet);
	    		break;
	    	case 9:
	    		//�����豸ִ������֮����������ķ�����Ϣ,���ڿ�����֪���豸ִ�����
	    		ret = 9;	    		
	    		btTemp = new byte[4];
	    	    System.arraycopy(pt, 5, btTemp, 0, 4);  //ȡ��4���ֽ�
	    	    buffer =  ByteBuffer.wrap(btTemp); 
	    	    //int userID = buffer.getInt();
	    	    int userID = StreamTool.bytesToInt(btTemp);
	    	    btTemp = new byte[4];
	    	    System.arraycopy(pt, 9, btTemp, 0, 4);  //ȡ��4���ֽ�
	    	    buffer =  ByteBuffer.wrap(btTemp); 
	    	    //int ID = buffer.getInt();
	    	    int ID = StreamTool.bytesToInt(btTemp);
	    	    str_MM = new byte[16];
	    	    System.arraycopy(pt, 13, str_MM, 0, 16);  //ȡ��16���ֽ�
	    	    str_STA = new byte[len - 30];
	    	    System.arraycopy(pt, 29, str_STA, 0, (len - 30));  //ȡ��(len - 26)���ֽ�
	    	    flag = pt[len - 1]; //��־λ�����һ�ֽ�
	    	    
	    	    ByteBuffer ackbf = ByteBuffer.allocate(7);
	    	    short templen = 7;
	    	    ackbf.put(StreamTool.shortToByte(templen));    // �ܳ���   //���ܻ���Ҫȷ�ϣ��޸�
	    	    ackbf.put(versionid);    //�汾��
	    	    ackbf.put(devicetype);    //�豸����
	    	    ackbf.put((byte) 9);    //9��Ӧ16������9H����ʾ�ð����豸ִ�н������ȷ�ϰ�
	    	    ackbf.put((byte) 1);    //ȷ�Ͻ�� 	       
	    	    
	    	    ackbf.put((byte) 5);     //�ɹ�   	       	    
	    	    //����ʵ����Ҫ�����ͷ��ظ�, �Ա�ͻ��˼�������
	    	    packet.setData(ackbf.array());
	    	    UdpService.response(packet);  		
	    		
	    		
	    		break;
	    	case 10:
	    		//���Ƿ�ͼƬ,Ҫ��ȡͼƬ���ݣ��ȴ���ȫ���������ͼƬ
	    		try{
		    		ret = 10;
		    		btTemp = new byte[4];
		    	    System.arraycopy(pt, 5, btTemp, 0, 4);  //ȡ��4���ֽ�
		    	    buffer =  ByteBuffer.wrap(btTemp); 
		    	    deviceID = buffer.getInt();
		    	    deviceID = StreamTool.bytesToInt(btTemp);  //�豸ID
		    	    byte[] smallbtTemp = new byte[2];
		    	    System.arraycopy(pt, 9, smallbtTemp, 0, 2);  //ȡ��2���ֽ�
		    	    buffer =  ByteBuffer.wrap(smallbtTemp); 
		    	    
		    	    short totalpacketnum = StreamTool.byteToShort(smallbtTemp);
		    	    //short totalpacketnum = buffer.getShort();
		    	    smallbtTemp = new byte[2];
		    	    System.arraycopy(pt, 11, smallbtTemp, 0, 2);  //ȡ��2���ֽ�
		    	    buffer =  ByteBuffer.wrap(smallbtTemp);
		    	    short currentpacketnum = StreamTool.byteToShort(smallbtTemp);   //��ǰ�����
		    	    //short currentpacketnum = buffer.getShort();
		    	    System.out.println("Server rev " + currentpacketnum);
		    	    btTemp = new byte[4];
		    	    System.arraycopy(pt, 13, btTemp, 0, 4);   //���͵�ͼƬ��������̫�����˷���Դ
		    	    buffer =  ByteBuffer.wrap(btTemp);
		    	    Integer name = StreamTool.bytesToInt(btTemp);
		    	    //Integer name = buffer.getInt();
		    	    String nameStr = name.toString();		    	    
		    	    btTemp = new byte[4];
		    	    System.arraycopy(pt, 17, btTemp, 0, 2);  //ȡ��2���ֽ�
		    	    //
		    	    buffer =  ByteBuffer.wrap(btTemp);
		    	    //short datalength = buffer.getShort();
		    	    short datalength = StreamTool.byteToShort(btTemp);   //��ͼƬ���ݵĳ���
		    	    filePath += String.valueOf(deviceID); //�����豸ID��ͼƬ�ֱ���
		    	    filePath += "\\";
		    	    //����豸ID���ļ��в����ھʹ���
		    	    try {
		    	    	   if (!(new File(filePath).isDirectory())) {
		    	    	    new File(filePath).mkdir();
		    	    	   }
		    	    	  } catch (SecurityException e) {
		    	    	   e.printStackTrace();
		    	    }
		    	    File file = new File(filePath + nameStr + ".jpg");
		    	    if(!file.exists()) file.createNewFile(); // �����ھʹ������ļ�
		    	    RandomAccessFile fdf = new RandomAccessFile(filePath + nameStr + ".jpg", "rw");
		    	    fdf.seek(currentpacketnum * UdpService.sendLen); // ������������ ����Ϊ���ͼƬû���꣬ÿ�η��͵Ĵ�С�ǹ̶���
		    	    //Ŀǰ����˽��Э���ͷ��19�ֽڣ����Դӵ�19�ֽڿ�ʼȡ���ݣ����η����ļ�
		    	    byte[] btFile = new byte[pt.length - paketheadlen];
		    	    System.arraycopy(pt, paketheadlen, btFile, 0, pt.length - paketheadlen);
		    	    //ÿ�ζ�ִ�����д�룬���ҹر��ļ����
		    	    fdf.write(btFile);  //д���ļ�
		    	    fdf.close();
		    	  //����յ�����end�����͸��ն�APP��������Ϣ
		    	    String srt2=new String(btFile,"UTF-8");	
		    	    if(totalpacketnum == currentpacketnum || srt2.equals("end"))
		    	    {	//System.out.println(srt2);  	    	
						System.out.println("�ļ��������");		
						//������Ϣ���ն�APP
						mqttserver myMqtt = mqttserver.getInstance();
						myMqtt.sedMessage();
						break;
					}
		    	    //����ȷ�ϰ�
		    	    bf = ByteBuffer.allocate(ackpaketheadlen);
		    	    temp = ackpaketheadlen;
		    	    bf.put(StreamTool.shortToByte(temp));    // �ܳ���   //���ܻ���Ҫȷ�ϣ��޸�
		    	    bf.put((byte) 11);    //11����Ӧ16������BH����ʾ�ð���ͼƬ��ȷ�ϰ�
		    	    bf.put(StreamTool.shortToByte(totalpacketnum));    // �ܰ���
		    	    bf.put(StreamTool.shortToByte(currentpacketnum)); // ��ǰ��������
		    	    bf.put(StreamTool.intToByte(name));  // ����
		    	    bf.put((byte) 1);     //�ɹ�   	       	    
		    	    //����ʵ����Ҫ�����ͷ��ظ�, �Ա�ͻ��˼�������
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
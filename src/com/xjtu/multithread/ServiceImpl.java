package com.xjtu.multithread;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @˵�� ��ӡ�յ������ݰ������ҽ�����ԭ�ⷵ�أ��м��������߱�ʾִ�к�ʱ
 * @author �����
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
    	String filePath = "D:\\temp\\";
    	//�������ݰ���ǰ5���ֽڣ����Ǹ����ݰ��ĳ��Ⱥ�3�ֽڰ�ͷ,�����������5���ֽڣ��Ͳ��ý����ˣ�����
    	if(null == pt || pt.length <= 5)
    	{
    		return ret;
    	}
    	byte[] btTemp = new byte[4];
    	btTemp = new byte[4];
	    System.arraycopy(pt, 0, btTemp, 0, 4);  //ȡ��4���ֽ�
	    int len = StreamTool.bytesToInt(btTemp);  //���ݰ��ܳ���    	
    	if(len != pt.length)
    	{
    		return ret;
    	}    	
    	
    	switch(pt[6])
    	{
	    	case 1: 
	    	    //����һ��������,Ҫ��ȡIDά���豸״̬
	    		ret = 1;
	    		break;
	    	case 9:
	    		//�����豸ִ������֮����������ķ�����Ϣ,���ڿ�����֪���豸ִ�����
	    		ret = 9;
	    		break;
	    	case 10:
	    		//���Ƿ�ͼƬ,Ҫ��ȡͼƬ���ݣ��ȴ���ȫ���������ͼƬ
	    		try{
		    		ret = 10;
		    		btTemp = new byte[4];
		    	    System.arraycopy(pt, 7, btTemp, 0, 4);  //ȡ��4���ֽ�
		    	    int deviceID = StreamTool.bytesToInt(btTemp);  //�豸ID
		    	    byte[] smallbtTemp = new byte[2];
		    	    System.arraycopy(pt, 11, smallbtTemp, 0, 2);  //ȡ��2���ֽ�
		    	    short totalpacketnum = StreamTool.byteToShort(smallbtTemp);
		    	    smallbtTemp = new byte[2];
		    	    System.arraycopy(pt, 13, smallbtTemp, 0, 2);  //ȡ��2���ֽ�
		    	    short currentpacketnum = StreamTool.byteToShort(smallbtTemp);   //��ǰ�����
		    	    System.out.println("Server rev " + currentpacketnum);
		    	    btTemp = new byte[4];
		    	    System.arraycopy(pt, 15, btTemp, 0, 4);   //���͵�ͼƬ��������̫�����˷���Դ
		    	    Integer name = StreamTool.bytesToInt(btTemp);
		    	    String nameStr = name.toString();		    	    
		    	    btTemp = new byte[4];
		    	    System.arraycopy(pt, 19, btTemp, 0, 4);  //ȡ��4���ֽ�
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
		    	    //Ŀǰ����˽��Э���ͷ��23�ֽڣ����Դӵ�23�ֽڿ�ʼȡ���ݣ����η����ļ�
		    	    byte[] btFile = new byte[pt.length - paketheadlen];
		    	    System.arraycopy(pt, paketheadlen, btFile, 0, pt.length - paketheadlen);
		    	    //����յ�����end�����͸��ն�APP��������Ϣ
		    	    String srt2=new String(btFile,"UTF-8");		    	    
		    	    if(srt2.equals("end")) {		    	    	
						System.out.println("�ļ��������");		
						//������Ϣ���ն�APP
						mqttserver myMqtt = mqttserver.getInstance();
						myMqtt.sedMessage();
						break;
					}
		    	    fdf.write(btFile);  //д���ļ�
		    	    fdf.close();
		    	    ByteBuffer bf = ByteBuffer.allocate(ackpaketheadlen);
		    	    short temp = ackpaketheadlen;
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
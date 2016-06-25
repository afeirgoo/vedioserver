package com.xjtu.filestran;

import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileNotFoundException;  
import java.io.IOException;  
import java.io.InputStream;  
import java.net.DatagramPacket;  
import java.net.DatagramSocket;  
import java.net.SocketException;  
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  
  
/** 
 * 涓嬭浇鏈嶅姟鍣�,閲囩敤UDP鍗忚锛屼紶閫佺殑杩囩▼涓彲鑳戒細涓㈠寘锛屽鑷翠笅杞界殑鏂囦欢涓嶅畬鏁� 
 */  
public class DownloadServer {  
    // 鎻愪緵鏈嶅姟   
    public void service() {  
        try {  
            // 鍒涘缓鏈満鎸囧畾绔彛8289鐨勬湇鍔″櫒   
            DatagramSocket dataSocket = new DatagramSocket(8289);  
            // 绾跨▼姹狅紝鍥哄畾鏈夊崄涓嚎绋�   
            ExecutorService ThreadPool = Executors.newFixedThreadPool(10);  
  
            while (true) {// 涓嶆柇鎺ユ敹鏉ヨ嚜瀹㈡埛绔殑璇锋眰   
                byte[] buff = new byte[101];// 鏂囦欢鍚嶉暱搴︿笉瓒呰繃50   
                DatagramPacket dataPacket = new DatagramPacket(buff, buff.length);  
                dataSocket.receive(dataPacket);// 绛夊緟鎺ユ敹鏉ヨ嚜瀹㈡埛绔殑鏁版嵁鍖�   
                // 鎺ユ敹鍒版暟鎹寘锛屽紑涓�涓嚎绋嬩负璇ュ鎴锋湇鍔�   
                ThreadPool.execute(new WorkThread(dataPacket));  
            }  
        } catch (SocketException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
    // 鍐呴儴绫伙紝涓烘瘡涓鎴锋彁渚涙湇鍔�   
    private class WorkThread implements Runnable {  
        private DatagramPacket packet;  
        private DatagramSocket dataSocket;  
  
        public WorkThread(DatagramPacket packet) {  
            this.packet = packet;  
            try {// 鍒涘缓鏈満鍙互绔彛鐨凞atagramSocket   
                dataSocket = new DatagramSocket();  
            } catch (SocketException e) {  
                e.printStackTrace();  
            }  
        }  
  
        // 鑾峰彇鍙互涓嬭浇鐨勬枃浠跺垪琛ㄤ紶閫佺粰瀹㈡埛绔�   
        private void showFiles() {  
            File files = new File("upload_download");  
            File[] allFile = files.listFiles();// 鑾峰彇鎵�鏈夋枃浠�   
            StringBuffer message = new StringBuffer();  
            for (File f : allFile) {  
                if (f.isFile()) {  
                    message.append(f.getName());  
                    message.append('\n');  
                }  
            }  
            // 鏋勯�犲搷搴旀暟鎹寘   
            byte[] response = message.toString().getBytes();  
            DatagramPacket dataPacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());  
            try {// 鍙戦��   
                dataSocket.send(dataPacket);  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
  
        // 涓嬭浇鎸囧畾鐨勬枃浠�   
        private void download(String fileName) {  
            try {  
                InputStream in = new FileInputStream("upload_download/" + fileName);  
                DatagramPacket dataPacket;  
                byte[] response = new byte[60000];// 姣忔鍙戦��60000瀛楄妭   
                while (true) {  
                    int len = in.read(response, 0, response.length);  
                    dataPacket = new DatagramPacket(response, len, packet.getAddress(), packet.getPort());  
                    dataSocket.send(dataPacket);// 鍙戦��   
                    if (in.available() == 0)// 鍙戦�佸畬姣�   
                        break;  
                }  
                in.close();  
            } catch (FileNotFoundException e) {  
                e.printStackTrace();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
  
        @Override  
        public void run() {  
            // 鑾峰彇瀹㈡埛绔紶閫佽繃鏉ョ殑鏁版嵁   
            byte[] data = packet.getData();  
            // 琛ㄧず瀹㈡埛绔偣鍑绘樉绀烘枃浠舵寜閽紝璇ヨ姹傛槸瑕佸緱鍒版墍鏈夊彲浠ヤ笅杞界殑鏂囦欢   
            if (data[0] == 0)  
                showFiles();  
            else if (data[0] == 1)// 琛ㄧず瀹㈡埛绔殑璇锋眰鏄笅杞借姹�   
                download(new String(data, 1, packet.getLength()).trim());  
            else  
                System.out.println("请求错误");  
        }  
    }  
  
    public static void main(String[] args) {  
        new DownloadServer().service();  
    }  
}   
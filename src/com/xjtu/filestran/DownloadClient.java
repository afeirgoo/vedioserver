package com.xjtu.filestran; 
import java.awt.BorderLayout;  
import java.awt.GridLayout;  
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
import java.io.File;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.OutputStream;  
import java.net.DatagramPacket;  
import java.net.DatagramSocket;  
import java.net.InetAddress;  
  
import javax.swing.*;  
  
/** 
 * 涓嬭浇瀹㈡埛绔� 
 */  
public class DownloadClient extends JFrame {  
    // 鏄剧ず鍙笅杞界殑鏂囦欢   
    private JTextArea textArea = new JTextArea();  
  
    private JPanel panel = new JPanel();  
    // 涓嬭浇鏃朵繚瀛樻枃浠�   
    private JFileChooser saveFile = new JFileChooser(".");  
  
    private JButton showButton = new JButton("显示文件");  
    private JButton downloadButton = new JButton("下载...");  
    // 涓嬭浇鏃跺～鍏ヨ涓嬭浇鐨勬枃浠跺悕锛屾敞鎰忔枃浠跺悕蹇呴』鏄痶extArea鏄剧ず鐨勬枃浠跺悕   
    private JTextField downloadFile = new JTextField("");  
  
    private DatagramSocket dataSocket=null;  
  
    public DownloadClient() {  
        // frame 鐨勫熀鏈缃�   
        this.setTitle("下载客户端");  
        this.setVisible(true);  
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        this.setSize(400, 500);  
        this.setLayout(new BorderLayout());  
        this.setResizable(false);  
  
        // 璁剧疆涓嶅彲缂栬緫   
        textArea.setEditable(false);  
  
        panel.setLayout(new GridLayout(3, 2, 5, 5));  
        panel.add(new JLabel("点击按钮显示可下载的文件"));  
        panel.add(showButton);  
        panel.add(downloadFile);  
        panel.add(downloadButton);  
  
        // 缁勪欢鍔犲叆frame涓�   
        add(new JScrollPane(textArea));  
        add(panel, BorderLayout.SOUTH);  
  
        // saveFile鍙兘鎵撳紑鐩綍   
        saveFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);  
  
        // 鏄剧ず鏂囦欢鎸夐挳娉ㄥ唽浜嬩欢   
        showButton.addActionListener(new ActionListener() {  
            @Override  
            public void actionPerformed(ActionEvent e) {  
                showButton.setEnabled(false);  
                downloadButton.setEnabled(false);  
                showFiles();  
                showButton.setEnabled(true);  
                downloadButton.setEnabled(true);  
            }  
        });  
  
        // 涓嬭浇鎸夐挳娉ㄥ唽浜嬩欢   
        downloadButton.addActionListener(new ActionListener() {  
            @Override  
            public void actionPerformed(ActionEvent e) {  
                showButton.setEnabled(false);  
                downloadButton.setEnabled(false);  
                downloadFile();  
                showButton.setEnabled(true);  
                downloadButton.setEnabled(true);  
            }  
        });  
    }  
  
    // 鏄剧ず鏂囦欢   
    private void showFiles() {  
        try {  
            if (dataSocket == null)  
                dataSocket = new DatagramSocket();  
            // 鍒涘缓鍙戦�佹暟鎹寘骞跺彂閫佺粰鏈嶅姟鍣�   
            byte[] request = { 0 };  
            DatagramPacket requestPacket = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), 8289);  
            dataSocket.send(requestPacket);  
  
            // 鎺ユ敹鏈嶅姟鍣ㄧ殑鏁版嵁鍖咃紝鏄剧ず鍦╰extArea涓�   
            byte[] receive = new byte[1024 * 1024];  
            DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);  
            dataSocket.receive(receivePacket);  
            String str = new String(receivePacket.getData(), 0, receivePacket.getLength());  
            textArea.setText(str);  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
    // 涓嬭浇鏂囦欢   
    private void downloadFile() {  
        // 鑾峰彇瑕佷笅杞界殑鏂囦欢鍚�   
        String fileName = downloadFile.getText().trim();  
        // 鎵�鏈夊彲浠ヤ笅杞界殑鏂囦欢   
        String allFiles = textArea.getText();  
        // 鏂囦欢鍚嶄负绌�   
        if (fileName == null || "".equals(fileName))  
            JOptionPane.showMessageDialog(null, "请选中正确的文件名", "文件名错误", JOptionPane.WARNING_MESSAGE);  
        // 鏂囦欢鍚嶆槸鍦ㄥ彲浠ヤ笅杞界殑鏂囦欢涓�   
        else if (allFiles.contains((fileName + '\n'))) {  
            saveFile.showSaveDialog(null);  
            File f = saveFile.getSelectedFile();// 鑾峰彇閫変腑鐨勬枃浠跺す   
            if (f.exists()) {  
                // 妫�娴嬭鏂囦欢鏄惁宸茬粡瀛樺湪浜庣洰褰曚腑   
                String[] fileNames = f.list();  
                boolean exit = false;  
                for (String name : fileNames)  
                    if (name.equals(fileName)) {  
                        exit = true;  
                        break;  
                    }  
  
                if (exit)// 濡傛灉瑕佷笅杞界殑鏂囦欢宸茬粡瀛樺湪   
                    JOptionPane.showMessageDialog(null, "此文件已经存在", "请选择另外的文件下载", JOptionPane.WARNING_MESSAGE);  
                else {  
                    // 鍙戦�佺殑璇锋眰   
                    byte[] request = (new String(new byte[] { 1 }) + fileName).getBytes();  
                    try {  
                        if (dataSocket == null)  
                            dataSocket = new DatagramSocket();  
                        // 鍒涘缓鍙戦�佹暟鎹寘骞跺彂閫佺粰鏈嶅姟鍣�   
                        DatagramPacket requestPacket = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), 8289);  
                        dataSocket.send(requestPacket);  
  
                        // 鎺ユ敹鏈嶅姟鍣ㄧ殑鏁版嵁鍖�,鎶婃枃浠朵繚瀛樺湪閫変腑鐨勬枃浠跺す涓�   
                        OutputStream out = new FileOutputStream(f.getAbsolutePath() + "/" + fileName, true);  
                        byte[] receive = new byte[60000];// 姣忔鎺ユ敹60000瀛楄妭   
                        DatagramPacket receivePacket;  
                        // 涓嶆柇鎺ユ敹鏉ヨ嚜鏈嶅姟鍣ㄧ殑鏁版嵁鍖�   
                        while (true) {  
                            receivePacket = new DatagramPacket(receive, receive.length);  
                            dataSocket.receive(receivePacket);  
                            out.write(receivePacket.getData(), 0, receivePacket.getLength());// 杈撳嚭娴佹妸鏂囦欢鍐呭杈撳嚭鍒版枃浠朵腑   
                            out.flush();  
                            if (receivePacket.getLength() != receive.length)  
                                break;  
                        }  
                        out.close();  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
  
            } else  
                // 閫夋嫨鐨勬枃浠跺す涓嶅瓨鍦�   
                JOptionPane.showMessageDialog(null, "请选择正确的存储路径", "存储路径错误", JOptionPane.WARNING_MESSAGE);  
  
        } else {// 鏂囦欢鍚嶉敊璇�   
            JOptionPane.showMessageDialog(null, "请选择正确的文件名", "文件名错误", JOptionPane.WARNING_MESSAGE);  
        }  
    }  
  
    public static void main(String[] args) {  
        new DownloadClient();  
    }  
}  
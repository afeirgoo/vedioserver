package com.xjtu.udpserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class FilesTransport extends JFrame implements ActionListener {
    private String file = "D:\\receive.doc";
    private JLabel mesg = null;
    private boolean isReceive = false;
    public FilesTransport() {
        getContentPane().setLayout(null);
        mesg = new JLabel("");
        mesg.setBounds(12, 10, 202, 22);
        getContentPane().add(mesg);
        JLabel label = new JLabel("Receive File : D:\\receive.doc");
        label.setBounds(12, 43, 202, 22);
        getContentPane().add(label);
        JButton btnNewButton = new JButton("Receive");
        btnNewButton.setBounds(12, 75, 93, 23);
        btnNewButton.addActionListener(this);
        getContentPane().add(btnNewButton);
        new ReceiveThread().start();
        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }
    public static void main(String[] args) {
        new FilesTransport();
    }
    public void actionPerformed(ActionEvent actionevent) {
        if (isReceive) {
            return;
        }
        isReceive = true;
    }
    class ReceiveThread extends Thread {
        public void run() {
            try {
                DatagramSocket server = new DatagramSocket(5050);
                byte[] recvBuf = new byte[4096];
                DatagramPacket recvPacket = new DatagramPacket(recvBuf,
                        recvBuf.length);
                while (true) {
                    try {
                        //System.out.println(server.getSendBufferSize());
                        if (server.getSendBufferSize() > 0) {
                            mesg.setText("client Send file!");
                        }
                        if (isReceive) {
                            FileOutputStream out = new FileOutputStream(file);
                            while (server.getSendBufferSize() > 0) {
                                server.receive(recvPacket);
                                if("end".equals(new String(recvPacket.getData()))){
                                    break;
                                }
                                out.write(recvPacket.getData(), 0, recvPacket
                                        .getLength());
                            }
                            out.close();
                            isReceive = false;
                        }
                        sleep(500);
                    } catch (Exception e) {
                        System.out.println("ignore");
                    }
                }
            } catch (Exception e) {
                System.out.println("Server init error");
                System.exit(0);
            }
        }
    }
}
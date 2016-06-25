package com.xjtu.vedionetwork;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class vediomainserver
{
    public static void main(String[] args) throws Exception
    {    	
        ServerSocket serverSocket = new ServerSocket(4000);

        while (true)
        {
            // 一直处于监听状态,这样可以处理多个用户
            Socket socket = serverSocket.accept();

            // 启动读写线程
            new ServerInputThread(socket).start();
            new ServerOutputThread(socket).start();
        }

    }

}
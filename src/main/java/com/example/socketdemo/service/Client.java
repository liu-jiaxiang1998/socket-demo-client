package com.example.socketdemo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client implements Runnable {
    private final String threadName;

    public Client(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            /** 只要服务端监听了某个端口，即使serverSocket.accept(); 这一行还没有执行，只是执行了下面这一行，仍然是属于建立连接了！！此时再报错就是读超时！！ */
            socket.connect(new InetSocketAddress("localhost", 8999), 5000);//连接超时
//            socket.setSoTimeout(10000);//读取超时

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 发送消息给服务器
            out.println("线程名称：" + threadName + "。客户端\nip地址为" + InetAddress.getLocalHost().getHostAddress());
            out.println("线程名称：" + threadName + "。客户端设备名称为" + InetAddress.getLocalHost().getHostName());
            out.println("end");

            // 接收服务器的响应
            String response;
            int count = 0;
            while ((response = in.readLine()) != null) {
                System.out.println("线程名称：" + threadName + "。行数：" + (count++) + "。服务端响应：" + response);
            }
            System.out.println("线程名称：" + threadName + "执行完成！！");
            // 关闭连接
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("111+" + e.getMessage());
        }
    }
}

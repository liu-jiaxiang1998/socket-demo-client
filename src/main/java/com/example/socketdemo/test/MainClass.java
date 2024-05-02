package com.example.socketdemo.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainClass {
    public static void main(String[] args) {
        // 创建一个用于存储接收到的串口数据的阻塞队列
        BlockingQueue<Byte> dataQueue = new LinkedBlockingQueue<>();
        // 创建一个串口数据接收线程，并将阻塞队列作为参数传入
        Thread thread = new Thread(new SerialPortUtil(dataQueue));
        thread.start();

        // 在主线程中获取从串口接收到的数据
        while(true){
            try {
                Byte receivedData = dataQueue.take();
                // 在这里可以对接收到的数据进行处理或者传递到其他地方使用
                System.out.println("Received data: " + receivedData);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



}

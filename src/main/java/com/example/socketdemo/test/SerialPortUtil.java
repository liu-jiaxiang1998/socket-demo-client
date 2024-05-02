package com.example.socketdemo.test;

import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

/**
* 称重帧的长度：2轴 41   3轴 50    4轴 59    5轴 68    6轴 77
* date: 2024/5/2
* author: ljx
*/
@Slf4j
public class SerialPortUtil implements Runnable {
    private BlockingQueue<Byte> dataQueue;
    private SerialPort serialPort;

    public SerialPortUtil(BlockingQueue<Byte> dataQueue) {
        this.dataQueue = dataQueue;
        this.serialPort = SerialPort.getCommPort("/dev/ttyTHS0");
        this.serialPort.setBaudRate(9600);
    }

    @Override
    public void run() {
        for (int i = 0; i < 3; i++) {
            if (this.serialPort.openPort()) {
                log.info("Port opened successfully.");
                break;
            }
            else {
                log.error("Failed to open port.Try to reconnect!");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        }

        if (!this.serialPort.openPort()) {
            log.error("Failed to open port.Terminal serial port thread!");
            return;
        }

        while (true) {
            try {
                    if (this.serialPort.bytesAvailable() > 0) {
                        byte[] buffer = new byte[this.serialPort.bytesAvailable()];
                        int bytesRead = this.serialPort.readBytes(buffer, buffer.length);
                        for (int i = 0; i < bytesRead; i++) {
                            this.dataQueue.put(buffer[i]);
                        }
                    }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        // 获取所有可用的串口列表
        SerialPort[] ports = SerialPort.getCommPorts();

        // 遍历所有串口，并打印串口信息
        for (SerialPort port : ports) {
            System.out.println("Port Name: " + port.getSystemPortName());
            System.out.println("Description: " + port.getDescriptivePortName());
            System.out.println("Baud Rate: " + port.getBaudRate());
            System.out.println("========================================");
        }

        // 选择一个串口，打开并设置参数
        SerialPort chosenPort = SerialPort.getCommPort("COM3");
        chosenPort.setBaudRate(9600);

        // 打开串口
        if (chosenPort.openPort()) {
            System.out.println("Port opened successfully.");
        } else {
            System.err.println("Failed to open port.");
            return;
        }

        // 读取串口数据
        byte[] buffer = new byte[1024];
        int bytesRead = chosenPort.readBytes(buffer, buffer.length);
        System.out.println("Received " + bytesRead + " bytes: " + new String(buffer, 0, bytesRead));

        // 关闭串口
        chosenPort.closePort();
    }
}

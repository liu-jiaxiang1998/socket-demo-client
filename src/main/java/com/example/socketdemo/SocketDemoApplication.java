package com.example.socketdemo;

import com.example.socketdemo.communicate.*;
import com.example.socketdemo.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 通信部分还是单独写各个类，毕竟一个发送接收相机的socket，一个只向工控机发送自定义消息数据的socket，以及一个持续接收 Serial Port 数据的通道，这三个想要统一化还是很难的！！
 * date: 2024/5/3
 * author: ljx
 */
@SpringBootApplication
@Slf4j
public class SocketDemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SocketDemoApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Override
    public void run(String... args) {
//        new Thread(new Client("Thread1")).start();
//        Thread.sleep(3000);
//        new Thread(new Client("Thread2")).start();
        BlockingQueue<CameraCaptureCommand> commandQueue = new LinkedBlockingQueue<>();
        ConcurrentHashMap<Integer, PassingFrame> passingFrameMap = new ConcurrentHashMap<Integer, PassingFrame>();
        BlockingQueue<CameraCaptureResult> cameraCaptureResultQueue = new LinkedBlockingQueue<>();
        BlockingQueue<ToGKJMessage> toGKJMessageQueue = new LinkedBlockingQueue<>();
        ConcurrentHashMap<Integer, String> infraredCaptureResultMap = new ConcurrentHashMap<>();
        BlockingQueue<Byte> serialPortDataQueue = new LinkedBlockingQueue<>();

        new Thread(new CameraSocket(ProjectProperties.CE_CAMERA_IP, Integer.valueOf(ProjectProperties.CE_CAMERA_PORT), commandQueue, passingFrameMap, cameraCaptureResultQueue)).start();
        log.info("侧身相机通信线程已开启");
        new Thread(new LocalSocket(Integer.valueOf(ProjectProperties.LOCAL_LISTEN_PORT), toGKJMessageQueue, infraredCaptureResultMap, cameraCaptureResultQueue)).start();
        log.info("本地python程序通信线程已开启");
        new Thread(new SerialPortCommunication(serialPortDataQueue)).start();
        log.info("串口通信线程已开启");
        new Thread(new DecodeSerialPortThread(serialPortDataQueue, commandQueue, toGKJMessageQueue, passingFrameMap, infraredCaptureResultMap)).start();
        log.info("解码串口数据线程已开启");
        new Thread(new ClientSocket(ProjectProperties.REMOTE_IP, Integer.valueOf(ProjectProperties.REMOTE_COMMUNICATE_PORT), toGKJMessageQueue)).start();
        log.info("工控机通信线程已开启");
    }
}

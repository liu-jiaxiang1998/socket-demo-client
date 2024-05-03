package com.example.socketdemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 通信部分还是单独写各个类，毕竟一个发送接收相机的socket，一个只向工控机发送自定义消息数据的socket，以及一个持续接收 Serial Port 数据的通道，这三个想要统一化还是很难的！！
 * date: 2024/5/3
 * author: ljx
 */
@SpringBootApplication
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


    }
}

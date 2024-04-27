package com.example.socketdemo;

import com.example.socketdemo.service.Client;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SocketDemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SocketDemoApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        new Thread(new Client("Thread1")).start();
        Thread.sleep(3000);
        new Thread(new Client("Thread2")).start();
    }
}

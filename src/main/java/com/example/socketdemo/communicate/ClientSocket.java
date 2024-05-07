package com.example.socketdemo.communicate;

import com.example.socketdemo.entity.ToGKJMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * 和工控机通信
 * date: 2024/5/6
 * author: ljx
 */
@Slf4j
public class ClientSocket implements Runnable {

    private String gkjIp;
    private int gkjPort;

    private BlockingQueue<ToGKJMessage> toGKJMessageQueue;

    public ClientSocket(String gkjIp, int gkjPort, BlockingQueue<ToGKJMessage> toGKJMessageQueue) {
        this.gkjIp = gkjIp;
        this.gkjPort = gkjPort;
        this.toGKJMessageQueue = toGKJMessageQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = new Socket(gkjIp, gkjPort);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                ObjectMapper objectMapper = new ObjectMapper();
                while (socket.isConnected()) {
                    try {
                        writer.write(objectMapper.writeValueAsString(toGKJMessageQueue.take()));
                        writer.newLine();
                        writer.flush();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
                socket.close();
            } catch (Exception e) {
                log.error(e.getMessage());
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    log.error(ex.getMessage());
                }
            }
        }
    }
}

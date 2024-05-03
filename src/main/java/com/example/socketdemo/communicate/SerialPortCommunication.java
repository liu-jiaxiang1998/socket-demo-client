package com.example.socketdemo.communicate;

import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
public class SerialPortCommunication implements Runnable {
    private BlockingQueue<Byte> serialPortDataQueue;
    private SerialPort serialPort;

    public SerialPortCommunication(BlockingQueue<Byte> serialPortDataQueue) {
        this.serialPortDataQueue = serialPortDataQueue;
        this.serialPort = SerialPort.getCommPort("/dev/ttyTHS0");
        this.serialPort.setBaudRate(9600);
    }

    @Override
    public void run() {
        for (int i = 0; i < 3; i++) {
            if (this.serialPort.openPort()) {
                log.info("Port opened successfully.");
                break;
            } else {
                log.error("Failed to open port.Try to reconnect!");
                try {
                    Thread.sleep(5000);
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
                        this.serialPortDataQueue.put(buffer[i]);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}

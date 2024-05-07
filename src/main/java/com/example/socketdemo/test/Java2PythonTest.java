package com.example.socketdemo.test;

import com.example.socketdemo.entity.CameraCaptureResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class Java2PythonTest {
    /**
     * 测试完成，可以正常通信！！
     * date: 2024/5/6
     * author: ljx
     */
    public static void main(String[] args) throws IOException {
        int port = 8999;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is listening on port " + port);
        while (true) {
            try (Socket socket = serverSocket.accept()) {
                System.out.println("New client connected: " + socket);

                CameraCaptureResult result = new CameraCaptureResult();
                result.setUuid(123456);
                result.setLeftImgPath("/path/to/left/image.jpg");
                result.setRightImgPath("/path/to/right/image.jpg");
                result.setLane(1);
                result.setLaneNumber(2);
                result.setDirection(0);
                result.setImgName("capture_image");
                result.setLicencePlate("ABC123");
                result.setColor("blue");
                result.setSpeed(60.5f);
                result.setIsCompleted(true);

                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(result);

                // 发送 JSON 字符串给 Python 程序
                for (int i = 0; i < 10; i++) {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                    writer.write(json);
                    writer.newLine();  // 添加换行符
                    writer.flush();    // 刷新输出流
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
//                    String receivedData;
//                    while ((receivedData = in.readLine()) != null) {
//                        log.info("收到数据：" + receivedData);
//                    }
//                    String receivedData = in.readLine();

                    String receivedData = "[-1, 123456, \"capture_image\", 1, 0, 0, 0, 0, 0, 0]";
                    log.info("收到数据：" + receivedData);
                    ObjectMapper mapper = new ObjectMapper();
                    Object[] arrays = objectMapper.readValue(receivedData, Object[].class);
                    for (Object item : arrays) {
                        System.out.println(item);
                    }

                    // 2024-05-06 18:16:58.428 [main] INFO  - 收到数据：[-1, 123456, "capture_image", 1, 0, 0, 0, 0, 0, 0]
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

//                String line;
//                StringBuilder data = new StringBuilder();
//                while ((line = reader.readLine()) != null) {
//                    log.info("Received from client: " + line);
//                    data.append(line);
////                    writer.write("Received from client: " + message);
////                    writer.flush();
//                }
//
//                // 使用 Jackson 库解析 JSON 数据
//                ObjectMapper objectMapper = new ObjectMapper();
//                Object[] resultFrame = objectMapper.readValue(data.toString(), Object[].class);
//
//                // 输出解析结果
//                for (Object num : resultFrame) {
//                    System.out.println("Received number: " + num);
//                }
            }
        }
    }
}

package com.example.socketdemo.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class Java2PythonTest {

    public static void main(String[] args) throws IOException {
        int port = 8999;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is listening on port " + port);
        while (true) {
            try (Socket socket = serverSocket.accept()) {
                System.out.println("New client connected: " + socket);

//                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                Object[] array = {1, "2", "dsasdsa", "你好", "E:/aaa"};

                // 将数组转换为 JSON 字符串
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(array);

                // 发送 JSON 字符串给 Python 程序
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                writer.write(json);
                writer.newLine();  // 添加换行符
                writer.flush();    // 刷新输出流

                ////                    writer.write("Received from client: " + message);
////                    writer.flush();
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

package com.example.socketdemo.communicate;

import com.example.socketdemo.entity.CameraCaptureResult;
import com.example.socketdemo.entity.FileType;
import com.example.socketdemo.entity.ToGKJMessage;
import com.example.socketdemo.entity.ToGKJMessageType;
import com.example.socketdemo.utils.CommonUtil;
import com.example.socketdemo.utils.FtpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 和本地的python程序通信！
 * date: 2024/5/6
 * author: ljx
 */
@Slf4j
public class LocalSocket implements Runnable {

    private int serverPort;

    private BlockingQueue<ToGKJMessage> toGKJMessageQueue;

    private ConcurrentHashMap<Integer, String> infraredCaptureResultMap;

    private BlockingQueue<CameraCaptureResult> cameraCaptureResultQueue;

    public LocalSocket(int serverPort, BlockingQueue<ToGKJMessage> toGKJMessageQueue,
                       ConcurrentHashMap<Integer, String> infraredCaptureResultMap, BlockingQueue<CameraCaptureResult> cameraCaptureResultQueue) {
        this.serverPort = serverPort;
        this.toGKJMessageQueue = toGKJMessageQueue;
        this.infraredCaptureResultMap = infraredCaptureResultMap;
        this.cameraCaptureResultQueue = cameraCaptureResultQueue;
    }

    @Override
    public void run() {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
            log.info("Server is listening on port " + serverPort);
        } catch (IOException e) {
            log.error("Server can/t listen on port " + serverPort);
            log.error(e.getMessage());
        }

        while (true) {
            try (Socket socket = serverSocket.accept()) {
//                socket.setSoTimeout(5000);

                SendThread sendThread = new LocalSocket.SendThread(socket, cameraCaptureResultQueue);
                ReceiveThread receiveThread = new LocalSocket.ReceiveThread(socket, toGKJMessageQueue, infraredCaptureResultMap);
                sendThread.start();
                receiveThread.start();

                sendThread.join();
                receiveThread.join();

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


    class SendThread extends Thread {
        private Socket socket;
        private BlockingQueue<CameraCaptureResult> cameraCaptureResultQueue;
        private Logger logger;

        SendThread(Socket socket, BlockingQueue<CameraCaptureResult> cameraCaptureResultQueue) {
            this.socket = socket;
            this.cameraCaptureResultQueue = cameraCaptureResultQueue;
            logger = LoggerFactory.getLogger(CameraSocket.SendThread.class);
        }

        @Override
        public void run() {
            ObjectMapper objectMapper = new ObjectMapper();
            while (socket.isConnected()) {
                try {
                    CameraCaptureResult cameraCaptureResult = cameraCaptureResultQueue.take();
                    String json = objectMapper.writeValueAsString(cameraCaptureResult);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                    writer.write(json);
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    class ReceiveThread extends Thread {
        private Socket socket;
        private BlockingQueue<ToGKJMessage> toGKJMessageQueue;

        private ConcurrentHashMap<Integer, String> infraredCaptureResultMap;
        private Logger logger;

        ReceiveThread(Socket socket, BlockingQueue<ToGKJMessage> toGKJMessageQueue, ConcurrentHashMap<Integer, String> infraredCaptureResultMap) {
            this.socket = socket;
            this.toGKJMessageQueue = toGKJMessageQueue;
            this.infraredCaptureResultMap = infraredCaptureResultMap;
            logger = LoggerFactory.getLogger(CameraSocket.ReceiveThread.class);
        }

        @Override
        public void run() {
            ObjectMapper objectMapper = new ObjectMapper();
            while (socket.isConnected()) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    String receivedData = in.readLine();
                    Object[] arrays = objectMapper.readValue(receivedData, Object[].class);
//                    String  pictureName = (String)arrays[arrays.length - 1];
                    String rightImgPath = (String) arrays[arrays.length - 2];
                    String leftImgPath = (String) arrays[arrays.length - 3];
                    if (!rightImgPath.equals(leftImgPath)) {
                        throw new RuntimeException("左右图的地址应该是一样的！");
                    }
                    ToGKJMessage toGKJMessage = new ToGKJMessage();

                    try {
                        if (arrays.length == 10) {
                            /** 货车检测失败 */
                            toGKJMessage.setType(ToGKJMessageType.DETECT_FAIL_FRAME);

                            String remoteALLCEUploadPath = CommonUtil.getRemoteUploadPath(FileType.ALL_CE, leftImgPath);
                            if (!FtpUtil.isConnected()) FtpUtil.connect();
                            FtpUtil.uploadFile(remoteALLCEUploadPath, leftImgPath);
                            arrays[arrays.length - 2] = CommonUtil.getRemoteAbsolutePath(remoteALLCEUploadPath);
                            arrays[arrays.length - 3] = CommonUtil.getRemoteAbsolutePath(remoteALLCEUploadPath);
                        } else {
                            /** 货车检测成功 */
                            toGKJMessage.setType(ToGKJMessageType.DETECT_SUCCESS_FRAME);
                            String headImgPath = (String) arrays[arrays.length - 4];
                            String cropImgPath = (String) arrays[arrays.length - 5];
                            Integer uuid = (Integer) arrays[0];
                            String cehwImgPath = infraredCaptureResultMap.remove(uuid);

                            String remoteCEUploadPath = CommonUtil.getRemoteUploadPath(FileType.CE, leftImgPath);
                            String remoteCEHWUploadPath = CommonUtil.getRemoteUploadPath(FileType.CE_HW, cehwImgPath);
                            String remoteCROPUploadPath = CommonUtil.getRemoteUploadPath(FileType.CROP, cropImgPath);
                            String remoteHEADUploadPath = CommonUtil.getRemoteUploadPath(FileType.HEAD, headImgPath);
                            if (!FtpUtil.isConnected()) FtpUtil.connect();
                            FtpUtil.uploadFile(remoteCEUploadPath, leftImgPath);
                            FtpUtil.uploadFile(remoteCEHWUploadPath, cehwImgPath);
                            FtpUtil.uploadFile(remoteCROPUploadPath, cropImgPath);
                            FtpUtil.uploadFile(remoteHEADUploadPath, headImgPath);
                            arrays[arrays.length - 2] = CommonUtil.getRemoteAbsolutePath(remoteCEUploadPath);
                            arrays[arrays.length - 3] = CommonUtil.getRemoteAbsolutePath(remoteCEUploadPath);
                            arrays[arrays.length - 4] = CommonUtil.getRemoteAbsolutePath(remoteHEADUploadPath);
                            arrays[arrays.length - 5] = CommonUtil.getRemoteAbsolutePath(remoteCROPUploadPath);
                        }
                    } catch (Exception e) {
                        log.error("FTP文件上传出现问题，但是结果帧仍然发送给工控机！");
                    }

                    toGKJMessage.setContent(objectMapper.writeValueAsString(arrays));
                    toGKJMessageQueue.put(toGKJMessage);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }
}

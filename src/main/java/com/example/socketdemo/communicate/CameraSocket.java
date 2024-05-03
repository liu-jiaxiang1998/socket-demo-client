package com.example.socketdemo.communicate;

import com.example.socketdemo.entity.CameraCaptureCommand;
import com.example.socketdemo.entity.CameraCaptureResult;
import com.example.socketdemo.utils.CrcUtil;
import com.example.socketdemo.utils.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CameraSocket implements Runnable {
    private String serverAddress;
    private int serverPort;

    private BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue;
    private ConcurrentHashMap<Integer, CameraCaptureResult> cameraCaptureResultMap;

    public CameraSocket(String serverAddress, int serverPort, BlockingQueue<CameraCaptureCommand> commandQueue, ConcurrentHashMap<Integer, CameraCaptureResult> cameraCaptureResultMap) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.cameraCaptureCommandQueue = commandQueue;
        this.cameraCaptureResultMap = cameraCaptureResultMap;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = new Socket(serverAddress, serverPort);
                socket.setSoTimeout(5000);

                SendThread sendThread = new SendThread(socket, cameraCaptureCommandQueue);
                ReceiveThread receiveThread = new ReceiveThread(socket);
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
        private BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue;
        private Logger logger;

        SendThread(Socket socket, BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue) {
            this.socket = socket;
            this.cameraCaptureCommandQueue = cameraCaptureCommandQueue;
            logger = LoggerFactory.getLogger(SendThread.class);
        }

        @Override
        public void run() {
            while (socket.isConnected()) {
                try {
                    CameraCaptureCommand captureCommand = cameraCaptureCommandQueue.take();
                    OutputStream outputStream = socket.getOutputStream();
                    String command = CrcUtil.crcXmodem(captureCommand.getId().toString(), captureCommand.getLane().toString());
                    byte[] commandBytes = HexUtil.hexStringToByteArray(command);
                    outputStream.write(commandBytes);
                    outputStream.flush();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    class ReceiveThread extends Thread {
        private Socket socket;
        private BlockingQueue<CameraCaptureResult> cameraCaptureResultQueue;
        private Logger logger;

        ReceiveThread(Socket socket) {
            this.socket = socket;
            logger = LoggerFactory.getLogger(ReceiveThread.class);
        }

        @Override
        public void run() {
            while (socket.isConnected()) {
                try {
                    InputStream inputStream = socket.getInputStream();
                    byte[] lengthData = new byte[7];
                    inputStream.read(lengthData);
                    long startTime = System.currentTimeMillis();
                    int packLength = Integer.parseInt(HexUtil.byteArrayToHexString(lengthData).substring(6, 14), 16);
                    byte[] dataBody;
                    if (packLength > 1024) {
                        dataBody = new byte[packLength + 7];
                        System.arraycopy(lengthData, 0, dataBody, 0, 7);
                        int remainingBytes = packLength;
                        int offset = 7;
                        while (remainingBytes > 0) {
                            int bytesRead = inputStream.read(dataBody, offset, remainingBytes);
                            remainingBytes -= bytesRead;
                            offset += bytesRead;
                        }
                    } else {
                        dataBody = inputStream.readNBytes(packLength);
                    }
                    inputStream.readNBytes(3);

                    String data1 = HexUtil.byteArrayToHexString(dataBody);
                    int uuid = Integer.parseInt(data1.substring(14, 18), 16);
                    int year = Integer.parseInt(data1.substring(18, 22), 16);
                    int yue = Integer.parseInt(data1.substring(22, 24), 16);
                    int ri = Integer.parseInt(data1.substring(24, 26), 16);
                    int shi = Integer.parseInt(data1.substring(26, 28), 16);
                    int fen = Integer.parseInt(data1.substring(28, 30), 16);
                    int second = Integer.parseInt(data1.substring(30, 32), 16);
                    int millisecond = Integer.parseInt(data1.substring(32, 36), 16);
                    logger.info("检测编号" + uuid + "    检测时间：" + year + "年" + yue + "月" + ri + "日" + shi + "点" + fen + "分" + second + "秒" + millisecond + "毫秒");
                    int laneNumber = Integer.parseInt(data1.substring(36, 38), 16);
//            int shibie = Integer.parseInt(data1.substring(38, 40), 16);
                    String color = new String(Base64.getDecoder().decode(data1.substring(40, 46)), StandardCharsets.UTF_8);
                    String licencePlate = data1.substring(46, 78);
//            int tuxianggeshu = Integer.parseInt(data1.substring(78, 80), 16);

                    int index = 80;
                    String dirName = "captureImgTmpDir";
                    if (!Files.isDirectory(Paths.get(dirName))) Files.createDirectories(Paths.get(dirName));
                    int imgBytesLength = Integer.parseInt(data1.substring(index, index + 8), 16) * 2;
                    byte[] imgBytes = HexUtil.hexStringToByteArray(data1.substring(index + 8, index + 8 + imgBytesLength));
                    String pictureName = year + "_" + yue + "_" + ri + "_" + shi + "_" + fen + "_" + second + "_" + millisecond + "_" + laneNumber + ".jpg";
                    Path filePath = Paths.get(dirName, pictureName);
                    Files.write(filePath, imgBytes);

                    long endTime = System.currentTimeMillis();
                    logger.info("抓拍耗时：" + (endTime - startTime) + "ms");

                    CameraCaptureResult cameraCaptureResult = cameraCaptureResultMap.get(uuid);
                    if (cameraCaptureResult.getUuid() != uuid) {
                        throw new Exception("未获取到在过车帧里构建的cameraCaptureResult！舍弃本条记录！");
                    }
                    cameraCaptureResult.setLeftImgPath(filePath.toAbsolutePath().toString());
                    cameraCaptureResult.setRightImgPath(filePath.toAbsolutePath().toString());
                    cameraCaptureResult.setImgName(pictureName);
                    cameraCaptureResult.setLicencePlate(licencePlate);
                    cameraCaptureResult.setColor(color);
                    cameraCaptureResult.setSpeed(0f);
                    cameraCaptureResult.setLaneNumber(laneNumber);
                    cameraCaptureResult.setIsCompleted(true);
                    cameraCaptureResultQueue.put(cameraCaptureResult);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }
}


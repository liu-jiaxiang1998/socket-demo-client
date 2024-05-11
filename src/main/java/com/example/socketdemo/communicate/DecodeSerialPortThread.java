package com.example.socketdemo.communicate;

import cn.hutool.core.util.ArrayUtil;
import com.example.socketdemo.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.*;

@Slf4j
public class DecodeSerialPortThread implements Runnable {

    private static final String START_STR = "FF"; // 起始标志
    private static final String END_STR = "FE"; // 终止标志

    private static Integer cur_batch = 0;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    private BlockingQueue<Byte> serialPortDataQueue;
    private BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue;
    private BlockingQueue<ToGKJMessage> toGKJMessagesQueue;
    private ConcurrentHashMap<Integer, PassingFrame> passingFrameMap;
    private ConcurrentHashMap<Integer, String> infraredCaptureResultMap;

    public DecodeSerialPortThread(BlockingQueue<Byte> serialPortDataQueue, BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue,
                                  BlockingQueue<ToGKJMessage> toGKJMessagesQueue, ConcurrentHashMap<Integer, PassingFrame> passingFrameMap,
                                  ConcurrentHashMap<Integer, String> infraredCaptureResultMap) {
        this.serialPortDataQueue = serialPortDataQueue;
        this.cameraCaptureCommandQueue = cameraCaptureCommandQueue;
        this.toGKJMessagesQueue = toGKJMessagesQueue;
        this.passingFrameMap = passingFrameMap;
        this.infraredCaptureResultMap = infraredCaptureResultMap;
    }

    @Override
    public void run() {
        while (true) {
            try {
                while (true) {
                    Byte start = serialPortDataQueue.take();
                    if (String.format("%02X", start).equals(START_STR)) {
                        Byte commandCode = serialPortDataQueue.take();
                        while (String.format("%02X", commandCode).equals(START_STR)) {
                            commandCode = serialPortDataQueue.take();
                        }

                        if (commandCode.intValue() != 2 && commandCode.intValue() != 5) {
                            log.error("命令码不正确，接收下一条命令！");
                            break;
                        }

                        Byte length = serialPortDataQueue.take();
                        Byte[] content = new Byte[length];
                        for (int i = 0; i < length; i++) {
                            content[i] = serialPortDataQueue.take();
                        }
                        serialPortDataQueue.take();
                        serialPortDataQueue.take();

                        ObjectMapper objectMapper = new ObjectMapper();

                        if (commandCode == 2) {
//                            StringBuilder sb = new StringBuilder();
//                            for (byte b : content) sb.append(String.format("%02x", b)).append(" ");
//                            log.info("过车帧数据内容为：" + sb);
                            int lane = content[0] - 1;
                            int number = content[1] & 0xFF;
                            int direction = content[2] & 0xFF;
                            int in_out = content[3] & 0xFF;
//                            log.info("\n*过车序号：%d\n*车道：%d\n*方向：%d\n*入/出：%d".formatted(number, lane, direction, in_out));
                            log.info("过车帧！！ *过车序号：%d *车道：%d *方向：%d *入/出：%d".formatted(number, lane, direction, in_out));
                            if (in_out == 1) {
                                int uuid = DecodeSerialPortThread.cur_batch * 1000 + number;
                                log.info("uuid : " + uuid);
                                if (number == 255) {
                                    DecodeSerialPortThread.cur_batch++;
                                }

                                if (lane == 0 || lane == 1) {
//                                if (lane == 0 && lane == 1) { //改了这个，多测点数据！！
                                    log.info("***此车为 %d 号车道，不做处理***".formatted(lane));
                                } else {
                                    log.info("***此车为正向，进行相机抓拍，图片及附属信息入队***");
                                    /** 这样设计的原因是因为传递给检测程序的参数不仅仅来自相机抓拍的结果，也包含过车帧里面的信息！其实完全可以不用的，懒得改了！ */
                                    PassingFrame passingFrame = new PassingFrame();
                                    passingFrame.setUuid(uuid);
                                    passingFrame.setLane(lane);
                                    passingFrame.setDirection(direction);
                                    passingFrameMap.put(uuid, passingFrame);

                                    CameraCaptureCommand cameraCaptureCommand = new CameraCaptureCommand();
                                    cameraCaptureCommand.setId(uuid);
                                    cameraCaptureCommand.setLane(lane == 2 ? 12 : 11);
                                    this.cameraCaptureCommandQueue.put(cameraCaptureCommand);

                                    Callable<String> infraredSocket = new InfraredSocket();
                                    CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
                                        try {
                                            return infraredSocket.call();
                                        } catch (Exception e) {
                                            log.error("调用红外相机出现问题！");
                                            throw new RuntimeException(e);
                                        }
                                    }, executorService);
                                    completableFuture.thenAccept(result -> {
                                        log.info("红外相机抓拍成功！");
                                        infraredCaptureResultMap.put(uuid, result);
                                    });

                                    ToGKJMessage toGKJMessage = new ToGKJMessage();
                                    toGKJMessage.setType(ToGKJMessageType.CAPTURE_FRAME);
                                    toGKJMessage.setContent(objectMapper.writeValueAsString(cameraCaptureCommand));
                                    this.toGKJMessagesQueue.put(toGKJMessage);
                                }
                            } else {
                                log.info("*此车为反向，不做处理***");
                            }
                        } else {
//                            StringBuilder sb = new StringBuilder();
//                            for (byte b : content) sb.append(String.format("%02x", b)).append(" ");
//                            log.info("称重帧数据内容为：" + sb);
                            Integer uuid = DecodeSerialPortThread.cur_batch * 1000 + (content[6] & 0xFF);
                            Float weight = ByteBuffer.wrap(ArrayUtil.unWrap(ArrayUtil.reverse(Arrays.copyOfRange(content, 8, 12)))).getFloat();
                            Float speed = ByteBuffer.wrap(ArrayUtil.unWrap(ArrayUtil.reverse(Arrays.copyOfRange(content, 12, 16)))).getFloat();
                            ToGKJMessage toGKJMessage = new ToGKJMessage();
                            WeightFrame weightFrame = new WeightFrame();
                            weightFrame.setUuid(uuid);
                            weightFrame.setWeight(weight);
                            weightFrame.setSpeed(speed);
                            toGKJMessage.setType(ToGKJMessageType.WEIGHT_FRAME);
                            toGKJMessage.setContent(objectMapper.writeValueAsString(weightFrame));
                            toGKJMessagesQueue.put(toGKJMessage);
                            log.info("解析后的称重帧数据内容为：" + weightFrame);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("解码串口信息报错！", e);
            }
        }
    }
}

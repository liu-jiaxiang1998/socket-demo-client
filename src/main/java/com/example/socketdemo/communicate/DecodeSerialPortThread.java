package com.example.socketdemo.communicate;

import cn.hutool.core.util.ArrayUtil;
import com.example.socketdemo.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.*;

@Slf4j
public class DecodeSerialPortThread implements Runnable {

    private static final String START_STR = "ff"; // 起始标志
    private static final String END_STR = "fe"; // 终止标志

    private static Integer cur_batch = 0;
    ExecutorService executorService = Executors.newFixedThreadPool(5);
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
                    if (String.format("%02", start).equals(START_STR)) {
                        Byte commandCode = serialPortDataQueue.take();
                        while (String.format("%02", commandCode).equals(START_STR)) {
                            commandCode = serialPortDataQueue.take();
                        }

                        if (commandCode != 2 || commandCode != 5) {
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

                        if (commandCode == 2) {
                            log.info("过车帧数据内容为：" + content);
                            int lane = content[0] - 1;
                            int number = content[1];
                            int direction = content[2];
                            int in_out = content[3];
                            log.info("\n*过车序号：%d\n*车道：%d\n*方向：%d\n*入/出：%d".formatted(number, lane, direction, in_out));
                            if (in_out == 1) {
                                int uuid = DecodeSerialPortThread.cur_batch * 1000 + number;
                                log.info("uuid : " + uuid);
                                if (number == 255) {
                                    DecodeSerialPortThread.cur_batch++;
                                }

                                if (lane == 0 || lane == 1) {
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
                                            throw new RuntimeException(e);
                                        }
                                    }, executorService);
                                    completableFuture.thenAccept(result -> {
                                        log.info("红外相机抓拍成功！");
                                        infraredCaptureResultMap.put(uuid, result);
                                    });

                                    ToGKJMessage toGKJMessage = new ToGKJMessage();
                                    toGKJMessage.setType(ToGKJMessageType.CAPTURE_FRAME);
                                    toGKJMessage.setContent(cameraCaptureCommand);
                                    this.toGKJMessagesQueue.put(toGKJMessage);
                                }
                            } else {
                                log.info("*此车为反向，不做处理***");
                            }
                        } else {
                            log.info("称重帧数据内容为：" + content);
                            Integer uuid = DecodeSerialPortThread.cur_batch * 1000 + content[6];
                            Float weight = ByteBuffer.wrap(ArrayUtil.unWrap(Arrays.copyOfRange(content, 8, 12))).getFloat();
                            Float speed = ByteBuffer.wrap(ArrayUtil.unWrap(Arrays.copyOfRange(content, 12, 16))).getFloat();
                            ToGKJMessage toGKJMessage = new ToGKJMessage();
                            WeightFrame weightFrame = new WeightFrame();
                            weightFrame.setUuid(uuid);
                            weightFrame.setWeight(weight);
                            weightFrame.setSpeed(speed);
                            toGKJMessage.setType(ToGKJMessageType.WEIGHT_FRAME);
                            toGKJMessage.setContent(weightFrame);
                            toGKJMessagesQueue.put(toGKJMessage);
                            log.info("解析后的称重帧数据内容为：" + weightFrame);
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }
}

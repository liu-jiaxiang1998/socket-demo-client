package com.example.socketdemo.test;

import com.example.socketdemo.utils.CrcUtil;
import com.example.socketdemo.utils.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author ljx
 * @description: 连接相机抓拍图片成功！！
 * 终于知道流的读取特点了！！如果长为100的字节流，只读了10个，剩下的还在流里继续存着，仍然可以读！！直到全读完！！原来如此！！
 * 而且由于是每次先发送抓拍指令后接收图片，因此不用担心串数据！！而且许多连接同时开启时也不用担心串数据，因为流是属于socket的！！因此不同socket的流不一样！！
 * @date 2024/4/25
 */
@Slf4j
public class ConnectCameraTest {
    public static void main(String[] args) {

        String serverIp = "10.70.123.220";
        int serverPort = 6668;
//        String tempData = "02 00 33 04 00 01 11 01 7F 39 03";
        String[] chedaoHaos = {"11", "12", "21", "22"};

        long time1 = System.currentTimeMillis();

        while (true) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(serverIp, serverPort), 5000);
                log.info("Connect " + serverIp + " over.");
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                for (int i = 1; i < 2; i++) {
                    String xuhao = CrcUtil.decimalToHexadecimal(i);
                    for (String chedao : chedaoHaos) {
                        String tempData1 = CrcUtil.crcXmodem(xuhao, chedao);
                        byte[] tempDataBytes = HexUtil.hexStringToByteArray(tempData1);
//                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                    out.println(new String(tempDataBytes));
                        outputStream.write(tempDataBytes);
                        outputStream.flush();

                        byte[] lengthData = new byte[7];
                        inputStream.read(lengthData);

                        long timeend1 = System.currentTimeMillis();

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

                        byte[] tail = new byte[3];
                        int read = inputStream.read(tail);
                        log.info("read" + read);

                        long timeend2 = System.currentTimeMillis();
                        String data1 = HexUtil.byteArrayToHexString(dataBody);
//                    String data1 = HexUtil.byteArrayToHexString(dataBody).toUpperCase();

                        int bianhao = Integer.parseInt(data1.substring(14, 18), 16);
                        int nian = Integer.parseInt(data1.substring(18, 22), 16);
                        int yue = Integer.parseInt(data1.substring(22, 24), 16);
                        int ri = Integer.parseInt(data1.substring(24, 26), 16);
                        int shi = Integer.parseInt(data1.substring(26, 28), 16);
                        int fen = Integer.parseInt(data1.substring(28, 30), 16);
                        int miao = Integer.parseInt(data1.substring(30, 32), 16);
                        int haomiao = Integer.parseInt(data1.substring(32, 36), 16);
                        log.info("检测编号" + bianhao + "    检测时间：" + nian + "年" + yue + "月" + ri + "日" + shi + "点" + fen + "分" + miao + "秒" + haomiao + "毫秒");

                        int chedaohao = Integer.parseInt(data1.substring(36, 38), 16);
                        int shibie = Integer.parseInt(data1.substring(38, 40), 16);
                        String chepaiyanse = data1.substring(40, 46);
                        String chepaihao = data1.substring(46, 78);
                        int tuxianggeshu = Integer.parseInt(data1.substring(78, 80), 16);

                        int longBytes = 0;
                        int count = 0;
                        int index = 80;
                        String dirName = "cap_result";

                        if (chepaihao.equals("20202020202020202020202020200000") || !chepaihao.equals("20202020202020202020202020200000")) {
                            log.info("车道号为" + chedaohao);
                            String chepaihaozhongwen = new String(HexUtil.hexStringToByteArray(chepaihao)).replace("\u0000", "");
                            log.info("车牌颜色 " + new String(HexUtil.hexStringToByteArray(chepaiyanse)) + " 车牌号是" + chepaihaozhongwen);

                            if (!Files.isDirectory(Paths.get(dirName))) {
                                Files.createDirectories(Paths.get(dirName));
                            }

                            for (int j = 0; j < tuxianggeshu; j++) {
                                longBytes = Integer.parseInt(data1.substring(index, index + 8), 16) * 2;
                                count++;
                                byte[] imgBytes = HexUtil.hexStringToByteArray(data1.substring(index + 8, index + 8 + longBytes));
                                String pictureName = nian + "_" + yue + "_" + ri + "_" + shi + "_" + fen + "_" + miao + "_" + haomiao + "_" + chedaohao + "_" + chedao + "_" + count + ".jpg";
//                            String pictureName = nian + "_" + yue + "_" + ri + "_" + shi + "_" + fen + "_" + miao + "_" + haomiao + "_" + chedaohao + "_" + chedao + "_" + chepaihaozhongwen + "_" + count + ".jpg";
                                Path filePath = Paths.get(dirName, pictureName);
                                Files.write(filePath, imgBytes);
                                index += 8 + longBytes;
                            }
                        }

                        long timeend3 = System.currentTimeMillis();
                        log.info("抓图时间：" + (timeend1 - time1) + "ms");
                        log.info("接收时间：" + (timeend2 - timeend1) + "ms");
                        log.info("解码时间：" + (timeend3 - timeend2) + "ms");
                        log.info("总时间：" + (timeend3 - time1) + "ms");
                    }
                }
            } catch (IOException e) {
                log.error("无法连接111");
                e.printStackTrace();
            } catch (DecoderException e) {
                log.error("无法连接222");
                throw new RuntimeException(e);
            }
            try {
                log.error("睡眠1min之后尝试重连！");
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                log.error("线程睡眠被打断！！");
            }
        }
    }
}


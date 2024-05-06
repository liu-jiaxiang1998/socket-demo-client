package com.example.socketdemo.communicate;

import com.example.socketdemo.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * 红外相机抓拍已测试成功！！另外测试发现根本不用登录就能抓拍。。
 * date: 2024/5/5
 * author: ljx
 */
@Slf4j
public class InfraredSocket implements Callable<String> {

    @Override
    public String call() {
        long t1 = System.currentTimeMillis();
        try {
//            String loginUrlPre = "http://10.70.123.225/cgi-bin";
//            String loginUrl = loginUrlPre + "/user?Operation=Login&Name=admin&Pass=Admin123";
//            URL url = new URL(loginUrl);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setDoOutput(true);
//            String params = "Operation=Login&Name=admin&Pass=Admin123";
//            connection.getOutputStream().write(params.getBytes());
//            InputStream loginResponseStream = connection.getInputStream();
//            log.info("小盒子红外相机登录完成");

            String streamUrl = "http://10.70.123.225:5000/cgi-bin/stream";
            String streamParams = "Type=JPEG&Source=Jpeg&Mode=TCP&Port=0&Channel=0&Heart-beat=No&Frames=1&Snap=Yes";
            URL streamURL = new URL(streamUrl + "?" + streamParams);
            HttpURLConnection streamConnection = (HttpURLConnection) streamURL.openConnection();
            streamConnection.setRequestMethod("GET");
//            streamConnection.setRequestProperty("Cookie", connection.getHeaderField("Set-Cookie"));
            String saveImagePath = CommonUtil.getInfraredImagePath();

            int responseCode = streamConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = streamConnection.getInputStream();
                     OutputStream outputStream = new FileOutputStream(saveImagePath)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                log.warn("请求失败，响应码：" + responseCode);
            }

            long t2 = System.currentTimeMillis();
            log.info("红外相机抓拍时间为 " + (t2 - t1) + "ms。路径：" + saveImagePath);
            return saveImagePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

package com.example.socketdemo.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ftp.Ftp;
import com.example.socketdemo.entity.ProjectProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class FtpUtil {
    private static Ftp ftp;

    public static Boolean connect() {
        ftp = new Ftp(ProjectProperties.REMOTE_IP, 21, ProjectProperties.REMOTE_USERNAME, ProjectProperties.REMOTE_PASSWORD, CharsetUtil.CHARSET_UTF_8);
        return isConnected();
    }

    public static Boolean isConnected() {
        try {
            ftp.pwd();
            return true;
        } catch (Exception e) {
            log.error("FTP未连接！");
            return false;
        }
    }

    public static void uploadFile(String remoteUploadPath, String localFilePath) throws Exception {
        File file = new File(localFilePath);
        if (file.exists()) {
            if (ftp.upload(remoteUploadPath, file)) {
                log.info(localFilePath + " 上传成功！删除本地文件！");
                file.delete();
            } else {
                file.delete();
                throw new Exception(localFilePath + " 文件上传失败！删除本地文件！");
            }
        } else {
            throw new Exception("本地文件不存在！");
        }
    }

    public static void disconnect() {
        if (isConnected()) {
            try {
                ftp.close();
            } catch (IOException e) {
                log.error("FTP关闭连接出现问题" + e.getMessage());
            }
        }
    }
}

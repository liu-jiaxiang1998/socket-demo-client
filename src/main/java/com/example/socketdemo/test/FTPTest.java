package com.example.socketdemo.test;

import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpMode;
import com.example.socketdemo.entity.FileType;
import com.example.socketdemo.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FTPTest {
    public static void main(String[] args) {
//        Ftp ftp = new Ftp("10.70.123.224", 21,"zm","1", CharsetUtil.CHARSET_UTF_8);
        Ftp ftp = new Ftp("localhost", 21);
        ftp.setMode(FtpMode.Passive);
        String fileSeparator = System.getProperty("file.separator");
        String root = "E:/ftp_test";
//        String root = "F:/";
        String pwd = ftp.pwd();
        log.info(root + pwd);
        /** /aaa 是从根目录开始。aaa 是从当前目录开始！这个hutool的FTP工具类在上传文件时会自动帮助你创建目录！！ */
        String ceLocalPath = "E:/a/b/c/2024_05_07_13_53_08_935.jpg";
        ftp.upload(CommonUtil.getRemoteUploadPath(FileType.CE, ceLocalPath), new File(ceLocalPath));

//        ftp.upload("remotePath", "localFile");
//        ftp.download("remotePath", "localFile");
//        List<String> fileList = ftp.ls("remotePath");
//        ftp.delFile("remotePath");
//        ftp.cd("directoryPath");
//        ftp.mkdir("directoryPath");
//        ftp.delDir("directoryPath");
//        long size = ftp.getSize("remoteFile");
//        ftp.close();

//        log.info(ftp.toString());

    }
}

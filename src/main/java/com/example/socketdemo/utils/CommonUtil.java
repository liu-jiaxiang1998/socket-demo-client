package com.example.socketdemo.utils;

import com.example.socketdemo.entity.ProjectProperties;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {
    public static String formatFileName(Date currentTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
        return dateFormat.format(currentTime);
    }

    public static String getInfraredImagePath() {
        String directoryPath = System.getProperty("user.dir") + System.getProperty("file.separator") + ProjectProperties.CAPTURE_ROOT_PATH + System.getProperty("file.separator") +
                ProjectProperties.INFRARED_PATH;
        File directory = new File(directoryPath);
        if (!directory.exists()) directory.mkdirs();
        return directoryPath + System.getProperty("file.separator") + CommonUtil.formatFileName(new Date()) + ".jpg";
    }

    public static String getCameraImagePath() {
        String directoryPath = System.getProperty("user.dir") + System.getProperty("file.separator") + ProjectProperties.CAPTURE_ROOT_PATH + System.getProperty("file.separator") +
                ProjectProperties.CAMERA_PATH;
        File directory = new File(directoryPath);
        if (!directory.exists()) directory.mkdirs();
        return directoryPath + System.getProperty("file.separator") + CommonUtil.formatFileName(new Date()) + ".jpg";
    }
}

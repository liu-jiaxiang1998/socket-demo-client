package com.example.socketdemo.utils;

import com.example.socketdemo.entity.FileType;
import com.example.socketdemo.entity.ProjectProperties;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CommonUtil {
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static String formatFileName(Date currentTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
        return dateFormat.format(currentTime);
    }

    public static String getInfraredImagePath() {
        String directoryPath = System.getProperty("user.dir") + System.getProperty("file.separator") + ProjectProperties.LOCAL_CAPTURE_ROOT_PATH + System.getProperty("file.separator") +
                ProjectProperties.LOCAL_INFRARED_PATH;
        File directory = new File(directoryPath);
        if (!directory.exists()) directory.mkdirs();
        return directoryPath + System.getProperty("file.separator") + CommonUtil.formatFileName(new Date()) + ".jpg";
    }

    public static String getCameraImagePath() {
        String directoryPath = System.getProperty("user.dir") + System.getProperty("file.separator") + ProjectProperties.LOCAL_CAPTURE_ROOT_PATH + System.getProperty("file.separator") +
                ProjectProperties.LOCAL_CAMERA_PATH;
        File directory = new File(directoryPath);
        if (!directory.exists()) directory.mkdirs();
        return directoryPath + System.getProperty("file.separator") + CommonUtil.formatFileName(new Date()) + ".jpg";
    }

    public static String getRemoteUploadPath(FileType type, String localPath) {
        StringBuilder remoteRootPath = new StringBuilder();
        remoteRootPath.append(FILE_SEPARATOR).append(ProjectProperties.REMOTE_CAPTURE_ROOT_PATH).append(FILE_SEPARATOR);
        switch (type) {
            case CE:
                remoteRootPath.append(ProjectProperties.REMOTE_CE_PATH);
                break;
            case CE_HW:
                remoteRootPath.append(ProjectProperties.REMOTE_CE_HW_PATH);
                break;
            case CROP:
                remoteRootPath.append(ProjectProperties.REMOTE_CROP_PATH);
                break;
            case HEAD:
                remoteRootPath.append(ProjectProperties.REMOTE_HEAD_PATH);
                break;
            case ALL_CE:
                remoteRootPath.append(ProjectProperties.REMOTE_ALL_CE_PATH);
                break;
        }
        remoteRootPath.append(FILE_SEPARATOR);
        String timeStr = localPath.substring(localPath.lastIndexOf(FILE_SEPARATOR), localPath.lastIndexOf("."));
        List<String> stringList = Arrays.stream(timeStr.split("_")).map(a -> {
            if (a.startsWith("0")) {
                return a.substring(1, a.length());
            } else {
                return a;
            }
        }).collect(Collectors.toList());
        return remoteRootPath.append(stringList.get(0)).append(FILE_SEPARATOR)
                .append(stringList.get(1)).append(FILE_SEPARATOR)
                .append(stringList.get(2)).append(FILE_SEPARATOR)
                .append(stringList.get(3)).append(FILE_SEPARATOR).toString();
    }

    public static String getRemoteAbsolutePath(String remoteFTPPath) {
        if (ProjectProperties.REMOTE_FTP_PATH.endsWith("\\")) {
            return ProjectProperties.REMOTE_FTP_PATH.substring(0, ProjectProperties.REMOTE_FTP_PATH.lastIndexOf("\\")) + remoteFTPPath;
        } else {
            return ProjectProperties.REMOTE_FTP_PATH + remoteFTPPath;
        }
    }
}

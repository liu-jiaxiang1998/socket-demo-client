package com.example.socketdemo.entity;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Slf4j
public class ProjectProperties {
    public static Integer LOCAL_LISTEN_PORT;
    public static String LOCAL_CAPTURE_ROOT_PATH;
    public static String LOCAL_CAMERA_PATH;
    public static String LOCAL_INFRARED_PATH;

    public static String CE_CAMERA_IP;
    public static Integer CE_CAMERA_PORT;

    public static String REMOTE_CAPTURE_ROOT_PATH;
    public static String REMOTE_CE_PATH;
    public static String REMOTE_CE_HW_PATH;
    public static String REMOTE_CROP_PATH;
    public static String REMOTE_HEAD_PATH;
    public static String REMOTE_ALL_CE_PATH;

    public static String REMOTE_FTP_PATH;

    public static String REMOTE_IP;
    public static Integer REMOTE_COMMUNICATE_PORT;
    public static String REMOTE_USERNAME;
    public static Integer REMOTE_PASSWORD;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ProjectProperties.class.getClassLoader().getResourceAsStream("application.yml")) {
            Map<String, Object> jsonData = yaml.load(inputStream);
            LOCAL_LISTEN_PORT = getProperty(jsonData, "project.local.listenPort");
            LOCAL_CAPTURE_ROOT_PATH = getProperty(jsonData, "project.local.capturePath.root");
            LOCAL_CAMERA_PATH = getProperty(jsonData, "project.local.capturePath.ceCamera");
            LOCAL_INFRARED_PATH = getProperty(jsonData, "project.local.capturePath.infrared");

            CE_CAMERA_IP = getProperty(jsonData, "project.ceCamera.ip");
            CE_CAMERA_PORT = getProperty(jsonData, "project.ceCamera.port");

            REMOTE_CAPTURE_ROOT_PATH = getProperty(jsonData, "project.remote.capturePath.root");
            REMOTE_CE_PATH = getProperty(jsonData, "project.remote.capturePath.ce");
            REMOTE_CE_HW_PATH = getProperty(jsonData, "project.remote.capturePath.ceHW");
            REMOTE_CROP_PATH = getProperty(jsonData, "project.remote.capturePath.crop");
            REMOTE_HEAD_PATH = getProperty(jsonData, "project.remote.capturePath.head");
            REMOTE_ALL_CE_PATH = getProperty(jsonData, "project.remote.capturePath.allCE");

            REMOTE_FTP_PATH = getProperty(jsonData, "project.remote.ftpPath");

            REMOTE_IP = getProperty(jsonData, "project.remote.ip");
            REMOTE_COMMUNICATE_PORT = getProperty(jsonData, "project.remote.communicatePort");
            REMOTE_USERNAME = getProperty(jsonData, "project.remote.username");
            REMOTE_PASSWORD = getProperty(jsonData, "project.remote.password");

            log.info("项目初始属性加载完毕");
        } catch (Exception e) {
            log.error("项目初始属性加载出错！" + e.getMessage());
        }
    }


    // 递归方法来获取多层嵌套的属性值
    private static <T> T getProperty(Map<String, Object> data, String key) {
        String[] keys = key.split("\\.");
        Map<String, Object> current = data;
        for (int i = 0; i < keys.length - 1; i++) {
            current = (Map<String, Object>) current.get(keys[i]);
            if (current == null) {
                return null;
            }
        }
        return (T) current.get(keys[keys.length - 1]);
    }
}

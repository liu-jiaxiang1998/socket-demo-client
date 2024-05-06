package com.example.socketdemo.entity;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Slf4j
public class ProjectProperties {
    public static String CAPTURE_ROOT_PATH;
    public static String CAMERA_PATH;
    public static String INFRARED_PATH;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ProjectProperties.class.getClassLoader().getResourceAsStream("application.yml")) {
            Map<String, Object> jsonData = yaml.load(inputStream);
            CAPTURE_ROOT_PATH = getProperty(jsonData, "project.capturePath.root");
            CAMERA_PATH = getProperty(jsonData, "project.capturePath.camera_ce");
            INFRARED_PATH = getProperty(jsonData, "project.capturePath.infrared");
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

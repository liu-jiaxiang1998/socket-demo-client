package com.example.socketdemo.entity;

import lombok.Data;

@Data
public class CameraCaptureResult {
    private Integer uuid;
    private String leftImgPath;
    private String rightImgPath;
    private Integer lane;
    private Integer laneNumber;
    private Integer direction;
    private String imgName;
    private String licencePlate;
    private String color;
    private Float speed;
    private Boolean isCompleted;
}

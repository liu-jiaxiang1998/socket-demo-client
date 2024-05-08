package com.example.socketdemo.entity;

import lombok.Data;

@Data
public class ToGKJMessage {
    private ToGKJMessageType type;
    private String content;
}

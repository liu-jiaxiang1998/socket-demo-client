package com.example.socketdemo.communicate;

import com.example.socketdemo.entity.CameraCaptureResult;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
* 和本地的python程序通信！
 * todo
* date: 2024/5/6
* author: ljx
*/
@Slf4j
public class LocalSocket implements Runnable {

    private ConcurrentHashMap<Integer, CameraCaptureResult> cameraCaptureResultMap;
    @Override
    public void run() {

    }
}

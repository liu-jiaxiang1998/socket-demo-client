package com.example.socketdemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestLog {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testLogLevel() {
        logger.trace("trace输出");
        logger.debug("debug输出");
        logger.info("info输出");
        logger.warn("warn输出");
        logger.error("error输出");
        logger.info(logger.isEnabledForLevel(Level.TRACE) + "");
        logger.info(logger.isEnabledForLevel(Level.DEBUG) + "");
        logger.info(logger.isEnabledForLevel(Level.INFO) + "");
        logger.info(logger.isEnabledForLevel(Level.WARN) + "");
        logger.info(logger.isEnabledForLevel(Level.ERROR) + "");
    }
}

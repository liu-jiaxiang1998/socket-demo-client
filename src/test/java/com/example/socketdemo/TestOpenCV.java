package com.example.socketdemo;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

//import static org.opencv.highgui.HighGui.imshow;
//import static org.opencv.highgui.HighGui.waitKey;
//import static org.opencv.imgcodecs.Imgcodecs.imread;
//import static org.opencv.imgcodecs.Imgcodecs.imwrite;
//import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
//import static org.opencv.imgproc.Imgproc.cvtColor;

@RunWith(SpringRunner.class)
@SpringBootTest
class TestOpenCV {
    @Test
    public void testOpencv() throws Exception {
        // 解决awt报错问题
//        System.setProperty("java.awt.headless", "false");
//        System.out.println(System.getProperty("java.library.path"));
//        // 加载动态库
//        URL url = ClassLoader.getSystemResource("lib/opencv_java490.dll");
//        System.load(url.getPath());
//        // 读取图像
//        Mat image = imread("E:\\personal_java_projects\\socket-demo-client\\cap_result\\2024_4_27_15_48_0_91_1_11_1.jpg");
//        if (image.empty()) {
//            throw new Exception("image is empty");
//        }
//        imshow("Original Image", image);
//
//        // 创建输出单通道图像
//        Mat grayImage = new Mat(image.rows(), image.cols(), CvType.CV_8SC1);
//        // 进行图像色彩空间转换
//        cvtColor(image, grayImage, COLOR_RGB2GRAY);
//
//        imshow("Processed Image", grayImage);
//        imwrite("D://hello.jpg", grayImage);
//        waitKey();
    }
}
package com.example.socketdemo.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class HexUtil {
    public static byte[] hexStringToByteArray(String hexString) throws DecoderException {
        return Hex.decodeHex(hexString.replaceAll("\\s+", ""));
//        return Hex.decodeHex(hexString);
    }

    public static String byteArrayToHexString(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }
//    public static void main(String[] args) {
//        /**  "Hello World" 的十六进制表示.由每个字符的 ASCII 码值转换而来的。在 ASCII 编码中，每个字符都对应一个唯一的整数值。例如，字符 'H' 的 ASCII 码值是 72 */
//        String hexString = "48656C6C6F20576F726C64";
//        try {
//            byte[] byteArray = hexStringToByteArray(hexString);
//            // 打印字节数组内容
//            for (byte b : byteArray) {
//                System.out.print(b + " ");
//                System.out.println(new String(new byte[]{b}));
//            }
//        } catch (DecoderException e) {
//            e.printStackTrace();
//        }
//
////        byte[] byteArray = {72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100}; // "Hello World" 的字节数组表示
////        String hexString = byteArrayToHexString(byteArray);
////        System.out.println(hexString); // 输出：48656C6C6F20576F726C64
//    }
}

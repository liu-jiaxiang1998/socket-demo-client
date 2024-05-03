package com.example.socketdemo.utils;


public class CrcUtil {
    public static String crcXmodem(String xuhao, String chedaohao) {
        String raw = "00 33 04 " + xuhao + " " + chedaohao + " 01";
        byte[] bytes = hexStringToByteArray(raw);
        int crcValue = Crc16XmodemUtil.crc16_ccitt_xmodem(bytes, 0, bytes.length);
        String crc = String.format("%04X", crcValue);
        crc = crc.substring(0, 2) + " " + crc.substring(2, 4);
        return "02 00 33 04" + " " + xuhao + " " + chedaohao + " 01 " + crc + " 03";
    }

    public static String decimalToHexadecimal(int decimal) {
        String binary = String.format("%16s", Integer.toBinaryString(decimal)).replace(' ', '0');
        String byte1 = binary.substring(0, 8);
        String byte2 = binary.substring(8);
        String hexadecimal = String.format("%02X %02X", Integer.parseInt(byte1, 2), Integer.parseInt(byte2, 2));
        return hexadecimal;
    }

    private static byte[] hexStringToByteArray(String s) {
        String[] hexValues = s.split(" ");
        byte[] bytes = new byte[hexValues.length];
        for (int i = 0; i < hexValues.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexValues[i], 16);
        }
        return bytes;
    }
}

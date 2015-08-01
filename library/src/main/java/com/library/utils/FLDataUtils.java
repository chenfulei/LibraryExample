package com.library.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 自定义 数据处理
 *
 * Created by chen_fulei on 2015/8/1.
 */
public class FLDataUtils {



    /*****************字符串加密(MD5加密)*******************/
    public static String getMD5Hex(String str){
        byte[] data = getMD5(str.getBytes());

        BigInteger bi = new BigInteger(data).abs();

        String result = bi.toString(36);
        return result;
    }
    public static byte[] getMD5(byte[] data){
        MessageDigest digest;
        try {
            digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(data);
            byte[] hash = digest.digest();
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Debug.Log(e);
        }

        return null;
    }
    /*****************字符串加密(MD5加密)*******************/
    /**********************生产 Base64 characters  start**************************/
    private static final char[] map1 = new char[64];
    static {
        int i=0;
        for (char c='A'; c<='Z'; c++) map1[i++] = c;
        for (char c='a'; c<='z'; c++) map1[i++] = c;
        for (char c='0'; c<='9'; c++) map1[i++] = c;
        map1[i++] = '+'; map1[i++] = '/'; }

    // Mapping table from Base64 characters to 6-bit nibbles.
    private static final byte[] map2 = new byte[128];
    static {
        for (int i=0; i<map2.length; i++) map2[i] = -1;
        for (int i=0; i<64; i++) map2[map1[i]] = (byte)i; }

    //Source: http://www.source-code.biz/base64coder/java/Base64Coder.java.txt
    public static char[] encode64(byte[] in, int iOff, int iLen) {

        int oDataLen = (iLen*4+2)/3;       // output length without padding
        int oLen = ((iLen+2)/3)*4;         // output length including padding
        char[] out = new char[oLen];
        int ip = iOff;
        int iEnd = iOff + iLen;
        int op = 0;
        while (ip < iEnd) {
            int i0 = in[ip++] & 0xff;
            int i1 = ip < iEnd ? in[ip++] & 0xff : 0;
            int i2 = ip < iEnd ? in[ip++] & 0xff : 0;
            int o0 = i0 >>> 2;
            int o1 = ((i0 &   3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
            int o3 = i2 & 0x3F;
            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : '='; op++;
            out[op] = op < oDataLen ? map1[o3] : '='; op++; }
        return out;
    }
/**********************生产 Base64 characters  end**************************/
}

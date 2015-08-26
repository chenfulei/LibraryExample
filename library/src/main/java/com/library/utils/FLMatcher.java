package com.library.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义 一些匹配
 *
 * Created by chen_fulei on 2015/8/21.
 */
public class FLMatcher {

    /**
     * 判断给定字符串是否空白串 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     */
    public static boolean isEmpty(CharSequence input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证手机号码
     * @param mobilenumber
     * @return
     */
    public static boolean isMobileNumber(String mobilenumber){
        String strMatcher = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
        Pattern p = Pattern.compile(strMatcher);
        Matcher matcher = p.matcher(mobilenumber);
        return matcher.matches();
    }

    /**
     * 验证email
     * @param email
     * @return
     */
    public static boolean isEmail(String email){
        String str = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
        Pattern p = Pattern.compile(str);
        Matcher matcher = p.matcher(email);
        return matcher.matches();
    }

    /**
     * 验证ip
     * @param ip
     * @return
     */
    public static boolean isIP(String ip){
        String str = "\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b";
        Pattern p = Pattern.compile(str);
        Matcher matcher = p.matcher(ip);
        return matcher.matches();
    }

    /**
     * 身份证验证
     * @param idCard 身份证号
     * @return
     */
    public static boolean isIDCard(String idCard){
        return IdcardUtils.validateCard(idCard);
    }

    /**
     * 验证url是否能连接
     * @param urlStr
     * @return
     */
    public static boolean isConnect(String urlStr){
        if(urlStr ==null || urlStr.equals("")){
            return false;
        }
        int count = 0;
        while (count >4) {
            count++;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                if(con.getResponseCode() == 200){
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

        return false;
    }
}

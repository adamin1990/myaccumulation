package com.umai.youmai.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * youmai
 * Created by YichenZ on 2015/1/28 10:27.
 */
public class RegularVerification {

    private static Pattern pattern;
    private static Matcher matcher;

    /**
     * 判断是否手机号
     * @param mobile
     * @return
     */
    public static boolean isMobileNO(String mobile){
        pattern =Pattern.compile("^(0|86|17951)?(13[0-9]|15[0-9]|17[678]|18[0-9]|14[57]|17[0-9])[0-9]{8}$");
        matcher =pattern.matcher(mobile);
        return  matcher.matches();
    }

    /**
     * 判断是否是密码
     * @param password
     * @return
     */
    public static boolean isPasswordVer(String password){
        pattern =Pattern.compile("[A-Za-z0-9]{6,16}$");
        matcher =pattern.matcher(password);
        return matcher.matches();
    }
}

package com.itheima.login.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * MD5 加密工具类
 * <p>
 * 使用标准 MessageDigest 实现，输出 32 位小写十六进制摘要。
 * 与数据库 user.password 列存储格式保持一致。
 */
public final class Md5Util {

    private Md5Util() {
    }

    /**
     * 对明文进行 MD5 加密，返回 32 位小写十六进制字符串。
     *
     * @param plainText 明文
     * @return 密文摘要
     */
    public static String md5(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }

    /**
     * 校验明文与密文是否匹配。
     *
     * @param plainText 明文
     * @param cipher    密文
     * @return 匹配返回 true
     */
    public static boolean matches(String plainText, String cipher) {
        if (plainText == null || cipher == null) {
            return false;
        }
        return md5(plainText).equalsIgnoreCase(cipher);
    }
}
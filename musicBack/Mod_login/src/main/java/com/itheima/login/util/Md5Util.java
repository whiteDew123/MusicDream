package com.itheima.login.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * MD5 加密工具类
 * <p>
 * 采用 Spring 提供的 DigestUtils 实现，输出 32 位小写十六进制摘要。
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
        return DigestUtils.md5DigestAsHex(plainText.getBytes(StandardCharsets.UTF_8));
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

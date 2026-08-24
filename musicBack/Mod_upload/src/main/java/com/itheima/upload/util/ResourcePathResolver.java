package com.itheima.upload.util;

import java.io.File;

/**
 * 统一资源目录解析器（不依赖进程工作目录）。
 * <p>
 * 资源目录固定为项目内的 {@code musicBack/resource/<sub>}。
 * 解析优先级：
 * 1. 环境变量 {@code MUSIC_RESOURCE_ROOT}（绝对路径，部署/云端常用）；
 * 2. 从当前工作目录向上逐级查找 {@code musicBack/resource} 或 {@code resource} 目录。
 * 这样无论从哪个目录启动（IDEA 模块目录、musicBack、项目根等）都能正确定位。
 */
public final class ResourcePathResolver {

    private ResourcePathResolver() {
    }

    /** 环境变量名：资源根目录（绝对路径） */
    public static final String ROOT_ENV = "MUSIC_RESOURCE_ROOT";

    /** 子目录（music / image / lrc） */
    public static final String MUSIC = "music";
    public static final String IMAGE = "image";
    public static final String LRC = "lrc";

    /**
     * 解析某类资源的绝对目录路径（结尾带 /）。
     */
    public static String resolveDir(String sub) {
        String envRoot = System.getenv(ROOT_ENV);
        if (envRoot != null && !envRoot.isBlank()) {
            return trailingSlash(envRoot) + sub + "/";
        }
        File root = findResourceRoot();
        return root.getAbsolutePath().replace('\\', '/') + "/" + sub + "/";
    }

    private static File findResourceRoot() {
        File probe = new File(System.getProperty("user.dir")).getAbsoluteFile();
        for (int i = 0; i < 8; i++) {
            File cand1 = new File(probe, "musicBack/resource");
            if (cand1.isDirectory()) {
                return cand1;
            }
            File cand2 = new File(probe, "resource");
            if (cand2.isDirectory()) {
                return cand2;
            }
            File parent = probe.getParentFile();
            if (parent == null) {
                break;
            }
            probe = parent;
        }
        return new File("resource");
    }

    private static String trailingSlash(String s) {
        return (s.endsWith("/") || s.endsWith("\\")) ? s : s + "/";
    }
}

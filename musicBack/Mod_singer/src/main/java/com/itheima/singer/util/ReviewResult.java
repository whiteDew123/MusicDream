package com.itheima.singer.util;

/**
 * 歌曲自动审核结果
 *
 * <p>PASS = 直接上架，REJECT = 自动驳回（携带原因），MANUAL = 转人工审核</p>
 */
public class ReviewResult {

    /** 审核通过 */
    public static final String PASS = "PASS";
    /** 自动驳回 */
    public static final String REJECT = "REJECT";
    /** 转人工 */
    public static final String MANUAL = "MANUAL";

    private final String result;
    private final String message;

    private ReviewResult(String result, String message) {
        this.result = result;
        this.message = message;
    }

    /**
     * 审核通过
     */
    public static ReviewResult pass() {
        return new ReviewResult(PASS, null);
    }

    /**
     * 自动驳回
     *
     * @param reason 驳回原因（如命中的敏感词、URL 不合法等）
     */
    public static ReviewResult reject(String reason) {
        return new ReviewResult(REJECT, reason);
    }

    /**
     * 转人工审核
     */
    public static ReviewResult manual() {
        return new ReviewResult(MANUAL, null);
    }

    public boolean isPass() {
        return PASS.equals(result);
    }

    public boolean isReject() {
        return REJECT.equals(result);
    }

    public boolean isManual() {
        return MANUAL.equals(result);
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}
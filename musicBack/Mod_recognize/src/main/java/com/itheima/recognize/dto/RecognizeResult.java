package com.itheima.recognize.dto;

import lombok.Data;

/**
 * 听歌识曲结果
 */
@Data
public class RecognizeResult {

    /** 匹配到的歌曲ID */
    private Integer musicId;

    /** 匹配得分（命中指纹数） */
    private Integer matchScore;

    /** 是否识别成功 */
    private Boolean success;
}

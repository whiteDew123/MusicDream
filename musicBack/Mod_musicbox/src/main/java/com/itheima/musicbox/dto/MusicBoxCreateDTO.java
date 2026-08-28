package com.itheima.musicbox.dto;

import lombok.Data;

import java.util.List;

@Data
public class MusicBoxCreateDTO {
    private String title;
    private String moodTag;
    private String message;
    private List<Integer> songIds;
}
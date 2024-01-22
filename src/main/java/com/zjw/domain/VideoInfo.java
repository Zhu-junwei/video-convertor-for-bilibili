package com.zjw.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 朱俊伟
 * @since 2024/01/22 22:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoInfo {
    private String videoInputPath;
    private String audioInputPath;
    private String videoOutPath;
    private String videoName;
}

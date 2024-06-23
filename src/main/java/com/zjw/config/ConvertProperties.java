package com.zjw.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 朱俊伟
 * @since 2024/01/22 21:27
 */
@Component
@EnableConfigurationProperties(ConvertProperties.class)
@ConfigurationProperties(prefix = "convert")
@Data
public class ConvertProperties {

    /**
     * ffmpeg文件的位置
     */
    private String ffmpegPath = "D:/ffmpeg/bin/ffmpeg.exe";

    /**
     * 原视频缓存的全路径
     */
    private String videoInputPath;

    /**
     * 视频与音频结合之后的视频的路径
     */
    private String videoOutPath;

    /**
     * 格式化前置
     */
    private FormatProperties PrefixFormat;

    @Data
    public static class FormatProperties {

        /**
         * 合并后的文件名时候加数字前缀
         */
        private Boolean addPrefixNum;

        /**
         * 设置数字前缀的最少位数，设置为3时文件名格式为“001_+文件名”
         */
        private Integer miniDigits = 2;

    }

}

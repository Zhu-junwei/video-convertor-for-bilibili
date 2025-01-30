package com.zjw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * @author jw
 * @since 2025/01/30 14:54
 */
public class VideoConverterApplicationTest {


    @Test
    public void HelloTest() throws JsonProcessingException {
        String json = """
                {"media_type":2,"has_dash_audio":true,"is_completed":true,"total_bytes":15100459,"downloaded_bytes":15100459,"title":"硬件工程师入门教程|电子工程师|零基础|全免费|2024全新课程|中文字幕|B站 点赞 收藏NO.1","type_tag":"80","cover":"http:\\/\\/i2.hdslb.com\\/bfs\\/archive\\/9d69a14a41b6f45b1fecc0ef16fcece95a3cff14.jpg","video_quality":80,"prefered_video_quality":80,"guessed_total_bytes":0,"total_time_milli":250751,"danmaku_count":620,"time_update_stamp":1737379710885,"time_create_stamp":1737379489341,"can_play_in_advance":true,"interrupt_transform_temp_file":false,"quality_pithy_description":"1080P","quality_superscript":"","variable_resolution_ratio":false,"cache_version_code":8230600,"preferred_audio_quality":0,"audio_quality":0,"avid":113418023535249,"spid":0,"seasion_id":0,"bvid":"BV1gHSyY3E6q","owner_id":355426434,"owner_name":"电子online","is_charge_video":false,"verification_code":0,"page_data":{"cid":26596411740,"page":1,"from":"vupload","part":"硬件工程师完全零基础入门课程介绍","link":"","rich_vid":"","vid":"","has_alias":false,"tid":122,"width":1920,"height":1080,"rotate":0,"download_title":"视频已缓存完成","download_subtitle":"硬件工程师入门教程|电子工程师|零基础|全免费|2024全新课程|中文字幕|B站 点赞 收藏NO.1 硬件工程师完全零基础入门课程介绍"}}
                """;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        int mediaType = jsonNode.get("media_type").asInt();
        System.out.println(mediaType);
    }
}

package com.zjw.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author jw
 * @since 2025/01/30 14:52
 */
public class Butils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getVideoName(String fileJson) throws JsonProcessingException {
        String videoName = "";
        JsonNode jsonNode = mapper.readTree(fileJson);
        if (jsonNode.has("page_data")) {
            JsonNode pageData = jsonNode.get("page_data");
            if (pageData.has("part")) {
                videoName = pageData.get("part").asText();
            }
        }
        return videoName;
    }

    public static int getVideoIndex(String fileJson) throws JsonProcessingException {
        int page = 0;
        JsonNode jsonNode = mapper.readTree(fileJson);
        if (jsonNode.has("page_data")) {
            JsonNode pageData = jsonNode.get("page_data");
            if (pageData.has("page")) {
                page = pageData.get("page").asInt();
            }
        }
        return page;
    }
}

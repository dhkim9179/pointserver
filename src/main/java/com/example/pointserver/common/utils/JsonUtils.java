package com.example.pointserver.common.utils;

import com.example.pointserver.common.exception.CustomException;
import com.example.pointserver.common.response.ResponseCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new CustomException(ResponseCode.JSON_DATA_FORMAT_ERROR, e.getMessage());
        }
    }

    public static <T> Object fromJson(String json, Class<T> cls) {
        try {
            return objectMapper.readValue(json, cls);
        } catch (JsonProcessingException e) {
            throw new CustomException(ResponseCode.JSON_DATA_FORMAT_ERROR, e.getMessage());
        }
    }

    public static String toPrettyPrintJson(String json) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readValue(json, Object.class));
        } catch (JsonProcessingException e) {
            return json;
        }
    }
}

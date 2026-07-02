package com.smartquantify.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON工具类
 * 提供对象与JSON字符串之间的转换功能
 */
public class JsonUtil {

    /**
     * ObjectMapper实例，配置了JavaTimeModule支持Java 8日期时间类型
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    /**
     * 私有构造函数，防止实例化
     */
    private JsonUtil() {
    }

    /**
     * 将对象转换为JSON字符串
     * @param obj 待转换对象
     * @return JSON字符串
     * @throws RuntimeException 转换失败时抛出
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * 将JSON字符串转换为指定类型的对象
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 目标泛型类型
     * @return 转换后的对象
     * @throws RuntimeException 转换失败时抛出
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * 将JSON字符串转换为指定泛型类型的对象
     * @param json JSON字符串
     * @param typeReference 目标类型引用
     * @param <T> 目标泛型类型
     * @return 转换后的对象
     * @throws RuntimeException 转换失败时抛出
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }
}
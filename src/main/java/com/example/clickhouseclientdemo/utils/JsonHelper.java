/*
 * Copyright 2007 西安交通信息投资营运有限公司 版权所有
 */

package com.example.clickhouseclientdemo.utils;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * Json操作类
 */
@Slf4j
public class JsonHelper {
    private static ObjectMapper _objectMapper;

    /**
     * 默认日期时间格式字符串
     */
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期格式字符串
     */
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 默认时间格式字符串
     */
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    /**
     * 中国时区
     */
    public static final String GMT8 = "GMT+8";

    /**
     * 空字符串
     */
    public static final String Empty = "";

    /** 获取ObjectMapper对象 */
    public static ObjectMapper getObjectMapper() {
        if (_objectMapper == null) {
            _objectMapper = new ObjectMapper();
            // 设置序列化时忽略 null 属性
            _objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            //忽略空Bean转json的错误
            _objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            //忽略 在json字符串中存在，但是在对象中不存在对应属性的情况，防止错误。
            _objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            _objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
            // 在序列化一个空对象时不抛出异常
            _objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            // 忽略反序列化时在json字符串中存在, 但在java对象中不存在的属性
            _objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            //不要将日期序列化为时间戳
            _objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            // 时间格式化
            _objectMapper.setTimeZone(TimeZone.getTimeZone(GMT8));
            _objectMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATETIME_PATTERN));
            JavaTimeModule javaTimeModule = new JavaTimeModule();

            //region 注册序列化
            javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN)));
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN)));
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN)));
            javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN)));
            //endregion

            //region 注册反序列化
            javaTimeModule.addDeserializer(ZonedDateTime.class, new JsonDeserializer<>() {
                @Override
                public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    return DateUtil.parse(p.getText()).toLocalDateTime().atZone(ZoneId.systemDefault());
                }
            });
            javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    return DateUtil.parse(p.getText()).toLocalDateTime();
                }
            });
            javaTimeModule.addDeserializer(LocalDate.class, new JsonDeserializer<>() {
                @Override
                public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    return DateUtil.parse(p.getText()).toLocalDateTime().toLocalDate();
                }
            });
            javaTimeModule.addDeserializer(LocalTime.class, new JsonDeserializer<>() {
                @Override
                public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    return DateUtil.parse(p.getText()).toLocalDateTime().toLocalTime();
                }
            });
            javaTimeModule.addDeserializer(Date.class, new JsonDeserializer<>() {
                @Override
                public Date deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
                    return DateUtil.parse(p.getText()).toJdkDate();
                }
            });
            //endregion

            _objectMapper.registerModule(javaTimeModule);
        }
        return _objectMapper;
    }

    /**
     * 把对象转为Json字符串
     * @param object   对象
     * @param isPretty 是否美化
     */
    public static String toJsonString(Object object, boolean isPretty) {
        if (object == null) return Empty;
        String jsonString = Empty;
        try {
            var mapper = getObjectMapper();
            if (isPretty) {
                jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                jsonString = mapper.writeValueAsString(object);
            }
        } catch (JsonProcessingException e) {
            log.error("对象转为json字符串时出错,{}", e.getMessage(), e);
        }
        return jsonString;
    }

    /**
     * Json字符串转对象
     */
    public static <T> T toJsonObject(String data, Class<T> c) {
        try {
            return getObjectMapper().readValue(data, c);
        } catch (IOException e) {
            log.error("将json字符转换为对象时失败,{}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Json字符串转对象
     */
    public static <T> T toJsonObject(String data, JavaType valueType) {
        try {
            return getObjectMapper().readValue(data, valueType);
        } catch (IOException e) {
            log.error("将json字符转换为对象时失败,{}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Json字符串转对象
     * <code>
     *     List<Map<String,Object>> list = toJsonObject(data);
     *     Map<String,Object> map = toJsonObject(data);
     * </code>
     */
    public static <T> T toJsonObject(String data) {
        try {
            return getObjectMapper().readValue(data, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("将json字符转换为对象时失败,{}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Json字符串转集合对象
     */
    public static <T> T toJsonListObject(String data, Class<?> listClass, Class<?> elementClass) {
        try {
            return getObjectMapper().readValue(data, getJsonCollectionType(listClass, elementClass));
        } catch (IOException e) {
            throw new RuntimeException("将json字符转换为对象时失败!", e);
        }
    }

    /**
     * Json字符串转Map对象
     */
    public static <T> T toJsonMapObject(String data, Class<?> collectionClass, Class<?> keyClass, Class<?> elementClass) {
        try {
            return getObjectMapper().readValue(data, getJsonCollectionType(collectionClass, keyClass, elementClass));
        } catch (IOException e) {
            throw new RuntimeException("将json字符转换为对象时失败!", e);
        }
    }

    private static JavaType getJsonCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return getObjectMapper().getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}

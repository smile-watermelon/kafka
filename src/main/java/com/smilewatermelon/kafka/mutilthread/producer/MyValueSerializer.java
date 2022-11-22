package com.smilewatermelon.kafka.mutilthread.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

/**
 * @author guagua
 * @date 2022/11/22 16:54
 * @describe
 */
public class MyValueSerializer implements Serializer<Object> {

    @Override
    public byte[] serialize(String topic, Object data) {
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }
}

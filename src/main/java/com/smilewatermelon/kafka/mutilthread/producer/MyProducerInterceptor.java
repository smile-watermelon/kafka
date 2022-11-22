package com.smilewatermelon.kafka.mutilthread.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * @author guagua
 * @date 2022/11/22 16:33
 * @describe
 */
public class MyProducerInterceptor implements ProducerInterceptor<String, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        try {
            String key = mapper.writeValueAsString(record.key());
            String value = mapper.writeValueAsString(record.value());
            return new ProducerRecord<>(record.topic(), record.partition(), record.timestamp(), key, value, record.headers());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (metadata != null) {
            System.out.println("消息leader 节点已接收：topic=" + metadata.topic()
                    + " part=" + metadata.partition() + " offset=" + metadata.offset());

        } else {
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}

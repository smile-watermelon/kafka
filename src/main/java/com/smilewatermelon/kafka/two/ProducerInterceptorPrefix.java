package com.smilewatermelon.kafka.two;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

public class ProducerInterceptorPrefix implements ProducerInterceptor<String, String> {


    private volatile long sendSuccess = 0;

    private volatile long sendFail = 0;

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {

        String value = "prefix1-" + record.value();

        return new ProducerRecord<>(record.topic(), record.partition(), record.timestamp(), record.key(), value, record.headers());
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (exception == null) {
            sendSuccess++;
        } else {
            sendFail++;
        }
    }

    @Override
    public void close() {
        double successRatio = (double) sendSuccess / ((double) sendSuccess + (double) sendFail);
        System.out.println("[INFO] 发送成功率=" + String.format("%f", successRatio * 100 + "%"));
    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}

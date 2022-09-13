package com.smilewatermelon.kafka.three;


import com.smilewatermelon.kafka.basic.ConsumerConst;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

class CustomConsumerInterceptorTest {


    public static void main(String[] args) {
        Properties properties = ConsumerConst.initConfig();
        properties.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, CustomConsumerInterceptor.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

    }

}
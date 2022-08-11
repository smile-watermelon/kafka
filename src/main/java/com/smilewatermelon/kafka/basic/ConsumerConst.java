package com.smilewatermelon.kafka.basic;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerConst {

//    public static final String brokerList = "10.211.55.20:9092,10.211.55.21:9092,10.211.55.22:9092";
    public static final String brokerList = "10.211.55.20:9092";

    public static final String topic = "demo";
    public static final String clientId = "consumer.client.demo";

    public static final String groupId = "group.demo";

    public static Properties initConfig() {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

//        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return properties;
    }

    public static void main(String[] args) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(initConfig());
        consumer.subscribe(Collections.singleton(topic));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            System.out.println(records.isEmpty());
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(111);
                System.out.println(record.value());
                consumer.commitSync();
            }
        }
    }
}

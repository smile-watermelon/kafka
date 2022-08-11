package com.smilewatermelon.kafka.basic;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Producer {

    public static final String brokerList = "10.211.55.20:9092,10.211.55.21:9092,10.211.55.22:9092";
    public static final String topic = "demo";


    public static void main(String[] args) {
        Properties properties = new Properties();

        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        properties.put("bootstrap.servers", brokerList);

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i <10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, "hello kafka" + i);
            try {
                producer.send(record);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        producer.close();
    }

}

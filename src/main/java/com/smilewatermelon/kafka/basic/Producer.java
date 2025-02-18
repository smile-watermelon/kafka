package com.smilewatermelon.kafka.basic;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * @author guagua
 */
public class Producer {

    public static final String BROKER_LIST = "10.211.55.20:9092,10.211.55.21:9092,10.211.55.22:9092";
    public static final String TOPIC = "demo";


    public static void main(String[] args) {
        Properties properties = new Properties();

        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        properties.put("bootstrap.servers",  BROKER_LIST);

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i <10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, "hello kafka" + i);
            try {
                Future<RecordMetadata> future = producer.send(record);
                RecordMetadata recordMetadata = future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        producer.close();
    }



}

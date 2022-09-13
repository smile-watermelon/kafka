package com.smilewatermelon.kafka.basic;

import com.smilewatermelon.kafka.three.CustomConsumerInterceptor;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ProducerConst {

//    public static final String brokerList = "10.211.55.20:9092,10.211.55.21:9092,10.211.55.22:9092";
    public static final String brokerList = "10.211.55.20:9092";

    public static final String topic = "demo";
    public static final String clientId = "producer.client.demo";

    public static final String groupId = "group.demo";

    public static Properties initConfig() {
        Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        properties.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
//        properties.put(ProducerConfig.ACKS_CONFIG, "all");

        return properties;
    }

    public static void main(String[] args) {
        KafkaProducer<String, String> producer = new KafkaProducer<>(initConfig());

        for (int i = 0; i <100; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, "hello kafka" + i);
            try {
                Future<RecordMetadata> future = producer.send(record);
                RecordMetadata recordMetadata = future.get();
                System.out.println(recordMetadata);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }

    /**
     * 测试消费者拦截器
     *
     * @param producer
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void testConsumerInterceptor(KafkaProducer<String, String> producer) throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, 0,
                System.currentTimeMillis() - CustomConsumerInterceptor.expire_interval, null, "first-expire-data");
        producer.send(record).get();

        ProducerRecord<String, String> record1 = new ProducerRecord<>(topic, 0,
                System.currentTimeMillis(), null, "normal-data");
        producer.send(record).get();

        ProducerRecord<String, String> record3 = new ProducerRecord<>(topic, 0,
                System.currentTimeMillis() - CustomConsumerInterceptor.expire_interval, null, "last-expire-data");
        producer.send(record).get();
    }
}

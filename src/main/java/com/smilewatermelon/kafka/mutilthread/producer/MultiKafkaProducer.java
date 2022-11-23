package com.smilewatermelon.kafka.mutilthread.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smilewatermelon.kafka.basic.Producer;
import com.smilewatermelon.kafka.mutilthread.consumer.MultiThreadConsumer1;
import com.smilewatermelon.kafka.pojo.User;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @author guagua
 * @date 2022/11/22 16:11
 * @describe
 */
public class MultiKafkaProducer {

    //        public static final String brokerList = "10.211.55.20:9092,10.211.55.21:9092,10.211.55.22:9092";
    public static final String brokerList = "10.211.55.20:9092";
    public static final String topic = "group-demo";
    public static final ExecutorService executors = Executors.newFixedThreadPool(5);

    public static Properties initConfig() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "producer.client1");
//        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000);
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);
//        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 81920);
//        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)


        return properties;
    }

    public static void main(String[] args) throws InterruptedException {
        int threadNumber = 5;

//        for (int i = 0; i < threadNumber; i++) {
//        executors.submit(new KafkaProducerThread(initConfig(), topic));

//        }

        extracted();
        TimeUnit.SECONDS.sleep(10);
    }

    private static void extracted() {
        Random random = new Random();
        ObjectMapper objectMapper = new ObjectMapper();
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(initConfig());
        for (int i = 0; i < 4; i++) {
            try {
                User user = User.builder().age(random.nextInt(30))
                        .name("guagua" + i)
                        .email("1@qq.com").build();
                String value = objectMapper.writeValueAsString(user);
                System.out.println(value + " " + value.getBytes(StandardCharsets.UTF_8).length);


//                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic,  value);
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, 2, "","hello lady ke...");

                Future<RecordMetadata> send = kafkaProducer.send(producerRecord);

//                RecordMetadata metadata = send.get();

//                System.out.println(metadata.topic() + " " + metadata.partition() + " " + metadata.offset());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        kafkaProducer.close();
    }

    static class KafkaProducerThread extends Thread {

        private final Properties properties;
        private final String topics;
        private final KafkaProducer<String, String> kafkaProducer;

        public KafkaProducerThread(Properties properties, String topics) {
            this.properties = properties;
            this.topics = topics;
            this.kafkaProducer = new KafkaProducer<>(properties);
        }

        @Override
        public void run() {
            Random random = new Random(30);
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println("exec...");
            try {
                for (int i = 0; i < 4; i++) {
                    User user = User.builder().age(random.nextInt())
                            .name("guagua" + i)
                            .email("1@qq.com").build();
                    String value = objectMapper.writeValueAsString(user);
                    System.out.println(this.topics + " " + value);
//                    ProducerRecord<String, String> producerRecord = new ProducerRecord<>(this.topics, 1, "p1", value);
                    ProducerRecord<String, String> producerRecord = new ProducerRecord<>(this.topics, value);
                    Future<RecordMetadata> send = this.kafkaProducer.send(producerRecord);
//                    RecordMetadata metadata = send.get();
//                    System.out.println(Thread.currentThread().getName() + " " + metadata.topic() + " " + metadata.partition() + " " + metadata.offset());
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } finally {
                this.kafkaProducer.close();
            }

        }
    }
}

package com.smilewatermelon.kafka.three.chapter;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaConsumerAnalysis {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerAnalysis.class);

    public static final String brokerList = "10.211.55.20:9092";

    public static final String topic = "topic-demo";

    public static final String topicTest = "test";

    public static final String clientId = "three-chapter.consumer.demo";
    public static final String groupId = "group.demo";

    public static final AtomicBoolean isRunning = new AtomicBoolean(true);

    public static Properties initConfig() {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // 自定义反序列化器
//        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CompanyDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);

        return properties;
    }


    public static void main(String[] args) {
        consumerTopic();
        // 订阅所有分区
        subscribeAllPartitions();

    }

    /**
     * 订阅所有分区
     */
    public static void subscribeAllPartitions() {
        Properties properties = initConfig();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);

        List<TopicPartition> partitions = new ArrayList<>();

        for (PartitionInfo partition : partitionInfos) {
            partitions.add(new TopicPartition(partition.topic(), partition.partition()));
        }

        consumer.assign(partitions);

        while (isRunning.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
            for (ConsumerRecord<String, String> record : records.records(topic)) {
                System.out.println("topic = " + topic + " record = " + record.value() + " offset = " + record.offset());
            }
        }
    }

    public static void consumerMultiTopic() {
        List<String> topicList = Arrays.asList(topic, topicTest);
        Properties properties = initConfig();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(topicList);

        // 定于topic 主题的 0分区
        consumer.assign(Collections.singletonList(new TopicPartition(topic, 0)));
        // 查看主题的分区信息
        List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);

        while (isRunning.get()) {
            for (String topic : topicList) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records.records(topic)) {
                    System.out.println("topic = " + topic + " record = " + record.value());
                }
            }
        }
    }

    public static void consumerTopic() {
        Properties properties = initConfig();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(topic));


        try {
            while (isRunning.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("topic = " + record.topic() + " partition = " + record.partition() + ", offset = " + record.offset());
                    System.out.println("key = " + record.key() + ", value = " + record.value());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            consumer.close();
        }
    }
}

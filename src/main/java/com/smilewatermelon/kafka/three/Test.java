package com.smilewatermelon.kafka.three;

import com.smilewatermelon.kafka.basic.Consumer;
import com.smilewatermelon.kafka.basic.ConsumerConst;
import com.smilewatermelon.kafka.basic.ProducerConst;
import com.smilewatermelon.kafka.two.Company;
import com.smilewatermelon.kafka.two.CompanySerializer;
import com.smilewatermelon.kafka.two.ProducerInterceptorPrefix;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.*;

/**
 * 3.2.5 位移提交
 */
public class Test {


    public static void main(String[] args) {
        Properties properties = ConsumerConst.initConfig();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, ConsumerConst.groupId);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        commitOffset(consumer);
    }

    public static void commitOffset(KafkaConsumer<String, String> consumer) {

        TopicPartition tp = new TopicPartition(ConsumerConst.topic, 0);
        Set<TopicPartition> singleton = Collections.singleton(tp);


        consumer.assign(Collections.singletonList(tp));


        // 当前消费到的位移
        long lastConsumedOffset = -1;
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            if (records.isEmpty()) {
                break;
            }
            List<ConsumerRecord<String, String>> partitionRecords = records.records(tp);
            lastConsumedOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
            consumer.commitSync(); // 同步提交位移
        }

        System.out.println("consumed offset is " + lastConsumedOffset);
        Map<TopicPartition, OffsetAndMetadata> metadataMap = consumer.committed(singleton);
        System.out.println("committed offset is " + metadataMap.get(tp).offset());

        long position = consumer.position(tp);
        System.out.println("the offset of next record is " + position);
    }


    /**
     * 批量同步位移
     *
     * @param consumer
     */
    public static void batchCommit(KafkaConsumer consumer) {
        TopicPartition tp = new TopicPartition(ConsumerConst.topic, 0);
        Set<TopicPartition> singleton = Collections.singleton(tp);

        final int minBatchSize = 200;

        List<ConsumerRecord> buffer = new ArrayList<>();


        consumer.assign(Collections.singletonList(tp));

        // 当前消费到的位移
        long lastConsumedOffset = -1;
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                buffer.add(record);
            }
            if (buffer.size() >= minBatchSize) {
                consumer.commitSync();
                buffer.clear();
            }
        }
    }

    /**
     * 带参数同步位移
     */
    public static void commitParameters(KafkaConsumer consumer) {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                long offset = record.offset();
                TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());

                consumer.commitSync(Collections.singletonMap(topicPartition, new OffsetAndMetadata(offset + 1)));
            }
        }
    }

    /**
     * 按照分区粒度提交位移
     */
    public static void commitPartition(KafkaConsumer consumer) {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            Set<TopicPartition> partitions = records.partitions();
            for (TopicPartition partition : partitions) {
                List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                for (ConsumerRecord<String, String> record : partitionRecords) {
                    // do something
                }
                long offset = partitionRecords.get(partitionRecords.size() - 1).offset();
                consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(offset + 1)));
            }
        }
    }

    public static void commitAsync(KafkaConsumer consumer) {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                // do something
            }

            consumer.commitAsync(new OffsetCommitCallback() {
                @Override
                public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                    if (exception == null) {
                        System.out.println("offsets = " + offsets);
                    } else {
                        exception.printStackTrace();
                    }
                }
            });
        }
    }
    /** 最后把关
    try {
        while (isRunning.get()) {

            consumer.commitAsync();
        }
    } final {
        try {
            consumer.commitAsync();
        } finally {
            consumer.close();
        }
    }
    */
}

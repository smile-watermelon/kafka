package com.smilewatermelon.kafka.three.chapter;

import com.smilewatermelon.kafka.basic.ConsumerConst;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

/**
 * 3.2.5 位移提交
 *
 * @author guagua
 */
public class Test {

    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    private static volatile boolean isRunning = true;

    public static void setIsRunning(boolean isRun) {
        isRunning = isRun;
    }


    public static void main(String[] args) {
        Properties properties = ConsumerConst.initConfig();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, ConsumerConst.GROUP_ID);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, ConsumerConst.CLIENT_ID);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

//        commitOffset(consumer);
        commitParameters(consumer);
    }

    /**
     * 同步提交，按照批次提交
     * @param consumer
     */
    public static void commitOffset(KafkaConsumer<String, String> consumer) {

        TopicPartition tp = new TopicPartition(ConsumerConst.TOPIC, 0);
        Set<TopicPartition> singleton = Collections.singleton(tp);


        consumer.assign(Collections.singletonList(tp));


        // 当前消费到的位移
        long lastConsumedOffset = -1;
        while (isRunning) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            // ToDo 如果是一只需要拉取数据，这个不应该break掉。
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
        TopicPartition tp = new TopicPartition(ConsumerConst.TOPIC, 0);
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
     * 单条提交位移
     * 带参数同步位移
     */
    public static void commitParameters(KafkaConsumer<String, String> consumer) {
        consumer.subscribe(Collections.singletonList("topic-demo"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                long offset = record.offset();
                logger.info("当前数据的offset，value=" + record.value() + " offset=" + offset);
                TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                consumer.commitSync(Collections.singletonMap(topicPartition, new OffsetAndMetadata(offset + 1)));
            }
        }
    }

    /**
     * 按照分区提交位移
     * 同步提交 偏移量
     * 按照分区粒度提交位移
     */
    public static void commitPartition(KafkaConsumer<String, String> consumer) {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            Set<TopicPartition> partitions = records.partitions();
            for (TopicPartition partition : partitions) {
                // 获取指定分区种的数据
                List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                for (ConsumerRecord<String, String> record : partitionRecords) {
                    // do something
                    String value = record.value();
                }
                long offset = partitionRecords.get(partitionRecords.size() - 1).offset();
                consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(offset + 1)));

            }
        }
    }

    /**
     * 异步提交，设置回到函数
     *
     * @param consumer
     */
    public static void commitAsync(KafkaConsumer<String, String> consumer) {
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
     consumer.commitSync();
     } finally {
     consumer.close();
     }
     }
     */
}

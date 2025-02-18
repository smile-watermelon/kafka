package com.smilewatermelon.kafka.three.chapter;

import com.smilewatermelon.kafka.basic.ConsumerConst;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import javax.swing.plaf.synth.SynthLookAndFeel;
import java.time.Duration;
import java.util.*;

public class OffsetPosition {


    public static void main(String[] args) {
        Properties properties = ConsumerConst.initConfig();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, ConsumerConst.GROUP_ID);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

    }

    /**
     * 指定消费位移
     *
     * @param consumer
     */
    public static void seek(KafkaConsumer<String, String> consumer) {
        consumer.subscribe(Arrays.asList(ConsumerConst.TOPIC));
        /**
         * 分区分配是在poll 方法进行的，在seek之前为了保证消费者分区正确，先进行poll操作
         * 等分配到了分区，再进行位移重置操作
         */
        consumer.poll(Duration.ofMillis(1000));
        // 获取分区
        Set<TopicPartition> assignment = consumer.assignment();
        for (TopicPartition topicPartition : assignment) {
            consumer.seek(topicPartition, 10);
        }

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(record.value());
            }
        }
    }

    /**
     * 更好的指定位移的方式
     *
     * @param consumer
     */
    public static void seek2(KafkaConsumer<String, String> consumer) {
        consumer.subscribe(Arrays.asList(ConsumerConst.TOPIC));
        HashSet<TopicPartition> assignment = new HashSet<>();

        /**
         * 等待分区分配完成
         */
        while (assignment.size() == 0) {
            consumer.poll(Duration.ofMillis(1000));
            assignment = (HashSet<TopicPartition>) consumer.assignment();
        }
        // 重置消费位移
        for (TopicPartition topicPartition : assignment) {
            consumer.seek(topicPartition, 10);
        }

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(record.value());
            }
        }
    }

    /**
     * 从末尾开始消费
     *
     * @param consumer
     */
    public static void seekTail(KafkaConsumer<String, String> consumer) {
        consumer.subscribe(Collections.singletonList(ConsumerConst.TOPIC));

        Set<TopicPartition> assignment = new HashSet<>();
        while (assignment.size() == 0) {
            consumer.poll(Duration.ofMillis(100));
            assignment = consumer.assignment();
        }
        /** 从尾开始消费
         * endOffsets 方法的重载方法，如下，可以设置等待获取的超时时间，如果不指定，根据request.timeout.ms 来设置，默认为30秒
         * endOffsets(Collection<TopicPartition> partitions, Duration timeout)
         */
        Map<TopicPartition, Long> offsets = consumer.endOffsets(assignment);
//        consumer.seekToEnd(assignment);

        /**
         * 从头开始消费
         */
//        Map<TopicPartition, Long> offsets = consumer.beginningOffsets(assignment);
//        consumer.seekToBeginning(assignment);

        for (TopicPartition topicPartition : assignment) {
            consumer.seek(topicPartition, offsets.get(topicPartition));
        }
    }

    /**
     * 指定时间消费
     * @param consumer
     */
    public static void seekWithTime(KafkaConsumer<String, String> consumer) {
        consumer.subscribe(Collections.singletonList(ConsumerConst.TOPIC));
        Set<TopicPartition> assignment = new HashSet<>();
        while (assignment.size() == 0) {
            consumer.poll(Duration.ofMillis(100));
            assignment = consumer.assignment();
        }

        HashMap<TopicPartition, Long> timestampToSearch = new HashMap<>();
        for (TopicPartition topicPartition : assignment) {
            timestampToSearch.put(topicPartition, System.currentTimeMillis() - 24 * 3600 * 1000);
        }

        Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestampToSearch);
        for (TopicPartition topicPartition : assignment) {
            OffsetAndTimestamp offsetAndTimestamp = offsets.get(topicPartition);
            if (offsetAndTimestamp != null) {
                consumer.seek(topicPartition, offsetAndTimestamp.offset());
            }
        }

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            // logic handle
        }
    }

    /**
     * 将消费位移保存的DB中
     */
    public static void offsetDB(KafkaConsumer<String, String> consumer) {
        consumer.subscribe(Collections.singleton(ConsumerConst.TOPIC));
        Set<TopicPartition> assignment = new HashSet<>();
        while (assignment.size() == 0) {
            consumer.poll(Duration.ofMillis(100));
            assignment = consumer.assignment();
        }

        // 获取分区
        for (TopicPartition topicPartition : assignment) {
            // 从数据库中获取offset
            long  offset  = getOffsetFromDB(topicPartition);
            consumer.seek(topicPartition, offset);
        }
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (TopicPartition partition : records.partitions()) {
                List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);

                for (ConsumerRecord<String, String> partitionRecord : partitionRecords) {
                    // 处理数据
                }

                long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();

                storeOffsetToDB(partition, lastOffset);
            }
        }

    }

    private static void storeOffsetToDB(TopicPartition partition, long lastOffset) {

    }

    private static long getOffsetFromDB(TopicPartition topicPartition) {
        return 0;
    }


}

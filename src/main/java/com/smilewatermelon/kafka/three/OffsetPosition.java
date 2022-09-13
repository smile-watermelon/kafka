package com.smilewatermelon.kafka.three;

import com.smilewatermelon.kafka.basic.ConsumerConst;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import javax.swing.plaf.synth.SynthLookAndFeel;
import java.time.Duration;
import java.util.*;

public class OffsetPosition {


    public static void main(String[] args) {
        Properties properties = ConsumerConst.initConfig();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, ConsumerConst.groupId);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

    }

    /**
     * 指定消费位移
     *
     * @param consumer
     */
    public static void seek(KafkaConsumer<String, String> consumer) {
        consumer.subscribe(Arrays.asList(ConsumerConst.topic));
        consumer.poll(Duration.ofMillis(1000));
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
        consumer.subscribe(Arrays.asList(ConsumerConst.topic));
        HashSet<TopicPartition> assignment = new HashSet<>();

        while (assignment.size() == 0) {
            consumer.poll(Duration.ofMillis(1000));
            assignment = (HashSet<TopicPartition>) consumer.assignment();
        }
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
        consumer.subscribe(Collections.singletonList(ConsumerConst.topic));

        Set<TopicPartition> assignment = new HashSet<>();
        while (assignment.size() == 0) {
            consumer.poll(Duration.ofMillis(100));
            assignment = consumer.assignment();
        }

        Map<TopicPartition, Long> offsets = consumer.endOffsets(assignment);
        for (TopicPartition topicPartition : assignment) {
            consumer.seek(topicPartition, offsets.get(topicPartition));
        }
    }

    public static void seekWithTime(KafkaConsumer<String, String> consumer) {
        consumer.subscribe(Collections.singletonList(ConsumerConst.topic));
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
}

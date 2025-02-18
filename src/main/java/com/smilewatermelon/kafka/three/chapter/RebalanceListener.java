package com.smilewatermelon.kafka.three.chapter;


import com.smilewatermelon.kafka.basic.ConsumerConst;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

/**
 * 再均衡监听器
 */
public class RebalanceListener {


    public static void main(String[] args) {
        Properties properties = ConsumerConst.initConfig();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        rebalanceConsumer(consumer);
    }

    public static void rebalanceConsumer(KafkaConsumer<String, String> consumer) {
        HashMap<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

        consumer.subscribe(Arrays.asList(ConsumerConst.TOPIC), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                consumer.commitSync(currentOffsets);
                currentOffsets.clear();
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            }
        });

//        consumer.subscribe(Arrays.asList(ConsumerConst.topic), new ConsumerRebalanceListener() {
//            @Override
//            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
//                // 将消费位移保存到数据库
//            }
//
//            @Override
//            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
//                for (TopicPartition topicPartition : partitions) {
//                    consumer.seek(topicPartition, getOfffsetFromDB(topicPartition)); // 从数据库中读取消费位移
//                }
//            }
//        });

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(record.value());

                    currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));
                }
                consumer.commitAsync(currentOffsets, null);
            }
        } finally {
            consumer.close();
        }
    }

}

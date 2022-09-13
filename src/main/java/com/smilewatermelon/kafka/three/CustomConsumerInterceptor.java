package com.smilewatermelon.kafka.three;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 自定义消费者拦截器
 *
 */
public class CustomConsumerInterceptor implements ConsumerInterceptor<String, String> {

    public static final long expire_interval = 10 * 1000;

    @Override
    public ConsumerRecords<String, String> onConsume(ConsumerRecords<String, String> records) {
        long now = System.currentTimeMillis();

        HashMap<TopicPartition, List<ConsumerRecord<String, String>>> newRecords = new HashMap<>();

        for (TopicPartition topicPartition : records.partitions()) {
            List<ConsumerRecord<String, String>> recordList = records.records(topicPartition);
            ArrayList<ConsumerRecord<String, String>> newTpRecords = new ArrayList<>();
            for (ConsumerRecord<String, String> record : recordList) {
                if (now - record.timestamp() < expire_interval) {
                    newTpRecords.add(record);
                }
            }
            if (!newTpRecords.isEmpty()) {
                newRecords.put(topicPartition, newTpRecords);
            }
        }
        return new ConsumerRecords<>(newRecords);
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
        offsets.forEach(new BiConsumer<TopicPartition, OffsetAndMetadata>() {
            @Override
            public void accept(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata) {
                System.out.println(topicPartition + " : " + offsetAndMetadata.offset());
            }
        });
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}

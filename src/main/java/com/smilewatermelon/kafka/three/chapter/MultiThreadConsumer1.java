package com.smilewatermelon.kafka.three.chapter;

import com.smilewatermelon.kafka.basic.ConsumerConst;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * 多线程消费
 */
public class MultiThreadConsumer1 {


    public static void main(String[] args) {
        Properties properties = ConsumerConst.initConfig();
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        int consumerThreadNum = 4;
//        for (int i = 0; i < consumerThreadNum; i++) {
        new kafkaConsumerThread(properties, ConsumerConst.TOPIC, consumerThreadNum).start();
//        }
    }

    public static class kafkaConsumerThread extends Thread {
        private final KafkaConsumer<String, String> kafkaConsumer;
        private final ExecutorService executorService;

        private final HashMap<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

        private Integer threadNum;

        public kafkaConsumerThread(Properties properties, String topic, int threadNum) {
            kafkaConsumer = new KafkaConsumer<>(properties);
            this.kafkaConsumer.subscribe(Collections.singleton(topic));
            this.threadNum = threadNum;
            this.executorService = new ThreadPoolExecutor(threadNum, this.threadNum, 0L, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
        }

        public void run() {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                    if (!records.isEmpty()) {
                        executorService.submit(new RecordHandler(records, offsets));
                    }
                    kafkaConsumer.commitSync(offsets);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                kafkaConsumer.close();
            }
        }
    }

    public static class RecordHandler extends Thread {
        public final ConsumerRecords<String, String> records;

        private final HashMap<TopicPartition, OffsetAndMetadata> offsets;

        public RecordHandler(ConsumerRecords<String, String> records, HashMap<TopicPartition, OffsetAndMetadata> offsets) {
            this.records = records;
            this.offsets = offsets;
        }

        @Override
        public void run() {

            for (TopicPartition topicPartition : records.partitions()) {
                List<ConsumerRecord<String, String>> partitionRecords = records.records(topicPartition);
                long lastConsumedOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                // kafkaConsumerThread 父类对象的 offset 引用传递到子线程中，子线程修改位移数据，
                // 子线程消费结束后，父线程提交位移。
                synchronized (offsets) {
                    if (!offsets.containsKey(topicPartition)) {
                        offsets.put(topicPartition, new OffsetAndMetadata(lastConsumedOffset + 1));
                    } else {
                        long position = offsets.get(topicPartition).offset();
                        if (position < lastConsumedOffset + 1){
                            offsets.put(topicPartition, new OffsetAndMetadata(lastConsumedOffset + 1));
                        }
                    }
                }

            }
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(record.value());
            }
        }
    }


}

package com.smilewatermelon.kafka.mutilthread.consumer;

import com.smilewatermelon.kafka.basic.Consumer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.lang.management.ThreadInfo;
import java.nio.channels.Pipe;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author guagua
 * @date 2022/11/22 14:48
 * @describe
 */
public class MultiThreadConsumer2 {

    public static final String brokerList = "10.211.55.20:9092,10.211.55.21:9092,10.211.55.22:9092";
    public static final String topic = "group-demo";
    public static final String groupId = "group.demo";


    public static Properties iniConfig() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        return properties;
    }

    public static void main(String[] args) throws InterruptedException {
        Properties properties = iniConfig();
        int processors = Runtime.getRuntime().availableProcessors();
        processors = 3;
        KafkaConsumerThread kafkaConsumerThread =
                new KafkaConsumerThread(properties, topic, processors);

        kafkaConsumerThread.start();
    }

    public static class KafkaConsumerThread extends Thread {
        private final KafkaConsumer<String, String> kafkaConsumer;
        private final ExecutorService executorService;
        private final Integer threadNumbers;
        final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

        private final AtomicBoolean isRunning = new AtomicBoolean(true);

        public AtomicBoolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean running) {
            isRunning.set(running);
        }

        public KafkaConsumerThread(Properties properties, String topic, int threadNumbers) throws InterruptedException {
            this.kafkaConsumer = new KafkaConsumer<>(properties);
            this.kafkaConsumer.subscribe(Collections.singleton(topic), new ConsumerRebalanceListener() {
                // 分区重分配，再均衡之前和停止读消息之后被调用，参数partitions 表示再均衡前分配到的分区
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    kafkaConsumer.commitSync(currentOffsets);
                    currentOffsets.clear();
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

                }
            });
            this.kafkaConsumer.poll(Duration.ofMillis(10000));
            Set<TopicPartition> assignment = this.kafkaConsumer.assignment();
            for (TopicPartition topicPartition : assignment) {
                System.out.println(topicPartition.topic() + " " + topicPartition.partition()+"....");
//                this.kafkaConsumer.seek(topicPartition, 0);
            }
            TimeUnit.SECONDS.sleep(5);
            this.threadNumbers = threadNumbers;

            executorService = new ThreadPoolExecutor(this.threadNumbers, this.threadNumbers, 0L, TimeUnit.SECONDS, new
                    ArrayBlockingQueue<>(10000), new ThreadPoolExecutor.CallerRunsPolicy());
        }

        @Override
        public void run() {
            try {

                while (isRunning.get()) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
                    if (!records.isEmpty()) {
                        executorService.submit(new RecordsHandler(records, currentOffsets, this.kafkaConsumer));
//                        synchronized (currentOffsets) {
//                            if (!currentOffsets.isEmpty()) {
//                                kafkaConsumer.commitSync(currentOffsets);
//                            }
//                        }
                    }
                }
            } catch (Exception e) {

            } finally {
                this.kafkaConsumer.close();
            }
        }
    }

    public static class RecordsHandler extends Thread {
        private final ConsumerRecords<String, String> records;
        private final Map<TopicPartition, OffsetAndMetadata> currentOffsets;

        private final KafkaConsumer<String, String> kafkaConsumer;
        public RecordsHandler(ConsumerRecords<String, String> records, Map<TopicPartition, OffsetAndMetadata> currentOffsets, KafkaConsumer kafkaConsumer) {
            this.records = records;
            this.currentOffsets = currentOffsets;
            this.kafkaConsumer = kafkaConsumer;
        }

        @Override
        public void run() {
//            for (ConsumerRecord<String, String> record : records) {
//                System.out.println(record.value());
//            }
            Set<TopicPartition> partitions = records.partitions();

            for (TopicPartition tp : partitions) {
                List<ConsumerRecord<String, String>> tpRecords = records.records(tp);
                // 处理数据
                for (ConsumerRecord<String, String> tpRecord : tpRecords) {
                    System.out.println(tp.topic() + " " + tp.partition() + " " + tpRecord.value());
                }
                long lastConsumedOffset = tpRecords.get(tpRecords.size() - 1).offset();
                HashMap<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
                map.put(tp, new OffsetAndMetadata(lastConsumedOffset + 1));

//                this.kafkaConsumer.commitSync(map);
//                synchronized (currentOffsets) {
//                    if (!currentOffsets.containsKey(tp)) {
//                        OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(lastConsumedOffset + 1);
//                        currentOffsets.put(tp, offsetAndMetadata);
//                    } else {
//                        long position = currentOffsets.get(tp).offset();
//                        if (position < lastConsumedOffset + 1) {
//                            currentOffsets.put(tp, new OffsetAndMetadata(lastConsumedOffset + 1));
//                        }
//                    }
//                }
            }
        }
    }

}

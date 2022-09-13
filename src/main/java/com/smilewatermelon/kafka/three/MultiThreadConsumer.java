package com.smilewatermelon.kafka.three;

import com.smilewatermelon.kafka.basic.ConsumerConst;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * 多线程消费
 */
public class MultiThreadConsumer {


    public static void main(String[] args) {
        Properties properties = ConsumerConst.initConfig();
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        int consumerThreadNum = 4;
        for (int i = 0; i < consumerThreadNum; i++) {
            new kafkaConsumerThread(properties, ConsumerConst.topic).start();
        }
    }

    public static class kafkaConsumerThread extends Thread {
        private KafkaConsumer<String, String> kafkaConsumer;

        public kafkaConsumerThread(Properties properties, String topic) {
            kafkaConsumer = new KafkaConsumer<String, String>(properties);
            this.kafkaConsumer.subscribe(Collections.singleton(topic));
        }

        public void run() {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.println(record.value());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                kafkaConsumer.close();
            }
        }
    }


}

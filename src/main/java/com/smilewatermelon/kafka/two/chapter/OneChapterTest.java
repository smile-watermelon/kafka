package com.smilewatermelon.kafka.two.chapter;

import com.smilewatermelon.kafka.basic.ProducerConst;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 自定义序列化器
 */
public class OneChapterTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties properties = new Properties();

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 如果不指定会默认生成，格式为：Producer-1，Producer-2
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "two-chapter.test");

        // 设置重试次数
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);

        // 设置消息可靠性
        properties.put(ProducerConfig.ACKS_CONFIG, "1");

        // 使用自定义的序列化器
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CompanySerializer.class.getName());
        // 使用自定义的拦截器，可以指定多个拦截器，形成拦截器链，使用逗号分割eg:
        // properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, ProducerInterceptorPrefix.class.getName() + "," + ProducerInterceptorPrefix1.class.getName());
//        properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, ProducerInterceptorPrefix.class.getName());

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ProducerConst.BROKER_LIST);

        // 是线程安全的类，可以在多个线程中共享producer，
//        KafkaProducer<String, Company> producer = new KafkaProducer<>(properties);
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

//        Company com = Company.builder().name("guagua")
//                .address("加油").build();
//        ProducerRecord<String, Company> record = new ProducerRecord<>(ProducerConst.TOPIC, com);
        String com = "haha";
        ProducerRecord<String, String> record = new ProducerRecord<>(ProducerConst.TOPIC, com);


        // 同步发送
        try {
            // send 发送本身是异步的，调用get 同步阻塞，这里捕获发送期间的异常
            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get();

            System.out.println("消息发送成功！topic=" + metadata.topic() + " offset=" + metadata.offset());
        } catch (ExecutionException | InterruptedException e) {

        }

        // 异步发送，这里的回调函数也保证发送的顺序性，例如发送hello1，hello2，hello1的回调先触发。
        Future<RecordMetadata> future = producer.send(record, (metadata, e) -> {
            if (e != null) {
                e.printStackTrace();
            } else {
                System.out.println("消息发送成功！topic=" + metadata.topic() + " offset=" + metadata.offset());
            }
        });


        // 阻塞等待所有消息发送完成后再关闭
        producer.close();
    }
}

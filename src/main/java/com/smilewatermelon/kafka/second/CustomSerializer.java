package com.smilewatermelon.kafka.second;

import com.smilewatermelon.kafka.basic.ProducerConst;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * 自定义序列化器
 */
public class CustomSerializer {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties properties = new Properties();

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 使用自定义的序列化器
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CompanySerializer.class.getName());
        // 使用自定义的拦截器
        properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, ProducerInterceptorPrefix.class.getName());

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ProducerConst.brokerList);

        KafkaProducer<String, Company> producer = new KafkaProducer<>(properties);

        Company com = Company.builder().name("guagua")
                .address("加油").build();

        ProducerRecord<String, Company> record = new ProducerRecord<>(ProducerConst.topic, com);
        RecordMetadata recordMetadata = producer.send(record).get();


    }
}

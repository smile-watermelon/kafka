package com.smilewatermelon.kafka.producer;

import com.smilewatermelon.kafka.entity.UserEntity;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

/**
 * @类名: MyProducer
 * @描述:
 * @作者: Mabin
 * @版本: 1.0
 * @创建时间: 2023/9/23 17:06
 * @修改历史: （列表如下）
 * 时间    修改人   修改原因  修改内容
 * XXX     XXXX      XXXXX    源文件那个方法的那个代码块
 */
@Component
public class MyProducer implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.211.55.50:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, UserEntity> kafkaProducer = new KafkaProducer<>(properties);

        UserEntity user = UserEntity.builder()
                .id(1)
                .name("guagua")
                .build();
        ProducerRecord<String, UserEntity> producerRecord = new ProducerRecord<>("test", user);
        kafkaProducer.send(producerRecord);

        kafkaProducer.close();

    }
}

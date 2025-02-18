package com.smilewatermelon.kafka.consumer;

import com.smilewatermelon.kafka.entity.UserEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class MyConsumer {

//    @KafkaListener(id = "c1", topics = {"example"}, containerFactory = "KafkaListenerContainerFactory")
    @KafkaListener(id = "c1", topics = "example")
    public void listen(UserEntity user) {
        System.out.println(user);
    }
}

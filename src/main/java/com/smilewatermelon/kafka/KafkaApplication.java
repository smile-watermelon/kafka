package com.smilewatermelon.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @类名: KafkaApplication
 * @描述:
 * @作者: Mabin
 * @版本: 1.0
 * @创建时间: 2023/9/4 01:47
 * @修改历史: （列表如下）
 * 时间    修改人   修改原因  修改内容
 * XXX     XXXX      XXXXX    源文件那个方法的那个代码块
 */
@SpringBootApplication
@Slf4j
public class KafkaApplication {


    public static final int A = 1;
    private static String kafkaApplication;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(KafkaApplication.class, args);

        kafkaApplication = "KafkaApplication";
        log.info(kafkaApplication + " is running");
        log.warn(kafkaApplication + " is running");
        int a = A;
//        String[] names = context.getBeanDefinitionNames();
//        for (String name : names) {
//            System.out.println(name);
//        }
    }
}

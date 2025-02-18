package com.smilewatermelon.kafka.four.chapter;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * @author guagua
 * @date 2022/11/22 17:17
 * @describe
 */
public class KafkaAdminClient {
    public static final String brokerList = "10.211.55.20:9092,10.211.55.21:9092,10.211.55.22:9092";
    public static final String topic = "group-demo";

    public static Properties iniConfig() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        properties.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);

        return properties;
    }

    public static NewTopic getTopic(String topicName, int numPartitions, int replicationFactor) {
        return new NewTopic(topicName, numPartitions, (short) replicationFactor);
    }

    public static void main(String[] args) {
//        createTopic();
        String topicName = "group-demo";
        deleteTopic(topicName);
    }

    private static void deleteTopic(String topicName) {
        AdminClient adminClient = AdminClient.create(iniConfig());
        DeleteTopicsResult result = adminClient.deleteTopics(Collections.singleton(topicName));
        System.out.println(result.all());
    }

    private static void createTopic() {
        NewTopic newTopic = getTopic(topic, 4, 1);
        AdminClient adminClient = AdminClient.create(iniConfig());
        try {
            adminClient.createTopics(Collections.singleton(newTopic));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            adminClient.close();
        }
    }
}

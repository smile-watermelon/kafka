package com.smilewatermelon.kafka.two.chapter;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

public class ProducerInterceptorPrefix implements ProducerInterceptor<String, String> {


    private volatile long sendSuccess = 0;

    private volatile long sendFail = 0;


    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {

        String value = "prefix1-" + record.value();

        return new ProducerRecord<>(record.topic(), record.partition(), record.timestamp(), record.key(), value, record.headers());
    }


    /**
     * 在消息被应答之前或消息发送失败调用，优先与send() 方法的Callback回调，这个方法运行在Producer线程中，代码应尽量简单
     * 以下三个方法抛出的异常不会向外传递，会被记录到日志
     *
     * @param metadata The metadata for the record that was sent (i.e. the partition and offset).
     *                 If an error occurred, metadata will contain only valid topic and maybe
     *                 partition. If partition is not given in ProducerRecord and an error occurs
     *                 before partition gets assigned, then partition will be set to RecordMetadata.NO_PARTITION.
     *                 The metadata may be null if the client passed null record to
     *                 {@link org.apache.kafka.clients.producer.KafkaProducer#send(ProducerRecord)}.
     * @param exception The exception thrown during processing of this record. Null if no error occurred.
     */
    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (exception == null) {
            sendSuccess++;
        } else {
            sendFail++;
        }
    }

    @Override
    public void close() {
        double successRatio = (double) sendSuccess / ((double) sendSuccess + (double) sendFail);
        System.out.println("[INFO] 发送成功率=" + String.format("%f%s", successRatio * 100, "%"));
    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}

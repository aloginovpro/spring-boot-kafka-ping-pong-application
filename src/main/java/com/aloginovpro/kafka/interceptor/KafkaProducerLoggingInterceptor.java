package com.aloginovpro.kafka.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

@Slf4j
public class KafkaProducerLoggingInterceptor implements ProducerInterceptor<Object, Object> {

    @Override
    public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> record) {
        log.info("Producing kafka message: topic = {}, key = {}, value = {}",
                record.topic(),
               String.valueOf(record.key()),
               String.valueOf(record.value())
        );
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }


    @Override
    public void configure(Map<String, ?> configs) {
    }
}

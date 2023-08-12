package com.aloginovpro.kafka;

import com.aloginovpro.kafka.message.StringMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PingListener {

    private final KafkaTemplate<Void, Object> template;
    @Value("${topic.pong-topic-name}")
    private String pongTopic;

    @KafkaListener(topics = "${topic.ping-topic-name}")
    public void processPing(List<StringMessage> messages, Acknowledgment ack) {
        messages.forEach(message -> {
            String responseValue = Objects.equals(message.value(), "hello") ? "world" : "go away";
            template.send(pongTopic, new StringMessage(responseValue));
        });
        ack.acknowledge();
    }


}

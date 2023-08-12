package com.aloginovpro.kafka;

import com.aloginovpro.kafka.message.StringMessage;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.aloginovpro.kafka.SpringBootKafkaPingPongApplication.class)
@Testcontainers
@ContextConfiguration(initializers = SpringBootKafkaPingPongApplicationTests.KafkaInitializer.class)
class SpringBootKafkaPingPongApplicationTests {

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    static class KafkaInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.kafka.consumer.bootstrap-servers=localhost:" + kafkaContainer.getFirstMappedPort(),
                    "spring.kafka.producer.bootstrap-servers=localhost:" + kafkaContainer.getFirstMappedPort()
            ).applyTo(applicationContext.getEnvironment());
        }
    }


    @Test
    @SneakyThrows
    void testPingPong() {
        Consumer<Void, Object> consumer = createConsumer();
        consumer.subscribe(List.of("pong"));

        Producer<Void, Object> producer = createProducer();
        StringMessage outMessage = new StringMessage("hello");
        producer.send(new ProducerRecord<>("ping", outMessage)).get();

        StringMessage pongMessage = getSingleRecord(consumer, "pong");
        assertThat(pongMessage.value()).isEqualTo("world");
    }

    protected <T> T getSingleRecord(Consumer<Void, Object> consumer, String topicName) {
        return (T) KafkaTestUtils.getSingleRecord(consumer, topicName).value();
    }

    public Producer<Void, Object> createProducer() {
        return new DefaultKafkaProducerFactory<>(
                Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + kafkaContainer.getFirstMappedPort()
                ),
                new VoidSerializer(),
                new JsonSerializer<>()
        ).createProducer();
    }

    public Consumer<Void, Object> createConsumer() {
        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + kafkaContainer.getFirstMappedPort(),
                        ConsumerConfig.GROUP_ID_CONFIG, "test_group",
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
                ),
                new VoidDeserializer(),
                jsonDeserializer
        ).createConsumer();
    }

}

package com.kafkaSecretManagerPoc.AivenKafkaSecretManagerPoc.Kafka;

import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("gcp")
public class GCPKafkaSSLConfig {

    @Autowired
    private SecretManagerTemplate secretManagerTemplate;

    @Value("${sm://dev_spring_kafka_bootstrap-servers}")
    String kafkaBootStrapServer;

    @Value("${sm://kafka-keystore-password}")
    String kafkaKeystorePassword;

    @Value("${sm://kafka-truststore-password}")
    String kafkaTruststorePassword;

    @Value("${sm://kafka-key-password}")
    String kafkaKeyPassword;

    @Bean
    public Map<String, Object> kafkaSSLProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", kafkaBootStrapServer);
        props.put("security.protocol", "SSL");
        props.put("ssl.truststore.type", "JKS");
        props.put("ssl.keystore.type", "PKCS12");
        props.put("ssl.truststore.location", createTempFile("kafka-truststore", ".jks"));
        props.put("ssl.keystore.location", createTempFile("kafka-keystore", ".p12"));
        props.put("ssl.truststore.password", kafkaTruststorePassword);
        props.put("ssl.keystore.password", kafkaKeystorePassword);
        props.put("ssl.key.password", kafkaKeyPassword);
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        return props;
    }

    private String createTempFile(String secretName, String suffix) {
        try {
            byte[] content = secretManagerTemplate.getSecretBytes(secretName);
            Path tempFile = Files.createTempFile(secretName, suffix);
            Files.write(tempFile, content);
            return tempFile.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp file from secret: " + secretName, e);
        }
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(kafkaSSLProperties());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

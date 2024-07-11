package com.kafkaSecretManagerPoc.AivenKafkaSecretManagerPoc.Kafka;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
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
@Profile("aws")
public class AWSKafkaSSLConfig {

    private final AWSSecretsManager secretsManager;

    public AWSKafkaSSLConfig() {
        this.secretsManager = AWSSecretsManagerClientBuilder.defaultClient();
    }

    private String getSecret(String secretName) {
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = secretsManager.getSecretValue(getSecretValueRequest);
        return getSecretValueResult.getSecretString();
    }

    @Bean
    public Map<String, Object> kafkaSSLProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", getSecret("dev_spring_kafka_bootstrap-servers"));
        props.put("security.protocol", "SSL");
        props.put("ssl.truststore.type", "JKS");
        props.put("ssl.keystore.type", "PKCS12");
        props.put("ssl.truststore.location", createTempFile("kafka-truststore", ".jks"));
        props.put("ssl.keystore.location", createTempFile("kafka-keystore", ".p12"));
        props.put("ssl.truststore.password", getSecret("kafka-truststore-password"));
        props.put("ssl.keystore.password", getSecret("kafka-keystore-password"));
        props.put("ssl.key.password", getSecret("kafka-key-password"));
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        return props;
    }

    private String createTempFile(String secretName, String suffix) {
        try {
            byte[] content = getSecret(secretName).getBytes();
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

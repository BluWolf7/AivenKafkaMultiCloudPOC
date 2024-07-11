package com.kafkaSecretManagerPoc.AivenKafkaSecretManagerPoc.Kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "sample-topic", groupId = "sample-group-id")
    public void consumeMessage(String message){
        System.out.println("Consumed message: "+ message);
    }
}

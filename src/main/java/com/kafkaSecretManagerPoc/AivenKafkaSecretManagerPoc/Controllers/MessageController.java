package com.kafkaSecretManagerPoc.AivenKafkaSecretManagerPoc.Controllers;

import com.kafkaSecretManagerPoc.AivenKafkaSecretManagerPoc.Kafka.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    @Autowired
    KafkaProducerService kafkaProducerService;

    @PostMapping
    public ResponseEntity<String> publishMessage(@RequestBody String message){
        kafkaProducerService.sendMessage(message);
        return ResponseEntity.ok("Message Published Successfully");
    }
}

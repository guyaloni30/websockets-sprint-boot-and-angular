package com.example.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@AllArgsConstructor
public class TimestampService {
    private static final JsonMapper jsonMapper = new JsonMapper();

    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 1000)
    public void sendTimestamp() {
        String message = String.format("Periodic timestamp: %d", System.currentTimeMillis());
        try {
            messagingTemplate.convertAndSend("/topic/timestamp", jsonMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can't marshal " + message, e);
        }
    }
}

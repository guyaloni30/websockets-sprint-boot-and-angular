package com.example.websockets;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class PeriodicBroadcastService {
    private static final Map<String, Object> headers = Map.of("content-type", MediaType.APPLICATION_JSON_VALUE);

    private final SimpMessagingTemplate messagingTemplate;

    private int id = 1;

    @Scheduled(fixedRate = 1000)
    public void sendTimestamp() {
        WebsocketResponse message = new WebsocketResponse(-1, String.format("Periodic broadcast #" + id++));
        messagingTemplate.convertAndSend("/topic/broadcast", message, headers);
    }
}

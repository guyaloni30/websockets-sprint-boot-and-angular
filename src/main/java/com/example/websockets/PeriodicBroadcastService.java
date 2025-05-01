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

    @Scheduled(fixedRate = 5000)
    public void sendTimestamp() {
        MyWebsocketMessage message = new MyWebsocketMessage(-1, "Periodic keepalive broadcast #" + id++);
        messagingTemplate.convertAndSend(Consts.TOPIC_BROADCAST, message, headers);
    }
}

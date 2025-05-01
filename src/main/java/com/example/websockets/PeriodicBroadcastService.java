package com.example.websockets;

import com.example.websockets.messages.MyWebsocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@ConditionalOnProperty(name = "periodic.broadcast", havingValue = "true")
@Service
@EnableScheduling
@RequiredArgsConstructor
public class PeriodicBroadcastService {
    private static final Map<String, Object> headers = Map.of("content-type", MediaType.APPLICATION_JSON_VALUE);

    private final SimpMessagingTemplate messagingTemplate;

    private int id = 1;

    @Scheduled(fixedRate = 5000)
    public void sendTimestamp() {
        MyWebsocketMessage message = new MyWebsocketMessage(null, 1, "Periodic keepalive broadcast #" + id++);
        messagingTemplate.convertAndSend(Consts.TOPIC_PREFIX + Consts.TOPIC_BROADCAST, message, headers);
    }
}

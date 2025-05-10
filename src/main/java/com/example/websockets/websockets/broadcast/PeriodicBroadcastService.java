package com.example.websockets.websockets.broadcast;

import com.example.websockets.websockets.Consts;
import com.example.websockets.websockets.service.WebSocketsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(name = "periodic.broadcast", havingValue = "true")
@Service
@EnableScheduling
@RequiredArgsConstructor
public class PeriodicBroadcastService {
    private final WebSocketsService webSocketsService;

    private int time = 1;

    @Scheduled(fixedRate = 5000)
    public void sendTimestamp() {
        ++time;
        webSocketsService.sendAllJson(
                Consts.TOPIC_PREFIX + Consts.TOPIC_BROADCAST,
                new KeepaliveBroadcast(time));
    }
}

package com.example.websockets.websockets;

import com.example.websockets.websockets.service.WebSocketsService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class WebSocketController {
    private final WebSocketsService webSocketsService;

    @MessageMapping(Consts.REQUEST_GREETING)
    @SendTo(Consts.TOPIC_PREFIX + Consts.TOPIC_JOIN)
    public Messages.JoinBroadcast greeting(Messages.HelloRequest msg, SimpMessageHeaderAccessor headerAccessor) {
        //System.out.println("greeting " + msg);
        return new Messages.JoinBroadcast(headerAccessor.getSessionId(), "Greeting to " + msg.text() + " who joined the party");
    }

    @MessageMapping(Consts.REQUEST_HELLO)
    public void hello(Messages.HelloRequest msg, SimpMessageHeaderAccessor headerAccessor) {
        //System.out.println("hello " + msg);
        String sessionId = headerAccessor.getSessionId();
        webSocketsService.sendTo(
                headerAccessor,
                Consts.QUEUE_PREFIX + Consts.RESPONSE_TO_HELLO,
                new Messages.HelloResponse(
                        sessionId,
                        "Hello " + msg.text() + ", how can I help you?"));
    }
}

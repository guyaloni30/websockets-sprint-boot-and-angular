package com.example.websockets;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public MyWebsocketMessage greeting(MyWebsocketMessage msg) {
        return new MyWebsocketMessage(msg.id(), "Hello " + msg.text());
    }
}

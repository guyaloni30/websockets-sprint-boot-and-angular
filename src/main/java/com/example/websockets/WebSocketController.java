package com.example.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private static final JsonMapper jsonMapper = new JsonMapper();

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greeting(String name) {
        String response = "Hello " + name;
        System.out.println(response);
        try {
            return jsonMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can't marshal " + response, e);
        }
    }
}

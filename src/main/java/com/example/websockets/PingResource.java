package com.example.websockets;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "ping", produces = MediaType.APPLICATION_JSON_VALUE)
public class PingResource {
    @GetMapping
    public Pong ping() {
        return new Pong("pong", System.currentTimeMillis());
    }

    public record Pong(String response, long time) {
    }
}

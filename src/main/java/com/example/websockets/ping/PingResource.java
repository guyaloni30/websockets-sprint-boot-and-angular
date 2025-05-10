package com.example.websockets.ping;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/ping", produces = MediaType.APPLICATION_JSON_VALUE)
public class PingResource {
    @GetMapping
    public Pong ping() throws InterruptedException {
        Thread.sleep(500);
        return new Pong("pong", System.currentTimeMillis());
    }
}

package com.example.websockets.mains;

import com.example.websockets.client.WebSocketClient;

public class Hello3 {
    public static void main(String[] args) {
        try (MainWebSocketClient c = new MainWebSocketClient(3)) {
            c.run();
        }
        WebSocketClient.shutdown();
    }
}

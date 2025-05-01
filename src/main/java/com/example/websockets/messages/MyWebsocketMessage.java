package com.example.websockets.messages;

public record MyWebsocketMessage(String sessionId, int id, String text) {
}

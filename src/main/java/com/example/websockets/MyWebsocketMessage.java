package com.example.websockets;

public record MyWebsocketMessage(String sessionId, int id, String text) {
}

package com.example.websockets;

public interface Messages {
    record HelloRequest(String text) {
    }

    record HelloResponse(String sessionId, String text) {
    }

    record JoinBroadcast(String sessionId, String text) {
    }

    record KeepaliveBroadcast(long time) {
    }
}

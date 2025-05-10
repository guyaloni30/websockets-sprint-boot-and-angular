package com.example.websockets.client;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.Closeable;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@RequiredArgsConstructor
public abstract class WebSocketClient implements Closeable {
    private static final SockJsClient sockJsClient;
    private static final ThreadPoolTaskScheduler taskScheduler;
    private static final WebSocketStompClient stompClient;

    static {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketTransport transport = new WebSocketTransport(webSocketClient);
        sockJsClient = new SockJsClient(List.of(transport));
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("stomp-scheduler-");
        taskScheduler.setDaemon(true); // Critical for allowing process exit
        taskScheduler.initialize();
        stompClient = new WebSocketStompClient(sockJsClient);
        MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();
        stompClient.setMessageConverter(new CompositeMessageConverter(List.of(mappingJackson2MessageConverter)));
        stompClient.setTaskScheduler(taskScheduler);
        stompClient.start();
    }

    public static void shutdown() {
        stompClient.stop();
        sockJsClient.stop();
        System.out.println(sockJsClient);
        taskScheduler.shutdown();
        Thread
                .getAllStackTraces()
                .keySet()
                .stream()
                .filter(t -> !t.isDaemon())
                .filter(t -> !"main".equals(t.getName()))
                .map(t -> "Non-daemon thread: " + t.getName() + " - State: " + t.getState() + " - ThreadGroup: " + ((t.getThreadGroup() != null) ? t.getThreadGroup().getName() : "-"))
                .sorted()
                .forEach(System.out::println);
    }

    private final List<StompSession.Subscription> subscriptions = new ArrayList<>();

    private final URI uri;
    private final String rootPath;

    protected StompSession session;

    @Override
    public void close() {
        disconnect(0);
    }

    public final void send(String destination, Object message) {
        session.send(rootPath + destination, message);
    }

    public final boolean isConnected() {
        return (session != null) && session.isConnected();
    }

    public final CompletableFuture<Void> connectAsync() {
        return stompClient.connectAsync(uri, null, null, new MyStompSessionHandler())
                .thenAccept(session -> this.session = session);
    }

    public final void disconnect(long sleepAfterUnsubscribing) {
        subscriptions.forEach(StompSession.Subscription::unsubscribe);
        subscriptions.clear();
        if (sleepAfterUnsubscribing > 0) {
            try {
                Thread.sleep(sleepAfterUnsubscribing);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (session != null) {
            try {
                session.disconnect();
            } catch (Exception e) {
                //Do nothing
            }
            session = null;
        }
    }

    protected <T> void subscribe(StompSession session, String destination, Class<T> messageType, Consumer<T> handler) {
        subscriptions.add(session.subscribe(destination, new GenericMessageAdapter<>(messageType, handler)));
    }

    @AllArgsConstructor
    private static class GenericMessageAdapter<T> extends StompSessionHandlerAdapter {
        private final Class<T> messageType;
        private final Consumer<T> handler;

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return messageType;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            try {
                handler.accept((T) payload);
            } catch (ClassCastException e) {
                throw new RuntimeException("Unknown keepalive message type " + payload.getClass().getName(), e);
            }
        }
    }

    private class MyStompSessionHandler implements StompSessionHandler {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            WebSocketClient.this.afterConnected(session, connectedHeaders);
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            WebSocketClient.this.handleException(session, command, headers, payload, exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            WebSocketClient.this.handleTransportError(session, exception);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
        }
    }

    protected abstract void afterConnected(StompSession session, StompHeaders connectedHeaders);

    protected void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
    }

    protected void handleTransportError(StompSession session, Throwable exception) {
    }
}
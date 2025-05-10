package com.example.websockets.load;

import com.example.websockets.client.WebSocketClient;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class LoadTestWithoutSleepInServer {
    private final AtomicInteger receivedMessages = new AtomicInteger(0);
    private final List<LoadWebSocketClient> clients = IntStream.range(0, 100)
            .mapToObj(index -> new LoadWebSocketClient(receivedMessages, index))
            .toList();

    private final ExecutorService es;

    public static void main(String[] args) throws InterruptedException {
        try (ExecutorService es = Executors.newScheduledThreadPool(1000)) {
            LoadTestWithoutSleepInServer loadTest = new LoadTestWithoutSleepInServer(es);
            loadTest.connectAll();
            loadTest.startAll();
            sleep(1000);//Let clients close
            loadTest.clients.forEach(WebSocketClient::disconnect);
            System.out.println("done");
        }
        System.out.println("really done");
    }

    private void connectAll() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(clients.size());
        long start = System.currentTimeMillis();
        clients.forEach(c -> {
            es.submit(() -> {
                c.connect();
                countDownLatch.countDown();
            });
        });
        while (!countDownLatch.await(1, TimeUnit.SECONDS)) {
            System.out.println("Connecting, " + countDownLatch.getCount() + " clients left");
        }
        System.out.println("Connected in " + (System.currentTimeMillis() - start) + " ms");
    }

    private void startAll() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(clients.size());
        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        clients.forEach(c -> es.submit(sendMessages(c, endTime, countDownLatch)));
        while (!countDownLatch.await(1, TimeUnit.SECONDS)) {
            System.out.println("Running " + countDownLatch.getCount() + ", " + receivedMessages.get() + " messages received");
        }
    }

    private Runnable sendMessages(LoadWebSocketClient c, long endTime, CountDownLatch countDownLatch) {
        return () -> {
            try {
                while (System.currentTimeMillis() < endTime) {
                    sleep(100);
                    c.send(0);
                }
            } finally {
                countDownLatch.countDown();
            }
        };
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

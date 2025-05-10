package com.example.websockets.load;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class LoadTestWithoutSleepInServer {
    private final AtomicInteger receivedMessages = new AtomicInteger(0);
    private final AtomicInteger receivedBroadcastMessages = new AtomicInteger(0);
    private final List<LoadWebSocketClient> clients = IntStream.range(0, 15_000)
            .mapToObj(index -> new LoadWebSocketClient(receivedBroadcastMessages, receivedMessages, index))
            .toList();

    public static void main(String[] args) throws InterruptedException {
        try (ExecutorService es = Executors.newScheduledThreadPool(1000)) {
            LoadTestWithoutSleepInServer loadTest = new LoadTestWithoutSleepInServer();
            Thread.sleep(2000);
            loadTest.connectAll();
            Thread.sleep(2000);
            loadTest.startAll();
            Thread.sleep(2000);
            System.out.println("Disconnecting");
            loadTest.clients.forEach(c -> es.submit(() -> c.disconnect(100)));
            System.out.println("done");
        }
        System.out.println("really done");
    }

    private void connectAll() throws InterruptedException {
        System.out.println("Connecting");
        long start = System.currentTimeMillis();
        AtomicInteger retries = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(clients.size());
        try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
            clients.forEach(c -> executor.submit(() -> {
                boolean connected = false;
                do {
                    try {
                        c.connectAsync().get();
                        connected = true;
                    } catch (Exception e) {
                        retries.incrementAndGet();
                        System.out.println(c.getIndex() + ": " + e.getMessage());
                    }
                } while (!connected);
                latch.countDown();
            }));
            long startSchedule = System.currentTimeMillis();
            while (!latch.await(1, TimeUnit.SECONDS)) {
                System.out.println(latch.getCount() + " remaining in " + (System.currentTimeMillis() - startSchedule) + " ms with " + retries.get() + " retries");
            }
        }
        System.out.println(clients.size() + " clients connected in " + (System.currentTimeMillis() - start) + " ms with " + retries.get() + " retries");
    }

    private void startAll() {
        System.out.println("Running");
        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20);
        while (System.currentTimeMillis() < endTime) {
            sleep(1000);
            System.out.println("Running, " + receivedMessages.get() + " messages received, " + receivedBroadcastMessages.get() + " broadcast messages received");
            long start = System.currentTimeMillis();
            clients.forEach(c -> c.send(0));
            System.out.println("sent " + clients.size() + " in " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

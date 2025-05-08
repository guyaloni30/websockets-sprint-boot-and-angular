package com.example.websockets.load;

import com.example.websockets.Messages;
import com.example.websockets.client.WebSocketClient;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class WebSocketsLoadTest {
    private final List<WebSocketClient> clients = IntStream.range(0, 100_000)
            .mapToObj(index -> new WebSocketClient(index, "ws://localhost:8080/ws", false))
            .toList();

    private final ExecutorService es;

    public static void main(String[] args) throws InterruptedException {
        try (ExecutorService es = Executors.newScheduledThreadPool(1000)) {
            WebSocketsLoadTest loadTest = new WebSocketsLoadTest(es);
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
        while (!countDownLatch.await(100, TimeUnit.MILLISECONDS)) {
            System.out.println("Connecting " + countDownLatch.getCount());
        }
        System.out.println("Connected in " + (System.currentTimeMillis() - start) + " ms");
    }

    private void startAll() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(clients.size());
        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        clients.forEach(c -> es.submit(sendMessages(c, endTime, countDownLatch)));
        while (!countDownLatch.await(100, TimeUnit.MILLISECONDS)) {
            System.out.println("Running " + countDownLatch.getCount());
        }
    }

    private Runnable sendMessages(WebSocketClient c, long endTime, CountDownLatch countDownLatch) {
        return () -> {
            try {
                while (System.currentTimeMillis() < endTime) {
                    sleep(100);
                    c.send(new Messages.HelloRequest("client" + c.getId()));
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

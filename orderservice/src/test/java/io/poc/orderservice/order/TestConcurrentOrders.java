package io.poc.orderservice.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestConcurrentOrders {

    @Autowired
    private RestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String URL;

    @BeforeEach
    void setup() {
        URL = "http://localhost:" + port + "/api/v1/order-service/order-confirmation";
    }

    @Test
    public void testOrderConcurrency() throws InterruptedException {

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        String body = """
            {
               "userId": 3,
               "email": "user3@test.com",
               "orderStatus": "CREATED",
               "orderValue": 500,
               "items": [
                 {
                   "foodId": 3,
                   "quantity": 1,
                   "price": 200
                 },
                 {
                   "foodId": 8,
                   "quantity": 1,
                   "price": 300
                 }
               ]
            }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    restTemplate.postForObject(URL, request, String.class);
                    System.out.println("Success - " + Thread.currentThread().getName());
                } catch (Exception e) {
                    System.out.println("Failed  - " + Thread.currentThread().getName()
                            + " : " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }
}

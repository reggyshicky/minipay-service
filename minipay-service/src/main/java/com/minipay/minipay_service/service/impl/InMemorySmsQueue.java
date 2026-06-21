package com.minipay.minipay_service.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class InMemorySmsQueue {

    private final BlockingQueue<SmsJob> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Thread consumerThread;

    public void enqueue(SmsJob job) {
        queue.offer(job);
        log.info("Enqueued SMS job for {}, queue size now: {}", job.phoneNumber(), queue.size());
    }

    @PostConstruct
    public void startConsumer() {
        consumerThread = new Thread(this::consumeLoop, "sms-queue-consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
        log.info("In-memory SMS queue consumer started");
    }

    private void consumeLoop() {
        while (running.get()) {
            try {
                SmsJob job = queue.take(); // blocks until a job is available
                processJob(job);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processJob(SmsJob job) {
        log.info("==== [MOCK SMS - QUEUE CONSUMER] ====");
        log.info("To: {}", job.phoneNumber());
        log.info("Message: {}", job.message());
        log.info("Remaining queue size: {}", queue.size());
        log.info("======================================");
    }

    @PreDestroy
    public void stopConsumer() {
        running.set(false);
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }
}
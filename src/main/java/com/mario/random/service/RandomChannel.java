package com.mario.random.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RandomChannel {

    private static volatile ManagedChannel instance;
    private static final Object lock = new Object();

    @Getter
    private static String address;
    private static int port;

    private RandomChannel() {
    }

    // Method to initialize or get the singleton instance
    public static ManagedChannel getInstance(String address, int port) {
        if (instance == null || isChannelShutdownOrTerminated(instance)) {
            synchronized (lock) {
                log.debug("Lock - Initializing channel for address: {} and port: {}", address, port);
                if (instance == null || isChannelShutdownOrTerminated(instance)) {
                    // Reinitialize the channel if it's closed or terminated
                    RandomChannel.address = address;
                    RandomChannel.port = port;
                    instance = createChannel();
                }
            }
        }
        return instance;
    }

    private static ManagedChannel createChannel() {
        log.debug("Creating new channel for address: {} and port: {}", address, port);
        return ManagedChannelBuilder
                .forAddress(address, port)
                .usePlaintext()
                .enableRetry()
                .keepAliveWithoutCalls(true)
                .build();
    }
    public static boolean isAlive() {
        return instance != null && !isChannelShutdownOrTerminated(instance);
    }

    private static boolean isChannelShutdownOrTerminated(ManagedChannel channel) {
        return channel.isShutdown() || channel.isTerminated();
    }

    public static void shutdown() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
    }

    public static ManagedChannel ensureHealthyChannel() {
        if (instance == null || isChannelShutdownOrTerminated(instance)) {
            synchronized (lock) {
                if (instance == null || isChannelShutdownOrTerminated(instance)) {
                    instance = createChannel();
                }
            }
        }
        return instance;
    }
}

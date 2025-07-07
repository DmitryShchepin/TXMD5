package com.mario.random.service;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ChannelKiller {

    private static final int SHUTDOWN_INTERVAL_MINUTES = 5;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void scheduleChannelShutdown() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                log.debug("Scheduled shutdown of the channel");
                RandomChannel.shutdown();
            } catch (Exception e) {
                log.error("Error during scheduled shutdown: {}", e.getMessage());
            }
        }, SHUTDOWN_INTERVAL_MINUTES, SHUTDOWN_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }
}

package net.neoforged.meta.triggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements a background thread that will continuously run an "iteration function".
 * Either on a fixed interval or when manually requested, which is why we don't use
 * a simple scheduled executor.
 */
public class ControllerLoop implements SmartLifecycle, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ControllerLoop.class);

    private final Duration maxInterval;
    private final Runnable iteration;
    private final Lock signalLock = new ReentrantLock();
    private final Condition signalCondition = signalLock.newCondition();
    private Thread controllerThread;
    private volatile boolean running;
    private boolean iterationRequested;

    public ControllerLoop(Duration maxInterval, Runnable iteration) {
        this.maxInterval = maxInterval;
        this.iteration = iteration;
    }

    private void runControllerLoop() {
        while (running) {
            try {
                waitForIteration();
                if (!running) {
                    return; // Explicitly check now since we've been asleep for a bit
                }
                iteration.run();
            } catch (Throwable e) {
                LOG.error("Failed a controller loop iteration.", e);
            }
        }
    }

    private void waitForIteration() {
        signalLock.lock();
        try {
            var nextIteration = new Date();
            nextIteration.setTime(nextIteration.getTime() + maxInterval.toMillis());

            while (running && !iterationRequested && new Date().before(nextIteration)) {
                try {
                    signalCondition.awaitUntil(nextIteration);
                } catch (InterruptedException ignored) {
                    // This causes a re-check of running on the next loop
                    Thread.currentThread().interrupt();
                }
            }
            if (!running) {
                return; // We were canceled
            }
            if (iterationRequested) {
                LOG.debug("Running manually requested iteration");
            }
            iterationRequested = false;
        } finally {
            signalLock.unlock();
        }
    }

    public void triggerIteration() {
        signalLock.lock();
        try {
            signalCondition.signalAll();
        } finally {
            signalLock.unlock();
        }
    }

    @Override
    public void start() {
        if (!running) {
            running = true;
            controllerThread = new Thread(this::runControllerLoop);
            controllerThread.setName("trigger-controller");
            controllerThread.setDaemon(true);
            controllerThread.start();
        }
    }

    @Override
    public void stop() {
        if (running) {
            try {
                running = false;
                controllerThread.interrupt();
                triggerIteration();
                controllerThread.join();
                controllerThread = null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void close() {
        stop();
    }
}

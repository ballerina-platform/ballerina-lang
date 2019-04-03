package org.wso2.transport.http.netty.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Http2BackPressureListener implements BackPressureListener {
    private static final Logger LOG = LoggerFactory.getLogger(Http2BackPressureListener.class);

    private Semaphore semaphore;

    /**
     * Creates the semaphore and sets the source or target channel.
     */
    public Http2BackPressureListener() {

        this.semaphore = new Semaphore(0);
    }

    @Override
    public void onUnWritable() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Semaphore acquired in thread {}.", Thread.currentThread().getName());
        }
        try {
            LOG.warn("{} Semaphore acquired.", Thread.currentThread().getName());
            boolean permitAcquired = semaphore.tryAcquire(5000, TimeUnit.MILLISECONDS);
            if (!permitAcquired) {
                LOG.warn("Http2BackPressureListener was not able to acquire a permit within the time limit {} ",
                         "5000ms");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onWritable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Semaphore released in thread {} ", Thread.currentThread().getName());
        }
        LOG.warn("{} Semaphore released.", Thread.currentThread().getName());
        semaphore.release();
    }
}

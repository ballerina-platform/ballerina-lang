package org.wso2.siddhi.core.util.transport;

/**
 * Backoff Retry Counter to count when to retry next during reconnection
 */
public class BackoffRetryCounter {
    private final String[] timeIntervalNames = new String[]{"5 sec", "10 sec", "15 sec", "30 sec", "1 min", "1 min",
            "2 min", "5 min"};
    private final long[] timeIntervals = new long[]{5000, 10000, 15000, 30000, 60000, 60000, 120000, 300000};

    private int intervalIndex = 0;

    public synchronized void reset() {
        intervalIndex = 0;
    }

    public synchronized void increment() {
        if (intervalIndex < timeIntervals.length - 2) {
            intervalIndex++;
        }
    }

    public long getTimeIntervalMillis() {
        return timeIntervals[intervalIndex];
    }


    public String getTimeInterval() {
        return timeIntervalNames[intervalIndex];
    }
}

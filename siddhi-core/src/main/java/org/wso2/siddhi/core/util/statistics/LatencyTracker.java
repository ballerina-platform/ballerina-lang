package org.wso2.siddhi.core.util.statistics;

/**
 * Calculates the process latency. markIn and markOut is used to denote start
 * and end of processing events respectively. Latency is the time gap between
 * markIn and markOut calls of given thread.
 */
public interface LatencyTracker {
    /**
     * This is to be called when starting the latency calculation
     */
    void markIn();

    /**
     * This is to be called when latency calculation should be stopped
     *  */
    void markOut();

    /**
     * @return Name of the latency tracker
     */
    String getName();

}


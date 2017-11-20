package org.wso2.siddhi.core.util.statistics;

/**
 * Event Buffer holder
 */
public interface EventBufferHolder {

    long getBufferedEvents();

    boolean containsBufferedEvents();
}

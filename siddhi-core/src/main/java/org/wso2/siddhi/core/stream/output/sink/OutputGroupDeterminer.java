package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.event.Event;

/**
 * Output events are grouped before sent out to the transport to achieve efficient network communication. Implementations of
 * this interface should implement the logic to decide the grouping of events
 */
public interface OutputGroupDeterminer {
    /**
     * Deciding the group of a given event and returning a unique identifier to identify a group. A correct implementation of this method
     * should be retruining  the same group identifier for all events belongs a single group.
     * @param event Event that needs to be decided to which group it belongs to
     * @return Unique Identifier to identify the group of the event
     */
    String decideGroup(Event event);
}

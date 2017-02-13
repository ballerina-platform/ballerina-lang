package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;

public interface OutputTransportCallback {
    /**
     * Sending events via output transport
     *
     * @param payload payload of the event
     * @param event   one of the event constructing the payload
     * @throws ConnectionUnavailableException
     */
    void publish(Object payload, Event event) throws ConnectionUnavailableException;
}

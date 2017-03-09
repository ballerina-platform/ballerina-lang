package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.transport.DynamicOptions;

public interface OutputTransportListener {
    /**
     * Sending events via output transport
     *
     * @param payload payload of the event
     * @param transportOptions   one of the event constructing the payload
     * @throws ConnectionUnavailableException
     */
    void publish(Object payload, DynamicOptions transportOptions) throws ConnectionUnavailableException;
}

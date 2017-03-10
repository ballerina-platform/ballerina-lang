package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.event.Event;

/**
 * This interface is for output transports which can partition a single channel/connection into several
 * logical partitions which utilizes the some physical connection.
 */
public interface PartitionableChannelPublisher {

    void publish(Object payload, Event event, Object partitionId);

}

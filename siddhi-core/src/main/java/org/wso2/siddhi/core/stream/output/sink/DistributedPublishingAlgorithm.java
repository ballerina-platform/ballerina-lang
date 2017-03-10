package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.util.transport.OptionHolder;

/**
 * Created by sajith on 3/9/17.
 */
public interface DistributedPublishingAlgorithm {
    void publish(Object payload, Event event, OptionHolder optionHolder);
}

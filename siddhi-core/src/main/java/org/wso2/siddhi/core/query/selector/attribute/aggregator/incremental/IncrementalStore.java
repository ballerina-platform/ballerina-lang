package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;

public interface IncrementalStore {
    public void add(long timestamp, Object data); // TODO: 5/11/17 no need to take aggregator

    public Object get();

    public void reset();
}

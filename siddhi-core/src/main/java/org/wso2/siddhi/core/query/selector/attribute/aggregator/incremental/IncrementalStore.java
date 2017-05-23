package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;

public interface IncrementalStore {
    public void add(long timestamp, String groupByAttribute, Object data); // TODO: groupBy must be a list?

    public Object get();

    public void reset();
}

package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;

public interface IncrementalStore {
    public void add(Object data, AttributeAggregator aggregator);

    public Object get(AttributeAggregator aggregator);

    public void reset(AttributeAggregator aggregator);
}

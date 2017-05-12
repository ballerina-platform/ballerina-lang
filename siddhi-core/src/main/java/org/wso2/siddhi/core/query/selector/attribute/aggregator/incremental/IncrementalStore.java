package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;

public interface IncrementalStore {
    public void add(Object data, AttributeAggregator aggregator); // TODO: 5/11/17 no need to take aggregator  

    public Object get(AttributeAggregator aggregator);

    public void reset(AttributeAggregator aggregator);
}

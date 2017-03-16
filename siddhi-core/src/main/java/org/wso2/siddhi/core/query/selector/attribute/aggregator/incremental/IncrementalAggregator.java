package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;

public interface IncrementalAggregator {
    public String getAttributeName();

    public Attribute.Type getType();

    public Object aggregate(Object... results);

    public Expression[] getBaseAggregators();
}

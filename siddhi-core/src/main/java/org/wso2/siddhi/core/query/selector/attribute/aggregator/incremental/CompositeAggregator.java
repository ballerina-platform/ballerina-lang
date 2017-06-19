package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;

public interface CompositeAggregator {

    Object aggregate(Object... results);

    Attribute[] getIncrementalAttributes();

    Expression[] getIncrementalAttributeInitialValues();

    Expression[] getIncrementalAggregators();
}

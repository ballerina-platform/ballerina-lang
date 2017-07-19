package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Abstract class for incremental aggregators
 */
public abstract class IncrementalAttributeAggregator {

    public abstract void init(String attributeName, Attribute.Type attributeType);

    public abstract Object aggregate(Object... results);

    public abstract Attribute[] getBaseAttributes();

    public abstract Expression[] getBaseAttributeInitialValues();

    public abstract Expression[] getBaseAggregators();

    public abstract Attribute.Type getReturnType();
}

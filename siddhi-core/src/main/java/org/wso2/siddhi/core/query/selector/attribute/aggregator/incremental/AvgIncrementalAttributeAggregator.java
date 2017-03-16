package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

public class AvgIncrementalAttributeAggregator implements IncrementalAggregator {

    private String attributeName;
    private Attribute.Type type;


    public AvgIncrementalAttributeAggregator(AttributeFunction attributeFunction) {
        if (attributeFunction.getParameters() == null || attributeFunction.getParameters().length != 1) {
            // TODO: 3/3/17 exception
        }

        if (attributeFunction.getParameters()[0] == null || !(attributeFunction.getParameters()[0] instanceof Variable)) {
            // TODO: 3/3/17 Exception
        }
        this.attributeName = ((Variable) attributeFunction.getParameters()[0]).getAttributeName();
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public Attribute.Type getType() {
        return Attribute.Type.DOUBLE;
    }

    public Object aggregate(Object... results) {
        if (results == null || results.length != 2) {
            // TODO: 3/3/17 exception
        }
        Double sum = (Double) results[0];
        Long count = (Long) results[1];
        return sum / count;
    }

    public Expression[] getBaseAggregators() {
        Expression sum = Expression.function("sum", Expression.variable(attributeName));
        Expression count = Expression.function("count", Expression.variable(attributeName));
        return new Expression[]{sum, count};
    }
}

package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

public class SumIncrementalAttributeAggregator implements CompositeAggregator {

    private String attributeName;
    private Attribute.Type type;


    public SumIncrementalAttributeAggregator(AttributeFunction attributeFunction) {
        if (attributeFunction.getParameters() == null || attributeFunction.getParameters().length != 1) {
            // TODO: 3/3/17 exception
        }

        if (attributeFunction.getParameters()[0] == null || !(attributeFunction.getParameters()[0] instanceof Variable)) {
            // TODO: 3/3/17 Exception
        }
        this.attributeName = ((Variable) attributeFunction.getParameters()[0]).getAttributeName();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public Attribute.Type getType() {
        return Attribute.Type.DOUBLE;
    }

    public Object aggregate(Object... results) {
        if (results == null || results.length != 1) {
            // TODO: 3/3/17 exception
        }
        Double sum = (Double) results[0];
        return sum;
    }

    public Expression[] getBaseAggregators() {
        Expression sum = Expression.function("sum", Expression.variable(attributeName));
        return new Expression[]{sum};
    }

    /*public static Expression getInternalExpression(String baseCategory) {
        switch (baseCategory) {
            case "sum":
                return Expression.variable(attributeName);
            default:
                throw new Error("Only sum base aggregate is defined for sum aggregator");
        }
    }*/
}

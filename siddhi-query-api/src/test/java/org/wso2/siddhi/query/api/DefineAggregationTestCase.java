package org.wso2.siddhi.query.api;

import org.junit.Test;
import org.wso2.siddhi.query.api.aggregation.TimeSpecifier;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

public class DefineAggregationTestCase {
    @Test
    public void testDefineAggregation() {

        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregationDefinition").from(
                InputStream.stream("StockStream")).select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("StockStream")).
                        select("price", Expression.variable("price").ofStream("StockStream")).
                        groupBy(Expression.variable("price").ofStream("StockStream")).
                        having(
                                Expression.compare(
                                        Expression.add(Expression.value(7), Expression.value(9.5)),
                                        Compare.Operator.GREATER_THAN,
                                        Expression.variable("price"))
                        )
        ).aggriateBy("symbol", Attribute.Type.STRING).every(
                TimeSpecifier.range(TimeSpecifier.second(), TimeSpecifier.minute()));


        //Assert.assertEquals(1, aggregationDefinition.getAttributePosition("price"));
        //Assert.assertEquals(Attribute.Type.FLOAT, aggregationDefinition.getAttributeType("volume"));
    }
}


package org.wso2.siddhi.query.api;

import org.junit.Test;
import org.wso2.siddhi.query.api.aggregation.ExactTimeSpecifier;
import org.wso2.siddhi.query.api.aggregation.TimeSpecifier;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

public class DefineAggregationTestCase {
    @Test
    public void testDefineAggregationWithTimeRage() {

        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregationDefinition").from(
                InputStream.stream("StockStream")).select(
                Selector.selector().
                        select("timeStamp", Expression.variable("timeStamp").ofStream("StockStream")).
                        select("symbol", Expression.variable("symbol").ofStream("StockStream")).
                        select("price", Expression.variable("price").ofStream("StockStream")).
                        groupBy(Expression.variable("price").ofStream("StockStream")).
                        having(
                                Expression.compare(
                                        Expression.add(Expression.value(7), Expression.value(9.5)),
                                        Compare.Operator.GREATER_THAN,
                                        Expression.variable("price"))
                        )
        ).aggregateBy(Expression.variable("timeStamp")).every(
                TimeSpecifier.range(TimeSpecifier.second(), TimeSpecifier.minute()));

    }

    @Test
    public void testDefineAggregationWithExactTimeSpecifier() {
        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregationDefinition").from(
                InputStream.stream("StockStream")).select(
                Selector.selector().
                        select("timeStamp", Expression.variable("timeStamp").ofStream("StockStream")).
                        select("symbol", Expression.variable("symbol").ofStream("StockStream")).
                        select("price", Expression.variable("price").ofStream("StockStream")).
                        groupBy(Expression.variable("price").ofStream("StockStream")).
                        having(
                                Expression.compare(
                                        Expression.value(23.0f),
                                        Compare.Operator.GREATER_THAN,
                                        Expression.variable("price"))
                        )
        ).aggregateBy(Expression.variable("timeStamp")).every(
                TimeSpecifier.exact().add(TimeSpecifier.second()).add(TimeSpecifier.month())
        );
    }
}


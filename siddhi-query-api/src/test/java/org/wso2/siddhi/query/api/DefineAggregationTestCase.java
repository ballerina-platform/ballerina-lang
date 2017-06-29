package org.wso2.siddhi.query.api;

import org.junit.Test;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;

public class DefineAggregationTestCase {
    @Test
    public void testDefineAggregationWithTimeRage() {

        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregation")
                .from(InputStream.stream("StockStream"))
                .select(Selector.basicSelector()
                        .select("timestamp", Expression.variable("timestamp").ofStream("StockStream"))
                        .select("symbol", Expression.variable("symbol").ofStream("StockStream"))
                        .select("price", Expression.variable("price").ofStream("StockStream"))
                        .groupBy(Expression.variable("price").ofStream("StockStream")))
                .aggregateBy(Expression.variable("timestamp"))
                .every(TimePeriod.range(TimePeriod.Duration.SECONDS, TimePeriod.Duration.DAYS));
    }

    @Test
    public void testDefineAggregationWithExactTimeSpecifier() {
        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregationDefinition")
                .from(InputStream.stream("StockStream"))
                .select(Selector.basicSelector()
                        .select("timestamp", Expression.variable("timestamp").ofStream("StockStream"))
                        .select("symbol", Expression.variable("symbol").ofStream("StockStream"))
                        .select("price", Expression.variable("price").ofStream("StockStream"))
                        .groupBy(Expression.variable("price").ofStream("StockStream")))
                .aggregateBy(Expression.variable("timestamp")).every(TimePeriod.interval(TimePeriod.Duration.SECONDS,
                        TimePeriod.Duration.MINUTES, TimePeriod.Duration.HOURS));
    }
}
